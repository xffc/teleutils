package io.github.xffc.teleutils

import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.request.GetMe
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.BaseResponse
import io.github.xffc.teleutils.commands.ExecutableCommand
import io.github.xffc.teleutils.mcclient.packets.Packet
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy

lateinit var COMMAND_REGEX: Regex
    private set

lateinit var bot: TelegramBot
    private set

val logger: Logger = LoggerFactory.getLogger("TeleUtils")

val json = Json {
    ignoreUnknownKeys = true
}

val prettyJson = Json {
    encodeDefaults = true
    prettyPrint = true
    ignoreUnknownKeys = true
}

@Serializable
data class Configuration(
    val token: String = "XXX:XXX-XXX",
    val proxy: ProxyConfiguration? = null
) {
    @Serializable
    data class ProxyConfiguration(
        val host: String,
        val port: Int,
        val type: Proxy.Type
    ) {
        fun create(): Proxy = Proxy(type, InetSocketAddress(host, port))
    }
}

fun main(vararg args: String) {
    val config: Configuration = File(args.getOrNull(0) ?: "config.json").let { file ->
        if (file.createNewFile()) {
            Configuration().also { file.writeText(prettyJson.encodeToString(it)) }
        } else prettyJson.decodeFromString(file.readText())
    }

    val httpClient = OkHttpClient.Builder().run {
        if (config.proxy != null) proxy(config.proxy.create())
        build()
    }

    bot = TelegramBot.Builder(config.token)
        .okHttpClient(httpClient)
        .build()

    // init registries
    Packet.clientboundRegistry
    Packet.serverboundRegistry

    val botInfo = bot.execute(GetMe()).user()
    logger.info("Logged as ${botInfo.username()} (${botInfo.id()})")

    logger.info("Commands:")
    ExecutableCommand.registry.forEach { (name, data) ->
        if (data.isAlias) return@forEach
        logger.info("/$name${if (data.aliases != null) " (${data.aliases.joinToString()})" else ""}")
        data.arguments.forEachIndexed { index, argument ->
            logger.info(" ${index + 1}. ${argument.name} - ${if (argument.required) "required" else "optional"}")
        }
    }

    COMMAND_REGEX = """^/\w+@${botInfo.username()}(?:\s.*|$)""".toRegex()

    bot.setUpdatesListener { updates ->
        updates.forEach(::handleUpdate)

        UpdatesListener.CONFIRMED_UPDATES_ALL
    }
}

fun handleUpdate(update: Update) {
    val message = update.message() ?: return
    val text = message.text() ?: return
    val from = message.from() ?: return
    val chat = message.chat() ?: return

    val commandCheck =
        if (from.id() == chat.id()) text.startsWith("/")
        else text.matches(COMMAND_REGEX)

    if (!commandCheck) return

    val command = text.substringAfter("/").split("@", " ")[0]
    val arguments = text.split(" ").drop(1)

    logger.info("${from.username() ?: from.firstName()} (${from.id()}) > $text")

    runCatching {
        ExecutableCommand.registry[command]?.instance?.run(update, arguments) ?: return
    }.onFailure { sendError(message, it) }
}

fun sendError(message: Message, throwable: Throwable) =
    SendMessage(message.chat().id(), "❌ ${throwable.message}")
        .asyncExecute()

fun <REQ : BaseRequest<REQ, RES>, RES : BaseResponse> REQ.asyncExecute(
    onFailure: (IOException) -> Unit = {},
    onResponse: (RES) -> Unit = {}
) = bot.asyncExecute(this, onFailure, onResponse)

fun <REQ : BaseRequest<REQ, RES>, RES : BaseResponse> TelegramBot.asyncExecute(
    request: REQ,
    onFailure: (IOException) -> Unit = {},
    onResponse: (RES) -> Unit = {}
) {
    execute(request, object : Callback<REQ, RES> {
        override fun onResponse(request: REQ, response: RES) {
            onResponse(response)
        }

        override fun onFailure(request: REQ, e: IOException) {
            onFailure(e)
        }
    })
}