package com.gzy.gmi.app.GMIViewer;

import com.gzy.gmi.app.GMIViewer.widgets.GMIScrollBar;
import com.gzy.gmi.app.GMIViewer.widgets.GMIScrollBarUI;
import com.gzy.gmi.util.GMILoader;
import com.gzy.gmi.util.MHDInfo;
import com.gzy.gmi.util.RawData;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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
    private GMIScrollBar scrollBar00, scrollBar01, scrollBar10, scrollBar11;
    private GMICanvas canvas00, canvas01, canvas10, canvas11;
    private GMIHistogram histogram;

    private static final int WINDOW_MIN_WIDTH = 700;
    private static final int WINDOW_MIN_HEIGHT = 450;
    private static final int TOOL_PANE_WIDTH = 250;
    private static final int DISPLAY_PANE_MIN_WIDTH = 450;

    private static final int HISTOGRAM_HEIGHT = 150;

    private CTWindow ctWindow;

    /** initialize components to be used in frame */
    private void initComponents() {
        //           tools      image x 4
        // layout: |       |        +        |
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        /* === TOOL PANE === */
        // tool pane init
        toolPane = new JPanel();
        toolPane.setBackground(Color.GRAY);
        toolPane.setMinimumSize(new Dimension(TOOL_PANE_WIDTH, WINDOW_MIN_HEIGHT));
        SpringLayout.Constraints tpConstraints = layout.getConstraints(toolPane);
        tpConstraints.setWidth(Spring.constant(TOOL_PANE_WIDTH));
        add(toolPane, tpConstraints);

        SpringLayout toolPaneLayout = new SpringLayout();
        toolPane.setLayout(toolPaneLayout);

        // histogram
        histogram = new GMIHistogram();
        histogram.setBackground(Color.BLACK);
        toolPane.add(histogram);

        /* === DISPLAY PANE === */
        // Display pane outer
        displayPaneOuter = new JPanel();
        displayPaneOuter.setMinimumSize(new Dimension(DISPLAY_PANE_MIN_WIDTH, WINDOW_MIN_HEIGHT));
        displayPaneOuter.setLayout(new BorderLayout());
        displayPaneOuter.setBackground(Color.CYAN);
        add(displayPaneOuter);

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

        scrollBar00 = new GMIScrollBar();
        scrollBar00.setBackground(Color.BLACK);
        scrollBar00.setPreferredSize(new Dimension(25, 0));
        scrollBar00.setUI(new GMIScrollBarUI());

        scrollBar01 = new GMIScrollBar();
        scrollBar01.setBackground(Color.BLACK);
        scrollBar01.setPreferredSize(new Dimension(25, 0));
        scrollBar01.setUI(new GMIScrollBarUI());

        scrollBar10 = new GMIScrollBar();
        scrollBar10.setBackground(Color.BLACK);
        scrollBar10.setPreferredSize(new Dimension(25, 0));
        scrollBar10.setUI(new GMIScrollBarUI());

        // fill canvas wrappers
        JPanel canvasWrapper00 = new JPanel();
        canvasWrapper00.setBackground(Color.BLACK);
        canvasWrapper00.setBorder(BorderFactory.createTitledBorder(null, "TOP", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper00.setLayout(new BorderLayout());
        canvasWrapper00.add(canvas00);
        canvasWrapper00.add(scrollBar00, BorderLayout.EAST);
        displayPane.add(canvasWrapper00);

        JPanel canvasWrapper01 = new JPanel();
        canvasWrapper01.setBackground(Color.BLACK);
        canvasWrapper01.setBorder(BorderFactory.createTitledBorder(null, "RIGHT", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper01.setLayout(new BorderLayout());
        canvasWrapper01.add(canvas01);
        canvasWrapper01.add(scrollBar01, BorderLayout.EAST);
        displayPane.add(canvasWrapper01);

        JPanel canvasWrapper10 = new JPanel();
        canvasWrapper10.setBackground(Color.BLACK);
        canvasWrapper10.setBorder(BorderFactory.createTitledBorder(null, "FRONT", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper10.setLayout(new BorderLayout());
        canvasWrapper10.add(canvas10);
        canvasWrapper10.add(scrollBar10, BorderLayout.EAST);
        displayPane.add(canvasWrapper10);

        JPanel canvasWrapper11 = new JPanel();
        canvasWrapper11.setBackground(Color.BLACK);
        canvasWrapper11.setBorder(BorderFactory.createTitledBorder(null, "???", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper11.setLayout(new BorderLayout());
        canvasWrapper11.add(canvas11);
        displayPane.add(canvasWrapper11);

        // layout
        layout.putConstraint(SpringLayout.WEST, toolPane, 0, SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, toolPane, 0, SpringLayout.SOUTH, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, toolPane, 0, SpringLayout.NORTH, getContentPane());
        layout.putConstraint(SpringLayout.WEST, displayPaneOuter, TOOL_PANE_WIDTH, SpringLayout.WEST, toolPane);
        layout.putConstraint(SpringLayout.EAST, displayPaneOuter, 0, SpringLayout.EAST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, displayPaneOuter, 0, SpringLayout.NORTH, getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, displayPaneOuter, 0, SpringLayout.SOUTH, getContentPane());

        toolPaneLayout.putConstraint(SpringLayout.NORTH, histogram, 5, SpringLayout.NORTH, toolPane);
        toolPaneLayout.putConstraint(SpringLayout.SOUTH, histogram, HISTOGRAM_HEIGHT, SpringLayout.NORTH, histogram);
        toolPaneLayout.putConstraint(SpringLayout.WEST, histogram, 3, SpringLayout.WEST, toolPane);
        toolPaneLayout.putConstraint(SpringLayout.EAST, histogram, -3, SpringLayout.EAST, toolPane);


        // add listeners
        canvas00.addLayerChangeListener(scrollBar00);
        scrollBar00.addAdjustmentListener(canvas00);
        canvas01.addLayerChangeListener(scrollBar01);
        scrollBar01.addAdjustmentListener(canvas01);
        canvas10.addLayerChangeListener(scrollBar10);
        scrollBar10.addAdjustmentListener(canvas10);

        canvas00.bindAxisTo(canvas10, canvas01);
        canvas01.bindAxisTo(canvas00, canvas10);
        canvas10.bindAxisTo(canvas00, canvas01);
    }

    /** mhd Information data */
    MHDInfo mhdInfo;
    /** raw image array data */
    RawData rawData;

    @SuppressWarnings("SuspiciousNameCombination")
    public void onDataLoaded() {
        scrollBar00.setMinimum(0);
        scrollBar00.setVisibleAmount(Math.max(20, mhdInfo.z / 10));
        scrollBar00.setMaximum(scrollBar00.getVisibleAmount() + mhdInfo.z - 1);
        scrollBar01.setMinimum(0);
        scrollBar01.setVisibleAmount(Math.max(20, mhdInfo.x / 10));
        scrollBar01.setMaximum(scrollBar01.getVisibleAmount() + mhdInfo.x - 1);
        scrollBar10.setMinimum(0);
        scrollBar10.setVisibleAmount(Math.max(20, mhdInfo.y / 10));
        scrollBar10.setMaximum(scrollBar10.getVisibleAmount() + mhdInfo.y - 1);
        // load ctWindow
        ctWindow = new CTWindow(rawData.lowestValue, rawData.highestValue);
        // canvas data init must be done after scrollbars'
        canvas00.loadImageData(rawData.topSlice, mhdInfo.x, mhdInfo.y, ctWindow);
        canvas01.loadImageData(rawData.rightSlice, mhdInfo.y, mhdInfo.z, ctWindow);
        canvas10.loadImageData(rawData.frontSlice, mhdInfo.x, mhdInfo.z, ctWindow);
        // load histogram
        histogram.loadHist(rawData.histogram, ctWindow);
        ctWindow.setWinLow(-400);
        ctWindow.setWinHigh(500);
        // TODO Optimize resizing
        setSize(new Dimension(mhdInfo.x * 3, mhdInfo.y * 2));
        setLocationRelativeTo(null);
        repaint();
    }

    // TODO DEBUGGING
    private final static String DEBUG_MHD_PATH = "E:\\GLORIA_WORKSPACE\\task\\task1101_ps\\WANG_DIAN_TANG_1\\image.mhd";
//    private final static String DEBUG_MHD_PATH = "C:\\Users\\Administrator\\Desktop\\a.mhd";
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
