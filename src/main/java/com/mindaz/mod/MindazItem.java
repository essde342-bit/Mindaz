package com.mindaz.mod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class MindazItem extends SwordItem {

    private static final int COOLDOWN = 30 * 20;

    public MindazItem(Settings settings) {
        super(
                ToolMaterial.DIAMOND,
                3.0F,
                -2.4F,
                settings
                        .maxCount(1)
                        .maxDamage(250)
        );
    }

    @Override
    public ActionResult use(
            World world,
            PlayerEntity player,
            Hand hand
    ) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            MindazMod.createPlatform(
                    (ServerWorld) world,
                    player.getBlockPos()
            );

            player.getItemCooldownManager().set(
                    stack,
                    COOLDOWN
            );
        }

        return ActionResult.SUCCESS;
    }
}
