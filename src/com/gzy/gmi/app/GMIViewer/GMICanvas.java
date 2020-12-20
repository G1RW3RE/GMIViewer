package com.gzy.gmi.app.GMIViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/** Canvas to paint image on */
public class GMICanvas extends JPanel {

    /** width of image slice */
    private int imgWidth;

    /** height of image slice */
    private int imgHeight;

    /** raw data of image slices */
    private int[][] imgData;

    /** index of image slice displaying, 0 <= currentLayer < imgData.length */
    private int currentLayer;

    /** displaying image */
    private BufferedImage image;
    /** raster */
    WritableRaster raster;

    public GMICanvas() {
        super();
    }

    /** put data of image slices into this component */
    public void loadImageData(int[][] data, int width, int height) {
        assert data != null;
        imgData = data;
        assert width > 0 && height > 0 && width * height == data[0].length;
        imgWidth = width;
        imgHeight = height;
        currentLayer = 0;
        image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        raster = (WritableRaster) image.getData();
        // set initial layer to middle
        onLayerChanged(data.length / 2);
    }

    /** invoke when layer changed */
    public void onLayerChanged(int layer) {
        currentLayer = layer;
        int[] rawImageData = imgData[currentLayer];
        raster.setPixels(0, 0, imgWidth, imgHeight, rawImageData);
        image.setData(raster);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
