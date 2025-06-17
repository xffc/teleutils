package io.github.xffc.teleutils

import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import io.github.xffc.teleutils.commands.Command
import io.github.xffc.teleutils.keyboards.Keyboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

val prettyJson = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

val config: BotConfig = File("config.json").let {
    if (it.createNewFile()) it.writeText(prettyJson.encodeToString(BotConfig()))
    prettyJson.decodeFromString(it.readText())
}

val bot = telegramBot(config.token)

suspend fun main() {
    bot.buildBehaviourWithLongPolling {
        Command.registry.forEach { it ->
            it.run { this@buildBehaviourWithLongPolling.register() }
        }

        Keyboard.registry.forEach { it ->
            it.run { this@buildBehaviourWithLongPolling.register() }
        }
    }.join()
}

@Serializable
data class BotConfig(
    val token: String = "123456:ABCDEF"
)