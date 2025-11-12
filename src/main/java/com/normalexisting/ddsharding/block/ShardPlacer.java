package com.normalexisting.ddsharding.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShardPlacer extends Block {
    final int MAX_RANGE = 16;
    boolean POWERED = false;

    public ShardPlacer(Properties properties) {
        super(properties);
    }

    Block B = Blocks.GLASS;

    @Override
    public void neighborChanged(BlockState blockstate, Level level, BlockPos pos, Block nn, BlockPos fromPos, boolean moving) {
        super.neighborChanged(blockstate, level, pos, nn, fromPos, moving);
        if (level.getBestNeighborSignal(pos) > 0) {
            if (POWERED) return;
            POWERED = true;

            for (int K = 1; K <= MAX_RANGE; K++) {
                BlockPos BP = BlockPos.containing(pos.getX(), pos.getY() + K, pos.getZ());
                Block tester = level.getBlockState(BP).getBlock();
                if (tester == ModBlocks.SHARD.get()) return; // do not override shards
                if (tester == Blocks.AIR) {
                    level.setBlock(BP, ModBlocks.SHARD.get().defaultBlockState(), 3);
                    return;
                }
            }
        } else POWERED = false;
    }
}
