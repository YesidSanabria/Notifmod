package com.yac.notifmod;

import com.yac.notifmod.commands.NotificationCommand;
import com.yac.notifmod.items.ModItems;
import com.yac.notifmod.networking.ModPacketsS2C;
import com.yac.notifmod.networking.payload.OakCallPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notifmod implements ModInitializer {

	public static final String MOD_ID = "notifmod";

	public static final Logger LOGGER = LoggerFactory.getLogger("notifmod");

	private static MinecraftServer SERVER = null;

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(OakCallPayload.ID, OakCallPayload.CODEC);
		LOGGER.info("Registrado tipo de payload S2C: {}", OakCallPayload.ID.id());


		ModPacketsS2C.register();
		ModItems.registerModItems();
		NotificationCommand.register();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER = server);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> SERVER = null);

		LOGGER.info("Hello Fabric world!");
	}
}