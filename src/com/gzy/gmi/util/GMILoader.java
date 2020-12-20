package com.gzy.gmi.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.Properties;

public class GMILoader {

    public static MHDInfo loadMHDFile(File file) throws IOException {
        Properties properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream(file);

        properties.load(fileInputStream);

        String fileName = properties.getProperty("ElementDataFile");
        String rawFilePath = file.getParent() + "/" + fileName;
        properties.setProperty("FilePath", rawFilePath);

        return new MHDInfo(properties);
    }

    public static RawData loadRawFile(File rawFile, int x, int y, int z, Class<?> dataType) throws IOException {
        RawData rawData;
        assert rawFile != null;
        if(rawFile.exists()) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(rawFile));
            int len = bufferedInputStream.available();
            byte[] data = new byte[len];
            bufferedInputStream.read(data, 0, len);
            rawData = new RawData(data, x, y, z, dataType);
        } else {
            rawData = null;
        }
        return rawData;
    }

    public static RawData loadRawFromMHD(MHDInfo info) throws IOException {
        return loadRawFile(info.rawFile, info.x, info.y, info.z, info.dataType);
    }

}
