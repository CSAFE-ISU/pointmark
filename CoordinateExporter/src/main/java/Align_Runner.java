import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mpicbg.ij.InvertibleTransformMapping;
import mpicbg.models.PointMatch;
import mpicbg.models.SimilarityModel2D;
import org.ahgamut.clqmtch.StackDFS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
    private boolean uiLoaded;

    public Align_Runner() {
        this.panel = new JPanel(new GridLayout(7, 4));
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
        this.uiLoaded = false;
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

        panel.add(new JLabel("Image Overlay?"));
        panel.add(viewOverlay);
        panel.add(new JLabel("Similarity Score?"));
        panel.add(viewScores);

        loadReactions();
        uiLoaded = true;
    }

    void loadReactions() {
        minRatioT.setText("0.8");
        maxRatioT.setText("1.2");
        deltaT.setText("0.5");
        epsilonT.setText("0.01");
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
                Qimg_points.setText(r.size() + "points");
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


        System.out.printf("option was %d\n", p);

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
                Mapper3 x = new Mapper3();
                org.ahgamut.clqmtch.Graph g = null;
                org.ahgamut.clqmtch.StackDFS s = new StackDFS();
                java.util.ArrayList<Integer> c;
                double[][] qc = null;
                double[][] kc = null;
                ArrayList<PointMatch> corr = new ArrayList<>();
                mpicbg.models.SimilarityModel2D tfunc = new SimilarityModel2D();
                mpicbg.ij.InvertibleTransformMapping<SimilarityModel2D> tform = new InvertibleTransformMapping<>(tfunc);

                ij.ImageStack res = new ImageStack();
                ij.ImageStack q_stack = q_img.getImageStack();
                ij.ImageStack k_stack = k_img.getImageStack();

                double[][] params = new double[2][3];
                try {
                    Thread.sleep(750);
                    /* construct the graph */
                    i[0] += 1;
                    g = x.construct_graph(q_pts, q_pts.length, k_pts, k_pts.length,
                            delta, epsilon, min_ratio, max_ratio);
                    System.out.println(g.toString());

                    /* find max clique (TODO: lower_bound) */
                    i[0] += 1;
                    s.process_graph(g); /* warning is glitch */

                    /* find transform fit */
                    i[0] += 1;
                    System.out.println("max clique");
                    c = g.get_max_clique();
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
                    Thread.sleep(750);
                    i[0] += 1;
                    System.out.println("fitting tform...");
                    tfunc.fit(corr);
                    tfunc.toMatrix(params);
                    System.out.println(Arrays.deepToString(params));
                    System.out.println("post PLS");

                    /* overlay with transform fit */
                    PointRoi qp1 = new PointRoi();
                    PointRoi kp1 = new PointRoi();

                    double[] z = new double[2];
                    for (int j = 0; j < c.size(); j++) {
                        z[0] = kc[j][0];
                        z[1] = kc[j][1];
                        z = tfunc.apply(z);
                        System.out.println(Arrays.toString(z) + "->" + Arrays.toString(qc[j]));
                        qp1.addPoint(qc[j][0], qc[j][1]);
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
                    ImagePlus rimg = new ImagePlus("result", res);
                    rimg.show();
                    System.out.println("saving...");
                    i[0] += 1;
                } catch (Exception e) {
                    System.out.println("failed: " + e.getMessage() + " " + e);
                    e.printStackTrace();
                    i[0] = -1;
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
                g.setStroke(new BasicStroke(6.5F));
                g.setPaint(Color.RED);
                for (Point p : q_pts) {
                    g.drawOval(p.x, p.y, 75, 75);
                }
                g.setStroke(new BasicStroke(8.5F));
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
