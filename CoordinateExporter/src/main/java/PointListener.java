import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * Abstract class for listeners relating to points applied to an image via the ROI Manager.
 */
public abstract class PointListener extends MouseAdapter {

    protected RoiManager roiManager;

    protected ImagePlus img;

    protected JLabel log;

    public PointListener(JLabel log, ImagePlus img) {
        roiManager = RoiManager.getRoiManager();
        this.img = img;
        this.log = log;
    }

    public void resetROI(PointRoi pointRoi, int selected) {
        if (selected == 0) {
            pointRoi.setStrokeColor(new Color(0, 0, 255));
            pointRoi.setPointType(2);
            pointRoi.setSize(3);
            pointRoi.setName("set_1");
        } else {
            pointRoi.setStrokeColor(new Color(255, 255, 0));
            pointRoi.setPointType(2);
            pointRoi.setSize(3);
            pointRoi.setName("set_2");
        }

        roiManager.setRoi(pointRoi, selected);
    }

    public void resetImage(int selected) {
        img.updateAndDraw();
        roiManager.select(selected);
    }

    public abstract void addSister(PointListener sister);

}
