package io.github.xffc.teleutils.keyboards

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import io.github.xffc.teleutils.bot
import io.github.xffc.teleutils.commands.PingCommand.ping

object PingKeyboard: Keyboard {
    override val key = "ping"

    override suspend fun Keyboard.Context.execute() {
        val address = args[0]

        val response = ping(address)

        bot.reply(
            message.chat.id,
            message.messageId,
            response.first,
            HTMLParseMode,
            replyMarkup = InlineKeyboardMarkup(censysButton(response.second))
        )
    }
}