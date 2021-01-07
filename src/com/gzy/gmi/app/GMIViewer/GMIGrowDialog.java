package com.gzy.gmi.app.GMIViewer;

import com.gzy.gmi.util.GMIMask3D;
import com.gzy.gmi.util.MHDInfo;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.function.Consumer;

public class GMIGrowDialog extends JDialog implements WindowListener, AdjustmentListener, ActionListener {

    private GMIFrame parent;

    private JTextField txtCordX, txtCordY, txtCordZ;
    private final JLabel lblCordX = new JLabel("x:");
    private final JLabel lblCordY = new JLabel("y:");
    private final JLabel lblCordZ = new JLabel("z:");
    private final JButton btnConfirm;
    private final JRadioButton rdbConn6 = new JRadioButton("6-connectivity");
    private final JRadioButton rdbConn26 = new JRadioButton("26-connectivity");

    private Consumer<GMIGrowDialog> onCloseCallback;

    private MHDInfo mhdInfo;

    public GMIGrowDialog(GMIFrame parent, MHDInfo mhdInfo) {
        super(parent, "选择区域生长种子点", false);
        this.parent = parent;
        this.mhdInfo = mhdInfo;
        setSize(510, 100);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(this);

        txtCordX = new JTextField();
        txtCordY = new JTextField();
        txtCordZ = new JTextField();
        txtCordX.setEditable(false);
        txtCordY.setEditable(false);
        txtCordZ.setEditable(false);
        add(lblCordX);
        add(txtCordX);
        add(lblCordY);
        add(txtCordY);
        add(lblCordZ);
        add(txtCordZ);
        btnConfirm = new JButton("确定");
        add(btnConfirm);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rdbConn6);
        buttonGroup.add(rdbConn26);
        add(rdbConn6);
        add(rdbConn26);

        setLayout(null);
        lblCordX.setBounds(10, 20, 30, 30);
        txtCordX.setBounds(25, 20, 60, 30);
        lblCordY.setBounds(90, 20, 30, 30);
        txtCordY.setBounds(115, 20, 60, 30);
        lblCordZ.setBounds(180, 20, 30, 30);
        txtCordZ.setBounds(205, 20, 60, 30);
        btnConfirm.setBounds(410, 20, 70, 30);
        rdbConn6.setBounds(280, 20, 120, 15);
        rdbConn26.setBounds(280, 35, 120, 15);

        btnConfirm.addActionListener(this);
    }

    public void popup(Consumer<GMIGrowDialog> onCloseCallback) {
        setLocationRelativeTo(parent);
        this.onCloseCallback = onCloseCallback;
        txtCordX.setText("" + parent.canvas01.getCurrentLayer());
        txtCordY.setText("" + parent.canvas10.getCurrentLayer());
        txtCordZ.setText("" + parent.canvas00.getCurrentLayer());
        btnConfirm.setEnabled(false);
        confirmed = false;
        rdbConn6.setSelected(true);
        setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // skip
    }

    @Override
    public void windowClosing(WindowEvent e) {
        // skip
    }

    @Override
    public void windowClosed(WindowEvent e) {
        onCloseCallback.accept(this);
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

    /** new mask */
    GMIMask3D mask3D;

    /** confirm button clicked */
    boolean confirmed;

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btnConfirm) {
            confirmed = true;
            int connType = rdbConn6.isSelected() ? 6 : 26;
            mask3D = GMIMask3D.regionGrowing(parent.canvas00.getCurrentMask(), parent.canvas01.getCurrentLayer(), parent.canvas10.getCurrentLayer(), parent.canvas00.getCurrentLayer(), connType);
            this.dispose();
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        txtCordX.setText("" + parent.canvas01.getCurrentLayer());
        txtCordY.setText("" + parent.canvas10.getCurrentLayer());
        txtCordZ.setText("" + parent.canvas00.getCurrentLayer());
        btnConfirm.setEnabled(parent.canvas00.getCurrentMask() != null);
    }
}
