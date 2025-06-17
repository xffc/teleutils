package io.github.xffc.teleutils.commands

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.replyWithPhoto
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.requests.abstracts.InputFile
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import dev.inmo.tgbotapi.utils.RiskFeature
import io.github.xffc.teleutils.bot
import io.github.xffc.teleutils.keyboards.Keyboard
import io.github.xffc.teleutils.keyboards.censysButton
import io.github.xffc.teleutils.keyboards.pingButton
import io.github.xffc.xproject.kraft.KraftClient
import io.github.xffc.xproject.kraft.packets.Handshake
import io.github.xffc.xproject.kraft.packets.PingRequest
import io.github.xffc.xproject.kraft.packets.PingResponse
import io.github.xffc.xproject.kraft.packets.StatusRequest
import io.github.xffc.xproject.kraft.packets.StatusResponse
import io.github.xffc.xproject.kraft.types.ServerStatus
import io.github.xffc.xproject.kraft.types.State
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.toJavaAddress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.text.split

@Suppress("UNUSED")
object ServerCommand : Command {
    override val name = "server"
    override val arguments = listOf<Argument<*>>(
        Argument.TextArgument("address", true),
        Argument.PortArgument("port", false, 25565u),
        Argument.IntArgument("protocol", false, 770)
    )

    @OptIn(RiskFeature::class)
    override suspend fun Command.Context.execute() {
        val host = args["address"] as String
        val port = args["port"] as UShort
        val protocol = args["protocol"] as Int

        val client = KraftClient.create(
            InetSocketAddress(host, port.toInt()),
            Handshake(protocol, host, port, State.STATUS),
            1024
        )

        val info = withContext(Dispatchers.IO) {
            val defer = CompletableDeferred<ServerInfo>()

            var status: ServerStatus? = null

            client.onPacket<StatusResponse> {
                status = this.status
                client.sendPacket(PingRequest(System.currentTimeMillis()))
            }

            client.onPacket<PingResponse> {
                val time = System.currentTimeMillis() - timestamp
                defer.complete(ServerInfo(status!!, time))
            }

            client.sendPacket(StatusRequest())

            defer.await()
        }

        val ip = (client.socket.remoteAddress.toJavaAddress() as java.net.InetSocketAddress)
            .address.hostAddress

        client.scope.launch {
            client.socket.close()
        }

        val response = StringBuilder()
        response.appendLine("🪪 <b>IP</b>: $ip")
        info.ping.also { response.appendLine("⏱️ <b>Ping</b>: <code>${it}ms</code>") }
        info.status.version.also { response.appendLine("🏷️ <b>Version</b>: <code>${it.name}</code> <i>(${it.protocol})</i>") }
        info.status.description?.also { response.appendLine("📄 <b>MOTD</b>:\n<pre code=\"txt\">${PlainTextComponentSerializer.plainText().serialize(it)}</pre>") }
        info.status.enforcesSecureChat.also { response.appendLine("⚠️ <b>Enforces secure chat</b>: ${it ?: false}") }
        info.status.players?.also {
            response.appendLine(
                "👥 <b>Players</b>: <code>${it.online}/${it.max}</code>\n${
                    it.sample.joinToString(
                        "\n"
                    ) { p -> " - <code>${p.name}</code>" }
                }"
            )
        }

        val favicon = info.status.favicon?.let { icon ->
            InputFile.fromInput("icon.png") {
                Buffer().also {
                    it.write(korlibs.encoding.Base64.decode(icon.split("data:image/png;base64,")[1], false))
                }
            }
        }

        val keyboard = InlineKeyboardMarkup(
            censysButton(ip),
            pingButton(host)
        )

        val sent = if (favicon != null)
            bot.replyWithPhoto(
                message.chat.id,
                message.messageId,
                favicon,
                response.toString(),
                parseMode = HTMLParseMode,
                replyMarkup = keyboard
            )
        else
            bot.reply(
                message.chat.id,
                message.messageId,
                response.toString(),
                parseMode = HTMLParseMode,
                replyMarkup = keyboard
            )

        Keyboard.users.put(
            message.from!!.id.chatId.long,
            sent.messageId.long
        )
    }

    private data class ServerInfo(
        val status: ServerStatus,
        val ping: Long
    )
}