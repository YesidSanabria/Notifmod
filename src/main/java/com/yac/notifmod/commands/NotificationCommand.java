package com.yac.notifmod.commands;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.text.Text;

import static org.apache.commons.compress.harmony.pack200.PackingUtils.log;

public class NotificationCommand {

    public NotificationCommand() { throw new AssertionError(); }

    public static void register() {
        log("Registering Commmands");

        Command<ServerCommandSource> command = context -> {
            ServerCommandSource source = context.getSource();
            return 0;
        };

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
        {
            //CommandManager.literal("calldexcom") - > Como se llama al comando desde el juego
            dispatcher.register(CommandManager.literal("calldexcom")
                            .then(CommandManager.argument("clear_operation_type", StringArgumentType.string())
                    .executes(context -> {
                        //Logica del comando
                        context.getSource().sendFeedback(() -> Text.literal("Se llamo /calldexcom."), false);
                        String typ = StringArgumentType.getString(context, "clear_operation_type");
                        return 1;
                    })));
        });

    }





}



