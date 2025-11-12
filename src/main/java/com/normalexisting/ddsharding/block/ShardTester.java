package com.normalexisting.ddsharding.block;

import com.mojang.serialization.MapCodec;
import com.normalexisting.ddsharding.procedures.Saved;
import com.normalexisting.ddsharding.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class ShardTester extends BaseEntityBlock {

    public static final MapCodec<ShardTester> CODEC = simpleCodec(ShardTester::new);
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public ShardTester(Properties properties) {
        super(properties);
    }

    // Block entities are tied to their corresponding blocks and viceversa

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ShardTesterEntity(pPos, pState);
    }

    public long getTotalShards(Level level) {
        return Saved.get(level.getServer()).getShards();
    }

    public void setTotalShards(Level level, long x) {
        Saved.get(level.getServer()).setShards(x);
    }


    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos,
                            BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            if(pLevel.getBlockEntity(pPos) instanceof ShardTesterEntity) {
                // STE.drops();
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public int getSignal(BlockState blockstate, BlockGetter blockAccess, BlockPos pos, Direction direction) {
        Block b = Blocks.DAYLIGHT_DETECTOR;
        int res = 0;
        if (blockAccess.getBlockEntity(pos) instanceof ShardTesterEntity STE) {
            Level level = STE.getLevel();
            if (STE.test(getTotalShards(level))) {
                res = 15;
            }
        }
        return res;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rs) {
        super.tick(state, level, pos, rs);
        level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
        level.neighborChanged(pos, this, pos);

        Block b = Blocks.DAYLIGHT_DETECTOR;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return !pLevel.isClientSide && pLevel.dimensionType().hasSkyLight() ? createTickerHelper(pBlockEntityType, ModBlockEntities.SHARD_TESTER_ENTITY.get(), ShardTester::tickEntity) : null;
    }

    private static void tickEntity(Level pLevel, BlockPos pPos, BlockState pState, ShardTesterEntity pShardTesterEntity) {
        pLevel.updateNeighborsAt(pPos, pLevel.getBlockState(pPos).getBlock());
        pLevel.neighborChanged(pPos, pLevel.getBlockState(pPos).getBlock(), pPos);
    }

    /*


    Stick = Resets the comparison threshold to 0.
    Redstone = Changes the comparison operator (equals, greater, less).
    ShardTester = Resets the total number of shards to zero.
    Shard = Print the total number of collected shards without the other stuff.
    Iron [nugget, ingot, block] = Increase the comparison threshold by [1, 10, 100]. Comparison threshold and total shards is a Long.
    Gold [nugget, ingot, block] = Decrease the comparison threshold by [1, 10, 100]. The comparison threshold cannot go below zero.

    Every interaction except for shardTester and shard will print out the specification of the shardTester.


     */

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel,
                                              BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        // ItemInteractionResult res = super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);;
        if (pLevel.isClientSide()) return ItemInteractionResult.SUCCESS;
        if (pLevel.getBlockEntity(pPos) instanceof ShardTesterEntity STE) {
            if (pStack.getItem() == Items.STICK) {
                STE.setValue(0);
            }
            else if (pStack.getItem() == Items.REDSTONE) {
                STE.changeComparison();
            }
            else if (pStack.getItem() == new ItemStack(ModBlocks.SHARDTESTER.get()).getItem()) {
                setTotalShards(pLevel, 0);
                Reference.chat("RESET SHARD COUNT", pPlayer);
                pLevel.updateNeighborsAt(pPos, pLevel.getBlockState(pPos).getBlock());
                pLevel.scheduleTick(pPos, pLevel.getBlockState(pPos).getBlock(), 0);

                return ItemInteractionResult.SUCCESS;
            }
            else if (pStack.getItem() == new ItemStack(ModBlocks.SHARD.get()).getItem()) {
                Reference.chat("SHARDS: " + getTotalShards(pLevel), pPlayer);
                pLevel.updateNeighborsAt(pPos, pLevel.getBlockState(pPos).getBlock());
                pLevel.scheduleTick(pPos, pLevel.getBlockState(pPos).getBlock(), 0);

                return ItemInteractionResult.SUCCESS;
            }
            else if (pStack.getItem() == Items.IRON_NUGGET) {
                STE.addValue(1);
            }
            else if (pStack.getItem() == Items.IRON_INGOT) {
                STE.addValue(10);
            }
            else if (pStack.getItem() == Items.IRON_BLOCK) {
                STE.addValue(100);
            }
            else if (pStack.getItem() == Items.GOLD_NUGGET) {
                STE.addValue(-1);
            }
            else if (pStack.getItem() == Items.GOLD_INGOT) {
                STE.addValue(-10);
            }
            else if (pStack.getItem() == Items.GOLD_BLOCK) {
                STE.addValue(-100);
            }
            else {
            }

            if (STE.getValue() < 0) STE.setValue(0);

            String comparison = "IS EQUAL TO";
            if (STE.compareAgainst == 1) comparison = "IS LARGER THAN";
            if (STE.compareAgainst == 2) comparison = "IS SMALLER THAN";
            String status = "TESTER AT " + pPos + " CHECKING IF TOTAL SHARDS [" + getTotalShards(pLevel) + "] " + comparison + " " + STE.getValue();
            Reference.chat(status, pPlayer);
        }

        pLevel.updateNeighborsAt(pPos, pLevel.getBlockState(pPos).getBlock());
        pLevel.scheduleTick(pPos, pLevel.getBlockState(pPos).getBlock(), 0);

        return ItemInteractionResult.SUCCESS;
    }
}
