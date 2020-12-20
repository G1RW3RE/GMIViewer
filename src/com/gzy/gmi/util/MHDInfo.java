package com.gzy.gmi.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class MHDInfo {

    /** key/value of mhd */
    private final Properties properties;

    /** DimSize = x y z, int[3] */
    public final int[] dimSize;
    public final int x, y, z;

    /** ElementSpacing = a b c, double[3] */
    public final double[] spacing;

    /** Raw File */
    public final File rawFile;

    /** Data Type */
    public final Class<?> dataType;

    public MHDInfo(Properties properties) throws MHDWrongFormatException {

        this.properties = properties;

        // load DimSize
        String dimSizeS = properties.getProperty("DimSize");
        if(dimSizeS == null) { throw new MHDWrongFormatException("Key \"DimSize\" not found."); }
        String[] ss = dimSizeS.split(" ");
        if(ss.length != 3) { throw new MHDWrongFormatException("Value of \"DimSize\" is in wrong format: " + dimSizeS); }
        dimSize = new int[3];
        try {
            x = dimSize[0] = Integer.parseInt(ss[0]);
            y = dimSize[1] = Integer.parseInt(ss[1]);
            z = dimSize[2] = Integer.parseInt(ss[2]);
        } catch (NumberFormatException numberFormatException) {
            throw new MHDWrongFormatException("Value of \"DimSize\" is in wrong format: " + dimSizeS);
        }

        // load spacing
        String spacingS = properties.getProperty("ElementSpacing");
        if(spacingS == null) { throw new MHDWrongFormatException("Key \"ElementSpacing\" not found."); }
        String[] ss2 = spacingS.split(" ");
        if(ss2.length != 3) { throw new MHDWrongFormatException("Value of \"ElementSpacing\" is in wrong format: " + spacingS); }
        spacing = new double[3];
        try {
            spacing[0] = Double.parseDouble(ss2[0]);
            spacing[1] = Double.parseDouble(ss2[1]);
            spacing[2] = Double.parseDouble(ss2[2]);
        } catch (NumberFormatException numberFormatException) {
            throw new MHDWrongFormatException("Value of \"ElementSpacing\" is in wrong format: " + spacingS);
        }

        // load raw file
        rawFile = new File(properties.getProperty("FilePath"));

        // load dataType
        dataType = GMIDataType.parseDataType(properties.getProperty("ElementType"));

    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /** DEBUG */
    public void debugOutput() {
        System.out.println("------------------------DEBUG------------------------");
        Enumeration<?> enumeration = properties.propertyNames();
        while(enumeration.hasMoreElements()) {
            String s = ((String) enumeration.nextElement()).intern();
            System.out.println(s + " = " + properties.getProperty(s));
        }
        System.out.println("spacing : " + spacing[0] + " " + spacing[1] + " " + spacing[2]);
        System.out.println("dimSize : " + dimSize[0] + " " + dimSize[1] + " " + dimSize[2]);
        System.out.println(rawFile);
        System.out.println("----------------------END DEBUG----------------------\n");
    }

    public static class MHDWrongFormatException extends IOException {
        public MHDWrongFormatException(String description) {
            super("Wrong MHD Format : " + description);
        }
    }
}
