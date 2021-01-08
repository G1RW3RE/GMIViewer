package com.gzy.gmi.app.GMIViewer.widgets;

import com.gzy.gmi.util.GMIMask3D;

import javax.imageio.ImageIO;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GMIMaskListCellRenderer extends JPanel implements ListCellRenderer<GMIMask3D> {

    /** mask color */
    private Color color;

    /** mask name */
    private String name;

    /** mask visible */
    private boolean visible;

    /** JList component */
    private JList<? extends GMIMask3D> jList;

    /** cell selection status */
    private boolean isSelected, cellHasFocus;

    private Font FORE_FONT = new Font("等线", Font.BOLD, 16);

    public static Image eyeOpenIcon;
    public static Image eyeCloseIcon;

    static {
        try {
            InputStream stream;
            stream = GMIMaskListCellRenderer.class.getClassLoader().getResourceAsStream("eyeopen.png");
            if(stream != null) {
                BufferedImage eyeOpenImage = ImageIO.read(stream);
                eyeOpenIcon = eyeOpenImage.getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH);
            }
            stream = GMIMaskListCellRenderer.class.getClassLoader().getResourceAsStream("eyeclose.png");
            if(stream != null) {
                BufferedImage eyeCloseImage = ImageIO.read(stream);
                eyeCloseIcon = eyeCloseImage.getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends GMIMask3D> list, GMIMask3D mask, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        jList = list;
        color = new Color(mask.getColor());
        name = mask.getMaskName();
        visible = mask.visible;
        this.isSelected = isSelected;
        this.cellHasFocus = cellHasFocus;
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
        if(isSelected) {
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
        if(visible) {
            g.drawImage(eyeOpenIcon, getWidth() - 40, getHeight() / 2 - 10, this);
        } else {
            g.drawImage(eyeCloseIcon, getWidth() - 40, getHeight() / 2 - 10, this);
        }
    }
}
