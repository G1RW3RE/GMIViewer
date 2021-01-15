package com.gzy.gmi.util;


/**
 * Data structure to store data of .raw and other necessary info
 * */
public class RawData {

    /** raw data */
    public final short[] data;

    public final Class<?> dataType;

    /** discrete histogram of raw image */
    public final int[] histogram;

    public final int lowestValue, highestValue;

    private final int x, y, z;

    public final static String ORIENT_BOTTOM = "bottom";
    public final static String ORIENT_RIGHT = "right";
    public final static String ORIENT_FRONT = "front";

    /**
     * @param data raw binary input of .raw file
     * @param dataType data type of binary stream, usually MET_SHORT\MET_BYTE
     * */
    public RawData (byte[] data, int x, int y, int z, Class<?> dataType) {
        this.dataType = dataType;
        this.x = x;
        this.y = y;
        this.z = z;
        int multiply;
        if(dataType == Byte.TYPE) {
            multiply = 1;
        } else if(dataType == Short.TYPE) {
            multiply = 2;
        } else if(dataType == Integer.TYPE) {
            multiply = 4;
        } else if(dataType == Long.TYPE) {
            multiply = 8;
        } else {
            multiply = 1;
        }
        this.data = new short[data.length / multiply];
        long tStart, tFinish;
        tStart = System.currentTimeMillis();
        int i, j, k;
        int index = 0, mulIndex = 0;
        int lowestValue0 = Integer.MAX_VALUE;
        int highestValue0 = Integer.MIN_VALUE;
        histogram = new int[CTWindow.HIGHEST_CT_VALUE - CTWindow.LOWEST_CT_VALUE + 1];
        for(k = 0; k < z; k++) {
            for(j = 0; j < y; j++) {
                for(i = 0; i < x; i++) {
                    // note that instant numbers are of int type (like 0xFF)
                    switch (multiply) {
                        case 1: // 8 : byte
                            this.data[index] = data[mulIndex];
                            break;
                        case 2: // 16 : short
                            this.data[index] = (short) ((((int) data[mulIndex]) & 0xFF) | ((((int) data[mulIndex + 1] & 0xFF) << 8)));
                            break;
                        case 4: // 32 : int
                            this.data[index] = (short) (
                                    ((int) data[mulIndex] & 0xFF)
                                            | (((int) data[mulIndex + 1] & 0xFF) << 8)
                                            | (((int) data[mulIndex + 2] & 0xFF) << 16)
                                            | (((int) data[mulIndex + 3] & 0xFF) << 24)
                            );
                            break;
                        case 8: // 64 : long
                            this.data[index] = (short) (
                                    ((long) data[mulIndex] & 0xFF)
                                            | (((long) data[mulIndex + 1] & 0xFF) << 8)
                                            | (((long) data[mulIndex + 2] & 0xFF) << 16)
                                            | (((long) data[mulIndex + 3] & 0xFF) << 24)
                                            | (((long) data[mulIndex + 4] & 0xFF) << 32)
                                            | (((long) data[mulIndex + 5] & 0xFF) << 40)
                                            | (((long) data[mulIndex + 6] & 0xFF) << 48)
                                            | (((long) data[mulIndex + 7] & 0xFF) << 56)
                                    );
                            break;
                        default:
                            this.data[index] = 0;
                    }
                    lowestValue0 = Math.min(this.data[index], lowestValue0);
                    highestValue0 = Math.max(this.data[index], highestValue0);
                    // histogram
                    histogram[Math.max(Math.min(this.data[index] - CTWindow.LOWEST_CT_VALUE, CTWindow.MAX_WINDOW_SIZE - 1), 0)]++;
                    index += 1;
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

    /**
     * @param buffer input buffer of image slice
     * @param layer current slice layer
     * @return filled buffer
     * */
    public int[] getSlice(int[] buffer, String orientation, int layer) {
        if(buffer == null || layer < 0) {
            return null;
        } else {
            switch (orientation) {
                case ORIENT_BOTTOM:
                    return getBottomSlice(buffer, layer);
                case ORIENT_FRONT:
                    return getFrontSlice(buffer, layer);
                case ORIENT_RIGHT:
                    return getRightSlice(buffer, layer);
                default:
                    return null;
            }
        }
    }

    /**
     * get bottom slice at layer
     * @return true if finished with no exception
     * */
    public int[] getBottomSlice(int[] buffer, int layer) {
        if(buffer == null || buffer.length != x * y || layer < 0 || layer >= z) {
            return null;
        } else {
            for(int j = 0; j < y; j++) {
                for(int i = 0; i < x; i++) {
                    buffer[i + j * x] = data[i + j * x + layer * x * y]; // TODO optimise
                }
            }
            return buffer;
        }
    }

    /**
     * get front slice at layer
     * @return true if finished with no exception
     * */
    public int[] getFrontSlice(int[] buffer, int layer) {
        if(buffer == null || buffer.length != x * z || layer < 0 || layer >= y) {
            return null;
        } else {
            for(int k = 0; k < z; k++) {
                for(int i = 0; i < x; i++) {
                    buffer[i + (z - 1 - k) * x] = data[i + layer * x + k * x * y]; // TODO optimise
                }
            }
            return buffer;
        }
    }

    /**
     * get right slice at layer
     * @return true if finished with no exception
     * */
    public int[] getRightSlice(int[] buffer, int layer) {
        if(buffer == null || buffer.length != y * z || layer < 0 || layer >= x) {
            return null;
        } else {
            for(int k = 0; k < z; k++) {
                for(int j = 0; j < y; j++) {
                    buffer[j + (z - 1 - k) * y] = data[layer + j * x + k * x * y]; // TODO optimise
                }
            }
            return buffer;
        }
    }

    public int[] getBottomSlice(int layer) {
        return getBottomSlice(new int[x * y], layer);
    }

    public int[] getFrontSlice(int layer) {
        return getFrontSlice(new int[x * z], layer);
    }

    public int[] getRightSlice(int layer) {
        return getRightSlice(new int[y * z], layer);
    }
}
