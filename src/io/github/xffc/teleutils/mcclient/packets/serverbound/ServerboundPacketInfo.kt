package io.github.xffc.teleutils.mcclient.packets.serverbound

import io.github.xffc.teleutils.mcclient.packets.Packet

sealed interface ServerboundPacketInfo<T: Packet>: Packet.PacketInfo<T>