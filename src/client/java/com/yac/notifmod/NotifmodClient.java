package com.yac.notifmod;

import com.yac.notifmod.client.toast.OakCallToast;
import com.yac.notifmod.networking.payload.OakCallPayload; // Asegúrate que este import sea correcto

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
// El import de MinecraftClient se usa dentro de la lambda
import net.minecraft.client.MinecraftClient;
// Importa el contexto necesario para el handler
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;


@Environment(EnvType.CLIENT)
public class NotifmodClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		registerPacketHandlers();
	}

	private void registerPacketHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(
				OakCallPayload.ID,
				(payload, context) -> {
					// --- CORRECCIÓN: Usar context.client().execute() ---
					context.client().execute(() -> { // Obtiene el cliente y luego ejecuta
						MinecraftClient mc = context.client();
						if (mc.player != null) {
							OakCallToast toast = new OakCallToast();
							toast.setJustUpdated();
							mc.getToastManager().add(toast);
						}
					});
				}
		);
	}
}