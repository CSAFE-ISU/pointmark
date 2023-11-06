import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public abstract class PointListener extends MouseAdapter {

    protected RoiManager roiManager;

    protected ImagePlus img;

    protected JLabel log;

    public PointListener(JLabel log) {
        roiManager = RoiManager.getRoiManager();
        img = IJ.getImage();
        this.log = log;
    }

    public void resetROI(PointRoi pointRoi, int selected) {
        if (selected == 0) {
            pointRoi.setStrokeColor(new Color(0, 0, 255));
            pointRoi.setPointType(2);
            pointRoi.setSize(3);
            pointRoi.setName("valid");
        }
        else {
            pointRoi.setStrokeColor(new Color(255, 255, 0));
            pointRoi.setPointType(2);
            pointRoi.setSize(3);
            pointRoi.setName("invalid");
        }

        roiManager.setRoi(pointRoi, selected);
    }

    public void resetImage(int selected) {
        img.updateAndDraw();
        roiManager.select(selected);
    }

}
