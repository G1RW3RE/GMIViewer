package com.gzy.gmi.app.GMIViewer.widgets;

public interface LayerChangeNotifier {
    public void addLayerChangeListener(LayerChangeListener listener);
    public void removeLayerChangeListener(LayerChangeListener listener);
    /**
     * @param event event chain
     *  */
    public void notifyLayerChangeListeners(LayerChangeEvent event);
}
