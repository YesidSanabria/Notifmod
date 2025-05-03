package com.yac.notifmod.networking.payload;

import com.yac.notifmod.Notifmod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs; // Importar PacketCodecs
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

// Añade el campo 'playerName' al record
public record OakCallPayload(String playerName) implements CustomPayload {

    public static final CustomPayload.Id<OakCallPayload> ID = new CustomPayload.Id<>(Identifier.of(Notifmod.MOD_ID, "oak_call"));

    // --- CODEC ACTUALIZADO para leer/escribir un String ---
    public static final PacketCodec<RegistryByteBuf, OakCallPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, // Codec para leer/escribir un String
            OakCallPayload::playerName, // Getter para el campo playerName
            OakCallPayload::new // Constructor del record (recibe un String)
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}