package com.gzy.gmi.app.TestApp;

import com.gzy.gmi.util.GMILoader;
import com.gzy.gmi.util.MHDInfo;
import com.gzy.gmi.util.RawData;
import com.gzy.gmi.widget.GMICanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;

public class TestFrame extends JFrame {

    GMICanvas canvas;

    public TestFrame() {
        super("测试框架");
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        initComponents();
        loadResource();

        setVisible(true);
    }

    private void initComponents() {
        canvas = new GMICanvas() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(img != null) {
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    System.err.println("updated.");
                }
            }
        };
        setContentPane(canvas);
    }

    MHDInfo mhdInfo;
    RawData rawData;

    private void loadResource() {
        Thread thread = new Thread(() -> {
            try {
                mhdInfo = GMILoader.loadMHDFile(new File("E:\\GLORIA_WORKSPACE\\task\\task1015_ps\\HSW09_0017_01.mhd"));
                mhdInfo.debugOutput();
                rawData = GMILoader.loadRawFromMHD(mhdInfo);
                onLoaded();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        thread.start();
    }

    Image img;
    private void onLoaded() {

        //noinspection SuspiciousNameCombination
        img = getImageFromArray(rawData.leftSlice[190], mhdInfo.y, mhdInfo.z);
        repaint();
    }

    static int winLow = -800;
    static int winHigh = 1000;
    static int winLen = winHigh - winLow;

    public static Image getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int val;
        for(int i = 0; i < pixels.length; i++) {
            val = pixels[i];
            if(val < winLow) { val = 0x00; }
            else if(val > winHigh) { val = 0xFF; }
            else {
                val = (val - winLow) * 255 / winLen;
                val = 0xFF & val;
            }
            pixels[i] = val;
        }
        WritableRaster raster = (WritableRaster) image.getData();
        raster.setPixels(0, 0, width, height, pixels);
        image.setData(raster);
        return image;
    }
}
