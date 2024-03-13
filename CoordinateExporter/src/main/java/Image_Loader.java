import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Image_Loader implements PlugIn {
    private final JButton imgLoadButton;
    private final JTextArea imgPath;
    private final JCheckBox markupAvailable;
    private final JButton markupLoadButton;
    private final JTextArea markupPath;
    private final JPanel panel;
    private final JFileChooser chooser;
    ImagePlus img;
    private boolean img_valid;
    private boolean markup_valid;

    public Image_Loader() {
        this.panel = new JPanel(new GridLayout(6, 2));
        this.chooser = new JFileChooser();

        this.imgLoadButton = new JButton();
        imgLoadButton.setText("Load Image...");
        this.imgPath = new JTextArea();
        imgPath.setEditable(false);
        imgPath.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        this.markupAvailable = new JCheckBox();
        markupAvailable.setText("I have already marked up this image");
        this.markupLoadButton = new JButton();
        markupLoadButton.setText("Markup File Location...");
        this.markupPath = new JTextArea();
        markupPath.setEditable(false);
        markupPath.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        this.img = null;
        this.loadUI();
        this.loadReactions();
    }

    public static void callFromMacro() {
        Image_Loader x = new Image_Loader();
        x.run("");
    }

    private void loadUI() {
        panel.add(new JLabel());
        panel.add(new JLabel());
        panel.add(imgLoadButton);
        panel.add(imgPath);
        panel.add(markupAvailable);
        panel.add(new JLabel());
        panel.add(markupLoadButton);
        panel.add(markupPath);
        markupAvailable.setSelected(false);
        markupLoadButton.setEnabled(false);
        markupPath.setEnabled(false);
    }

    private void loadReactions() {
        markupAvailable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (markupAvailable.isSelected()) {
                    markupPath.setEnabled(true);
                    markupLoadButton.setEnabled(true);
                } else {
                    markupPath.setEnabled(false);
                    markupLoadButton.setEnabled(false);
                }
            }
        });

        markupLoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String validPath = checkFileLoad("json", "txt");
                if (validPath == null || validPath.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Invalid File!");
                    markup_valid = false;
                } else {
                    markupPath.setText(validPath);
                    markup_valid = true;
                }
            }
        });

        imgLoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String validPath = checkFileLoad("tiff", "jpg", "png");
                if (validPath == null || validPath.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Invalid File!");
                    img_valid = false;
                } else {
                    imgPath.setText(validPath);
                    img_valid = true;
                }
            }
        });

    }

    String checkFileLoad(String... fileTypes) {
        chooser.setFileFilter(new FileNameExtensionFilter("Load Image Data", fileTypes));
        int returnValue = chooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            return file.getAbsolutePath();
        }
        return "";
    }

    public void run(String arg) {
        int p = JOptionPane.showConfirmDialog(null, this.panel,
                "Load Image and Markup", JOptionPane.OK_CANCEL_OPTION);
        if (p == JOptionPane.CANCEL_OPTION) return;
        if (!img_valid) return;
        ImagePlus tmp = IJ.openImage(imgPath.getText());
        tmp.show();

        boolean mark_bounds;
        PolygonRoi pol = null;
        PointRoi pts;

        if (markup_valid) {
            try {
                MarkupData m = MarkupData.fromFile(markupPath.getText());
                if (m.nbounds != 0) {
                    pol = m.getBoundsAsRoi();
                    mark_bounds = true;
                } else {
                    this.showBoundsHelper(tmp);
                    mark_bounds = false;
                }
                if (m.npoints != 0) {
                    pts = m.getPointsAsRoi();
                } else {
                    pts = new PointRoi();
                }
            } catch (Exception e) {
                // might need to show an error here
                System.out.println("unable to read JSON!");
                e.printStackTrace();
                this.img.close();
                return;
            }
        } else {
            // if we didn't load a JSON,
            // we still need to mark bounds
            mark_bounds = false;
            pts = new PointRoi();
        }

        if (!mark_bounds) {
            pol = this.showBoundsHelper(tmp);
        }
        if (pol == null || pts == null) {
            return;
        }
        tmp.setRoi(pol);
        ImageStack overlay = new ImageStack();
        ImageProcessor marked = tmp.getProcessor().duplicate();
        ImageProcessor mask = tmp.createRoiMask();
        marked.fillOutside(pol);
        overlay.addSlice(marked);
        overlay.addSlice(mask);
        overlay.addSlice(tmp.getProcessor());

        this.img = new ImagePlus(tmp.getShortTitle(), overlay);
        tmp.close();

        pts.setSize(3);
        pts.setFillColor(Color.RED);
        pts.setStrokeColor(Color.RED);
        img.setProperty("points", pts);
        img.setProperty("bounds", pol);
        img.setRoi(pts);
        ij.IJ.setTool("Multi-Point");
        img.show();
    }

    PolygonRoi showBoundsHelper(ImagePlus tmp) {
        final Object obj = new Object();
        ij.IJ.setTool(Toolbar.POLYGON);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JDialog d = new JDialog();
                JPanel subpanel = new JPanel(new GridLayout(1, 1));
                JTextArea b = new JTextArea();
                b.setText("Close Window afer marking bounds");
                b.setEditable(false);
                subpanel.add(b);
                d.setContentPane(subpanel);
                d.setTitle("Mark Boundary");
                d.pack();
                d.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        synchronized (obj) { // can lock when worker thread releases with wait
                            obj.notify(); // signals wait
                        }
                        super.windowClosed(e);
                    }
                });
                d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                d.setVisible(true);
            }
        };
        try {
            synchronized (obj) {
                SwingUtilities.invokeLater(r);
                obj.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        PolygonRoi pol = (PolygonRoi) tmp.getRoi();
        return pol;
    }
}
