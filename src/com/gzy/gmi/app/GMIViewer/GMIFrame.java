package com.gzy.gmi.app.GMIViewer;

import com.gzy.gmi.app.GMIViewer.widgets.GMICanvas;
import com.gzy.gmi.app.GMIViewer.widgets.GMIHistogram;
import com.gzy.gmi.app.GMIViewer.widgets.GMIMaskListCellRenderer;
import com.gzy.gmi.app.GMIViewer.widgets.GMIScrollBar;
import com.gzy.gmi.app.GMIViewer.widgets.GMIScrollBarUI;
import com.gzy.gmi.app.GMIViewer.widgets.LayerChangeEvent;
import com.gzy.gmi.app.GMIViewer.widgets.LayerChangeListener;
import com.gzy.gmi.util.CTWindow;
import com.gzy.gmi.util.GMILoader;
import com.gzy.gmi.util.GMIMask3D;
import com.gzy.gmi.util.MHDInfo;
import com.gzy.gmi.util.RawData;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * main frame of GMIViewer
 * */
public class GMIFrame extends JFrame implements MouseListener, MouseMotionListener,
        LayerChangeListener, AdjustmentListener, ActionListener {

    public GMIFrame() {
        super("GMIViewer医学图像查看器");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(WINDOW_MIN_WIDTH, WINDOW_MIN_HEIGHT));

        initComponents();
        initMenuBar();
        deactivateAllFunctions();
        setVisible(true);
    }

    private JMenuItem fileMenuItemOpen;
    private JMenuItem fileMenuItemExit;

    private void initMenuBar() {
        JMenuBar jMenuBar = new JMenuBar();
        JMenu menuFile = new JMenu("文件");
        menuFile.setPreferredSize(new Dimension(50, 25));
        menuFile.setFont(GLOBAL_FONT);
        menuFile.setHorizontalAlignment(SwingConstants.CENTER);

        fileMenuItemOpen = new JMenuItem("打开 ...");
        fileMenuItemExit = new JMenuItem("退出");
        fileMenuItemOpen.setFont(GLOBAL_FONT);
        fileMenuItemExit.setFont(GLOBAL_FONT);
        fileMenuItemOpen.setPreferredSize(new Dimension(200, 30));
        fileMenuItemExit.setPreferredSize(new Dimension(200, 30));

        menuFile.add(fileMenuItemOpen);
        menuFile.addSeparator();
        menuFile.add(fileMenuItemExit);
        jMenuBar.add(menuFile);
        setJMenuBar(jMenuBar);

        fileMenuItemOpen.addActionListener(this);
        fileMenuItemExit.addActionListener(this);
    }

    private JPanel toolPane;
    private JPanel displayPaneWrapper, displayPane;
    private GMIScrollBar scrollBar00, scrollBar01, scrollBar10, scrollBar11;
    GMICanvas canvas00, canvas01, canvas10, canvas11;
    private GMIHistogram histogram;
    private JTextField txtWindowSize, txtWindowPosition;
    private final JLabel lblWindowSize = new JLabel("窗宽:");
    private final JLabel lblWindowPosition = new JLabel("　窗位:");
    private JTextField txtCordX, txtCordY, txtCordZ;
    private final JLabel lblCordX = new JLabel("x:");
    private final JLabel lblCordY = new JLabel("y:");
    private final JLabel lblCordZ = new JLabel("z:");
    private JTextField txtIntensity, txtMaskBelong;
    private final JLabel lblIntensity = new JLabel("强度:");
    private final JLabel lblMaskBelong = new JLabel("遮罩:");
    private JList<GMIMask3D> lstMask;
    private DefaultListModel<GMIMask3D> maskListModel;
    private JPopupMenu maskMenu;
    private final JMenu maskMenuEditMenu = new JMenu("编辑");
    private final JMenuItem editMenuItemThresh = new JMenuItem("阈值分割 ...");
    private final JMenuItem editMenuItemGrow = new JMenuItem("区域生长 ...");
    private final JMenuItem editMenuItemRename = new JMenuItem("重命名");
    private final JMenuItem editMenuItemRecolor = new JMenuItem("修改颜色");
    private final JMenuItem editMenuItemClear = new JMenuItem("清空");
    private final JMenuItem maskMenuItemAdd = new JMenuItem("新建遮罩");
    private final JMenuItem maskMenuItemDuplicate = new JMenuItem("创建副本");
    private final JMenuItem maskMenuItemDelete = new JMenuItem("删除");

    private static final int WINDOW_MIN_WIDTH = 700;
    private static final int WINDOW_MIN_HEIGHT = 450;
    private static final int TOOL_PANE_WIDTH = 250;
    private static final int DISPLAY_PANE_MIN_WIDTH = 450;

    private static final int HISTOGRAM_HEIGHT = 160;
    private static final int ADJUSTMENT_INPUT_HEIGHT = 25;

    private static final Font GLOBAL_FONT = new Font("等线", Font.BOLD, 16);
    private static final Font GLOBAL_FONT_TINY = new Font("等线", Font.BOLD, 12);

    private CTWindow ctWindow;

    /** mhd Information data */
    MHDInfo mhdInfo;
    /** raw image array data */
    RawData rawData;

    /** list of masks */
    List<GMIMask3D> mask3DList;

    /** initialize components to be used in frame */
    private void initComponents() {
        //           tools      image x 4
        // layout: |       |        +        |
        SpringLayout layout = new SpringLayout();
        setLayout(layout);


    /* === TOOL PANE === */

        // tool pane init
        toolPane = new JPanel();
        toolPane.setMinimumSize(new Dimension(TOOL_PANE_WIDTH, WINDOW_MIN_HEIGHT));
        SpringLayout.Constraints tpConstraints = layout.getConstraints(toolPane);
        tpConstraints.setWidth(Spring.constant(TOOL_PANE_WIDTH));
        add(toolPane, tpConstraints);

        SpringLayout toolPaneLayout = new SpringLayout();
        toolPane.setLayout(toolPaneLayout);

        // histogram
        JPanel histogramWrapper = new JPanel();
        histogramWrapper.setBorder(BorderFactory.createTitledBorder(null, "直方图",
                TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, GLOBAL_FONT, Color.BLACK));
        histogramWrapper.setLayout(new BorderLayout(0, 5));
        histogram = new GMIHistogram();
        histogram.setBackground(Color.BLACK);
        histogramWrapper.add(histogram);

        JPanel windowAdjustWrapper = new JPanel();
        windowAdjustWrapper.setPreferredSize(new Dimension(TOOL_PANE_WIDTH, 30));

        txtWindowSize = new JTextField();
        txtWindowSize.setHorizontalAlignment(SwingConstants.RIGHT);
        txtWindowSize.setFont(GLOBAL_FONT);
        txtWindowSize.setEditable(false);
        txtWindowPosition = new JTextField();
        txtWindowPosition.setHorizontalAlignment(SwingConstants.RIGHT);
        txtWindowPosition.setFont(GLOBAL_FONT);
        txtWindowPosition.setEditable(false);
        lblWindowSize.setFont(GLOBAL_FONT);
        lblWindowPosition.setFont(GLOBAL_FONT);

        windowAdjustWrapper.setLayout(new GridLayout(1, 4));
        windowAdjustWrapper.add(lblWindowSize);
        windowAdjustWrapper.add(txtWindowSize);
        windowAdjustWrapper.add(lblWindowPosition);
        windowAdjustWrapper.add(txtWindowPosition);

        histogramWrapper.add(windowAdjustWrapper, BorderLayout.SOUTH);
        toolPane.add(histogramWrapper);

        // x, y, z; intensity, maskId display area
        JPanel cordWrapper = new JPanel();
        cordWrapper.setBorder(BorderFactory.createTitledBorder(null, "坐标",
                TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, GLOBAL_FONT, Color.BLACK));
        cordWrapper.setLayout(new GridLayout(2, 1));
        toolPane.add(cordWrapper);

        // x, y, z display text box
        JPanel cordWrapperUp = new JPanel();
        cordWrapperUp.setLayout(null);
        cordWrapperUp.setPreferredSize(new Dimension(TOOL_PANE_WIDTH, 35));
        cordWrapper.add(cordWrapperUp);

        txtCordX = new JTextField();
        txtCordX.setHorizontalAlignment(SwingConstants.RIGHT);
        txtCordX.setFont(GLOBAL_FONT);
        txtCordX.setEditable(false);
        txtCordY = new JTextField();
        txtCordY.setHorizontalAlignment(SwingConstants.RIGHT);
        txtCordY.setFont(GLOBAL_FONT);
        txtCordY.setEditable(false);
        txtCordZ = new JTextField();
        txtCordZ.setHorizontalAlignment(SwingConstants.RIGHT);
        txtCordZ.setFont(GLOBAL_FONT);
        txtCordZ.setEditable(false);
        lblCordX.setFont(GLOBAL_FONT);
        lblCordY.setFont(GLOBAL_FONT);
        lblCordZ.setFont(GLOBAL_FONT);
        cordWrapperUp.add(lblCordX);
        cordWrapperUp.add(txtCordX);
        cordWrapperUp.add(lblCordY);
        cordWrapperUp.add(txtCordY);
        cordWrapperUp.add(lblCordZ);
        cordWrapperUp.add(txtCordZ);

        // value, mask display text box
        JPanel cordWrapperDown = new JPanel();
        cordWrapperDown.setLayout(null);
        cordWrapperDown.setPreferredSize(new Dimension(TOOL_PANE_WIDTH, 35));
        cordWrapper.add(cordWrapperDown);

        txtIntensity = new JTextField();
        txtIntensity.setHorizontalAlignment(SwingConstants.RIGHT);
        txtIntensity.setFont(GLOBAL_FONT);
        txtIntensity.setEditable(false);
        txtMaskBelong = new JTextField();
        txtMaskBelong.setHorizontalAlignment(SwingConstants.LEFT);
        txtMaskBelong.setFont(GLOBAL_FONT_TINY);
        txtMaskBelong.setEditable(false);
        lblIntensity.setFont(GLOBAL_FONT);
        lblMaskBelong.setFont(GLOBAL_FONT);
        cordWrapperDown.add(lblIntensity);
        cordWrapperDown.add(txtIntensity);
        cordWrapperDown.add(lblMaskBelong);
        cordWrapperDown.add(txtMaskBelong);

        // mask list area
        JPanel maskWrapper = new JPanel();
        maskWrapper.setLayout(new BorderLayout(5, 2));
        maskWrapper.setBorder(BorderFactory.createTitledBorder(null, "图层",
                TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, GLOBAL_FONT, Color.BLACK));
        toolPane.add(maskWrapper);

        // mask menu
        maskMenu = new JPopupMenu();
        maskMenu.setBorderPainted(false);
        maskMenu.setPopupSize(180, 4 * 30);
        maskMenuEditMenu.setFont(GLOBAL_FONT);
        editMenuItemThresh.setFont(GLOBAL_FONT);
        editMenuItemGrow.setFont(GLOBAL_FONT);
        editMenuItemRename.setFont(GLOBAL_FONT);
        editMenuItemRecolor.setFont(GLOBAL_FONT);
        editMenuItemClear.setFont(GLOBAL_FONT);
        maskMenuItemAdd.setFont(GLOBAL_FONT);
        maskMenuItemDuplicate.setFont(GLOBAL_FONT);
        maskMenuItemDelete.setFont(GLOBAL_FONT);

        maskMenu.add(maskMenuEditMenu);
        maskMenuEditMenu.add(editMenuItemThresh);
        maskMenuEditMenu.add(editMenuItemGrow);
        maskMenuEditMenu.addSeparator();
        maskMenuEditMenu.add(editMenuItemRename);
        maskMenuEditMenu.add(editMenuItemRecolor);
        maskMenuEditMenu.addSeparator();
        maskMenuEditMenu.add(editMenuItemClear);
        maskMenu.addSeparator();
        maskMenu.add(maskMenuItemAdd);
        maskMenu.addSeparator();
        maskMenu.add(maskMenuItemDuplicate);
        maskMenu.add(maskMenuItemDelete);

        // list
        lstMask = new JList<>();
        lstMask.setCellRenderer(new GMIMaskListCellRenderer());
        lstMask.setModel(maskListModel = new DefaultListModel<>());
        lstMask.setBackground(Color.LIGHT_GRAY);
        maskWrapper.add(new JScrollPane(lstMask));


    /* === DISPLAY PANE === */

        // Display pane outer
        displayPaneWrapper = new JPanel();
        displayPaneWrapper.setMinimumSize(new Dimension(DISPLAY_PANE_MIN_WIDTH, WINDOW_MIN_HEIGHT));
        displayPaneWrapper.setLayout(new BorderLayout());
        add(displayPaneWrapper);

        // display pane inner
        displayPane = new JPanel();
        displayPane.setLayout(new GridLayout(2, 2));
        displayPaneWrapper.add(displayPane);

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
        canvasWrapper00.setBorder(BorderFactory.createTitledBorder(null, "TOP",
                TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper00.setLayout(new BorderLayout());
        canvasWrapper00.add(canvas00);
        canvasWrapper00.add(scrollBar00, BorderLayout.EAST);
        displayPane.add(canvasWrapper00);

        JPanel canvasWrapper01 = new JPanel();
        canvasWrapper01.setBackground(Color.BLACK);
        canvasWrapper01.setBorder(BorderFactory.createTitledBorder(null, "RIGHT",
                TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper01.setLayout(new BorderLayout());
        canvasWrapper01.add(canvas01);
        canvasWrapper01.add(scrollBar01, BorderLayout.EAST);
        displayPane.add(canvasWrapper01);

        JPanel canvasWrapper10 = new JPanel();
        canvasWrapper10.setBackground(Color.BLACK);
        canvasWrapper10.setBorder(BorderFactory.createTitledBorder(null, "FRONT",
                TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper10.setLayout(new BorderLayout());
        canvasWrapper10.add(canvas10);
        canvasWrapper10.add(scrollBar10, BorderLayout.EAST);
        displayPane.add(canvasWrapper10);

        JPanel canvasWrapper11 = new JPanel();
        canvasWrapper11.setBackground(Color.BLACK);
        canvasWrapper11.setBorder(BorderFactory.createTitledBorder(null, "3D(待实现)",
                TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        canvasWrapper11.setLayout(new BorderLayout());
        canvasWrapper11.add(canvas11);
        displayPane.add(canvasWrapper11);


    /* === LAYOUTS === */

        // global layout
        layout.putConstraint(SpringLayout.WEST, toolPane, 0, SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, toolPane, 0, SpringLayout.SOUTH, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, toolPane, 0, SpringLayout.NORTH, getContentPane());
        layout.putConstraint(SpringLayout.WEST, displayPaneWrapper, TOOL_PANE_WIDTH, SpringLayout.WEST, toolPane);
        layout.putConstraint(SpringLayout.EAST, displayPaneWrapper, 0, SpringLayout.EAST, getContentPane());
        layout.putConstraint(SpringLayout.NORTH, displayPaneWrapper, 0, SpringLayout.NORTH, getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, displayPaneWrapper, 0, SpringLayout.SOUTH, getContentPane());

        // tool pane layout
        toolPaneLayout.putConstraint(SpringLayout.NORTH, histogramWrapper, 0, SpringLayout.NORTH, toolPane);
        toolPaneLayout.putConstraint(SpringLayout.SOUTH, histogramWrapper, HISTOGRAM_HEIGHT + ADJUSTMENT_INPUT_HEIGHT, SpringLayout.NORTH, histogramWrapper);
        toolPaneLayout.putConstraint(SpringLayout.WEST, histogramWrapper, 0, SpringLayout.WEST, toolPane);
        toolPaneLayout.putConstraint(SpringLayout.EAST, histogramWrapper, -0, SpringLayout.EAST, toolPane);

        toolPaneLayout.putConstraint(SpringLayout.NORTH, cordWrapper, 0, SpringLayout.SOUTH, histogramWrapper);
        toolPaneLayout.putConstraint(SpringLayout.WEST, cordWrapper, 0, SpringLayout.WEST, toolPane);
        toolPaneLayout.putConstraint(SpringLayout.EAST, cordWrapper, -0, SpringLayout.EAST, toolPane);

        toolPaneLayout.putConstraint(SpringLayout.NORTH, maskWrapper, 0,  SpringLayout.SOUTH, cordWrapper);
        toolPaneLayout.putConstraint(SpringLayout.WEST, maskWrapper, 0, SpringLayout.WEST, toolPane);
        toolPaneLayout.putConstraint(SpringLayout.EAST, maskWrapper, -0, SpringLayout.EAST, toolPane);
        toolPaneLayout.putConstraint(SpringLayout.SOUTH, maskWrapper, 0, SpringLayout.SOUTH, toolPane);

        lblCordX.setBounds(5, 0, 20, 30);
        txtCordX.setBounds(25, 2, 50, 28);
        lblCordY.setBounds(85, 0, 20, 30);
        txtCordY.setBounds(105, 2, 50, 28);
        lblCordZ.setBounds(165, 0, 20, 30);
        txtCordZ.setBounds(185, 2, 50, 28);

        lblIntensity.setBounds(5, 0, 40, 30);
        txtIntensity.setBounds(45, 2, 70, 28);
        lblMaskBelong.setBounds(122, 0, 40, 30);
        txtMaskBelong.setBounds(167, 2, 68, 28);

    /* === LISTENERS === */

        canvas00.addLayerChangeListener(this);
        canvas01.addLayerChangeListener(this);
        canvas10.addLayerChangeListener(this);
        scrollBar00.addAdjustmentListener(this);
        scrollBar01.addAdjustmentListener(this);
        scrollBar10.addAdjustmentListener(this);

        histogram.addMouseListener(this);
        histogram.addMouseMotionListener(this);
        txtWindowSize.addMouseListener(this);
        txtWindowPosition.addMouseListener(this);

        lstMask.addMouseListener(this);

        editMenuItemThresh.addActionListener(this);
        editMenuItemGrow.addActionListener(this);
        editMenuItemRename.addActionListener(this);
        editMenuItemRecolor.addActionListener(this);
        editMenuItemClear.addActionListener(this);
        maskMenuItemAdd.addActionListener(this);
        maskMenuItemDuplicate.addActionListener(this);
        maskMenuItemDelete.addActionListener(this);
    }

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
        canvas00.loadImageData(rawData.bottomSlice, mhdInfo.x, mhdInfo.y, ctWindow, GMICanvas.ORIENT_BOTTOM);
        canvas01.loadImageData(rawData.rightSlice, mhdInfo.y, mhdInfo.z, ctWindow, GMICanvas.ORIENT_RIGHT);
        canvas10.loadImageData(rawData.frontSlice, mhdInfo.x, mhdInfo.z, ctWindow, GMICanvas.ORIENT_FRONT);
        canvas00.setMaskList(mask3DList);
        canvas01.setMaskList(mask3DList);
        canvas10.setMaskList(mask3DList);
        // load histogram
        histogram.loadHist(rawData.histogram, ctWindow);
        txtWindowSize.setText("" + (rawData.highestValue - rawData.lowestValue));
        txtWindowPosition.setText("" + (rawData.highestValue + rawData.lowestValue) / 2);
        // TODO Optimize resizing
        setSize(new Dimension(mhdInfo.x * 3, mhdInfo.y * 2));
        setLocationRelativeTo(null);
        // update canvas status
        activateAllFunctions();
        scrollBar00.setValue(canvas00.getCurrentLayer());
        scrollBar01.setValue(canvas01.getCurrentLayer());
        scrollBar10.setValue(canvas10.getCurrentLayer());
        repaint();
    }

//    public void loadData() {
//        Thread thread = new Thread(() -> {
//            try {
//                mhdInfo = GMILoader.loadMHDFile(new File(DEBUG_MHD_PATH));
//                mhdInfo.debugOutput();
//                rawData = GMILoader.loadRawFromMHD(mhdInfo);
//                mask3DList = new LinkedList<>();
//                onDataLoaded();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        });
//        thread.start();
//    }
    public void loadFile(File file) {
        SwingUtilities.invokeLater(() -> {
            try {
                deactivateAllFunctions();
                mhdInfo = GMILoader.loadMHDFile(file);
                mhdInfo.debugOutput();
                rawData = GMILoader.loadRawFromMHD(mhdInfo);
                mask3DList = new LinkedList<>();
                onDataLoaded();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "打开文件失失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /** invoke to stop any response of program when no file loaded (or other circumstances) */
    private void deactivateAllFunctions() {
        histogram.removeMouseListener(this);
        histogram.removeMouseMotionListener(this);
        txtWindowSize.removeMouseListener(this);
        txtWindowPosition.removeMouseListener(this);
        lstMask.removeMouseListener(this);
        canvas00.removeLayerChangeListener(this);
        canvas01.removeLayerChangeListener(this);
        canvas10.removeLayerChangeListener(this);
        scrollBar00.removeAdjustmentListener(this);
        scrollBar01.removeAdjustmentListener(this);
        scrollBar10.removeAdjustmentListener(this);
    }

    /** invoke to activate the program */
    private void activateAllFunctions() {
        histogram.addMouseListener(this);
        histogram.addMouseMotionListener(this);
        txtWindowSize.addMouseListener(this);
        txtWindowPosition.addMouseListener(this);
        lstMask.addMouseListener(this);
        canvas00.addLayerChangeListener(this);
        canvas01.addLayerChangeListener(this);
        canvas10.addLayerChangeListener(this);
        scrollBar00.addAdjustmentListener(this);
        scrollBar01.addAdjustmentListener(this);
        scrollBar10.addAdjustmentListener(this);
    }

    /** the index when menu popups */
    private int currentMaskListIndex;

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getSource() == txtWindowSize) {
            // Double click to change window size
            if (e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1) {
                String winSizeStr = JOptionPane.showInputDialog(this,
                        "请输入窗宽(" + CTWindow.MIN_WINDOW_SIZE + "~" + CTWindow.MAX_WINDOW_SIZE + ")：",
                        "设置窗宽",
                        JOptionPane.PLAIN_MESSAGE);
                int winSize;
                try {
                    winSize = Integer.parseInt(winSizeStr);
                    ctWindow.setWinSize(winSize);
                    updateOnCtWindowChanged();
                } catch (NumberFormatException ex) {
                    // ignored
                }
            }
        } else if (e.getSource() == txtWindowPosition) {
            // Double click to change window position
            if(e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1) {
                String winPositionStr = JOptionPane.showInputDialog(this,
                        "请输入窗位("
                                + CTWindow.MIN_WINDOW_SIZE + "~" + (CTWindow.MAX_WINDOW_SIZE - CTWindow.MIN_WINDOW_SIZE)
                                + ")：",
                        "设置窗位",
                        JOptionPane.PLAIN_MESSAGE);
                int winPosition;
                try {
                    winPosition = Integer.parseInt(winPositionStr);
                    ctWindow.setWinPosition(winPosition);
                    updateOnCtWindowChanged();
                } catch (NumberFormatException ex) {
                    // ignored
                }
            }
        } else if(e.getSource() == lstMask) {
            // Right click popup menu
            if(e.getButton() == MouseEvent.BUTTON3) {
                // right click
                if (maskListModel.isEmpty() || !lstMask.getCellBounds(0, maskListModel.getSize() - 1).contains(e.getPoint())) {
                    // outside the cells or list empty
                    maskMenuEditMenu.setEnabled(false);
                    maskMenuItemDuplicate.setEnabled(false);
                    maskMenuItemDelete.setEnabled(false);
                    currentMaskListIndex = -1;
                } else {
                    // inside the cells
                    maskMenuEditMenu.setEnabled(true);
                    maskMenuItemDuplicate.setEnabled(true);
                    maskMenuItemDelete.setEnabled(true);
                    currentMaskListIndex = lstMask.locationToIndex(e.getPoint());
                }
                maskMenu.show(lstMask, e.getX(), e.getY());
            }
            // triple click on cell, rename
            else if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 3) {
                if(!maskListModel.isEmpty() && lstMask.getCellBounds(0, maskListModel.getSize() - 1).contains(e.getPoint())) {
                    currentMaskListIndex = lstMask.locationToIndex(e.getPoint());
                    renameMask();
                }
            }
            // single click on cell
            else if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                if(!maskListModel.isEmpty() && lstMask.getCellBounds(0, maskListModel.getSize() - 1).contains(e.getPoint())) {
                    currentMaskListIndex = lstMask.locationToIndex(e.getPoint());
                    // TODO
                    Rectangle bounds = lstMask.getCellBounds(currentMaskListIndex, currentMaskListIndex);
                    Rectangle eyeBounds = new Rectangle(bounds.x + bounds.width - 40, bounds.y + bounds.height / 2 - 10, 20, 20);
                    if(eyeBounds.contains(e.getPoint())) {
                        maskListModel.getElementAt(currentMaskListIndex).toggleVisible();
                        lstMask.repaint();
                        updateOnMaskChanged();
                    }
                }
            }
        }
    }

    /** update layer data when ct window changes */
    private void updateOnCtWindowChanged() {
        txtWindowPosition.setText("" + ctWindow.getWinMid());
        txtWindowSize.setText("" + ctWindow.getWinSize());
        canvas00.updateOnCtWindowChange();
        canvas01.updateOnCtWindowChange();
        canvas10.updateOnCtWindowChange();
        repaint();
    }

    /** update layer data when mask changes */
    public void updateOnMaskChanged() {
        canvas00.updateOnMaskChange();
        canvas01.updateOnMaskChange();
        canvas10.updateOnMaskChange();
        txtMaskBelong.setText(Optional.ofNullable(canvas00.getCurrentMask()).map(GMIMask3D::getMaskName).orElse("无"));
        repaint();
    }

    /** mouse status */
    private boolean isMouseButton1Dragging;
    /** mouse status */
    private boolean isMouseButton3Dragging;

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getSource() == histogram) {
            isMouseButton1Dragging |= e.getButton() == MouseEvent.BUTTON1;
            isMouseButton3Dragging |= e.getButton() == MouseEvent.BUTTON3;
            if(isMouseButton1Dragging) lastMouseX1 = e.getX();
            if(isMouseButton3Dragging) lastMouseX3 = e.getX();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getSource() == histogram) {
            isMouseButton1Dragging &= !(e.getButton() == MouseEvent.BUTTON1);
            isMouseButton3Dragging &= !(e.getButton() == MouseEvent.BUTTON3);
            if(!isMouseButton1Dragging) lastMouseX1 = 0;
            if(!isMouseButton3Dragging) lastMouseX3 = 0;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // skip
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // skip
    }

    /** save last mouse position in drag event */
    private int lastMouseX1;
    private int lastMouseX3;

    /** speed up the drag-change rate by ratio */
    private final static int DRAG_RATIO = 2;

    @Override
    public void mouseDragged(MouseEvent e) {
        if(e.getSource() == histogram) {
            int dx;
            if(isMouseButton1Dragging) {
                dx = e.getX() - lastMouseX1;
                ctWindow.setWinPosition(ctWindow.getWinMid() + dx * DRAG_RATIO);
                lastMouseX1 = e.getX();
            }
            if(isMouseButton3Dragging) {
                dx = e.getX() - lastMouseX3;
                ctWindow.setWinSize(ctWindow.getWinSize() + dx * DRAG_RATIO);
                lastMouseX3 = e.getX();
            }
            updateOnCtWindowChanged();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // skip
    }

    @Override
    public void onLayerChanged(LayerChangeEvent event) {
        int layer = event.getLayer();
        if(event.getEventSource() == canvas00) {
            if(event.getType() == LayerChangeEvent.TYPE_CORD_X) {
                scrollBar01.setValue(layer);
                canvas10.changeAxisX(layer);
            } else if(event.getType() == LayerChangeEvent.TYPE_CORD_Y) {
                scrollBar10.setValue(layer);
                canvas01.changeAxisX(layer);
            } else if(event.getType() == LayerChangeEvent.TYPE_CORD_Z) {
                scrollBar00.setValue(layer);
//                canvas01.changeAxisY(mhdInfo.z - layer);
//                canvas10.changeAxisY(mhdInfo.z - layer);
            }
        } else if(event.getEventSource() == canvas01) {
            if(event.getType() == LayerChangeEvent.TYPE_CORD_X) {
                scrollBar10.setValue(layer);
                canvas00.changeAxisY(layer);
            } else if(event.getType() == LayerChangeEvent.TYPE_CORD_Y) {
                scrollBar00.setValue(mhdInfo.z - 1 - layer);
                canvas10.changeAxisY(layer);
            } else if(event.getType() == LayerChangeEvent.TYPE_CORD_Z) {
                scrollBar01.setValue(layer);
//                canvas10.changeAxisX(layer);
//                canvas00.changeAxisX(layer);
            }
        } else if(event.getEventSource() == canvas10) {
            if(event.getType() == LayerChangeEvent.TYPE_CORD_X) {
                scrollBar01.setValue(layer);
                canvas00.changeAxisX(layer);
            } else if(event.getType() == LayerChangeEvent.TYPE_CORD_Y) {
                scrollBar00.setValue(mhdInfo.z - 1 - layer);
                canvas01.changeAxisY(layer);
            } else if(event.getType() == LayerChangeEvent.TYPE_CORD_Z) {
                scrollBar10.setValue(layer);
//                canvas01.changeAxisX(mhdInfo.y - 1 - layer);
//                canvas00.changeAxisY(layer);
            }
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        int layer = e.getValue();
        if(e.getSource() == scrollBar00) {
            canvas00.changeLayer(layer);
            canvas01.changeAxisY(mhdInfo.z - 1 - layer);
            canvas10.changeAxisY(mhdInfo.z - 1 - layer);
            txtCordZ.setText("" + layer);
        } else if(e.getSource() == scrollBar01) {
            canvas01.changeLayer(e.getValue());
            canvas10.changeAxisX(layer);
            canvas00.changeAxisX(layer);
            txtCordX.setText("" + layer);
        } else if(e.getSource() == scrollBar10) {
            canvas10.changeLayer(e.getValue());
            canvas01.changeAxisX(layer);
            canvas00.changeAxisY(layer);
            txtCordY.setText("" + layer);
        }
        txtIntensity.setText("" + canvas00.getCurrentValue());
        txtMaskBelong.setText(Optional.ofNullable(canvas00.getCurrentMask()).map(GMIMask3D::getMaskName).orElse("无"));
        txtMaskBelong.setBackground(Optional.ofNullable(canvas00.getCurrentMask())
                .map(GMIMask3D::getColor).map(Color::new).orElse(txtIntensity.getBackground()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == maskMenuItemAdd) {
            GMIMask3D mask = new GMIMask3D(mhdInfo.x, mhdInfo.y, mhdInfo.z);
            addMask3DToList(mask);
        }
        else if(e.getSource() == editMenuItemThresh) {
            GMIThreshDialog dialog = new GMIThreshDialog(this, maskListModel.elementAt(currentMaskListIndex), rawData, mhdInfo,
                    ctWindow.getWinLow(), ctWindow.getWinHigh(), rawData.histogram);
            boolean isConfirmed = dialog.popup(canvas01.getCurrentLayer(), canvas10.getCurrentLayer(), canvas00.getCurrentLayer());
            updateOnMaskChanged();
        }
        else if(e.getSource() == editMenuItemGrow) {
            GMIGrowDialog dialog = new GMIGrowDialog(this, mhdInfo);
            lstMask.removeMouseListener(this);
            canvas00.startRegionGrowMode();
            canvas01.startRegionGrowMode();
            canvas10.startRegionGrowMode();
            scrollBar00.addAdjustmentListener(dialog);
            scrollBar01.addAdjustmentListener(dialog);
            scrollBar10.addAdjustmentListener(dialog);
            dialog.popup(this::growDialogCallback);
        }
        else if(e.getSource() == maskMenuItemDelete) {
            int opt = JOptionPane.showConfirmDialog(this, "确认要删除图层吗？这一操作不可逆。", "删除图层", JOptionPane.OK_CANCEL_OPTION);
            if(opt == JOptionPane.OK_OPTION) {
                removeMask3DFromList(maskListModel.elementAt(currentMaskListIndex));
            }
        }
        else if(e.getSource() == maskMenuItemDuplicate) {
            GMIMask3D mask3D = maskListModel.elementAt(currentMaskListIndex).createCopy();
            addMask3DToList(mask3D);
        }
        else if(e.getSource() == editMenuItemRename) {
            renameMask();
        }
        else if(e.getSource() == editMenuItemRecolor) {
            Color oldColor = new Color(maskListModel.elementAt(currentMaskListIndex).getColor());
            Color newColor = JColorChooser.showDialog(this, "修改图层颜色", oldColor);
            maskListModel.elementAt(currentMaskListIndex).setColor(newColor.getRGB());
            updateOnMaskChanged();
        }
        else if(e.getSource() == editMenuItemClear) {
            int opt = JOptionPane.showConfirmDialog(this, "确认要清空图层吗？", "清空", JOptionPane.OK_CANCEL_OPTION);
            if(opt == JOptionPane.OK_OPTION) {
                maskListModel.elementAt(currentMaskListIndex).clearMask();
                updateOnMaskChanged();
            }
        }

        // menu bar
        else if(e.getSource() == fileMenuItemOpen) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if(f.isDirectory()) {
                        return true;
                    }
                    if(f.getName().contains(".")) {
                        String suffix = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                        return suffix.length() != 0 && suffix.toLowerCase().equals("mhd");
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return "MHD文件(*.mhd)";
                }
            });
            fileChooser.showDialog(this, "选择文件");
            File file = fileChooser.getSelectedFile();
            if(file != null) {
                loadFile(file);
            }
        }
        else if(e.getSource() == fileMenuItemExit) {
            // TODO Save something?
            this.dispose();
        }
    }

    /** rename mask */
    private void renameMask() {
        String newMaskName = JOptionPane.showInputDialog(this, "请输入新的名称", "重命名", JOptionPane.PLAIN_MESSAGE);
        if(newMaskName != null && newMaskName.trim().length() != 0) {
            maskListModel.elementAt(currentMaskListIndex).setMaskName(newMaskName);
        }
    }

    public void growDialogCallback(GMIGrowDialog dialog) {
        lstMask.addMouseListener(this);
        canvas00.endRegionGrowMode();
        canvas01.endRegionGrowMode();
        canvas10.endRegionGrowMode();
        scrollBar00.addAdjustmentListener(dialog);
        scrollBar01.addAdjustmentListener(dialog);
        scrollBar10.addAdjustmentListener(dialog);
        if(dialog.confirmed) {
            addMask3DToList(dialog.mask3D);
        }
    }

    private synchronized void addMask3DToList(GMIMask3D mask3D) {
        maskListModel.addElement(mask3D);
        mask3DList.add(mask3D);
        updateOnMaskChanged();
    }

    private synchronized void removeMask3DFromList(GMIMask3D mask3D) {
        maskListModel.removeElement(mask3D);
        maskListModel.trimToSize();
        mask3DList.remove(mask3D);
        SwingUtilities.invokeLater(System::gc);
        updateOnMaskChanged();
    }
}
