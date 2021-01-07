package com.gzy.gmi.app.GMIViewer.widgets;

import com.gzy.gmi.util.GMIMask3D;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GMIMaskListCellRenderer extends JPanel implements ListCellRenderer<GMIMask3D> {

    /** mask color */
    private Color color;

    /** mask name */
    private String name;

    /** JList component */
    private JList<? extends GMIMask3D> jList;

    /** cell selection status */
    private boolean isSelected, cellHasFocus;

    private Font FORE_FONT = new Font("等线", Font.BOLD, 16);

    @Override
    public Component getListCellRendererComponent(JList<? extends GMIMask3D> list, GMIMask3D mask, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        jList = list;
        color = new Color(mask.getColor());
        name = mask.getMaskName();
        setPreferredSize(new Dimension(0, 35));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.setFont(FORE_FONT);
        g.drawString(name, 10, 20); // TODO 自适应
    }
}
