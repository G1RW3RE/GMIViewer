package com.gzy.gmi.util;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;

/** mask layer */
public class GMIMask3D {

    static final int[][] GROW_DIRECTIONS_6 = new int[][] {
            {0, 0, 1},
            {0, 1, 0},
            {1, 0, 0},
            {0, 0, -1},
            {0, -1, 0},
            {-1, 0, 0}
    };

    static final int[][] GROW_DIRECTIONS_26 = new int[][] {
            {0, 0, 1},
            {0, 0, -1},
            {0, 1, 0},
            {0, -1, 0},
            {1, 0, 0},
            {-1, 0, 0},
            {0, 1, 1},
            {0, 1, -1},
            {0, -1, 1},
            {0, -1, -1},
            {1, 0, 1},
            {1, 0, -1},
            {-1, 0, 1},
            {-1, 0, -1},
            {1, 1, 0},
            {1, -1, 0},
            {-1, 1, 0},
            {-1, -1, 0},
            {1, 1, 1},
            {1, 1, -1},
            {1, -1, 1},
            {1, -1, -1},
            {-1, 1, 1},
            {-1, 1, -1},
            {-1, -1, 1},
            {-1, -1, -1}
    };

    public final boolean[][] bottomSlice;
    public final boolean[][] rightSlice;
    public final boolean[][] frontSlice;

    /** dimensions */
    private final int dimX, dimY, dimZ;

    /** int ARGB */
    private int color;

    /** display name of mask */
    private String maskName;

    private static final String DEFAULT_MASK_NAME = "未命名遮罩";

    public GMIMask3D(int x, int y, int z) {
        this(x, y, z, null);
    }

    public GMIMask3D(int x, int y, int z, String name) {
        assert x > 0 && y > 0 && z > 0;
        bottomSlice = new boolean[z][x * y];
        rightSlice = new boolean[x][z * y];
        frontSlice = new boolean[y][x * z];
        bottomEmptySlice = new boolean[x * y];
        rightEmptySlice = new boolean[z * y];
        frontEmptySlice = new boolean[x * z];
        dimX = x;
        dimY = y;
        dimZ = z;
        maskName = Optional.ofNullable(name).orElse(DEFAULT_MASK_NAME);
        color = generateRandColor();
    }

    private final Random random = new Random();

    private int generateRandColor() {
        int r = (random.nextInt(155) + 100) << 16 ;
        int g = (random.nextInt(155) + 100) << 8;
        int b = (random.nextInt(155) + 100);
        return r | g | b;
    }

    /** @return RGB int color */
    public int getColor() {
        return color & 0x00_FF_FF_FF;
    }

    public void setColor(int color) {
        this.color = color;
    }

    /** @return mask name */
    public String getMaskName() {
        return maskName;
    }

    public void setMaskName(String maskName) {
        this.maskName = maskName;
    }

    /** set a dot onto mask */
    public void drawPoint(int x, int y, int z) {
        bottomSlice[z][x + y * dimX] = true;
        rightSlice[x][y + (dimZ - 1 - z) * dimY] = true;
        frontSlice[y][x + (dimZ - 1 - z) * dimX] = true;
    }

    public boolean getPoint(int x, int y, int z) {
        if(x < 0 || y < 0 || z < 0 || x >= dimX || y >= dimY || z >= dimZ) {
            return false;
        }
        return bottomSlice[z][x + y * dimX];
    }

    public boolean checkPointInBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < dimX && y < dimY && z < dimZ;
    }

    /** create a deep copy of itself */
    public GMIMask3D createCopy() {
        GMIMask3D mask3D = new GMIMask3D(this.dimX, this.dimY, this.dimZ, this.maskName + " 副本");
        for (int i = 0; i < bottomSlice.length; i++) {
            System.arraycopy(bottomSlice[i], 0, mask3D.bottomSlice[i], 0, bottomSlice[i].length);
        }
        for (int i = 0; i < frontSlice.length; i++) {
            System.arraycopy(frontSlice[i], 0, mask3D.frontSlice[i], 0, frontSlice[i].length);
        }
        for (int i = 0; i < rightSlice.length; i++) {
            System.arraycopy(rightSlice[i], 0, mask3D.rightSlice[i], 0, rightSlice[i].length);
        }
        return mask3D;
    }

    // empty slices uses to erase
    final boolean[] bottomEmptySlice;
    final boolean[] rightEmptySlice;
    final boolean[] frontEmptySlice;

    /** clear all */
    public void clearMask() {
        for (boolean[] booleans : bottomSlice) {
            System.arraycopy(bottomEmptySlice, 0, booleans, 0, booleans.length);
        }
        for (boolean[] booleans : frontSlice) {
            System.arraycopy(frontEmptySlice, 0, booleans, 0, booleans.length);
        }
        for (boolean[] booleans : rightSlice) {
            System.arraycopy(rightEmptySlice, 0, booleans, 0, booleans.length);
        }
    }

    /**
     * @param x region grow seed point
     * @param y region grow seed point
     * @param z region grow seed point
     * @param connType 6 or 26
     * */
    public static GMIMask3D regionGrowing(GMIMask3D maskOrigin, int x, int y, int z, int connType) {
        assert maskOrigin != null;
        assert connType == 6 || connType == 26;
        int[][] dpArray = connType == 6 ? GROW_DIRECTIONS_6 : GROW_DIRECTIONS_26;
        GMIMask3D maskNew = new GMIMask3D(maskOrigin.dimX, maskOrigin.dimY, maskOrigin.dimZ, "区域生长");
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{x, y, z});
        while (!queue.isEmpty()) {
            int[] point = queue.poll();
            if (!maskNew.getPoint(point[0], point[1], point[2])) {
                maskNew.drawPoint(point[0], point[1], point[2]);
                for (int[] dPoint : dpArray) {
                    if (maskOrigin.checkPointInBounds(point[0] + dPoint[0], point[1] + dPoint[1], point[2] + dPoint[2])
                            && !maskNew.getPoint(point[0] + dPoint[0], point[1] + dPoint[1], point[2] + dPoint[2])
                            && maskOrigin.getPoint(point[0] + dPoint[0], point[1] + dPoint[1], point[2] + dPoint[2])) {
                        queue.offer(new int[]{point[0] + dPoint[0], point[1] + dPoint[1], point[2] + dPoint[2]});
                    }
                }
            }
        }
        return maskNew;
    }
}
