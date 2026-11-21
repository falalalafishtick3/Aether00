package dev.aether.gui;

import net.minecraft.client.gui.DrawContext;

/** Small drawing toolkit: rounded rectangles, gradients, circles, shadows, colour math. */
public final class Ui {
    private static final int[][] INSETS = new int[12][];

    private static int[] insets(int r) {
        if (INSETS[r] == null) {
            int[] a = new int[r];
            for (int i = 0; i < r; i++) {
                double dy = r - i - 0.5;
                int dx = (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
                a[i] = Math.max(0, r - dx);
            }
            INSETS[r] = a;
        }
        return INSETS[r];
    }

    public static void rrect(DrawContext g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0 || (color >>> 24) == 0) return;
        r = Math.min(Math.min(r, 11), Math.min(w, h) / 2);
        if (r <= 0) { g.fill(x, y, x + w, y + h, color); return; }
        g.fill(x, y + r, x + w, y + h - r, color);
        int[] in = insets(r);
        for (int i = 0; i < r; i++) {
            g.fill(x + in[i], y + i, x + w - in[i], y + i + 1, color);
            g.fill(x + in[i], y + h - 1 - i, x + w - in[i], y + h - i, color);
        }
    }

    /** Rounded rect with a vertical gradient from c1 (top) to c2 (bottom). */
    public static void rrectV(DrawContext g, int x, int y, int w, int h, int r, int c1, int c2) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(Math.min(r, 11), Math.min(w, h) / 2);
        if (r <= 0) { g.fillGradient(x, y, x + w, y + h, c1, c2); return; }
        g.fillGradient(x, y + r, x + w, y + h - r, lerp(c1, c2, r / (float) h), lerp(c1, c2, (h - r) / (float) h));
        int[] in = insets(r);
        for (int i = 0; i < r; i++) {
            g.fill(x + in[i], y + i, x + w - in[i], y + i + 1, lerp(c1, c2, (i + 0.5f) / h));
            g.fill(x + in[i], y + h - 1 - i, x + w - in[i], y + h - i, lerp(c1, c2, (h - 1 - i + 0.5f) / h));
        }
    }

    public static void circle(DrawContext g, int cx, int cy, int r, int color) {
        if (r <= 0) { g.fill(cx, cy, cx + 1, cy + 1, color); return; }
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    /** Soft drop shadow under a rounded box. */
    public static void shadow(DrawContext g, int x, int y, int w, int h, int r, int strength) {
        for (int i = 1; i <= 5; i++) {
            int a = Math.max(0, strength - i * (strength / 6 + 1));
            rrect(g, x - i, y - i + 3, w + 2 * i, h + 2 * i, r + i, a << 24);
        }
    }

    // ---------------- colour math ----------------
    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (int) ((a >>> 24) + ((b >>> 24) - (a >>> 24)) * t);
        int rr = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int gg = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bb = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (aa << 24) | (rr << 16) | (gg << 8) | bb;
    }
    public static int alpha(int argb, float mul) {
        int a = Math.max(0, Math.min(255, Math.round((argb >>> 24) * mul)));
        return (a << 24) | (argb & 0xFFFFFF);
    }
    public static int withAlpha(int rgb, int a) { return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF); }
    public static int lighten(int argb, float k) { return lerp(argb | 0xFF000000, 0xFFFFFFFF, k); }
    public static int darken(int argb, float k) { return lerp(argb | 0xFF000000, 0xFF000000, k); }
    public static float ease(float t) { t = Math.max(0f, Math.min(1f, t)); return t * t * (3f - 2f * t); }
}
