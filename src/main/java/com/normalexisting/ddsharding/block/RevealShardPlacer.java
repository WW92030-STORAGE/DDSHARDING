package com.normalexisting.ddsharding.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class RevealShardPlacer extends Block {
    final int MAX_RANGE = 64;
    boolean POWERED = false;

    final int MAX_RADIUS = 64;

    public RevealShardPlacer(Properties properties) {
        super(properties);
    }

    Block B = Blocks.GLASS;

    @Override
    public void neighborChanged(BlockState blockstate, Level level, BlockPos pos, Block nn, BlockPos fromPos, boolean moving) {
        super.neighborChanged(blockstate, level, pos, nn, fromPos, moving);
        if (level.getBestNeighborSignal(pos) > 0) {
            if (POWERED) return;
            POWERED = true;

            // Find all shards
            boolean hasRedShard = false;
            ArrayList<Vec3i> blocks = new ArrayList<>(); // all solid blocks
            ArrayList<Vec3i> positions = new ArrayList<Vec3i>(); // all marker positions
            for (int dx = -1 * MAX_RANGE; dx <= MAX_RANGE; dx++) {
                for (int dz = -1 * MAX_RANGE; dz <= MAX_RANGE; dz++) {
                    for (int yy = level.getMinBuildHeight(); yy <= level.getMaxBuildHeight(); yy++) {
                        Vec3i bb = new Vec3i(pos.getX() + dx, yy, pos.getZ() + dz);
                        BlockPos bp = new BlockPos(bb);

                        BlockState bss = level.getBlockState(bp);
                        Block bs = bss.getBlock();

                        if (bss.isSolid()) {
                            Vec3i bprime = new Vec3i(bp.getX(), bp.getY() + 1, bp.getZ());
                            if (bp.getY() + 1 < level.getMaxBuildHeight()) {
                                Block bsprime = level.getBlockState(new BlockPos(bprime)).getBlock();
                                if (bsprime == Blocks.AIR) blocks.add(bb);
                            }
                        }

                        if (bs == ModBlocks.REVEALSHARD.get()) {
                            hasRedShard = true;
                            break;
                        }
                        else if (bs == ModBlocks.REVEAL_POS.get() && !RevealShardPosition.powered.contains(bp)) positions.add(bb);
                    }
                    if (hasRedShard)  break;
                }
                if (hasRedShard) break;
            }
            if (hasRedShard) return;

            // System.out.println("FOUND REVEALSHARD POSITIONS " + positions.toString());
            // System.out.println("FOUND SOLIDS " + blocks.toString());

            Vec3i aaaaa = new Vec3i(pos.getX(), pos.getY(), pos.getZ());
            if (positions.size() > 0) {
                int index = (int) (Math.random() * positions.size());
                aaaaa = positions.get(index);

                System.out.println("MARKERS - RANDOMLY CHOOSING MARKER: " + aaaaa.toString());
            } else if (blocks.size() > 0) {
                int index = (int) (Math.random() * blocks.size());
                aaaaa = blocks.get(index);

                System.out.println("MARKERS - RANDOMLY CHOOSING SOLID: " + aaaaa.toString());
            } else {
                int dx = -MAX_RANGE + (int)(Math.random() * (2 * MAX_RANGE + 1));
                int dy = -MAX_RANGE + (int)(Math.random() * (2 * MAX_RANGE + 1));
                int dz = -MAX_RANGE + (int)(Math.random() * (2 * MAX_RANGE + 1));

                int ypos = pos.getY() + dy;
                ypos = Math.max(Math.min(level.getMaxBuildHeight(), ypos), level.getMinBuildHeight());
                aaaaa = new Vec3i(pos.getX() + dx, ypos, pos.getZ() + dz);

                System.out.println("NO MARKERS - RANDOMLY CHOOSING: " + aaaaa.toString());
            }

            for (int K = 1; K <= MAX_RANGE; K++) {
                try {
                    BlockPos BP = BlockPos.containing(aaaaa.getX(), aaaaa.getY() + K, aaaaa.getZ());
                    Block tester = level.getBlockState(BP).getBlock();
                    if (tester == ModBlocks.REVEALSHARD.get()) return; // do not override shards
                    if (tester == Blocks.AIR) {
                        level.setBlock(BP, ModBlocks.REVEALSHARD.get().defaultBlockState(), 3);
                        return;
                    }
                } catch (Exception e) {

                }
            }


        } else POWERED = false;
    }
}
