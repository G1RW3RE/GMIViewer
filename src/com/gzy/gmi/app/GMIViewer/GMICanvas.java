package com.gzy.gmi.app.GMIViewer;

import com.gzy.gmi.app.GMIViewer.widgets.LayerChangeEvent;
import com.gzy.gmi.app.GMIViewer.widgets.LayerChangeListener;
import com.gzy.gmi.app.GMIViewer.widgets.LayerChangeNotifier;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/** Canvas to paint image on */
public class GMICanvas extends JPanel
        implements MouseWheelListener, MouseMotionListener, MouseListener,
        LayerChangeListener, LayerChangeNotifier, AdjustmentListener {

    // TODO stroke adjustment
    private static final BasicStroke DASHED_STROKE = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            new float[]{10.0f},
            0.0f);

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

    /** adaptor-listener structure */
    private final List<LayerChangeListener> layerChangeListeners;

    /** horizontal axis line */
    private int hAxis;

    /** vertical axis line */
    private int vAxis;

    /** event source of hAxis\vAxis */
    private GMICanvas hAxisSource, vAxisSource;

    /** CTWindow, controls display */
    private CTWindow ctWindow;

    /** displaying image */
    private BufferedImage image;
    /** raster */
    private WritableRaster raster;


    public GMICanvas() {
        super();
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        layerChangeListeners = new ArrayList<>();
    }

    /** put data of image slices into this component */
    public void loadImageData(int[][] data, int width, int height, CTWindow ctWindow) {
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
        // set initial layer to middle
        changeLayer(data.length / 2);
    }

    /** invoke when trying to change layer */
    private void changeLayer(int layer) {
        changeLayer(layer, null);
    }

    /** event driven layer changing */
    private void changeLayer(int layer, LayerChangeEvent event) {
        if(layer < 0 || layer > maxLayerIndex) { return; }
        if(layer == currentLayer) { return; }
        if(imgData != null) {
            currentLayer = layer;
            int[] displayImageData = resolveRaw(imgData[currentLayer]);
            raster.setPixels(0, 0, imgWidth, imgHeight, displayImageData);
            image.setData(raster);
            repaint();
            notifyLayerChangeListeners(event);
            if(hAxisSource != null) hAxisSource.onLayerChanged(new LayerChangeEvent(this, currentLayer));
            if(vAxisSource != null) vAxisSource.onLayerChanged(new LayerChangeEvent(this, currentLayer));
        }
    }

    public void bindAxisTo(GMICanvas horizontal, GMICanvas vertical) {
        assert horizontal != null && vertical != null;
        this.hAxisSource = horizontal;
        this.vAxisSource = vertical;
    }

    @Override
    public void addLayerChangeListener(LayerChangeListener listener) {
        if(listener != null) {
            layerChangeListeners.add(listener);
        }
    }

    @Override
    public void removeLayerChangeListener(LayerChangeListener listener) {
        if(listener != null) {
            layerChangeListeners.remove(listener);
        }
    }

    @Override
    public void notifyLayerChangeListeners(LayerChangeEvent lastEvent) {
        LayerChangeEvent event = lastEvent == null ? new LayerChangeEvent(this, currentLayer) : lastEvent;
        for(LayerChangeListener l : layerChangeListeners) {
            l.onLayerChanged(event);
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
            g.drawImage(image, visX, visY, visWidth, visHeight, this);
            g.drawString("" + currentLayer, 20, 20);
            // paint vAxis\hAxis
            int hAxisDisplay = hAxis * visWidth / imgWidth;
            int vAxisDisplay = vAxis * visHeight / imgHeight;
            ((Graphics2D) g).setStroke(DASHED_STROKE);
            g.setColor(Color.ORANGE);
            g.drawLine(visX, visY + hAxisDisplay, visX + visWidth, visY + hAxisDisplay);
            g.drawLine(visX + vAxisDisplay, visY, visX + vAxisDisplay, visY + visHeight);
            // TODO DEBUGGING
            g.drawString("(" + currentLayer + "," + hAxis + "," + vAxis + "): " + imgData[currentLayer][hAxis * imgWidth + vAxis], 10, 30);
        }
    }

    /** when ctWindow changes, invoke this method */
    public void updateOnCtWindowChange() {
        int[] displayImageData = resolveRaw(imgData[currentLayer]);
        raster.setPixels(0, 0, imgWidth, imgHeight, displayImageData);
        image.setData(raster);
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
    }

    @Override
    public void onLayerChanged(LayerChangeEvent event) {
        if(hAxisSource != null && event.getEventSource() == hAxisSource) {
            hAxis = event.getLayer();
            repaint();
        } else if(vAxisSource != null && event.getEventSource() == vAxisSource) {
            vAxis = event.getLayer();
            repaint();
        } else if(event.getEventSource() != this) {
            changeLayer(event.getLayer(), event);
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        changeLayer(e.getValue());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (vAxisSource == null || hAxisSource == null) {
            return;
        }
        int x = e.getX(), y = e.getY();
        if (x >= visX && x <= visX + visWidth && y >= visY && y <= visY + visHeight) {
            // inside visBox
            x = (x - visX) * imgWidth / visWidth;
            y = (y - visY) * imgHeight / visHeight;
            x = Math.min(imgWidth - 1, Math.max(0, x));
            y = Math.min(imgHeight - 1, Math.max(0, y));
            vAxisSource.onLayerChanged(new LayerChangeEvent(null, x));
            hAxisSource.onLayerChanged(new LayerChangeEvent(null, y));
            vAxis = x;
            hAxis = y;
            repaint();
        }
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
}
