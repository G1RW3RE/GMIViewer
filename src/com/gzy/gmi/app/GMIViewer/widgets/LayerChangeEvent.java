package com.gzy.gmi.app.GMIViewer.widgets;

import javax.swing.*;

public class LayerChangeEvent {

    private JComponent eventSource;

    private int layer;

    public LayerChangeEvent(int layer) {
        this(null, layer);
    }

    public LayerChangeEvent(JComponent eventSource, int layer) {
        this.setEventSource(eventSource);
        this.setLayer(layer);
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

    public void setEventSource(JComponent eventSource) {
        this.eventSource = eventSource;
    }
}
