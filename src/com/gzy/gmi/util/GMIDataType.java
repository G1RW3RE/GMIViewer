package com.gzy.gmi.util;

public final class GMIDataType {

    /** actually no unsigned value */
    public static Class<?> parseDataType(String typeName) {
        switch (typeName) {
            case "MET_CHAR":
            case "MET_UCHAR":
                return Byte.TYPE;
            case "MET_SHORT":
            case "MET_USHORT":
                return Short.TYPE;
            case "MET_INT":
            case "MET_UINT":
                return Integer.TYPE;
            case "MET_LONG_LONG":
            case "MET_ULONG_LONG":
                return Long.TYPE;
            default:
                return null;
        }
    }
}
