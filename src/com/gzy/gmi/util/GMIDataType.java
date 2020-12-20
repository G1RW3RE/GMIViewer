package com.gzy.gmi.util;

public class GMIDataType {

    public static Class<?> parseDataType(String typeName) {
        // TODO Complete the type-list
        switch (typeName) {
            case "MET_BYTE":
                return Byte.TYPE;
            case "MET_SHORT":
            case "MET_USHORT":
                return Short.TYPE;
            default:
                return null;
        }
    }
}
