package com.normalexisting.ddsharding.block;

import com.normalexisting.ddsharding.graphics.Minimap;
import com.normalexisting.ddsharding.procedures.Saved;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RevealShardBlock extends ShardBlock {
    public RevealShardBlock(Properties properties) {
        super(properties);
    }

    Block B = Blocks.GLASS;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        int R = 3;
        return box(8 - R, 8 - R, 8 - R, 8 + R, 8 + R, 8 + R);
    }


    @Override
    public void entityInside(BlockState blockstate, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Player) {
            level.destroyBlock(pos, false);
            System.out.println("DESTROYED REVEAL SHARD");
            level = entity.level();
            System.out.println("CURRENT LEVEL " + level.getServer());
            if (level.getServer() == null) return;

            Minimap.reveal.put(((Player)(entity)).getUUID(), System.currentTimeMillis());
        }
    }
}
