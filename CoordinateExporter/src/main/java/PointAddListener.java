import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * Class to reverse changes in position of points caused by the user. Essentially, does not allow
 * for points to be moved while the plugin is running in order to decrease human error.
 */
public class PointAddListener extends PointListener {

    int invalidROINum;

    int invalidCount;

    PointRoi invalidROI;

    PointAddListener sister;

    public PointAddListener(JLabel log, ImagePlus img, int num) {
        super(log, img);
        this.invalidROINum = num;
        invalidCount = 0;
    }

    @Override
    public void mousePressed(MouseEvent e) { //This doesn't seem to work with mouseDragged.

        int selected = roiManager.getSelectedIndex();

        if (selected != invalidROINum) {
            //To update the values to be checked as invalid when the other image's ROI is added to
            updateSisterInvalid(selected);
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
        }

        resetROI(invalidROI, invalidROINum);

        resetImage(selected);
        log.setText("Log: No adding points to another ROI!");

    }

    public void initInvalidROI() {
        invalidROI = (PointRoi)roiManager.getRoi(invalidROINum == 0 ? 0 : 1);
        invalidCount = invalidROI.getSize();
    }

    public void addSister(PointListener sister) {
        if (!this.getClass().equals(sister.getClass())) {
            return;
        }
        this.sister = (PointAddListener)sister;
    }

    public void updateSisterInvalid(int selected) {
        sister.invalidCount = roiManager.getRoi(selected).size();
    }
}
