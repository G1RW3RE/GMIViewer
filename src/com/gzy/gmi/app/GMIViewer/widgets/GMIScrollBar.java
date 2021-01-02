package com.gzy.gmi.app.GMIViewer.widgets;

import javax.swing.JScrollBar;

public class GMIScrollBar extends JScrollBar implements LayerChangeListener {
    public GMIScrollBar() {
        super();
        setUI(new GMIScrollBarUI());
    }

    @Override
    public void onLayerChanged(LayerChangeEvent event) {
        this.setValue(event.getLayer());
    }
}
