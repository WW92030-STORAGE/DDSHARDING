package com.normalexisting.ddsharding.block;

import com.normalexisting.ddsharding.procedures.Saved;
import com.normalexisting.ddsharding.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ShardTesterEntity extends BlockEntity {
    public long value = 0;
    public int compareAgainst = 0; // 0 EQUAL 1 LARGER 2 SMALLER
    public ShardTesterEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SHARD_TESTER_ENTITY.get(), pPos, pBlockState);
    }
    public boolean test(long x) {
        compareAgainst = Reference.MOD(compareAgainst, 3);
        if (compareAgainst == 0) return x == value;
        if (compareAgainst == 1) return x > value;
        return x < value;
    }

    public void setValue(long v) {
        value = v;
        this.setChanged();
    }

    public void addValue(long v) {
        value += v;
        this.setChanged();
    }

    public void changeComparison() {
        compareAgainst = Reference.MOD(compareAgainst + 1, 3);
        this.setChanged();
    }

    public long getValue() {
        return value;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.putLong("value", value);
        pTag.putInt("compare", compareAgainst);

        System.out.println("SAVED VALUE = " + Long.toString(value) + " / COMPARE_AGAINST " + Integer.toString(compareAgainst));
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        value = pTag.getLong("value");
        compareAgainst = pTag.getInt("compare");

        System.out.println("LOADED VALUE = " + Long.toString(value) + " / COMPARE_AGAINST " + Integer.toString(compareAgainst));
    }


    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        var packet = ClientboundBlockEntityDataPacket.create(this);
        System.out.println("PACKET GET_UPDATE_TAG" + packet.toString());
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        System.out.println("COMPOUNDTAG GET_UPDATE_TAG: " + pRegistries.toString());
        return saveWithoutMetadata(pRegistries);
    }
}
