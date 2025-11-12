package com.normalexisting.ddsharding.block;


import com.normalexisting.ddsharding.item.ModItems;
import com.normalexisting.ddsharding.util.Reference;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MODID);

    public static final RegistryObject<Block> SHARD = registerBlock("shard",
            () -> new ShardBlock(BlockBehaviour.Properties.of().noCollission().noOcclusion()
                    .strength(4f).lightLevel(x -> 10).requiresCorrectToolForDrops().sound(SoundType.GLASS)));

    public static final RegistryObject<Block> REVEALSHARD = registerBlock("revealshard",
            () -> new RevealShardBlock(BlockBehaviour.Properties.of().noCollission().noOcclusion()
                    .strength(4f).lightLevel(x -> 10).requiresCorrectToolForDrops().sound(SoundType.GLASS)));

    public static final RegistryObject<Block> SHARDPLACER = registerBlock("shardplacer",
            () -> new ShardPlacer(BlockBehaviour.Properties.of()
                    .strength(4f).lightLevel(x -> 10).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final RegistryObject<Block> REVEALSHARDPLACER = registerBlock("revealshardplacer",
            () -> new RevealShardPlacer(BlockBehaviour.Properties.of()
                    .strength(4f).lightLevel(x -> 10).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final RegistryObject<Block> REVEAL_POS = registerBlock("revealpos",
            () -> new RevealShardPosition(BlockBehaviour.Properties.of()
                    .strength(4f).lightLevel(x -> 10).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final RegistryObject<Block> SHARDTESTER = registerBlock("shardtester",
            () -> new ShardTester(BlockBehaviour.Properties.of()
                    .strength(4f).lightLevel(x -> 10).requiresCorrectToolForDrops().sound(SoundType.METAL)));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}