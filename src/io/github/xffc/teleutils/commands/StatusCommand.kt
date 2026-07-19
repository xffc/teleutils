package io.github.xffc.teleutils.commands

import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.model.request.ReplyParameters
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import io.github.xffc.kmc.serverstatus.ServerStatusClient
import io.github.xffc.kmc.serverstatus.ServerStatusData
import io.github.xffc.teleutils.asyncExecute
import io.github.xffc.teleutils.commands.util.Argument
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import okio.ByteString.Companion.decodeBase64
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Suppress("unused")
object StatusCommand : ExecutableCommand("status", "mcstatus") {
    val hostArgument = Argument.WordArgument("host")
    val portArgument = Argument.IntegerArgument("port", 0 until 65535, false, 25565)
    val protocolVersionArgument = Argument.IntegerArgument("protocolVersion", null, false, 776)

    override fun run(update: Update, args: List<String>) {
        val host = hostArgument.getValue(args.getOrNull(0))!!
        val port = portArgument.getValue(args.getOrNull(1))!!.toUShort()
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
        port: UShort,
        protocolVersion: Int,
        chat: Long
    ) {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port.toInt()))

        val future = CompletableFuture<ServerStatusData>()
        val client = ServerStatusClient(socket, host, port, protocolVersion, future)

        val result = runCatching {
            future.get(10, TimeUnit.SECONDS)
        }.getOrNull()

        client.disconnect()
        requireNotNull(result) { "Failed to fetch server status" }

        handleResult(chat, result)
    }

    private fun handleResult(
        chat: Long,
        result: ServerStatusData
    ) {
        val text = result.text()
        val favicon = result.status.favicon?.substringAfter("data:image/png;base64,")?.decodeBase64()?.toByteArray()

        if (favicon != null) SendPhoto(chat, favicon)
            .parseMode(ParseMode.HTML)
            .caption(text)
            .asyncExecute()
        else SendMessage(chat, text)
            .parseMode(ParseMode.HTML)
            .asyncExecute()
    }

    private fun ServerStatusData.text() = buildString {
        appendLine("⏱️ <b>Ping</b>: <code>${ping}ms</code>")
        appendLine("🏷️ <b>Version</b>: <code>${status.version.name}</code> (${status.version.protocol})")
        status.players?.let { appendLine("👥 <b>Players</b>: <code>${it.online}/${it.max}</code>") }
        appendLine("💬 <b>Enforces secure chat</b>: ${if (status.enforcesSecureChat) "✅" else "❌"}")
        status.description?.let {
            val text = PlainTextComponentSerializer.plainText().serialize(it)
            appendLine("""📋 <b>MOTD</b>: <pre>${text}</pre>""".trimIndent())
        }
    }
}