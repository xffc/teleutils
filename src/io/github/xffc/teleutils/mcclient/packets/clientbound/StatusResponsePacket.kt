package io.github.xffc.teleutils.mcclient.packets.clientbound

import io.github.xffc.teleutils.json
import io.github.xffc.teleutils.mcclient.SerializableComponent
import io.github.xffc.teleutils.mcclient.State
import io.github.xffc.teleutils.mcclient.packets.Packet
import io.github.xffc.teleutils.mcclient.readPrefixedString
import kotlinx.io.Buffer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

class StatusResponsePacket(status: String): Packet {
    val status = json.decodeFromString<Status>(status)

    companion object: ClientboundPacketInfo<StatusResponsePacket> {
        override val id = 0
        override val state = State.STATUS

        override fun from(buffer: Buffer) = StatusResponsePacket(
            buffer.readPrefixedString()
        )
    }

    @Serializable
    data class Status(
        val version: Version,
        val players: Players? = null,
        val description: SerializableComponent? = null,
        val favicon: String? = null,
        val enforcesSecureChat: Boolean = false
    ) {
        @Serializable
        data class Version(
            val name: String = "Old",
            val protocol: Int
        )

        @Serializable
        data class Players(
            val max: Int,
            val online: Int,
            val sample: List<Player>? = null
        )

        @Serializable
        data class Player(
            val name: String,
            val id: Uuid
        )
    }
}