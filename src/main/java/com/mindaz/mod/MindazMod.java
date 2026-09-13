package com.mindaz.mod;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class MindazMod implements ModInitializer {

    public static final String MOD_ID = "mindaz";

    private static final List<TemporaryBlock> TEMPORARY_BLOCKS =
            new ArrayList<>();

    private static final int WOOL_TIME = 10 * 20;

    public static final MindazItem MINDAZ = register(
            "mindaz",
            MindazItem::new,
            new Item.Settings()
    );

    private static MindazItem register(
            String name,
            Function<Item.Settings, Item> factory,
            Item.Settings settings
    ) {
        RegistryKey<Item> key = RegistryKey.of(
                RegistryKeys.ITEM,
                Identifier.of(MOD_ID, name)
        );

        return (MindazItem) Items.register(
                key,
                factory,
                settings
        );
    }

    @Override
    public void onInitialize() {

        ServerTickEvents.END_SERVER_TICK.register(
                MindazMod::tickTemporaryBlocks
        );

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        registerCommands(dispatcher)
        );
    }

    private static void registerCommands(
            CommandDispatcher<ServerCommandSource> dispatcher
    ) {
        dispatcher.register(
                CommandManager.literal("mindaz")
                        .executes(context -> {

                            ServerPlayerEntity player =
                                    context.getSource()
                                            .getPlayerOrThrow();

                            ItemStack stack = createMindaz(
                                    context.getSource()
                                            .getServer()
                            );

                            player.getInventory()
                                    .offerOrDrop(stack);

                            context.getSource().sendFeedback(
                                    () -> Text.literal(
                                            "Mindaz получен!"
                                    ),
                                    false
                            );

                            return 1;
                        })
        );
    }

    public static ItemStack createMindaz(
            MinecraftServer server
    ) {
        ItemStack stack = new ItemStack(MINDAZ);

        RegistryEntryLookup<Enchantment> enchantments =
                server.getRegistryManager()
                        .getOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> sharpness =
                enchantments.getOrThrow(
                        Enchantments.SHARPNESS
                );

        RegistryEntry<Enchantment> fireAspect =
                enchantments.getOrThrow(
                        Enchantments.FIRE_ASPECT
                );

        RegistryEntry<Enchantment> unbreaking =
                enchantments.getOrThrow(
                        Enchantments.UNBREAKING
                );

        EnchantmentHelper.apply(
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

            if (!oldState.isAir()) {
                continue;
            }

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

    private static void tickTemporaryBlocks(
            MinecraftServer server
    ) {
        Iterator<TemporaryBlock> iterator =
                TEMPORARY_BLOCKS.iterator();

        while (iterator.hasNext()) {

            TemporaryBlock block =
                    iterator.next();

            if (block.world().getTime()
                    < block.removeAtTick()) {
                continue;
            }

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

    private record TemporaryBlock(
            ServerWorld world,
            BlockPos pos,
            BlockState oldState,
            long removeAtTick
    ) {
    }
}
