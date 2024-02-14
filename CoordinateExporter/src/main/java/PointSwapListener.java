import ij.IJ;
import ij.ImagePlus;
import ij.Undo;
import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Listens for a double click, and will search out the nearest point within a certain range to swap
 * its ROI between the "valid" and "invalid" ROIs, or will swap the selected ROI.
 */
public class PointSwapListener extends PointListener implements KeyListener {

    private boolean qKeyPressed;

    PointSwapListener sister;

    int usable_ROI;

    public PointSwapListener(JLabel log, ImagePlus img, int roi) {
        super(log, img);
        qKeyPressed = false;
        usable_ROI = roi;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (qKeyPressed) {
            changeClosestPoint(e.getX(), e.getY());
        }
    }

    public void changeClosestPoint(int x, int y) {
        int selected = roiManager.getSelectedIndex();
        if (selected != usable_ROI) {return;}
        PointRoi pr = (PointRoi)roiManager.getRoi(selected);

        double magnification = img.getCanvas().getMagnification();
        int transX = img.getCanvas().getSrcRect().x;
        int transY = img.getCanvas().getSrcRect().y;

        x = (int)(x / magnification + transX);
        y = (int)(y / magnification + transY);

        if (pr == null || pr.size() == 0) {
            log.setText("Log: There is nothing here!");
            return;
        }

        log.setText("Log: Clicked at (" + x + ", " + y + ")\n");

        Point[] points = pr.getContainedPoints();
        Point closest = null;
        double closestDist = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < points.length; i++) {
            double xDiff = Math.abs(x - points[i].getX());
            double yDiff = Math.abs(y - points[i].getY());;
            double distance = Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
            if (closestDist > distance) {
                closest = points[i];
                closestDist = distance;
                index = i;
            }
        }

        log.setText("Closest distance: " + closestDist);

        //50 pixels is the standard limit for distance when the ImagePlus window is full-screen 100% size.
        if (closestDist > 50) {log.setText(log.getText() + "\n" + "No point close enough for selection!"); return;}

        //No direct way to delete point from ROI
        PointRoi newPR = new PointRoi();
        for (int i = 0; i < points.length; i++) {
            if (i != index) {
                newPR.addPoint(points[i].getX(), points[i].getY());
            }
        }

        resetROI(newPR, selected);

        //Now add the minDistance Point to the other Roi
        int otherIndex = (selected == 0) ? 1 : 0;
        PointRoi otherPR = (PointRoi) roiManager.getRoi(otherIndex);
        otherPR.addPoint(closest.getX(), closest.getY());
        roiManager.setRoi(otherPR, otherIndex);

        if (selected == 0) {
            log.setText("Valid: " + newPR.getContainedPoints().length + "     Invalid: "
                    + otherPR.getContainedPoints().length);
        }
        else {
            log.setText("Valid: " + otherPR.getContainedPoints().length + "     Invalid: "
                    + newPR.getContainedPoints().length);
        }

        resetImage(selected);
    }

    @Override
    public void keyTyped(KeyEvent e) { //Putting this here since it's just a single key press
        if ((e.getKeyChar() == 'g' || e.getKeyChar() == 'G')) {
            roiManager.select(img, roiManager.getSelectedIndex() == 0 ? 1 : 0);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!qKeyPressed && (e.getKeyChar() == 'q' || e.getKeyChar() == 'Q')) {
            qKeyPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (qKeyPressed) {
            qKeyPressed = false;
        }
    }

    public void addSister(PointListener sister) {
        if (!this.getClass().equals(sister.getClass())) {
            return;
        }
        this.sister = (PointSwapListener)sister;
    }
}
