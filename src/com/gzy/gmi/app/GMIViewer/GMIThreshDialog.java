package com.gzy.gmi.app.GMIViewer;

import com.gzy.gmi.app.GMIViewer.widgets.GMIHistogram;
import com.gzy.gmi.util.CTWindow;
import com.gzy.gmi.util.GMIMask3D;
import com.gzy.gmi.util.MHDInfo;
import com.gzy.gmi.util.RawData;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/** popup dialog when chooses thresh mask */
public class GMIThreshDialog extends JDialog implements MouseMotionListener, MouseListener, ActionListener, WindowListener {


    private final CTWindow ctWindow;

    private final GMIFrame parent;

    private final GMIMask3D mask;

    private final RawData rawData;

    private MHDInfo mhdInfo;

    private int dimX, dimY, dimZ;

    /** true if in size\position changing mode
     * false if in low\high changing mode */
    private boolean isSizePosMode;

    private final GMIHistogram histogram;
    private final JTextField txtWindowSize;
    private final JTextField txtWindowPosition;
    private final JLabel lblWindowSize = new JLabel("窗宽:");
    private final JLabel lblWindowPosition = new JLabel("窗位:");
    private final JTextField txtWindowLow;
    private final JTextField txtWindowHigh;
    private final JLabel lblWindowLow = new JLabel("最低：");
    private final JLabel lblWindowHigh = new JLabel("最高:");
    private final JButton btnSubmit;
    private final ButtonGroup buttonGroup;
    private final JRadioButton rdbSizePos;
    private final JRadioButton rdbLowHigh;

    private static final Font DEFAULT_FONT = new Font("等线", Font.BOLD, 16);

//    public GMIThreshDialog(GMIFrame parent, int[] hist) {
//        this(parent, CTWindow.LOWEST_CT_VALUE, CTWindow.HIGHEST_CT_VALUE, hist);
//    }

    public GMIThreshDialog(GMIFrame parent, GMIMask3D mask, RawData rawData, MHDInfo mhdInfo, int winLow, int winHigh, int[] hist) {
        super(parent, "阈值分割", true);
        setSize(400, 335);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);
        this.parent = parent;
        this.mask = mask;
        this.rawData = rawData;
        this.mhdInfo = mhdInfo;
        this.dimX = mhdInfo.x;
        this.dimY = mhdInfo.y;
        this.dimZ = mhdInfo.z;

        // load components
        ctWindow = new CTWindow(winLow, winHigh);
        histogram = new GMIHistogram();
        add(histogram);

        txtWindowSize = new JTextField();
        txtWindowPosition = new JTextField();
        lblWindowSize.setFont(DEFAULT_FONT);
        lblWindowPosition.setFont(DEFAULT_FONT);
        txtWindowSize.setFont(DEFAULT_FONT);
        txtWindowPosition.setFont(DEFAULT_FONT);
        txtWindowSize.setEditable(false);
        txtWindowPosition.setEditable(false);
        add(lblWindowSize);
        add(lblWindowPosition);
        add(txtWindowSize);
        add(txtWindowPosition);
        txtWindowLow = new JTextField();
        txtWindowHigh = new JTextField();
        lblWindowLow.setFont(DEFAULT_FONT);
        lblWindowHigh.setFont(DEFAULT_FONT);
        txtWindowLow.setFont(DEFAULT_FONT);
        txtWindowHigh.setFont(DEFAULT_FONT);
        txtWindowLow.setEditable(false);
        txtWindowHigh.setEditable(false);
        add(lblWindowLow);
        add(lblWindowHigh);
        add(txtWindowLow);
        add(txtWindowHigh);
        btnSubmit = new JButton("确认");
        btnSubmit.setFont(DEFAULT_FONT);
        add(btnSubmit);

        // radio button to switch function from size\pos changing to low\high changing
        buttonGroup = new ButtonGroup();
        rdbSizePos = new JRadioButton();
        rdbLowHigh = new JRadioButton();
        buttonGroup.add(rdbSizePos);
        buttonGroup.add(rdbLowHigh);
        rdbLowHigh.setSelected(true);
        isSizePosMode = false;
        add(rdbSizePos);
        add(rdbLowHigh);

        histogram.setBackground(Color.BLACK);
        histogram.setBounds(0, 0, 384, 220);
        histogram.loadHist(hist, ctWindow);
        lblWindowSize.setBounds(23, 225,60, 30);
        txtWindowSize.setBounds(78, 225, 60, 30);
        lblWindowPosition.setBounds(143, 225, 60, 30);
        txtWindowPosition.setBounds(198, 225, 60, 30);
        lblWindowLow.setBounds(23, 258,60, 30);
        txtWindowLow.setBounds(78, 258, 60, 30);
        lblWindowHigh.setBounds(143, 258, 60, 30);
        txtWindowHigh.setBounds(198, 258, 60, 30);
        btnSubmit.setBounds(290, 225, 86, 63);
        rdbSizePos.setBounds(4, 233, 16, 16);
        rdbLowHigh.setBounds(4, 266, 16, 16);

        // listeners
        histogram.addMouseListener(this);
        histogram.addMouseMotionListener(this);
        btnSubmit.addActionListener(this);
        rdbSizePos.addActionListener(this);
        rdbLowHigh.addActionListener(this);
        lblWindowSize.addMouseListener(this);
        lblWindowLow.addMouseListener(this);
        this.addWindowListener(this);

        setToolTipTexts();
    }

    private void setToolTipTexts() {
        histogram.setToolTipText(
                "<html>" +
                    "<font face=等线 size=5 color=#000000/>" +
                    "<p><b>窗宽/窗位模式：</b>左键拖动调整窗位，右键拖动调整窗宽。</p>" +
                    "<p><b>下界/上界模式：</b>左键拖动调整下界，右键拖动调整上界，左右键同时拖动调整窗位。</p>" +
                "</html>"
        );
    }

    /** true if the submit button clicked */
    private boolean confirmed;

    private int x, y, z;

    /**
     * pop up this dialog, and clear previous dialog state
     * @return true if click submit button to quit
     * */
    public boolean popup(int x, int y, int z) {
        setLocationRelativeTo(parent);
        confirmed = false;
        this.x = x;
        this.y = y;
        this.z = z;
        stashChanges();
        updateOnCtWindowChanged();
        setVisible(true);
        return confirmed;
    }

    private boolean[] bottomBackup;
    private boolean[] rightBackup;
    private boolean[] frontBackup;

    /** stash mask change for preview */
    private void stashChanges() {
        // stash changes
        bottomBackup = new boolean[dimX * dimY];
        rightBackup = new boolean[dimZ * dimY];
        frontBackup = new boolean[dimX * dimZ];
        System.arraycopy(mask.bottomSlice[z], 0, bottomBackup, 0, dimX * dimY);
        System.arraycopy(mask.rightSlice[x], 0, rightBackup, 0, dimZ * dimY);
        System.arraycopy(mask.frontSlice[y], 0, frontBackup, 0, dimX * dimZ);
    }


    /** mouse status */
    private boolean isMouseButton1Dragging;
    /** mouse status */
    private boolean isMouseButton3Dragging;

    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getSource() == lblWindowSize) {
            rdbSizePos.setSelected(true);
            rdbLowHigh.setSelected(false);
            isSizePosMode = true;
        }
        else if(e.getSource() == lblWindowLow) {
            rdbLowHigh.setSelected(true);
            rdbSizePos.setSelected(false);
            isSizePosMode = false;
        }
    }

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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void mouseDragged(MouseEvent e) {
        if(e.getSource() == histogram) {
            int dx;
            if(isSizePosMode) {
                // left drag to change window position
                if (isMouseButton1Dragging) {
                    dx = e.getX() - lastMouseX1;
                    ctWindow.setWinPosition(ctWindow.getWinMid() + dx * DRAG_RATIO);
                    lastMouseX1 = e.getX();
                }
                // right drag to change window size
                if (isMouseButton3Dragging) {
                    dx = e.getX() - lastMouseX3;
                    ctWindow.setWinSize(ctWindow.getWinSize() + dx * DRAG_RATIO);
                    lastMouseX3 = e.getX();
                }
            } else {
                // single left drag to change window low
                if(isMouseButton1Dragging && !isMouseButton3Dragging) {
                    dx = e.getX() - lastMouseX1;
                    ctWindow.setWinLow(ctWindow.getWinLow() + dx * DRAG_RATIO);
                    lastMouseX1 = e.getX();
                }
                // single right drag to change window high
                else if(isMouseButton3Dragging && !isMouseButton1Dragging) {
                    dx = e.getX() - lastMouseX3;
                    ctWindow.setWinHigh(ctWindow.getWinHigh() + dx * DRAG_RATIO);
                    lastMouseX3 = e.getX();
                }
                // both left & right drag to change window position
                else if(isMouseButton1Dragging && isMouseButton3Dragging) {
                    // use mouse1's dx
                    dx = e.getX() - lastMouseX1;
                    ctWindow.setWinPosition(ctWindow.getWinMid() + dx * DRAG_RATIO);
                    lastMouseX1 = e.getX();
                }
            }
            updateOnCtWindowChanged();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // skip
    }

    /** update layer data when change ct window on Histogram component */
    private void updateOnCtWindowChanged() {
        txtWindowPosition.setText("" + ctWindow.getWinMid());
        txtWindowSize.setText("" + ctWindow.getWinSize());
        txtWindowLow.setText("" + ctWindow.getWinLow());
        txtWindowHigh.setText("" + ctWindow.getWinHigh());
        maskPreview();
        repaint();
    }

    /** update currently displaying mask as preview, undo when quit without submitting */
    private void maskPreview() {
        for (int i = 0; i < mask.bottomSlice[z].length; i++) {
            mask.bottomSlice[z][i] = (rawData.bottomSlice[z][i] >= ctWindow.getWinLow() && rawData.bottomSlice[z][i] <= ctWindow.getWinHigh());
        }
        for (int i = 0; i < mask.rightSlice[x].length; i++) {
            mask.rightSlice[x][i] = (rawData.rightSlice[x][i] >= ctWindow.getWinLow() && rawData.rightSlice[x][i] <= ctWindow.getWinHigh());
        }
        for (int i = 0; i < mask.frontSlice[y].length; i++) {
            mask.frontSlice[y][i] = (rawData.frontSlice[y][i] >= ctWindow.getWinLow() && rawData.frontSlice[y][i] <= ctWindow.getWinHigh());
        }
        // update canvas view
        parent.updateOnMaskChanged();
    }

    /** confirm changes, update whole mask */
    private void maskConfirmed() {
        for(int sl = 0; sl < dimZ; sl++) {
            for (int i = 0; i < mask.bottomSlice[z].length; i++) {
                mask.bottomSlice[sl][i] = (rawData.bottomSlice[sl][i] >= ctWindow.getWinLow() && rawData.bottomSlice[sl][i] <= ctWindow.getWinHigh());
            }
        }
        for(int sl = 0; sl < dimX; sl++) {
            for (int i = 0; i < mask.rightSlice[x].length; i++) {
                mask.rightSlice[sl][i] = (rawData.rightSlice[sl][i] >= ctWindow.getWinLow() && rawData.rightSlice[sl][i] <= ctWindow.getWinHigh());
            }
        }
        for(int sl = 0; sl < dimY; sl++) {
            for (int i = 0; i < mask.frontSlice[y].length; i++) {
                mask.frontSlice[sl][i] = (rawData.frontSlice[sl][i] >= ctWindow.getWinLow() && rawData.frontSlice[sl][i] <= ctWindow.getWinHigh());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btnSubmit) {
            maskConfirmed();
            confirmed = true;
            this.dispose();
        } else if(e.getSource() == rdbSizePos) {
            isSizePosMode = true;
        } else if(e.getSource() == rdbLowHigh) {
            isSizePosMode = false;
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // skip
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if(!confirmed) {
            // restore changes
            System.arraycopy(bottomBackup, 0, mask.bottomSlice[z], 0, dimX * dimY);
            System.arraycopy(rightBackup, 0, mask.rightSlice[x], 0, dimZ * dimY);
            System.arraycopy(frontBackup, 0, mask.frontSlice[y], 0, dimX * dimZ);
            parent.updateOnMaskChanged();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // skip
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // skip
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // skip
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // skip
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // skip
    }
}
