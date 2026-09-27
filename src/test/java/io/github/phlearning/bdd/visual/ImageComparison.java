package io.github.phlearning.bdd.visual;

import java.awt.image.BufferedImage;

/**
 * Pixel comparison of two screenshots. A pixel differs when one of its colour channels moves
 * by more than {@code tolerance} (0-255): small anti-aliasing and rendering variations are
 * ignored, real changes are not.
 */
public final class ImageComparison {

    private static final int HIGHLIGHT = 0xFFE5383B;

    /**
     * @param differingPixels pixels that differ beyond the tolerance
     * @param diff            the actual image faded, differing pixels in red; null when sizes differ
     */
    public record Result(boolean sameSize, long differingPixels, long totalPixels, BufferedImage diff) {

        public double differingRatio() {
            return totalPixels == 0 ? 1 : (double) differingPixels / totalPixels;
        }
    }

    private ImageComparison() {}

    public static Result compare(BufferedImage expected, BufferedImage actual, int tolerance) {
        if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
            return new Result(false, 0, 0, null);
        }
        int width = actual.getWidth();
        int height = actual.getHeight();
        BufferedImage diff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        long differing = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = expected.getRGB(x, y);
                int b = actual.getRGB(x, y);
                if (differs(a, b, tolerance)) {
                    differing++;
                    diff.setRGB(x, y, HIGHLIGHT);
                } else {
                    diff.setRGB(x, y, fade(b));
                }
            }
        }
        return new Result(true, differing, (long) width * height, diff);
    }

    private static boolean differs(int a, int b, int tolerance) {
        for (int shift = 0; shift <= 16; shift += 8) {
            if (Math.abs(((a >> shift) & 0xFF) - ((b >> shift) & 0xFF)) > tolerance) {
                return true;
            }
        }
        return false;
    }

    /** Light grey version of a pixel, so that the red differences stand out. */
    private static int fade(int rgb) {
        int grey = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
        int light = 200 + grey * 55 / 255;
        return (light << 16) | (light << 8) | light;
    }
}
