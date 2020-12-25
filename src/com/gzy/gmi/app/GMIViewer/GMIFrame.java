package com.gzy.gmi.app.GMIViewer;

import com.gzy.gmi.util.GMILoader;
import com.gzy.gmi.util.MHDInfo;
import com.gzy.gmi.util.RawData;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * main frame of GMIViewer
 * */
public class GMIFrame extends JFrame {

    public GMIFrame() {
        super("GMIViewer医学图像查看器");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(WINDOW_MIN_WIDTH, WINDOW_MIN_HEIGHT));

        initComponents();
        loadData();

        setVisible(true);
    }

    private JPanel toolPane;
    private JPanel displayPaneOuter, displayPane;
    private JPanel canvasWrapper00, canvasWrapper01, canvasWrapper10, canvasWrapper11;
    private GMICanvas canvas00, canvas01, canvas10, canvas11;

    private static final int WINDOW_MIN_WIDTH = 550;
    private static final int WINDOW_MIN_HEIGHT = 400;
    private static final int TOOL_PANE_WIDTH = 150;
    private static final int DISPLAY_PANE_MIN_WIDTH = 400;

    /** initialize components to be used in frame */
    private void initComponents() {
        //           tools      image x 4
        // layout: |       |        +        |
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        // Tool pane TODO
        toolPane = new JPanel();
        toolPane.setBackground(Color.ORANGE);
        toolPane.setMinimumSize(new Dimension(TOOL_PANE_WIDTH, WINDOW_MIN_HEIGHT));
        SpringLayout.Constraints tpConstraints = layout.getConstraints(toolPane);
        tpConstraints.setWidth(Spring.constant(TOOL_PANE_WIDTH));
        add(toolPane, tpConstraints);

        // Display pane outer
        displayPaneOuter = new JPanel();
        displayPaneOuter.setMinimumSize(new Dimension(DISPLAY_PANE_MIN_WIDTH, WINDOW_MIN_HEIGHT));
        displayPaneOuter.setLayout(new BorderLayout());
        displayPaneOuter.setBackground(Color.CYAN);
        add(displayPaneOuter);

        // layout
        layout.putConstraint(SpringLayout.WEST, toolPane, 0, SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, toolPane, 0, SpringLayout.SOUTH, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, toolPane, 0, SpringLayout.NORTH, getContentPane());
        layout.putConstraint(SpringLayout.WEST, displayPaneOuter, TOOL_PANE_WIDTH, SpringLayout.WEST, toolPane);
        layout.putConstraint(SpringLayout.EAST, displayPaneOuter, 0, SpringLayout.EAST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, displayPaneOuter, 0, SpringLayout.NORTH, getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, displayPaneOuter, 0, SpringLayout.SOUTH, getContentPane());

        // Display pane inner
        displayPane = new JPanel();
        displayPane.setBackground(Color.yellow);
        displayPane.setLayout(new GridLayout(2, 2));
        displayPaneOuter.add(displayPane);

        canvas00 = new GMICanvas();
        canvas01 = new GMICanvas();
        canvas10 = new GMICanvas();
        canvas11 = new GMICanvas();
        canvas00.setBackground(Color.BLACK);
        canvas01.setBackground(Color.BLACK);
        canvas10.setBackground(Color.BLACK);
        canvas11.setBackground(Color.BLACK);

        canvasWrapper00 = new JPanel();
        canvasWrapper00.setBackground(Color.BLACK);
        canvasWrapper00.setBorder(BorderFactory.createTitledBorder(null, "TOP", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper00.setLayout(new BorderLayout());
        canvasWrapper00.add(canvas00);
        displayPane.add(canvasWrapper00);

        canvasWrapper01 = new JPanel();
        canvasWrapper01.setBackground(Color.BLACK);
        canvasWrapper01.setBorder(BorderFactory.createTitledBorder(null, "LEFT", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper01.setLayout(new BorderLayout());
        canvasWrapper01.add(canvas01);
        displayPane.add(canvasWrapper01);

        canvasWrapper10 = new JPanel();
        canvasWrapper10.setBackground(Color.BLACK);
        canvasWrapper10.setBorder(BorderFactory.createTitledBorder(null, "FRONT", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper10.setLayout(new BorderLayout());
        canvasWrapper10.add(canvas10);
        displayPane.add(canvasWrapper10);

        canvasWrapper11 = new JPanel();
        canvasWrapper11.setBackground(Color.BLACK);
        canvasWrapper11.setBorder(BorderFactory.createTitledBorder(null, "???", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper11.setLayout(new BorderLayout());
        canvasWrapper11.add(canvas11);
        displayPane.add(canvasWrapper11);

    }

    /** mhd Information data */
    MHDInfo mhdInfo;
    /** raw image array data */
    RawData rawData;

    @SuppressWarnings("SuspiciousNameCombination")
    public void onDataLoaded() {
        canvas00.loadImageData(rawData.topSlice, mhdInfo.x, mhdInfo.y);
        canvas01.loadImageData(rawData.leftSlice, mhdInfo.y, mhdInfo.z);
        canvas10.loadImageData(rawData.frontSlice, mhdInfo.x, mhdInfo.z);
        displayPane.repaint();
    }

    // TDOO DEBUGGING
    private final static String DEBUG_MHD_PATH = "D:\\迅雷下载\\1.3.6.1.4.1.14519.5.2.1.6279.6001.100684836163890911914061745866.mhd";
    // TODO DEBUGGING
    public void loadData() {
        Thread thread = new Thread(() -> {
            try {
                mhdInfo = GMILoader.loadMHDFile(new File(DEBUG_MHD_PATH));
                mhdInfo.debugOutput();
                rawData = GMILoader.loadRawFromMHD(mhdInfo);
                onDataLoaded();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        thread.start();
    }
}
