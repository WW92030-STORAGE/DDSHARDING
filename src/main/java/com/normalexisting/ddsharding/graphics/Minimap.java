package com.normalexisting.ddsharding.graphics;

import com.normalexisting.ddsharding.block.ModBlocks;
import com.normalexisting.ddsharding.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Graph;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix2d;
import org.joml.Vector2d;

import java.awt.*;
import java.util.*;
import java.lang.Thread;

/*

A rudimentary minimap system. It's not the best but it works.
Uses a GuiGraphics to draw dots and squares on the screen.

 */

public class Minimap {
    // Track the stats per player. Because it's controlled by a simple toggle this doens't have to persist.
    public static HashMap<UUID, Boolean> toggle = new HashMap<UUID, Boolean>();

    public static HashMap<UUID, Long> reveal = new HashMap<UUID, Long>();

    static final int RADIUS = 32;
    static final int SCALE = 2;

    // Obstacles are only displayed on the current Y level and the one above. For things like shards which might be useful to keep track of across higher ranges
    // we define a RAD and a FADE. The RAD is how far out the object appears in full opacity on the map, the FADE is the distance it takes to fade out (after exceeding RAD).
    static final double SHARD_RAD = 4;
    static final double SHARD_FADE = 4;
    static final double ENEMY_RAD = 4;
    static final double ENEMY_FADE = 4;

    static final long REVEAL_DURATION = 60;

    static final int VOID = GraphicsLib.RGB(0, 0, 0);
    static final int PLR = GraphicsLib.RGB(255, 0, 0);
    static final int WALL = GraphicsLib.dRGB(0.5, 0.5, 0.5);
    static final double[] SHARD = {0.5, 0, 1.0};
    static final double[] ENEMY = {1.0, 0.0, 0.0};

    static final int CLEAR_CACHE = 1000;

    public static boolean isGoodToDrawWall(Level level, Vec3 pos0) {
        BlockState b = level.getBlockState(Reference.BP(pos0));
        if (!b.isSolid()) return false;

        int dx[] = {1, 0, -1, 0};
        int dy[] = {0, 0, 0, 0};
        int dz[] = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++) {
            Vec3 v = pos0.add(new Vec3(dx[i], dy[i], dz[i]));
            BlockState bb = level.getBlockState(Reference.BP(v));
            if (!bb.isSolid()) return true;
        }
        return false;
    }

    public static void render(GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight, Player player) {
        guiGraphics.enableScissor(0, 0, SCALE * (1 + 2 * RADIUS) - 1, SCALE * (1 + 2 * RADIUS) - 1); // Only render to the top left corner
        if (!player.level().isClientSide()) return;

        Vec3 pos = player.position();
        Level level = player.level();
        double yaw = player.yHeadRot;

        UUID uuid = player.getUUID();
        if (!Minimap.toggle.containsKey(uuid)) {
            Minimap.toggle.put(uuid, true);
            System.out.println("INIT TOGGLE");
        }
        // System.out.println(uuid + " " + toggle.get(uuid));
        if (!toggle.get(uuid)) {
            guiGraphics.disableScissor();
            return;
        }



        double angle = Reference.YawToAngle(yaw) * Reference.DEG2RAD;
        // At zero degrees the X basis vector goes in the +Z direction and the Z basis vector goes in the +X direction.
        Vec3 basisx = new Vec3(Math.sin(angle), 0, Math.cos(angle));
        Vec3 basisz = new Vec3(Math.cos(angle), 0, -1 * Math.sin(angle));

        double SCALED = (double)SCALE;
        int BOUNDS = 2 * RADIUS * SCALE;

        GraphicsLib.drawSquare(guiGraphics, 0, 0, SCALE * (2 * RADIUS + 1), VOID);

            // Draw walls where necessary
            for (int sx = 0; sx <= BOUNDS; sx++) {
                for (int sz = 0; sz <= BOUNDS; sz++) {
                    int pColor = VOID;
                    double ox = (sx / SCALED) - RADIUS;
                    double oz = (sz / SCALED) - RADIUS;
                    Vec3 displacement = (basisx.scale(ox)).add(basisz.scale(oz));
                    Vec3 pos0 = pos.add(displacement);

                    for (int y = 0; y < 2; y++) {
                        if (isGoodToDrawWall(level, pos0)) pColor = WALL;
                        pos0 = pos0.add(new Vec3(0, 1, 0));
                    }

                    if (pColor != VOID) GraphicsLib.drawPixel(guiGraphics, sx, BOUNDS - sz, pColor);


                }
            }

        // Draw shards

        double LARGERAD = RADIUS * Math.sqrt(2);

        ArrayList<Block> filter = new ArrayList<Block>();
        filter.add(ModBlocks.SHARD.get());
        AABB thing = Reference.prism(pos, LARGERAD, SHARD_RAD + SHARD_FADE + 1);
        ArrayList<BlockPos> shards = Reference.getBlocksInAABB(level, thing, filter);

        for (BlockPos bp : shards) {
            double opacity = 0;
            double dist = Math.abs(bp.getY() + 0.5 - pos.y);
            if (dist <= SHARD_RAD) opacity = 1;
            else if (dist <= SHARD_RAD + SHARD_FADE) opacity = 1.0 - (dist - SHARD_RAD) / SHARD_FADE;

            // We need to convert the (x, z) into the relative transform with respect to the player

            double dx = bp.getX() + 0.5 - pos.x;
            double dz = bp.getZ() + 0.5 - pos.z;

            // These coordinates are using the basis vectors [1, 0] and [0, 1]. We need to convert these into the [basisx, basisz] vectors
            // Simplest way to do this is to scale by the inverse of the [basisx; basisz] matrix:

            Matrix2d toConvert = new Matrix2d(basisx.x, basisx.z, basisz.x, basisz.z).invert();
            Vector2d res = toConvert.transform(new Vector2d(dx, dz));

            int rx = Reference.FLOOR((res.x + RADIUS) * SCALE);
            int rz = Reference.FLOOR((RADIUS - res.y) * SCALE);

            GraphicsLib.drawSquare(guiGraphics, rx, rz, SCALE, GraphicsLib.dRGBA(SHARD[0], SHARD[1], SHARD[2], opacity));
        }

        // Draw entities on the map if enabled

        if (reveal.containsKey(player.getUUID())) {
            if (System.currentTimeMillis() - REVEAL_DURATION * 1000 > reveal.get(player.getUUID())) {
                reveal.remove(player.getUUID());
                return;
            }


            Collection<Entity> entities = Reference.getEntitiesAroundPlayer(player.level(), player, LARGERAD, ENEMY_RAD + ENEMY_FADE);
            for (Entity e : entities) {
                if (e instanceof Monster) {
                    Vec3 bp = e.position();
                    double opacity = 0;
                    double dist = Math.abs(bp.y() + 0.5 - pos.y);
                    if (dist <= ENEMY_RAD) opacity = 1;
                    else if (dist <= ENEMY_RAD + ENEMY_FADE) opacity = 1.0 - (dist - ENEMY_RAD) / ENEMY_FADE;

                    // We need to convert the (x, z) into the relative transform with respect to the player

                    double dx = bp.x() + 0.5 - pos.x;
                    double dz = bp.z() + 0.5 - pos.z;

                    // These coordinates are using the basis vectors [1, 0] and [0, 1]. We need to convert these into the [basisx, basisz] vectors
                    // Simplest way to do this is to scale by the inverse of the [basisx; basisz] matrix:

                    Matrix2d toConvert = new Matrix2d(basisx.x, basisx.z, basisz.x, basisz.z).invert();
                    Vector2d res = toConvert.transform(new Vector2d(dx, dz));

                    int rx = Reference.FLOOR((res.x + RADIUS) * SCALE);
                    int rz = Reference.FLOOR((RADIUS - res.y) * SCALE);

                    GraphicsLib.drawSquare(guiGraphics, rx, rz, SCALE, GraphicsLib.dRGBA(ENEMY[0], ENEMY[1], ENEMY[2], opacity));
                }
            }
        }

        // Draw a dot representing the player
        GraphicsLib.drawScaledPixel(guiGraphics, RADIUS, RADIUS, SCALE, PLR);

        guiGraphics.disableScissor();
    }
}