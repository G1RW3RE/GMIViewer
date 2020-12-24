package com.gzy.gmi.app.GMIViewer;

import com.gzy.gmi.util.GMILoader;
import com.gzy.gmi.util.MHDInfo;
import com.gzy.gmi.util.RawData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;

/**
 * main frame of GMIViewer
 * */
public class GMIFrame extends JFrame implements ComponentListener {

    public GMIFrame() {
        super("GMIViewer医学图像查看器");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(this);
        setMinimumSize(new Dimension(WINDOW_MIN_WIDTH, WINDOW_MIN_HEIGHT));

        initComponents();
        loadData();

        setVisible(true);
    }

    private JPanel tp;
    private JPanel dpOuter, dp;
    private GMICanvas canvas00, canvas01, canvas10, canvas11;

    private static final int WINDOW_MIN_WIDTH = 550;
    private static final int WINDOW_MIN_HEIGHT = 400;
    private static final int TOOL_PANE_WIDTH = 150;
    private static final int DISPLAY_PANE_MIN_WIDTH = 400;

    /** initialize components to be used in frame */
    private void initComponents() {
        // layout: | tools |     images      |

        // Tool pane TODO
        tp = new JPanel();
        tp.setBackground(Color.ORANGE);
        tp.setMinimumSize(new Dimension(TOOL_PANE_WIDTH, WINDOW_MIN_HEIGHT));
        add(tp);

        // Display pane TODO
        dpOuter = new JPanel();
        dpOuter.setMinimumSize(new Dimension(DISPLAY_PANE_MIN_WIDTH, WINDOW_MIN_HEIGHT));
        add(dpOuter);

        // Display pane inner TODO
        dp = new JPanel();
        dp.setBackground(Color.yellow);
        dp.setLayout(null);


        canvas00 = new GMICanvas();
//        canvas00.setBackground(new Color(0x616161));
        canvas01 = new GMICanvas();
//        canvas01.setBackground(new Color(0x898989));
        canvas10 = new GMICanvas();
//        canvas10.setBackground(new Color(0x8B8B8B));
        canvas11 = new GMICanvas();
//        canvas11.setBackground(new Color(0xC6C6C6));
        canvas00.setBackground(Color.BLACK);
        canvas01.setBackground(Color.BLACK);
        canvas10.setBackground(Color.BLACK);
        canvas11.setBackground(Color.BLACK);
        addComponentListener(canvas00);
        addComponentListener(canvas01);
        addComponentListener(canvas10);
        addComponentListener(canvas11);
        test00 = new JPanel();
        test00.setBackground(Color.BLACK);
        test00.setBorder(BorderFactory.createTitledBorder("Top"));
        test00.setLayout(new BorderLayout());
        test00.add(canvas00, "Center");
        dp.add(test00);
//        dp.add(canvas00);
        dp.add(canvas01);
        dp.add(canvas10);
        dp.add(canvas11);
        dpOuter.add(dp);

    }
    JPanel test00;

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        // fixed width
        tp.setLocation(0 ,0);
        tp.setSize(TOOL_PANE_WIDTH, getHeight());
        // fill the space left
        dpOuter.setLocation(TOOL_PANE_WIDTH, 0);
        dpOuter.setSize(getWidth() - TOOL_PANE_WIDTH, getHeight());
        // fill dpOuter
        dp.setLocation(0, 0);
        dp.setSize(dpOuter.getWidth(), dpOuter.getHeight());

        int canvasWidth = dp.getWidth() / 2;
        int canvasHeight = dp.getHeight() / 2;
//        canvas00.setBounds(0, 0, canvasWidth, canvasHeight);
        test00.setBounds(0, 0, canvasWidth, canvasHeight);
        canvas01.setBounds(canvasWidth, 0, canvasWidth, canvasHeight);
        canvas10.setBounds(0, canvasHeight, canvasWidth, canvasHeight);
        canvas11.setBounds(canvasWidth, canvasHeight, canvasWidth, canvasHeight);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // skip
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // skip
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // skip
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
        dp.repaint();
    }

    // TODO DEBUGGING
    public void loadData() {
        Thread thread = new Thread(() -> {
            try {
                mhdInfo = GMILoader.loadMHDFile(new File("E:\\GLORIA_WORKSPACE\\task\\task1015_ps\\HSW09_0017_01.mhd"));
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
