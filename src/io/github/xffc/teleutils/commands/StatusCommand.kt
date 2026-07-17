package io.github.xffc.teleutils.commands

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.model.request.ReplyParameters
import com.pengrad.telegrambot.request.EditMessageCaption
import com.pengrad.telegrambot.request.EditMessageText
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import io.github.xffc.teleutils.asyncExecute
import io.github.xffc.teleutils.commands.util.Argument
import io.github.xffc.teleutils.mcclient.Client
import io.github.xffc.teleutils.mcclient.packets.clientbound.PongResponsePacket
import io.github.xffc.teleutils.mcclient.packets.clientbound.StatusResponsePacket
import io.github.xffc.teleutils.mcclient.packets.serverbound.HandshakePacket
import io.github.xffc.teleutils.mcclient.State
import io.github.xffc.teleutils.mcclient.packets.serverbound.PingRequestPacket
import io.github.xffc.teleutils.mcclient.packets.serverbound.StatusRequestPacket
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import okio.ByteString.Companion.decodeBase64
import java.util.concurrent.CompletableFuture

@Suppress("unused")
object StatusCommand : ExecutableCommand("status", "mcstatus") {
    val hostArgument = Argument.WordArgument("host")
    val portArgument = Argument.IntegerArgument("port", 0 until 65535, false, 25565)
    val protocolVersionArgument = Argument.IntegerArgument("protocolVersion", null, false, 776)

    override fun run(update: Update, args: List<String>) {
        val host = hostArgument.getValue(args.getOrNull(0))!!
        val port = portArgument.getValue(args.getOrNull(1))!!
        val protocolVersion = protocolVersionArgument.getValue(args.getOrNull(2))!!

        SendMessage(update.message().chat().id(), "Connecting to <code>$host:$port</code>...")
            .replyParameters(ReplyParameters(update.message().messageId()))
            .parseMode(ParseMode.HTML)
            .asyncExecute { response ->
                val chat = response.message()?.chat()?.id() ?: return@asyncExecute
                handleClient(host, port, protocolVersion, chat)
            }
    }

    private fun handleClient(
        host: String,
        port: Int,
        protocolVersion: Int,
        chat: Long
    ) {
        val client = Client.connect(host, port)
        val future = CompletableFuture<Pair<String, Message>>()

        client.packetHandlers += packetHandler@{ _, packet ->
            when (packet) {
                is StatusResponsePacket -> handleStatus(client, chat, future, packet)
                is PongResponsePacket -> handlePing(client, chat, future, packet)
            }
        }

        client.sendPacket(
            HandshakePacket(
                protocolVersion,
                host,
                port.toUShort(),
                State.STATUS
            )
        )

        client.sendPacket(StatusRequestPacket())

        Thread.sleep(10000)
        client.disconnect()
    }

    private fun handleStatus(
        client: Client,
        chat: Long,
        future: CompletableFuture<Pair<String, Message>>,
        packet: StatusResponsePacket
    ) {
        val text = packet.status.text()
        val favicon = packet.status.favicon?.substringAfter("data:image/png;base64,")?.decodeBase64()?.toByteArray()

        if (favicon != null) SendPhoto(chat, favicon)
            .parseMode(ParseMode.HTML)
            .caption(text)
            .asyncExecute { future.complete(text to it.message()) }
        else SendMessage(chat, text)
            .parseMode(ParseMode.HTML)
            .asyncExecute { future.complete(text to it.message()) }

        client.sendPacket(PingRequestPacket(System.currentTimeMillis()))
    }

    private fun handlePing(
        client: Client,
        chat: Long,
        future: CompletableFuture<Pair<String, Message>>,
        packet: PongResponsePacket
    ) {
        val text = "⏱️ <b>Ping</b>: <code>${System.currentTimeMillis() - packet.timestamp}ms</code>\n"

        future.thenAccept { originalToMessage ->
            val original = originalToMessage.first
            val message = originalToMessage.second

            if (message.caption() != null)
                EditMessageCaption(message.chat().id(), message.messageId())
                    .caption(text + original)
                    .parseMode(ParseMode.HTML)
                    .asyncExecute()
            else if (message.text() != null)
                EditMessageText(message.chat().id(), message.messageId(), text + original)
                    .parseMode(ParseMode.HTML)
                    .asyncExecute()
        }

        client.disconnect()
    }

    private fun StatusResponsePacket.Status.text() = buildString {
        appendLine("🏷️ <b>Version</b>: <code>${version.name}</code> (${version.protocol})")
        players?.let { appendLine("👥 <b>Players</b>: <code>${it.online}/${it.max}</code>") }
        description?.let {
            val text = PlainTextComponentSerializer.plainText().serialize(it)
            appendLine("""📋 <b>MOTD</b>: <pre>${text}</pre>""".trimIndent())
        }
    }
}