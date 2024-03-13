import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.plugin.PlugIn;
import org.ahgamut.clqmtch.StackDFS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
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
                java.util.ArrayList<Integer> qc = new ArrayList<>();
                java.util.ArrayList<Integer> kc = new ArrayList<>();
                try {
                    Thread.sleep(750);
                    i[0] += 1;
                    g = x.construct_graph(q_pts, q_pts.length, k_pts, k_pts.length,
                            delta, epsilon, min_ratio, max_ratio);
                    System.out.println(g.toString());
                    i[0] += 1;
                    s.process_graph(g); /* warning is glitch */
                    i[0] += 1;
                    System.out.println("max clique");;
                    c = g.get_max_clique();
                    for (int z : c) {
                        qc.add(z / k_pts.length);
                        kc.add(z % k_pts.length);
                    }
                    System.out.println(c);
                    System.out.println(qc);
                    System.out.println(kc);
                    Thread.sleep(750);
                    i[0] += 1;
                    System.out.println("scoring/viz");
                    Thread.sleep(750);
                    System.out.println("saving...");
                    i[0] += 1;
                } catch (Exception e) {
                    System.out.println("failed" + e.getMessage());
                    i[0] = -1;
                }
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
