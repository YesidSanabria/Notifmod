package com.yac.notifmod.networking.payload;

import com.yac.notifmod.Notifmod;
import net.minecraft.network.RegistryByteBuf; // Necesario para PacketCodec
import net.minecraft.network.codec.PacketCodec; // Necesario para el Codec
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OakCallPayload() implements CustomPayload {

    public static final CustomPayload.Id<OakCallPayload> ID = new CustomPayload.Id<>(Identifier.of(Notifmod.MOD_ID, "oak_call"));

    // --- CODEC para payload vacío ---
    // Usamos RegistryByteBuf comúnmente con codecs, aunque PacketByteBuf podría funcionar.
    public static final PacketCodec<RegistryByteBuf, OakCallPayload> CODEC = PacketCodec.unit(new OakCallPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    // No necesitamos el método write() si usamos CODEC.unit()
}