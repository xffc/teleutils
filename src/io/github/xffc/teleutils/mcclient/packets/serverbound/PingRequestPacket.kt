package io.github.xffc.teleutils.mcclient.packets.serverbound

import io.github.xffc.teleutils.mcclient.State
import io.github.xffc.teleutils.mcclient.packets.Packet
import kotlinx.io.Buffer

class PingRequestPacket(
    val timestamp: Long
): Packet {
    override fun write(buffer: Buffer) {
        buffer.writeLong(timestamp)
    }

    companion object: ServerboundPacketInfo<PingRequestPacket> {
        override val id = 1
        override val state = State.STATUS
    }
}