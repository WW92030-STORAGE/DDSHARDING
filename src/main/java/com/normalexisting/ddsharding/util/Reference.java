package com.normalexisting.ddsharding.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reference {
    public static final String MODID = "ddsharding";

    public static final double DEG2RAD = Math.PI / 180.0;
    public static final double RAD2DEG = 180.0 / Math.PI;

    public static int FLOOR(double d) {
        return (int) (Math.floor(d));
    }

    public static int CEIL(double d) {
        return (int)(Math.ceil(d));
    }

    public static Vec3 FLOOR(Vec3 d) {
        return new Vec3(Math.floor(d.x), Math.floor(d.y), Math.floor(d.z));
    }

    public static Vec3 CEIL(Vec3 d) {
        return new Vec3(Math.ceil(d.x), Math.ceil(d.y), Math.ceil(d.z));
    }

    public static int MOD(int a, int b) {
        int res = a % b;
        while (res < 0) res += b;
        return res % b;
    }

    public static double MOD(double a, double b) {
        double res = a % b;
        while (res < 0) res += b;
        return res % b;
    }

    // Yaw specifies +Z is 0 degrees and goes clockwise. The conversion specifies +X is 0 degrees and goes counterclockwise
    public static double YawToAngle(double yaw) {
        double res = 270 - MOD(yaw, 360.0);
        return MOD(res, 360.0);
    }

    /*

    BLOCK AND LEVEL MANIPULATION

     */

    // Gets an Vec3i
    public static Vec3i Vi(Vec3 v) {
        return new Vec3i(FLOOR(v.x), FLOOR(v.y), FLOOR(v.z));
    }

    // Gets the block position of the block enclosing a point
    public static BlockPos BP(Vec3 v) {
        return new BlockPos(FLOOR(v.x), FLOOR(v.y), FLOOR(v.z));
    }

    // Get blocks within an AABB, with or without a filter
    public static ArrayList<BlockPos> getBlocksInAABB(Level level, AABB aabb) {
        return getBlocksInAABB(level, aabb, new HashSet<>());
    }

    public static ArrayList<BlockPos> getBlocksInAABB(Level level, AABB aabb, Collection<Block> filter) {
        ArrayList<BlockPos> res = new ArrayList<>();

        boolean SEQ = false;

        if (SEQ) {
            for (int x = FLOOR(aabb.minX); x <= FLOOR(aabb.maxX); x++) {
                for (int y = FLOOR(aabb.minY); y <= FLOOR(aabb.maxY); y++) {
                    for (int z = FLOOR(aabb.minZ); z <= FLOOR(aabb.maxZ); z++) {
                        if (filter.size() > 0) {
                            boolean has = false;
                            BlockState bs = level.getBlockState(new BlockPos(x, y, z));
                            for (Block b : filter) {
                                if (bs.getBlock() == b) has = true;
                            }
                            if (has) res.add(new BlockPos(x, y, z));
                        } else res.add(new BlockPos(x, y, z));
                    }
                }
            }
        } else {
            final int NTHREADS = Math.clamp(Runtime.getRuntime().availableProcessors() / 2, 1, 16);
            ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
            ArrayList<Callable<Void>> tasks = new ArrayList<>();

            for (int sy = 0; sy < NTHREADS; sy++) {
                final int id = sy;
                tasks.add(() -> {
                    analyzeLayers(id, NTHREADS, level, aabb, filter, res);
                    return null;
                });
            }

            try {
                executor.invokeAll(tasks);

                executor.shutdown();
            } catch (Exception e) {System.out.println("GETBLOCKS ANALYSIS ERROR: " + e.toString());}
        }
        return res;
    }

    static synchronized void appendBlockPoses(ArrayList<BlockPos> destination, ArrayList<BlockPos> toAppend) {
        for (BlockPos bp : toAppend) { destination.add(bp); }
    }

    static void analyzeLayers(int sy, int step, Level level, AABB aabb, Collection<Block> filter, ArrayList<BlockPos> res) {
        ArrayList<BlockPos> temp_list = new ArrayList<>();
        for (int y = FLOOR(aabb.minY) + sy; y <= FLOOR(aabb.maxY); y += step) {
            for (int x = FLOOR(aabb.minX); x <= FLOOR(aabb.maxX); x++) {
                for (int z = FLOOR(aabb.minZ); z <= FLOOR(aabb.maxZ); z++) {
                    if (filter.size() > 0) {
                        boolean has = false;
                        BlockState bs = level.getBlockState(new BlockPos(x, y, z));
                        for (Block b : filter) {
                            if (bs.getBlock() == b) {
                                has = true;
                                break;
                            }
                        }
                        if (has) temp_list.add(new BlockPos(x, y, z));
                    } else temp_list.add(new BlockPos(x, y, z));
                }
            }
        }

        appendBlockPoses(res, temp_list);
    }

    /*

    ENTITY MANIPULATION

     */

    // Get entities within an infinite cylindrical prism centered around the player
    public static Collection<Entity> getEntitiesAroundPlayer(Level level, Player player, double Radius) {
        return level.getEntities(player, prism(player.position(), Radius));
    }

    // Get entities within a finite cylindrical prism centered around the player
    public static Collection<Entity> getEntitiesAroundPlayer(Level level, Player player, double Radius, double vRad) {
        return level.getEntities(player, prism(player.position(), Radius, vRad));
    }

    // Generate bounding boxes in convenient ways

    // Centered around a point and vertical to the world boundaries
    public static AABB prism(Vec3 center, double RAD) {
        Vec3 inferior = center.subtract(new Vec3(RAD, center.y - 0.1, RAD));
        Vec3 superior = center.add(new Vec3(RAD, Minecraft.getInstance().level.getHeight() - center.y - 0.1, RAD));
        return new AABB(FLOOR(inferior), CEIL(superior));
    }

    // Centered around a point and vertical to another radius
    public static AABB prism(Vec3 center, double RAD, double VRAD) {
        Vec3 inferior = center.subtract(new Vec3(RAD, VRAD, RAD));
        Vec3 superior = center.add(new Vec3(RAD, VRAD, RAD));
        return new AABB(FLOOR(inferior), CEIL(superior));
    }

    public static String entityInfo(Entity entity) {
        return "Entity[" + entity.getName() + ", " + entity.getUUID() + "]";
    }

    /*

    PLAYER MANIPULATION

     */

    public static void chat(String s, Player player) {
        player.sendSystemMessage(Component.literal(s));
    }
}
