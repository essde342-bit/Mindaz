package com.mindaz.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MindazMod implements ModInitializer {

    public static final String MOD_ID = "mindaz";

    public static final MindazItem MINDAZ = new MindazItem(
            new net.minecraft.item.Item.Settings()
                    .maxCount(1)
                    .maxDamage(250)
    );

    private static final List<TemporaryBlock> TEMPORARY_BLOCKS = new ArrayList<>();

    private static final int WOOL_TIME = 200;

    @Override
    public void onInitialize() {

        Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "mindaz"),
                MINDAZ
        );

        ServerTickEvents.END_SERVER_TICK.register(
                MindazMod::tickTemporaryBlocks
        );
    }

    public static ItemStack createMindaz(MinecraftServer server) {

        ItemStack stack = new ItemStack(MINDAZ);

        RegistryEntryLookup<Enchantment> enchantments =
                server.getRegistryManager()
                        .getOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> sharpness =
                enchantments.getOrThrow(Enchantments.SHARPNESS);

        RegistryEntry<Enchantment> fireAspect =
                enchantments.getOrThrow(Enchantments.FIRE_ASPECT);

        RegistryEntry<Enchantment> unbreaking =
                enchantments.getOrThrow(Enchantments.UNBREAKING);

        net.minecraft.enchantment.EnchantmentHelper.apply(
                stack,
                builder -> {
                    builder.set(sharpness, 7);
                    builder.set(fireAspect, 2);
                    builder.set(unbreaking, 5);
                }
        );

        return stack;
    }

    public static void createPlatform(
            ServerWorld world,
            BlockPos playerPos
    ) {

        BlockPos center = playerPos.down();

        for (int x = -1; x <= 1; x++) {

            BlockPos pos = center.add(x, 0, 0);

            BlockState oldState =
                    world.getBlockState(pos);

            if (oldState.isAir()) {

                world.setBlockState(
                        pos,
                        Blocks.WHITE_WOOL.getDefaultState(),
                        3
                );

                TEMPORARY_BLOCKS.add(
                        new TemporaryBlock(
                                world,
                                pos,
                                oldState,
                                world.getTime() + WOOL_TIME
                        )
                );
            }
        }
    }

    private static void tickTemporaryBlocks(
            MinecraftServer server
    ) {

        Iterator<TemporaryBlock> iterator =
                TEMPORARY_BLOCKS.iterator();

        while (iterator.hasNext()) {

            TemporaryBlock block =
                    iterator.next();

            if (block.world().getTime()
                    >= block.removeAtTick()) {

                if (block.world()
                        .getBlockState(block.pos())
                        .isOf(Blocks.WHITE_WOOL)) {

                    block.world().setBlockState(
                            block.pos(),
                            block.oldState(),
                            3
                    );
                }

                iterator.remove();
            }
        }
    }

    private record TemporaryBlock(
            ServerWorld world,
            BlockPos pos,
            BlockState oldState,
            long removeAtTick
    ) {}
      }
