/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kachliky;

import cz.matfyz.sykora.nngui.data.DataProvider;
import cz.matfyz.sykora.nngui.data.RandomizedAdaptionProvider;
import cz.matfyz.sykora.nngui.network.AdaptationStrategy;
import cz.matfyz.sykora.nngui.network.Network;
import cz.matfyz.sykora.nngui.network.NetworkBuilder;
import cz.matfyz.sykora.nngui.network.Neuron;
import cz.matfyz.sykora.nngui.network.NeuronRow;
import cz.matfyz.sykora.nngui.network.SigmoidModel;
import cz.matfyz.sykora.nngui.network.SimpleBackPropagation;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import kachliky.components.ImageComp;
import plugins.AnimPlugin;

/**
 *
 * @author kirrie
 */
public class Animation extends AnimPlugin {

    private static final String name = "Kachlikovanie verzia 1";
    private static String imgFile = "d:/cvicenia/utils/Kachliky/res/image2.gif";
    private static Logger LOG = Logger.getLogger(Animation.class.getName());
    
    public static JFrame frame;  
    public static ImageComp image1Panel,image2Panel;
    public static JTextArea text;
    public static JTextField learnField,hiddenField,iterField;
    public static JList<ImageComp> listofImages;
    public static JButton train,resolve,setBest;
    public static BufferedImage img;
    
    public static MyProvider inpProvider,outProvider;
    public static ArrayList<DoubleContainer> weightRows;
    public static Network net;
    public static AdaptationStrategy strategy;
    
    public static double globalError;    
    public static int dlazdic_w, dlazdic_h, dlazdic_count;
    public static int size = 10, inpOutNeurons;
    public static double learningRate = 1;
    public static int defIter = 100;
    public static String hiddenFieldString = "10";
    
    class ImageModel implements ListCellRenderer<ImageComp> {

        @Override
        public Component getListCellRendererComponent(JList<? extends ImageComp> list, ImageComp value, int index, boolean isSelected, boolean cellHasFocus) {            
            return value;
        }
                                     
    }
    
    static class DoubleContainer {
        
        private double[] doubleAr;
        private double doubleI;
        
        public DoubleContainer(double doubleI) {
            this.doubleI = doubleI;
        }
        
        public DoubleContainer(double[] doubleAr) {
            this.doubleAr = doubleAr;
        }
        
    }
    
    class Training implements Runnable {

        @Override
        public void run() {
            BufferedImage imaag = ((ImageComp)image1Panel).getImage();        

            text.setText("");

            dlazdic_w = imaag.getWidth() / size;
            dlazdic_h = imaag.getHeight() / size;
            inpOutNeurons = size * size;
            // Ziskanie learnin rate z edit prvku
            learningRate = Double.parseDouble(learnField.getText());
            // Ziskanie pocet iteracii
            defIter = Integer.parseInt(iterField.getText());        
            // Rozparsovanie vstupnych parametrov pre architekturu siete        
            String[] parsedLayers = hiddenField.getText().split(":");                
            int[] network = new int[parsedLayers.length + 2];
            for (int i = 0; i<network.length; i++) {
                network[i] = i==0 || i==network.length-1 ? inpOutNeurons : Integer.parseInt(parsedLayers[i-1]);
            }

            // Postavenie siete
            NetworkBuilder builder = new NetworkBuilder();                        
            net = builder.createNetwork(network, new SigmoidModel());
            strategy = new SimpleBackPropagation(learningRate);

            LOG.log(Level.INFO, "Learning with " + learningRate);
            LOG.log(Level.INFO, "Hidden layers have " + hiddenField.getText() + " neurons");

            if(net == null) {
                LOG.log(Level.SEVERE,"network creation has failed");
            }

            dlazdic_count = dlazdic_h * dlazdic_w;
            double[][] dlazdica = new double[dlazdic_count][size * size];                

            for (int i = 0; i < imaag.getHeight(); i=i+size) {
                for (int j = 0; j < imaag.getWidth(); j=j+size) {
                    dlazdica[(i/size)*dlazdic_w + j/size] = imaag.getData().getPixels(j,i,size,size,new double[size * size]);                                                                                                                          
                }
            }

            inpProvider = new MyProvider(dlazdica);
            outProvider = new MyProvider(dlazdica);            

            try {
                net.setInputProvider(inpProvider);
                net.setDesiredOutputProvider(outProvider);
            } catch (Exception e) {

            }           

            try {            
                int iterations = defIter;                            
                double er=0d; 
                double min_er = -1d;
                weightRows = new ArrayList<>();
                int it = 0;
                
                // Pred prvou iteraciou
                er = getGlobalError(inpProvider, outProvider, net);
                text.append(0 + "-th iteration :::: " + er + " error\n"); 
                if (min_er > er || min_er == -1) {
                    it = 0;
                    min_er = er; 
                    weightRows.clear();
                    for (int row = 0; row < net.getRowCount(); row++) {
                        NeuronRow n_row = net.getRow(row);
                        for (int neuron = 0; neuron < n_row.getSize(); neuron++) {
                            Neuron n_neuron = n_row.getNeuron(neuron);
                            if (row == 0) {
                                weightRows.add(new DoubleContainer(n_neuron.getThreshold()));
                                continue;
                            }
                            weightRows.add(new DoubleContainer(n_neuron.getWeights()));
                            weightRows.add(new DoubleContainer(n_neuron.getThreshold()));
                        }
                    }
                }
                
                for ( int k = 0; k < iterations; k++) {                                                            
                    inpProvider.rewind();
                    outProvider.rewind();
                    //double error = 0;                    
                    for (int p = 0; p < dlazdic_count; p++) {                        
                        try {
                            for (int iter = 0; iter < 2; iter++) {
                                //net.calculateSingle(); 
                                /*
                                error = 0;
                                for(int i = 0; i < inpOutNeurons; i++) 
                                    error += Math.pow(net.getRow(net.getRowCount()-1).getNeuron(i).getOutput()-inpProvider.getValue(i),2);
                                */
                                net.adaptSingle(strategy);                                  
                            }
                            inpProvider.incIndex();
                            outProvider.incIndex();
                        } catch (Exception e) {

                        }
                        
                    }
                    
                    image2Panel.start(); 
                    inpProvider.rewind();
                    outProvider.rewind();
                    for (int p = 0; p < dlazdic_count; p++) {
                        net.calculateSingle();
                        image2Panel.change(p,size, dlazdic_w, dlazdic_h,net.getRow(net.getRowCount()-1));
                        inpProvider.incIndex();
                        outProvider.incIndex();
                    }
                    
                    image2Panel.done();
                    inpProvider.rewind();
                    outProvider.rewind();
                    
                    er = getGlobalError(inpProvider, outProvider, net);
                    text.append(k + 1 + "-th iteration :::: " + er + " error\n"); 
                    if (min_er > er || min_er == -1) {
                        it = k + 1;
                        min_er = er; 
                        weightRows.clear();
                        for (int row = 0; row < net.getRowCount(); row++) {
                            NeuronRow n_row = net.getRow(row);
                            for (int neuron = 0; neuron < n_row.getSize(); neuron++) {
                                Neuron n_neuron = n_row.getNeuron(neuron);
                                if (row == 0) {
                                    weightRows.add(new DoubleContainer(n_neuron.getThreshold()));
                                    continue;
                                }
                                weightRows.add(new DoubleContainer(n_neuron.getWeights()));
                                weightRows.add(new DoubleContainer(n_neuron.getThreshold()));
                            }
                        }
                    }
                    
                }

                text.append("Best --> "+ it + "-th iteration :::: " + min_er + " error\n"); 
                

            } catch (Exception e) {

            }
        }
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    showGui();
                }
                
            });
        } catch (Exception e) {
            
        }
    }

    private static void showGui() {
        frame = new JFrame("Kachliky");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
        frame.setPreferredSize(new Dimension(800,600));        
                
        try {
            Animation.class.newInstance().setPanel(frame.getContentPane());                
        } catch (Exception e) {
            
        }
        frame.setVisible(true);
        frame.pack();                
    }
    
    private static void value() {
        Raster r = image2Panel.image.getRaster();
        boolean same = false;        
        DefaultListModel<ImageComp> model = new DefaultListModel<>();        
        
        
        int count = 0;
        
        for (int dl_h = 0;dl_h < dlazdic_h; dl_h++)
            for (int dl_w = 0;dl_w < dlazdic_w; dl_w++) {
                
                // Raster pre dlazdicu z obrazka
                int[] imgPixels = r.getPixels(dl_w * size, dl_h * size, size, size, new int[inpOutNeurons]);
                                
                for (int i = 0; i<model.getSize(); i++) {
                    if (model.get(i) instanceof ImageComp) {
                        same = true;
                        // Raster pre dlazdicu z komponenty
                        int[] compPixels = model.get(i).image.getRaster().getPixels(0, 0, size, size, new int[inpOutNeurons]);
                        for (int y = 0; y < size; y++) {
                            for (int x = 0; x < size; x++) {
                                if (compPixels[y*size+x] != imgPixels[y*size+x]) {
                                    same = false;                                    
                                    break;
                                }
                            }
                            if (!same) {
                                break;                                
                            }                                                                                                                                           
                        }
                        
                        if (same) {
                            break;
                        }
                    }
                    
                }
                
                if (!same) {
                    ImageComp newComp = new ImageComp(size, size);
                    
                    WritableRaster writRaster = newComp.image.getRaster();
                    writRaster.setPixels(0, 0, size, size, imgPixels);
                    
                    model.addElement(newComp);                    
                }
                
            }
            listofImages.setModel(model);    
            listofImages.updateUI();
    }
    
    private static void resolve() {
        inpProvider.rewind();
        outProvider.rewind();
        image2Panel.start();
        for (int p = 0; p < dlazdic_count; p++) {                        
            try {                
                net.calculateSingle();                                     
                inpProvider.incIndex();
                outProvider.incIndex();
            } catch (Exception e) {

            }
            
            image2Panel.change(p,size, dlazdic_w, dlazdic_h,net.getRow(net.getRowCount()-1));
            
        }
        image2Panel.done();
    }
    
    private static double getGlobalError(MyProvider inpProvider, MyProvider outProvider, Network net) {
        double er = 0;
        try {
            while (net.calculateSingle()) {
                for(int i = 0; i < inpOutNeurons; i++) 
                    er += Math.pow(net.getRow(net.getRowCount()-1).getNeuron(i).getOutput()-inpProvider.getValue(i),2);
                inpProvider.incIndex();
                outProvider.incIndex();
            }
        } catch (Exception e) {
            return er;
        }
        return er;
    }        

    @Override
    public void animate() {
        
    }

    public void setBest() {
        if (net == null || weightRows == null) {
            return;
        }
                
        Iterator<DoubleContainer> iterator = weightRows.iterator();
        
        for (int row = 0; row < net.getRowCount(); row++) {
            NeuronRow n_row = net.getRow(row);
            for (int neuron = 0; neuron < n_row.getSize(); neuron++) {
                Neuron n_neuron = n_row.getNeuron(neuron);
                if (row == 0) {
                    n_neuron.setThreshold(iterator.next().doubleI);
                    continue;
                }
                n_neuron.setWeights(iterator.next().doubleAr);
                n_neuron.setThreshold(iterator.next().doubleI);
            }
        } 
        
        System.out.println("Done");
                
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public void setPanel(Container panel) {
        this.panel = panel;
        
        try {
            img = ImageIO.read(new File(imgFile));
            BufferedImage sk = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            sk.getGraphics().drawImage(img, 0, 0, null);
            img = sk;
        } catch (IOException ex) {
            System.out.println("Not found");
            LOG.log(Level.SEVERE, null, ex);
        }                
        
        // Images
        image1Panel = new ImageComp(img);
        image2Panel = new ImageComp(img.getWidth(),img.getHeight());        
        
        // Texts
        text = new JTextArea();
        text.setEditable(false);
        text.setRows(5);        
        
        // Buttons
        train = new JButton("Start");
        train.addActionListener(new ActionListener() {                    
            
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Training()).start();
            }
        });
        resolve = new JButton("Resolve");
        resolve.addActionListener(new ActionListener() {                    
            
            @Override
            public void actionPerformed(ActionEvent e) {
                resolve();
                value();
            }
        });
        setBest = new JButton("Set Best");
        setBest.addActionListener(new ActionListener() {                    
            
            @Override
            public void actionPerformed(ActionEvent e) {
                setBest();
            }
        });
        
        // Image List
        listofImages = new JList<>(); 
        listofImages.setCellRenderer(new ImageModel());
        
        // Text Fields
        learnField = new JTextField("" +learningRate);
        learnField.setColumns(20);
        learnField.setToolTipText("Learning parameter");        
        hiddenField = new JTextField(hiddenFieldString);
        hiddenField.setColumns(20);
        hiddenField.setToolTipText("Network achitecture");
        iterField = new JTextField("" + defIter);
        iterField.setColumns(20);
        iterField.setToolTipText("Number of iterations");
        
        // Panels
        JPanel mainPanel = new JPanel(new BorderLayout(5,5));
        JPanel editFields = new JPanel(new BorderLayout(5,5));
        JPanel imagesPanel = new JPanel(new BorderLayout(5,5));
        JPanel btnPanel = new JPanel(new BorderLayout(5,5));
        JScrollPane pane = new JScrollPane(text);
        
        imagesPanel.add(image1Panel, BorderLayout.LINE_START);
        imagesPanel.add(image2Panel,BorderLayout.LINE_END);
        
        btnPanel.add(train,BorderLayout.PAGE_START);
        btnPanel.add(setBest,BorderLayout.CENTER);
        btnPanel.add(resolve,BorderLayout.PAGE_END);        
                
        editFields.add(new JScrollPane(listofImages,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.NORTH);
        editFields.add(learnField, BorderLayout.WEST);
        editFields.add(hiddenField, BorderLayout.EAST);
        editFields.add(iterField,BorderLayout.CENTER);
        
        mainPanel.add(imagesPanel, BorderLayout.NORTH);
        mainPanel.add(btnPanel, BorderLayout.WEST);
        mainPanel.add(editFields, BorderLayout.EAST);
        mainPanel.add(pane,BorderLayout.PAGE_END);
        panel.add(mainPanel,BorderLayout.CENTER);
    }
}
