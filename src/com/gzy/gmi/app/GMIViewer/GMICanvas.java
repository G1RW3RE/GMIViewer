package com.gzy.gmi.app.GMIViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/** Canvas to paint image on */
public class GMICanvas extends JPanel implements ComponentListener {

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
        repaint();
    }

    /** Visual area of image */
    int visX, visY, visWidth, visHeight;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
//        if(image != null) {
//            g.drawImage(image, visX, visY, visWidth, visHeight, this);
//        }
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if(imgData != null) {
            int w = getWidth();
            int h = getHeight();
            if (imgWidth * h < imgHeight * w) {
                // imgWidth / imgHeight < w / h, [||]
                visWidth = h * imgWidth / imgHeight;
                visHeight = h;
                visX = (w - visWidth + 1) / 2;
                visY = 0;
            } else {
                // imgWidth / imgHeight < w / h, [二]
                visWidth = w;
                visHeight = w * imgHeight / imgWidth;
                visX = 0;
                visY = (h - visHeight + 1) / 2;
            }
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // skip
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // skip
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // skip
    }
}
