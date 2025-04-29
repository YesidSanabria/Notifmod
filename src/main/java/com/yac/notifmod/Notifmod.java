package com.yac.notifmod;

import com.yac.notifmod.items.ModItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notifmod implements ModInitializer {
	public static final String MOD_ID = "notifmod";

	public static final Logger LOGGER = LoggerFactory.getLogger("notifmod");

	private static MinecraftServer SERVER = null;

	@Override
	public void onInitialize() {
		ModItems.registerItems();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER = server);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> SERVER = null);

		LOGGER.info("Hello Fabric world!");
	}
}