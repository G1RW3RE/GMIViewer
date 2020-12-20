package com.gzy.gmi.util;


public class RawData {

    public final byte[] data;

    public final Class<?> dataType;

    public final int[][] topSlice;
    public final int[][] leftSlice;
    public final int[][] frontSlice;

    public RawData (byte[] data, int x, int y, int z, Class<?> dataType) {
        this.data = data;
        this.dataType = dataType;
        int multiply;
        if(dataType == Byte.TYPE) {
            multiply = 1;
        } else if(dataType == Short.TYPE) {
            multiply = 2;
        } else if(dataType == Integer.TYPE) {
            multiply = 3;
        } else if(dataType == Long.TYPE) {
            multiply = 4;
        } else {
            multiply = 1;
        }
        long t1, t2; // FIXME DEBUG
        t1 = System.currentTimeMillis(); // FIXME DEBUG
        topSlice = new int[z][x * y];
        leftSlice = new int[x][z * y];
        frontSlice = new int[y][x * z];
        int i, j, k;
        int mulIndex = 0;
        int pixel;
        for(k = 0; k < z; k++) {
            for(j = 0; j < y; j++) {
                for(i = 0; i < x; i++) {
                    switch (multiply) {
                        case 1: // 8 : byte
                            pixel = data[mulIndex];
                            break;
                        case 2: // 16 : short
                            pixel = ((int) data[mulIndex]) | (((int) data[mulIndex + 1]) << 8);
                            break;
                        default:
                            pixel = 0;
                    }
                    topSlice[k][i + j * x] = pixel;
                    leftSlice[i][j + k * y] = pixel;
                    frontSlice[j][i + k * x] = pixel;
                    mulIndex += multiply;
                }
            }
        }
        t2 = System.currentTimeMillis(); // FIXME DEBUG
        System.err.println("Finished in " + (t2 - t1) + " ms."); // FIXME DEBUG
    }

}
