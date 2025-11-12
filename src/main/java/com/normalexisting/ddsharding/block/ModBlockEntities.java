package com.normalexisting.ddsharding.block;

import com.normalexisting.ddsharding.util.Reference;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.MODID);

    public static final RegistryObject<BlockEntityType<ShardTesterEntity>> SHARD_TESTER_ENTITY =
            BLOCK_ENTITIES.register("shardtester_be", () -> BlockEntityType.Builder.of(
                    ShardTesterEntity::new, ModBlocks.SHARDTESTER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}