package com.gzy.gmi.app.GMIViewer.widgets;

import javax.swing.JComponent;

public class LayerChangeEvent {

    /** event source */
    private JComponent eventSource;

    /** number changed */
    private int layer;

    /** indicates which cord changed */
    private int which;

    public final static int TYPE_CORD_X = 0;
    public final static int TYPE_CORD_Y = 1;
    public final static int TYPE_CORD_Z = 2;

    public LayerChangeEvent(int layer, int which) {
        this(null, layer, which);
    }

    public LayerChangeEvent(JComponent eventSource, int layer, int which) {
        this.setEventSource(eventSource);
        this.setLayer(layer);
        this.setType(which);
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public JComponent getEventSource() {
        return eventSource;
    }

    public void setType(int which) {
        this.which = which;
    }

    public int getType() {
        return this.which;
    }

    public void setEventSource(JComponent eventSource) {
        this.eventSource = eventSource;
    }
}
