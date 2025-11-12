package com.normalexisting.ddsharding.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;

public class RevealShardPosition extends Block {
    final int MAX_RANGE = 64;
    boolean POWERED = false;

    final int MAX_RADIUS = 64;

    public static HashSet<BlockPos> powered = new HashSet<BlockPos>();

    public RevealShardPosition(Properties properties) {
        super(properties);
    }

    Block B = Blocks.GLASS;

    @Override
    public void neighborChanged(BlockState blockstate, Level level, BlockPos pos, Block nn, BlockPos fromPos, boolean moving) {
        super.neighborChanged(blockstate, level, pos, nn, fromPos, moving);
        if (level.getBestNeighborSignal(pos) > 0) {
            POWERED = true;
            powered.add(pos);

        } else {
            POWERED = false;
            if (powered.contains(pos)) powered.remove(pos);
        }
    }
}
