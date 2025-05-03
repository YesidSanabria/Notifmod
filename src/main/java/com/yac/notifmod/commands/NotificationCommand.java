package com.yac.notifmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.yac.notifmod.Notifmod;
import com.yac.notifmod.networking.payload.OakCallPayload; // Importa tu nuevo payload

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
// Quita la importación de PacketByteBufs si ya no la usas aquí
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
// Ya no necesitas importar Identifier aquí si solo usas el ID del Payload
// Ya no necesitas importar PacketByteBufs aquí

public class NotificationCommand {

    // Ya no necesitamos el Identifier aquí, lo define el Payload

    public static void register() {
        Notifmod.LOGGER.info("Registrando Comandos para " + Notifmod.MOD_ID);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
        {
            dispatcher.register(CommandManager.literal("calldexcom")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(NotificationCommand::run)
            );
        });
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            // --- CORRECCIÓN: Enviar una instancia del Payload ---
            String playerName = player.getName().getString(); // Obtiene el nombre del jugador
            ServerPlayNetworking.send(player, new OakCallPayload(playerName)); // Crea el payload CON el nombre

            source.sendFeedback(() -> Text.literal("¡Señal de llamada enviada!"), false);
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendError(Text.literal("Este comando solo puede ser ejecutado por un jugador."));
            return 0;
        }
    }
}



