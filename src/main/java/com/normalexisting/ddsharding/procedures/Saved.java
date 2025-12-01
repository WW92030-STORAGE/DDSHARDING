package com.normalexisting.ddsharding.procedures;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class Saved extends SavedData {
    public long shards = 0;

    public Saved() {
        setDirty();
    }

    public long getShards() {
        return this.shards;
    }

    public void setShards(long test) {
        this.shards = test;
        this.setDirty();
    }

    public Saved create() {
        return new Saved();
    }

    public static Saved load(CompoundTag tag, HolderLookup.Provider provider) {
        Saved data = new Saved();
        data.setShards(tag.getLong("shards"));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLong("shards", shards);
        return tag;
    }

    public static Factory<Saved> factory() {
        return new SavedData.Factory<>(Saved::new, Saved::load, null);
    }

    public static Saved get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), "saved");
    }
}
