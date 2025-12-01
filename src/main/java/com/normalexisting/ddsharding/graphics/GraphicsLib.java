package com.normalexisting.ddsharding.graphics;

import com.normalexisting.ddsharding.util.Reference;
import net.minecraft.client.gui.GuiGraphics;

/*

Simple library for 2D graphics when used with GuiGraphics

 */

public class GraphicsLib {
    public static int RGB(int r, int g, int b) {
        return RGBA(r, g, b, 255);
    }
    public static int RGBA(int r, int g, int b, int a) {
        r %= 256;
        g %= 256;
        b %= 256;
        a %= 256;
        int rgba = (a<<24) + (r<<16) + (g<<8) + b;
        return rgba;
    }

    public static int dRGB(double r, double g, double b) {
        return dRGBA(r, g, b, 1.0);
    }

    public static int dRGBA(double r, double g, double b, double a) {
        final int Z = 255;
        return RGBA(Reference.FLOOR(r * Z), Reference.FLOOR(g * Z), Reference.FLOOR(b * Z), Reference.FLOOR(a * Z));
    }

    public static void drawPixel(GuiGraphics guiGraphics, int x, int y, int pColor) {
        guiGraphics.fill(x, y, x + 1, y + 1, pColor);
    }

    public static void drawScaledPixel(GuiGraphics guiGraphics, int x, int y, int scale, int pColor) {
        guiGraphics.fill(x * scale, y * scale, (x + 1) * scale, (y + 1) * scale, pColor);
    }

    public static void drawSquare(GuiGraphics guiGraphics, int cx, int cy, int SIDE, int pColor) {
        guiGraphics.fill(cx, cy, cx + SIDE, cy + SIDE, pColor);
    }

    public static void drawSquareCentered(GuiGraphics guiGraphics, int cx, int cy, int SIDE, int pColor) {
        int sx = cx - SIDE / 2;
        int sy = cy - SIDE / 2;
        guiGraphics.fill(sx, sy, sx + SIDE, sy + SIDE, pColor);
    }
}
