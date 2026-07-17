package io.github.xffc.teleutils.mcclient.packets.serverbound

import io.github.xffc.teleutils.mcclient.State
import io.github.xffc.teleutils.mcclient.packets.Packet
import kotlinx.io.Buffer

class StatusRequestPacket: Packet {
    override fun write(buffer: Buffer) {}

    companion object: ServerboundPacketInfo<StatusRequestPacket> {
        override val id = 0
        override val state = State.STATUS
    }
}