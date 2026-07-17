package io.github.xffc.teleutils.mcclient.packets.clientbound

import io.github.xffc.teleutils.mcclient.State
import io.github.xffc.teleutils.mcclient.packets.Packet
import io.github.xffc.teleutils.mcclient.readPrefixedString
import kotlinx.io.Buffer

class PongResponsePacket(
    val timestamp: Long
): Packet {
    companion object: ClientboundPacketInfo<PongResponsePacket> {
        override val id = 1
        override val state = State.STATUS

        override fun from(buffer: Buffer) = PongResponsePacket(
            buffer.readLong()
        )
    }
}