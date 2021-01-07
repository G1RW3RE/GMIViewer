package com.gzy.gmi.app.GMIViewer;

import com.gzy.gmi.app.GMIViewer.widgets.LayerChangeEvent;
import com.gzy.gmi.app.GMIViewer.widgets.LayerChangeListener;
import com.gzy.gmi.app.GMIViewer.widgets.LayerChangeNotifier;
import com.gzy.gmi.util.CTWindow;
import com.gzy.gmi.util.GMIMask3D;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.List;

/** Canvas to paint image on */
public class GMICanvas extends JPanel
        implements MouseWheelListener, MouseMotionListener, MouseListener, LayerChangeNotifier {

    // TODO better stroke
    private static final BasicStroke DASHED_STROKE = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            new float[]{10.0f},
            0.0f);
    private static final int MASK_ALPHA = 0xD0_000000;

    /** width of image slice */
    private int imgWidth;

    /** height of image slice */
    private int imgHeight;

    /** raw data of image slices */
    private int[][] imgData;

    /** max index of layers, equals to imgData.length - 1 */
    private int maxLayerIndex;

    /** index of image slice displaying, 0 <= currentLayer < imgData.length */
    private int currentLayer;

    /** horizontal axis line */
    private int yAxis;

    /** vertical axis line */
    private int xAxis;

    /** CTWindow, controls display */
    private CTWindow ctWindow;

    /** displaying image */
    private BufferedImage image;
    /** raster */
    private WritableRaster raster;

    /** masks */
    private List<GMIMask3D> maskList;

    /** mask image */
    private BufferedImage maskImage;
    /** mask image raster */
    private WritableRaster maskRaster;

    /** deciding which mask to use */
    private String orientation;

    public final static String ORIENT_BOTTOM = "bottom";
    public final static String ORIENT_RIGHT = "right";
    public final static String ORIENT_FRONT = "front";

    /** notify when layer x, y, z changes */
    List<LayerChangeListener> layerChangeListenerList;

    public GMICanvas() {
        super();
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        layerChangeListenerList = new LinkedList<>();
    }

    /** put data of image slices into this component */
    public void loadImageData(int[][] data, int width, int height, CTWindow ctWindow, String orientation) {
        assert data != null;
        imgData = data;
        maxLayerIndex = imgData.length - 1;
        assert width > 0 && height > 0 && width * height == data[0].length;
        imgWidth = width;
        imgHeight = height;
        currentLayer = 0;
        this.ctWindow = ctWindow;
        image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        raster = (WritableRaster) image.getData();
        assert ORIENT_FRONT.equals(orientation) || ORIENT_BOTTOM.equals(orientation) || ORIENT_RIGHT.equals(orientation);
        this.orientation = orientation;
        maskImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        maskRaster = (WritableRaster) maskImage.getData();
        // set initial layer to middle
        changeLayer(data.length / 2);
    }

    /** invoke when trying to change layer */
    public void changeLayer(int layer) {
        if(layer < 0 || layer > maxLayerIndex) { return; }
        if(layer == currentLayer) { return; }
        if(imgData != null) {
            currentLayer = layer;
            int[] displayImageData = resolveRaw(imgData[currentLayer]);
            raster.setPixels(0, 0, imgWidth, imgHeight, displayImageData);
            image.setData(raster);
            if(maskList != null && maskList.size() != 0) {
                int[] displayMaskData = resolveMasks();
                maskRaster.setDataElements(0, 0, imgWidth, imgHeight, displayMaskData);
                maskImage.setData(maskRaster);
            }
            repaint();
        }
    }

    /** transform raw data into displayable image */
    public int[] resolveRaw(int[] rawData) {
        int len = rawData.length;
        int[] data = new int[len];
        for (int i = 0; i < len; i++) {
            if(rawData[i] > ctWindow.getWinHigh()) {
                data[i] = ctWindow.getWinHigh();
            } else if(rawData[i] < ctWindow.getWinLow()) {
                data[i] = ctWindow.getWinLow();
            } else {
                data[i] = rawData[i];
            }
            data[i] = (data[i] - ctWindow.getWinLow()) * 255 / ctWindow.getWinSize();
        }
        return data;
    }

    public int[] resolveMasks() {
        int len = imgWidth * imgHeight;
        int[] data = new int[len];
        boolean[] iMask;
        int color;
        if(ORIENT_BOTTOM.equals(orientation)) {
            for(GMIMask3D mask : maskList) {
                iMask = mask.bottomSlice[currentLayer];
                color = mask.getColor() | MASK_ALPHA;
                for (int i = 0; i < len; i++) {
                    if(iMask[i]) {
                        data[i] = color;
                    }
                }
            }
        } else if(ORIENT_FRONT.equals(orientation)) {
            for(GMIMask3D mask : maskList) {
                iMask = mask.frontSlice[currentLayer];
                color = mask.getColor() | MASK_ALPHA;
                for (int i = 0; i < len; i++) {
                    if(iMask[i]) {
                        data[i] = color;
                    }
                }
            }
        } else if(ORIENT_RIGHT.equals(orientation)) {
            for(GMIMask3D mask : maskList) {
                iMask = mask.rightSlice[currentLayer];
                color = mask.getColor() | MASK_ALPHA;
                for (int i = 0; i < len; i++) {
                    if(iMask[i]) {
                        data[i] = color;
                    }
                }
            }
        }
        return data;
    }

    /** change x axis */
    public void changeAxisX(int val) {
        if(val >= 0 && val < imgWidth) {
            this.xAxis = val;
            repaint();
        }
    }

    /** change y axis */
    public void changeAxisY(int val) {
        if(val >= 0 && val < imgHeight) {
            this.yAxis = val;
            repaint();
        }
    }

    public int getCurrentLayer() {
        return currentLayer;
    }

    public int getCurrentValue() {
        if(imgData != null) {
            return imgData[currentLayer][xAxis + yAxis * imgWidth];
        } else {
            return 0;
        }
    }

    /** get global masks */
    public void setMaskList(List<GMIMask3D> list) {
        this.maskList = list;
    }

    /** Visual area of image */
    private int visX, visY, visWidth, visHeight;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(image != null) {
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
            // draw CT image
            g.drawImage(image, visX, visY, visWidth, visHeight, this);
            // draw mask image
            g.drawImage(maskImage, visX, visY, visWidth, visHeight, this);
            // paint vAxis\hAxis
            int hAxisDisplay = yAxis * visWidth / imgWidth;
            int vAxisDisplay = xAxis * visHeight / imgHeight;
            ((Graphics2D) g).setStroke(DASHED_STROKE);
            g.setColor(Color.ORANGE);
            g.drawLine(visX, visY + hAxisDisplay, visX + visWidth, visY + hAxisDisplay);
            g.drawLine(visX + vAxisDisplay, visY, visX + vAxisDisplay, visY + visHeight);
        }
    }

    /** when ctWindow changes, invoke this method */
    public void updateOnCtWindowChange() {
        int[] displayImageData = resolveRaw(imgData[currentLayer]);
        raster.setPixels(0, 0, imgWidth, imgHeight, displayImageData);
        image.setData(raster);
    }

    /** when mask changes, invoke this method */
    public void updateOnMaskChange() {
        if(maskList != null && maskList.size() != 0) {
            int[] displayMaskData = resolveMasks();
//            maskRaster.setPixels(0, 0, imgWidth, imgHeight, displayMaskData);
            maskRaster.setDataElements(0, 0, imgWidth, imgHeight, displayMaskData);
            maskImage.setData(maskRaster);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int nextLayer = currentLayer + e.getUnitsToScroll() / 3;
        if(nextLayer < 0) {
            currentLayer = 0;
        } else if(nextLayer > maxLayerIndex) {
            nextLayer = maxLayerIndex;
        }
        changeLayer(nextLayer);
        notifyLayerChangeListeners(new LayerChangeEvent(this, nextLayer, LayerChangeEvent.TYPE_CORD_Z));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        x = Math.min(visX + visWidth, Math.max(visX, x));
        y = Math.min(visY + visHeight, Math.max(visY, y));
        // inside visBox
        x = (x - visX) * imgWidth / visWidth;
        y = (y - visY) * imgHeight / visHeight;
        x = Math.min(imgWidth - 1, Math.max(0, x));
        y = Math.min(imgHeight - 1, Math.max(0, y));
        xAxis = x;
        yAxis = y;
        notifyLayerChangeListeners(new LayerChangeEvent(this, x, LayerChangeEvent.TYPE_CORD_X));
        notifyLayerChangeListeners(new LayerChangeEvent(this, y, LayerChangeEvent.TYPE_CORD_Y));
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // skip
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            mouseDragged(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // skip
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // skip
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // skip
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // skip
    }

    @Override
    public void addLayerChangeListener(LayerChangeListener listener) {
        this.layerChangeListenerList.add(listener);
    }

    @Override
    public void removeLayerChangeListener(LayerChangeListener listener) {
        this.layerChangeListenerList.remove(listener);
    }

    @Override
    public void notifyLayerChangeListeners(LayerChangeEvent lastEvent) {
        for(LayerChangeListener l : layerChangeListenerList) {
            l.onLayerChanged(lastEvent);
        }
    }
}
