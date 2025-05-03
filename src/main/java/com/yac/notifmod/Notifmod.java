package com.yac.notifmod;

import com.yac.notifmod.commands.NotificationCommand;
import com.yac.notifmod.items.ModItems;
import com.yac.notifmod.networking.ModPacketsS2C;
import com.yac.notifmod.networking.payload.OakCallPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notifmod implements ModInitializer {

	public static final Identifier OAK_CALL_RINGTONE_ID = Identifier.of(Notifmod.MOD_ID, "oak_call_ringtone");

	public static SoundEvent OAK_CALL_RINGTONE_EVENT = SoundEvent.of(OAK_CALL_RINGTONE_ID);

	public static final String MOD_ID = "notifmod";

	public static final Logger LOGGER = LoggerFactory.getLogger("notifmod");

	private static MinecraftServer SERVER = null;

	@Override
	public void onInitialize() {

		// Registra Payload
		PayloadTypeRegistry.playS2C().register(OakCallPayload.ID, OakCallPayload.CODEC);
		LOGGER.info("Registrado tipo de payload S2C: {}", OakCallPayload.ID.id());


		// --- Registra el Evento de Sonido ---
		Registry.register(Registries.SOUND_EVENT, OAK_CALL_RINGTONE_ID, OAK_CALL_RINGTONE_EVENT);
		LOGGER.info("Registrado evento de sonido: {}", OAK_CALL_RINGTONE_ID);

		// Registra Items
		ModItems.registerModItems();
		// Registra Comandos
		NotificationCommand.register();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER = server);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> SERVER = null);

		LOGGER.info("Hello Fabric world!");
	}
}