package io.github.xffc.teleutils.mcclient.packets.clientbound

import io.github.xffc.teleutils.mcclient.packets.Packet

sealed interface ClientboundPacketInfo<T: Packet>: Packet.PacketInfo<T>