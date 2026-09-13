package com.mindaz.mod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class MindazItem extends Item {

    // 30 секунд = 600 игровых тиков
    private static final int COOLDOWN = 30 * 20;

    public MindazItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(
            World world,
            PlayerEntity player,
            Hand hand
    ) {

        ItemStack stack = player.getStackInHand(hand);

        // Если способность перезаряжается — ничего не делаем
        if (player.getItemCooldownManager()
                .isCoolingDown(this)) {

            return ActionResult.PASS;
        }

        // Выполняем способность только на сервере
        if (!world.isClient()) {

            MindazMod.createPlatform(
                    (ServerWorld) world,
                    player.getBlockPos()
            );

            // Запускаем перезарядку
            player.getItemCooldownManager()
                    .set(this, COOLDOWN);
        }

        return ActionResult.SUCCESS;
    }
}
