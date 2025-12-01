package com.normalexisting.ddsharding.block;

import com.normalexisting.ddsharding.procedures.Saved;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShardBlock extends TransparentBlock {
    public ShardBlock(Properties properties) {
        super(properties);
    }

    Block B = Blocks.GLASS;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        int R = 2;
        return box(8 - R, 8 - R, 8 - R, 8 + R, 8 + R, 8 + R);
    }

    /*

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        int R = 8;
        return box(8 - R, 8 - R, 8 - R, 8 + R, 8 + R, 8 + R);
    }

    */

    /*

    @Override
    public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(blockstate, world, pos, oldState, moving);
        world.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rs) {
        super.tick(state, level, pos, rs);
        level.scheduleTick(pos, this, 1);
        System.out.println(pos);
    }

     */


    @Override
    public void entityInside(BlockState blockstate, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Player) {
            level.destroyBlock(pos, false);
            System.out.println("DESTROYED SHARD");
            level = entity.level();
            System.out.println("CURRENT LEVEL " + level.getServer());
            if (level.getServer() == null) return;

            Saved saved = Saved.get(level.getServer());

            System.out.println("DESTROYING SHARDS ..." + saved.getShards());
            saved.setShards(saved.getShards() + 1);
            System.out.println("DESTROYED SHARDS ..." + saved.getShards());
        }
    }
}
