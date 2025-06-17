package io.github.xffc.teleutils.commands

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import io.github.xffc.teleutils.bot
import io.github.xffc.teleutils.keyboards.censysButton
import java.net.InetSocketAddress

@Suppress("UNUSED")
object PingCommand: Command {
    override val name = "ping"
    override val arguments = listOf(
        Argument.TextArgument("address", true)
    )

    override suspend fun Command.Context.execute() {
        val address = args["address"] as String

        val response = ping(address)

        bot.reply(
            message.chat.id,
            message.messageId,
            response.first,
            parseMode = HTMLParseMode,
            replyMarkup = InlineKeyboardMarkup(censysButton(response.second))
        )
    }

    /**
     * @return Pair<Text, IP>
     */
    fun ping(address: String): Pair<String, String> {
        val addr = InetSocketAddress(address, 0).address

        val startedAt = System.currentTimeMillis()
        val isReachable = addr.isReachable(3000)
        val ping = System.currentTimeMillis() - startedAt

        return (if (isReachable)
            "✅ <b>Ping successful</b>\n🪪 <b>IP</b>: <code>${addr.hostAddress}</code>\n⏱️ <b>Ping</b>: <code>${ping}ms</code>"
        else
            "❌ <b>Ping unsuccessful</b>\n🪪 <b>IP</b>: <code>${addr.hostAddress}</code>") to addr.hostAddress
    }
}