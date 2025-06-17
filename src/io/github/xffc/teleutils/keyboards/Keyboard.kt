package io.github.xffc.teleutils.keyboards

import com.mayakapps.kache.InMemoryKache
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.utils.SimpleFilter
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import korlibs.time.minutes
import kotlinx.coroutines.launch

sealed interface Keyboard {
    companion object {
        val registry = Keyboard::class.sealedSubclasses.map { it.objectInstance!! }

        // userId: keyboardMessageId
        val users = InMemoryKache<Long, Long>(1024) {
            expireAfterWriteDuration = 1.minutes
        }
    }

    val key: String

    suspend fun Context.execute()

    suspend fun BehaviourContext.register() =
        onMessageDataCallbackQuery(object : SimpleFilter<DataCallbackQuery> {
            override suspend fun invoke(o: DataCallbackQuery): Boolean =
                o.data.startsWith(key)
        }) { query ->
            launch {
                try {
                    val boundTo = users.get(query.user.id.chatId.long)
                    if (boundTo != query.message.messageId.long)
                        throw IllegalArgumentException("Keyboard is not yours")

                    Context
                        .build(query)
                        .execute()
                } catch (e: Exception) {
                    bot.answer(query, "❌ ${e.message}", true)
                }
            }
        }

    class Context private constructor(
        val query: MessageDataCallbackQuery,
        val message: ContentMessage<MessageContent>,
        val args: List<String>
    ) {
        companion object {
            fun build(query: MessageDataCallbackQuery) = Context(
                query,
                query.message,
                query.data.split(":").drop(1),
            )
        }
    }
}