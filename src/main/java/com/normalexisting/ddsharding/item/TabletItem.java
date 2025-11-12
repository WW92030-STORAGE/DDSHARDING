package com.normalexisting.ddsharding.item;

import com.normalexisting.ddsharding.graphics.Minimap;
import com.normalexisting.ddsharding.util.Reference;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class TabletItem extends Item {

    public TabletItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        InteractionResultHolder<ItemStack>  res = super.use(level, player, hand);
        if (level.isClientSide()) return res;

        System.out.println(Reference.entityInfo(player) + " USED TABLET");

        UUID uuid = player.getUUID();
        if (!Minimap.toggle.containsKey(uuid)) {
            Minimap.toggle.put(uuid, true);
            System.out.println("INIT TOGGLE");
        }
        Minimap.toggle.put(uuid, !Minimap.toggle.get(uuid));

        return res;
    }

    @Override
    public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(itemstack, world, entity, slot, selected);
        if (selected) {

        }

    }
}
