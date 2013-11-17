/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kachliky.components;

import cz.matfyz.sykora.nngui.network.Network;
import cz.matfyz.sykora.nngui.network.NeuronRow;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import javax.swing.JPanel;
import kachliky.MyProvider;

/**
 *
 * @author kirrie
 */
public class ImageComp extends JPanel {
    
    public BufferedImage image;
    private BufferedImage workingImage;
    private WritableRaster raster;
    
    public ImageComp(int w, int h) {
        image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics g = image.getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, w, h);
    }
    
    public ImageComp(BufferedImage image) {
        this.image = image;
    }
    
    public int getWidth() {
        return image.getWidth();
    }
    
    public int getHeight() {
        return image.getHeight();
    }
    
    public Dimension getMaximumSize() {
        return new Dimension(getWidth(),getHeight());
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(),getHeight());
    }
    
    public Dimension getSize() {
        return new Dimension(getWidth(),getHeight());
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);                
    }
    
    public void start() {
        workingImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        raster = workingImage.getRaster();
    }
    
    public void done() {
        image = workingImage;
        raster = null;
        this.repaint();
    }
    
    public void change(int p,int size,int dl_w, int dl_h, NeuronRow row) {
        int[] data = new int[row.getSize()];
        for (int i = 0; i < row.getSize(); i++) {
            data[i] = row.getNeuron(i).getOutput() > 0.5 ? 1 : 0;
        }
        
        raster.setPixels((p % dl_w) * size , (p / dl_w)  * size, size, size, data);
    }
}
