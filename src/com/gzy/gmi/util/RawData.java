package com.gzy.gmi.util;


/**
 * Data structure to store data of .raw and other necessary info
 * */
public class RawData {

    /** raw data */
    public final byte[] data;

    public final Class<?> dataType;

    public final int[][] bottomSlice;
    public final int[][] rightSlice;
    public final int[][] frontSlice;

    /** discrete histogram of raw image */
    public final int[] histogram;

    public final int lowestValue, highestValue;

    /**
     * @param data raw binary input of .raw file
     * @param dataType data type of binary stream, usually MET_SHORT\MET_BYTE
     * */
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
        long tStart, tFinish;
        tStart = System.currentTimeMillis();
        bottomSlice = new int[z][x * y];
        rightSlice = new int[x][z * y];
        frontSlice = new int[y][x * z];
        int i, j, k;
        int mulIndex = 0;
        int pixel;
        int lowestValue0 = Integer.MAX_VALUE;
        int highestValue0 = Integer.MIN_VALUE;
        histogram = new int[CTWindow.HIGHEST_CT_VALUE - CTWindow.LOWEST_CT_VALUE + 1];
        for(k = 0; k < z; k++) {
            for(j = 0; j < y; j++) {
                for(i = 0; i < x; i++) {
                    switch (multiply) {
                        case 1: // 8 : byte
                            pixel = data[mulIndex];
                            break;
                        case 2: // 16 : short
                            pixel = (((int) data[mulIndex]) & 0xFF) | ((((int) data[mulIndex + 1]) << 8));
                            break;
                        default:
                            pixel = 0;
                    }
                    lowestValue0 = Math.min(pixel, lowestValue0);
                    highestValue0 = Math.max(pixel, highestValue0);
                    // RAI slices
                    bottomSlice[k][i + j * x] = pixel;
                    rightSlice[i][j + (z - 1 - k) * y] = pixel;
                    frontSlice[j][i + (z - 1 - k) * x] = pixel;
                    // histogram
                    histogram[Math.min(pixel - CTWindow.LOWEST_CT_VALUE, CTWindow.MAX_WINDOW_SIZE - 1)]++;
                    mulIndex += multiply;
                }
            }
        }
        lowestValue = lowestValue0;
        highestValue = highestValue0;
        tFinish = System.currentTimeMillis();
        System.out.println(lowestValue + ", " + highestValue);
        System.out.println("Load finished in " + (tFinish - tStart) + " ms.");
    }

}
