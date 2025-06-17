package io.github.xffc.teleutils.keyboards

import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.URLInlineKeyboardButton

fun censysButton(address: String) =
    URLInlineKeyboardButton("🔍 Censys", "https://search.censys.io/hosts/${address}")

fun pingButton(address: String) =
    CallbackDataInlineKeyboardButton("📣 Ping", "${PingKeyboard.key}:$address")
