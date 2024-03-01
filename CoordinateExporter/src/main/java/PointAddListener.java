import ij.ImagePlus;
import ij.gui.PointRoi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Class to reverse changes in position of points caused by the user. Essentially, does not allow
 * for points to be moved while the plugin is running in order to decrease human error.
 */
public class PointAddListener extends PointListener {

    int invalidROINum;

    int invalidCount;

    PointRoi invalidROI;

    Point[] originalPoints;

    PointAddListener sister;

    public PointAddListener(JLabel log, ImagePlus img, int num) {
        super(log, img);
        this.invalidROINum = num;
        invalidCount = 0;
        originalPoints = new Point[0];
    }

    @Override
    public void mousePressed(MouseEvent e) { //This doesn't seem to work with mouseDragged.

        int selected = roiManager.getSelectedIndex();

        if (selected != invalidROINum) {
            //To update the values to be checked as invalid when the other image's ROI is added to
            updateSisterInfo(selected);
            updatePoints();
            return;
        }

        Point[] points = invalidROI.getContainedPoints();
        //Catches the first instance of a user trying to edit the wrong ROI.
        if (invalidCount < points.length) {
            PointRoi invalidNew = new PointRoi();
            for (int i = 0; i < points.length - 1; i++) {
                invalidNew.addPoint(points[i].getX(), points[i].getY());
            }
            invalidROI = invalidNew;
        } else if (invalidCount > points.length) {
            PointRoi invalidNew = new PointRoi();
            Point[] sisterPoints = sister.originalPoints;
            for (int i = 0; i < sisterPoints.length; i++) {
                invalidNew.addPoint(sisterPoints[i].getX(), sisterPoints[i].getY());
            }
            invalidROI = invalidNew;
        }

        resetROI(invalidROI, invalidROINum);

        resetImage(selected);
        log.setText("Log: No adding points to another ROI!");

    }

    public void initInvalidROI() {
        invalidROI = (PointRoi) roiManager.getRoi(invalidROINum == 0 ? 0 : 1);
        invalidCount = invalidROI.getContainedPoints().length;
    }

    public void addSister(PointListener sister) {
        if (!this.getClass().equals(sister.getClass())) {
            return;
        }
        this.sister = (PointAddListener) sister;
    }

    public void updateSisterInfo(int selected) {
        sister.invalidCount = roiManager.getRoi(selected).size();
    }

    public void updatePoints() {
        int selected = invalidROINum == 0 ? 1 : 0;
        originalPoints = roiManager.getRoi(selected).getContainedPoints();
    }
}
