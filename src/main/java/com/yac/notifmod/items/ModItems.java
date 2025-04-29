package com.yac.notifmod.items;

import com.yac.notifmod.Notifmod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item CALLDEX = registerItem("calldex", new Item(new Item.Settings()));

    private static Item registerItem(String itemId, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Notifmod.MOD_ID, itemId), item);
    }

    public static void registerModItems() {
        Notifmod.LOGGER.info("Registrando items..."+ Notifmod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(CALLDEX);
        });
    }




}
