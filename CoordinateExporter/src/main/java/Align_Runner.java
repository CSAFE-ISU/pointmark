import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.io.FileInfo;
import ij.io.TiffEncoder;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mpicbg.ij.InvertibleTransformMapping;
import mpicbg.models.PointMatch;
import mpicbg.models.SimilarityModel2D;
import org.ahgamut.clqmtch.StackDFS;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Align_Runner implements PlugIn {

    private final JPanel panel;
    private final JTextArea dummy;

    private final HashMap<String, ImagePlus> imgmap;
    private final JComboBox<String> Q_imgs;
    private final JComboBox<String> K_imgs;
    private final JFormattedTextField minRatioT;
    private final JFormattedTextField maxRatioT;
    private final JFormattedTextField deltaT;
    private final JFormattedTextField epsilonT;
    private final JFormattedTextField lowerBoundT;
    private final JCheckBox viewOverlay;
    private final JCheckBox viewScores;
    private final JLabel Qimg_points;
    private final JLabel Kimg_points;

    private final JButton overlaySaveButton;
    private final JTextArea overlayPath;

    private String targ_zip;
    private boolean uiLoaded;


    public Align_Runner() {
        this.panel = new JPanel(new GridLayout(8, 4));
        this.dummy = new JTextArea();
        this.imgmap = new HashMap<>();
        this.Q_imgs = new JComboBox<>();
        Q_imgs.setEditable(false);
        this.Qimg_points = new JLabel();
        this.K_imgs = new JComboBox<>();
        K_imgs.setEditable(false);
        this.Kimg_points = new JLabel();
        this.minRatioT = new JFormattedTextField(NumberFormat.getInstance());
        this.maxRatioT = new JFormattedTextField(NumberFormat.getInstance());
        this.deltaT = new JFormattedTextField(NumberFormat.getInstance());
        this.epsilonT = new JFormattedTextField(NumberFormat.getInstance());
        this.lowerBoundT = new JFormattedTextField(NumberFormat.getInstance());
        this.viewOverlay = new JCheckBox();
        this.viewScores = new JCheckBox();
        this.overlayPath = new JTextArea();
        this.overlaySaveButton = new JButton("Select ZIP Filename");
        this.uiLoaded = false;
        this.targ_zip = "";
        loadUI();
    }

    public static void callFromMacro() {
        Align_Runner x = new Align_Runner();
        x.run("");
    }

    void cannotStart() {
        dummy.setText("you need to have 2 images open!");
        uiLoaded = false;
    }

    void missingMarkup(ImagePlus img) {
        dummy.setText("The Image: " + img.getShortTitle() + " is not loaded properly!");
        uiLoaded = false;
    }

    boolean UICheck() {
        int[] idList = WindowManager.getIDList();
        if (idList == null || idList.length < 2) {
            cannotStart();
            return false;
        }
        for (int id : idList) {
            ImagePlus img = WindowManager.getImage(id);
            if (img == null) {
                cannotStart();
                return false;
            }
            PolygonRoi pol = (PolygonRoi) img.getProperty("bounds");
            PointRoi pts = (PointRoi) img.getProperty("points");
            if (pol == null || pts == null) {
                missingMarkup(img);
                return false;
            }
        }
        return true;
    }

    void loadUI() {
        if (!UICheck()) {
            panel.add(dummy);
            return;
        }

        int[] idList = WindowManager.getIDList();
        ImagePlus tmp;
        for (int id : idList) {
            tmp = WindowManager.getImage(id);
            if (tmp.getProperty("points") == null) continue;
            if (tmp.getProperty("bounds") == null) continue;
            imgmap.put(tmp.getShortTitle(), tmp);
            Q_imgs.addItem(tmp.getShortTitle());
            K_imgs.addItem(tmp.getShortTitle());
        }
        panel.add(new JLabel("Trying to Compare:"));
        panel.add(Q_imgs);
        panel.add(new JLabel("and"));
        panel.add(K_imgs);

        panel.add(new JLabel(""));
        panel.add(Qimg_points);
        panel.add(new JLabel(""));
        panel.add(Kimg_points);

        panel.add(new JLabel("Scale difference is around:"));
        panel.add(minRatioT);
        panel.add(new JLabel("and"));
        panel.add(maxRatioT);

        panel.add(new JLabel("Maximum Angular Distortion"));
        panel.add(deltaT);
        panel.add(new JLabel("degrees"));
        panel.add(new JLabel(""));

        panel.add(new JLabel("Maximum Scaling Distortion"));
        panel.add(epsilonT);
        panel.add(new JLabel("units"));
        panel.add(new JLabel(""));

        panel.add(new JLabel("Must Have At Least"));
        panel.add(lowerBoundT);
        panel.add(new JLabel("points"));
        panel.add(new JLabel("in common"));

        panel.add(overlaySaveButton);
        panel.add(overlayPath);
        panel.add(new JLabel("View Overlay?"));
        panel.add(viewOverlay);

        panel.add(new JLabel("View Similarity Score?"));
        panel.add(viewScores);

        overlayPath.setEnabled(false);
        overlayPath.setText(targ_zip);
        loadReactions();
        uiLoaded = true;
    }

    void loadReactions() {
        minRatioT.setText("0.8");
        maxRatioT.setText("1.2");
        deltaT.setText("0.1");
        epsilonT.setText("0.03");
        lowerBoundT.setText("10");
        viewOverlay.setSelected(true);
        viewScores.setSelected(false);
        viewScores.setEnabled(false);
        Q_imgs.setSelectedIndex(0);
        K_imgs.setSelectedIndex(1);

        Q_imgs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImagePlus z;
                PointRoi r;
                z = imgmap.get(Q_imgs.getSelectedItem());
                r = (PointRoi) z.getProperty("points");
                Qimg_points.setText(r.size() + " points");
            }
        });

        Q_imgs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getNumPoints(imgmap.get(Q_imgs.getSelectedItem()), Qimg_points);
            }
        });

        K_imgs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getNumPoints(imgmap.get(K_imgs.getSelectedItem()), Kimg_points);
            }
        });

        overlaySaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("ZIP file to save data in", "zip"));
                int returnValue = chooser.showSaveDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    overlayPath.setText(file.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(null, "Did not select a File!");
                    overlayPath.setText("");
                }
            }
        });

        this.getNumPoints(imgmap.get(K_imgs.getSelectedItem()), Kimg_points);
        this.getNumPoints(imgmap.get(Q_imgs.getSelectedItem()), Qimg_points);
    }

    void getNumPoints(ImagePlus img, JLabel targ) {
        if (img == null) return;
        PointRoi r = (PointRoi) img.getProperty("points");
        targ.setText(r.size() + " points");
    }

    public void run(String arg) {
        int p = JOptionPane.showConfirmDialog(null, this.panel,
                "Save Image + Markup", JOptionPane.OK_CANCEL_OPTION);
        if (!uiLoaded || p == JOptionPane.CANCEL_OPTION) return;
        if (overlayPath.getText() == null) return;
        if (overlayPath.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No ZIP savefile provided!");
            return;
        }
        targ_zip = overlayPath.getText();
        runWithProgress();
    }

    void runWithProgress() {
        ImagePlus q_img = imgmap.get(Q_imgs.getSelectedItem());
        ImagePlus k_img = imgmap.get(K_imgs.getSelectedItem());
        Point[] q_pts = ((PointRoi) q_img.getProperty("points")).getContainedPoints();
        Point[] k_pts = ((PointRoi) k_img.getProperty("points")).getContainedPoints();
        double delta = Double.parseDouble(deltaT.getText());
        double epsilon = Double.parseDouble(epsilonT.getText());
        double min_ratio = Double.parseDouble(minRatioT.getText());
        double max_ratio = Double.parseDouble(maxRatioT.getText());
        int lower_bound = Integer.parseInt(lowerBoundT.getText());
        boolean showOverlay = viewOverlay.isSelected();
        if (!targ_zip.endsWith(".zip")) {
            targ_zip += ".zip";
        }

        String[] works = {"Starting...",
                "Checking scales...",
                "Aligning...",
                "Calculating similarity scores...",
                "Cleaning up..."};
        int[] progressLevel = {5, 25, 50, 75, 99};
        final int[] i = {0};

        Thread work = new Thread(new Runnable() {
            @Override
            public void run() {
                java.util.ArrayList<Integer> c;
                ArrayList<PointMatch> corr = new ArrayList<>();
                mpicbg.models.SimilarityModel2D tfunc = new SimilarityModel2D();
                double[][] qc;
                double[][] kc;
                double[][] q0 = new double[q_pts.length][2];
                double[][] k0 = new double[k_pts.length][2];

                try {
                    for (int j = 0; j < q_pts.length; ++j) {
                        q0[j][0] = q_pts[j].getX();
                        q0[j][1] = q_pts[j].getY();
                    }

                    for (int j = 0; j < k_pts.length; ++j) {
                        k0[j][0] = k_pts[j].getX();
                        k0[j][1] = k_pts[j].getY();
                    }

                    /* find max clique (TODO: lower_bound) */
                    i[0] += 1;
                    System.out.println("max clique");
                    c = this.get_clique();

                    /* find transform fit */
                    i[0] += 1;
                    qc = new double[c.size()][2];
                    kc = new double[c.size()][2];
                    for (int j = 0; j < c.size(); ++j) {
                        int z = c.get(j);
                        qc[j][0] = q_pts[z / k_pts.length].getX();
                        qc[j][1] = q_pts[z / k_pts.length].getY();
                        kc[j][0] = k_pts[z % k_pts.length].getX();
                        kc[j][1] = k_pts[z % k_pts.length].getY();
                        corr.add(new PointMatch(
                                new mpicbg.models.Point(kc[j]),
                                new mpicbg.models.Point(qc[j])
                        ));
                    }
                    Thread.sleep(250);
                    i[0] += 1;

                    System.out.println("fitting tform...");
                    tfunc.fit(corr);

                    ImagePlus rimg = createOverlay(tfunc, q0, k0);
                    System.out.println("saving...");
                    if (!saveOverlay(rimg.getImageStack())) {
                        throw new IOException("unable to save zip");
                    }
                    rimg.show();
                    i[0] += 1;
                } catch (Exception e) {
                    System.out.println("failed: " + e.getMessage() + " " + e);
                    e.printStackTrace();
                    i[0] = -1;
                }
            }

            ArrayList<Integer> get_clique() {
                Mapper3 x = new Mapper3();
                org.ahgamut.clqmtch.Graph g = x.construct_graph(q_pts, q_pts.length, k_pts, k_pts.length,
                        delta, epsilon, min_ratio, max_ratio);
                i[0] += 1;
                org.ahgamut.clqmtch.StackDFS s = new StackDFS();
                s.process_graph(g); /* warning is glitch */
                System.out.println(g.toString());
                return g.get_max_clique();
            }

            ImagePlus createOverlay(mpicbg.models.SimilarityModel2D tfunc, double[][] qc, double[][] kc) {
                ij.ImageStack res = new ImageStack();
                ij.ImageStack q_stack = q_img.getImageStack();
                ij.ImageStack k_stack = k_img.getImageStack();
                mpicbg.ij.InvertibleTransformMapping<SimilarityModel2D> tform = new InvertibleTransformMapping<>(tfunc);

                PointRoi qp1 = new PointRoi();
                PointRoi kp1 = new PointRoi();

                double[] z;
                for (double[] pt : qc) {
                    qp1.addPoint(pt[0], pt[1]);
                }
                for (double[] pt : kc) {
                    z = tfunc.apply(pt);
                    kp1.addPoint(z[0], z[1]);
                }

                /* transform images via fit */
                ImagePlus tq = new ImagePlus();
                tq.setProcessor(q_stack.getProcessor(1));
                ImageProcessor q1 = colorify(tq).getProcessor();

                tq.setProcessor(q_stack.getProcessor(2));
                ImageProcessor q2 = burnPoints(tq, qp1, kp1).getProcessor().duplicate();

                ImageProcessor k1 = k_stack.getProcessor(1).createProcessor(q1.getWidth(), q1.getHeight());
                tform.mapInterpolated(k_stack.getProcessor(1), k1);
                tq.setProcessor(k1);
                k1 = colorify(tq).getProcessor();

                res.addSlice(q1);
                res.addSlice(k1);
                res.addSlice(q2);
                return new ImagePlus("Overlay", res);
            }

            boolean saveOverlay(ImageStack ov_stack) {
                System.out.println("Saving to " + targ_zip);
                DataOutputStream out;
                FileInfo info;
                ImagePlus img;
                TiffEncoder te;
                try {
                    ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(targ_zip)));
                    out = new DataOutputStream(new BufferedOutputStream(zos, 4096));
                    img = new ImagePlus("", ov_stack.getProcessor(1));
                    info = img.getFileInfo();
                    zos.putNextEntry(new ZipEntry("image_1.tiff"));
                    te = new TiffEncoder(info);
                    te.write(out);

                    img = new ImagePlus("", ov_stack.getProcessor(2));
                    info = img.getFileInfo();
                    zos.putNextEntry(new ZipEntry("image_2.tiff"));
                    te = new TiffEncoder(info);
                    te.write(out);

                    img = new ImagePlus("", ov_stack.getProcessor(3));
                    info = img.getFileInfo();
                    zos.putNextEntry(new ZipEntry("aligned_points.tiff"));
                    te = new TiffEncoder(info);
                    te.write(out);

                    out.close();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            ImagePlus colorify(ImagePlus imp) {
                BufferedImage bi = new BufferedImage(imp.getWidth(), imp.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) bi.getGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawImage(imp.getImage(), 0, 0, null);
                return new ImagePlus("", new ColorProcessor(bi));
            }

            ImagePlus burnPoints(ImagePlus imp, PointRoi q_pts, PointRoi k_pts) {
                BufferedImage bi = new BufferedImage(imp.getWidth(), imp.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) bi.getGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawImage(imp.getImage(), 0, 0, null);
                g.setStroke(new BasicStroke(16.5F));
                g.setPaint(Color.RED);
                for (Point p : q_pts) {
                    g.drawOval(p.x, p.y, 75, 75);
                }
                g.setStroke(new BasicStroke(18.5F));
                g.setPaint(Color.BLUE);
                for (Point p : k_pts) {
                    g.drawRect(p.x, p.y, 53, 53);
                }
                return new ImagePlus("", new ColorProcessor(bi));
            }
        });

        JFrame frame = new JFrame();
        JPanel subpanel = new JPanel(new GridLayout(2, 1));
        JProgressBar bar = new JProgressBar();
        JLabel currentWork = new JLabel();
        subpanel.add(currentWork);
        subpanel.add(bar);
        frame.setContentPane(subpanel);
        frame.setSize(320, 240);

        Thread ui = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (i[0] >= 0 && i[0] < works.length) {
                        currentWork.setText(works[i[0]]);
                        bar.setValue(progressLevel[i[0]]);
                        Thread.sleep(275);
                    }
                    frame.setVisible(false);
                    System.out.println("complete");
                } catch (Exception ignored) {
                }
            }
        });

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        ui.start();
        work.start();
    }
}
