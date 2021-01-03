package com.gzy.gmi.app.GMIViewer.widgets;

import com.gzy.gmi.app.GMIViewer.CTWindow;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class GMIHistogram extends JPanel {

    /** original histogram array */
    private int[] histogramData;

    /** image data */
    private int[] imgData;

    /** pre-proceeded BufferedImage of the histogram */
    private BufferedImage histImage;

    /** raster of histImage */
    private WritableRaster raster;

    /** highest value of the histogram */
    private int highestCount;

    /** shared CTWindow */
    private CTWindow ctWindow;

    public GMIHistogram() {
        super();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(histImage != null) {
            // paint hist
            g.drawImage(histImage, 0, 0, getWidth(), getHeight(), this);

            // TODO left/right drag control
            int winLow = (int) ((long)(ctWindow.getWinLow() - CTWindow.LOWEST_CT_VALUE) * getWidth() / CTWindow.MAX_WINDOW_SIZE);
            int winHigh = (int) ((long)(ctWindow.getWinHigh() - CTWindow.LOWEST_CT_VALUE) * getWidth() / CTWindow.MAX_WINDOW_SIZE);
            g.setColor(Color.GREEN);
            g.drawLine(0, getHeight() - 1, winLow, getHeight() - 1);
            g.drawLine(winLow, getHeight() - 1, winHigh, 1);
            g.drawLine(winHigh, 1, getWidth(), 1);
        }
    }

    public int[] histColor = new int[] {0xFF, 0x00, 0xFF, 0xFF};

    /** asynchronous method to load histogram */
    public void loadHist(int[] histogram, CTWindow ctWindow) {
        assert histogram != null && ctWindow != null;
        this.histogramData = histogram;
        this.ctWindow = ctWindow;

        int width = getWidth();
        int height = getHeight();
        histImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        raster = (WritableRaster) histImage.getData();
        imgData = new int[width * height * 4];

        // find the highest
        highestCount = 0;

        // scaling hist
        float ACC = CTWindow.MAX_WINDOW_SIZE * 1.0f / width;
        float accIndex = ACC;
        int index = 0;

        /* scaled array of histogram */
        int[] scaledHistData = new int[width];
        int i, j;
        for(i = 0; i < histogram.length; i++) {
            highestCount = Math.max(highestCount, histogram[i]);
            if(accIndex >= 1.0f) {
                scaledHistData[index] += histogram[i];
                accIndex -= 1.0f;
            } else if(accIndex >= 0.0f) {
                scaledHistData[index] += accIndex * histogram[i];
                highestCount = Math.max(highestCount, scaledHistData[index]);
                accIndex -= 1.0f;
                index++;
                i--;
            } else {
                // accIndex < 0
                accIndex = Math.abs(accIndex);
                scaledHistData[index] += accIndex * histogram[i];
                accIndex = ACC;
            }
        }
        // height of hist
        int maxCount = highestCount * 2 / 3;
        int count;
        for(i = 0; i < width; i++) {
            count = (int) ((long)scaledHistData[i] * height / maxCount);
            count = Math.min(count, height);
            for(j = 0; j < count; j++) {
                System.arraycopy(histColor, 0, imgData, ((height - 1 - j) * width + i) * 4, 4);
            }
        }
        raster.setPixels(0, 0, width, height, imgData);
        histImage.setData(raster);
    }
}
