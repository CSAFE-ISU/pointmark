import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Class to reverse changes in position of points caused by the user. Essentially, does not allow
 * for points to be moved while the plugin is running in order to decrease human error.
 */
public class PointDragListener extends PointListener {

    private static String address;

    private static String address2;

    Point[] initialState;

    Point mouse;

    int selected;

    boolean isSecond;

    public PointDragListener(JLabel log, boolean second) {
        super(log);

        String name = "";
        if (second) {
            name = "#2";
            address2 = this.toString();
            IJ.log("address for " + name + " is " + address2);
            isSecond = true;
        }
        else {
            name = "#1";
            address = this.toString();
            IJ.log("address for " + name + " is " + address);
            isSecond = false;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) { //This doesn't seem to work with mouseDragged.
        if (!this.toString().equals(address) && !this.toString().equals(address2)) {
            IJ.log(this.toString() + " was pressed");
            return;
        }

        selected = roiManager.getSelectedIndex();

        //so that you don't have to check both rois
        PointRoi initialRoi = (PointRoi)roiManager.getRoi(selected);

        initialState = initialRoi.getContainedPoints();

        mouse = MouseInfo.getPointerInfo().getLocation();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!this.toString().equals(address) && !this.toString().equals(address2)) {
            IJ.log(this.toString() + " was released");
            return;
        }

        Point mouseNew = MouseInfo.getPointerInfo().getLocation();
        if (mouse.equals(mouseNew)) {
            return;
        }

        //so that you don't have to check both rois
        PointRoi currentRoi = (PointRoi)roiManager.getRoi(selected);

        Point[] newState = currentRoi.getContainedPoints();

        Point movedPoint = null;
        int max = Math.min(initialState.length, newState.length); //might not need this since drag not alt
        for (int i = 0; i < max; i++) {
            if (!initialState[i].equals(newState[i])) {
                movedPoint = initialState[i];
                break;
            }
        }

        if (movedPoint != null) {
            
            PointRoi initialStateROI = new PointRoi();
            for (Point p : initialState) {
                initialStateROI.addPoint(p.getX(), p.getY());
            }

            resetROI(initialStateROI, selected);

            resetImage(selected);

            log.setText("Log: No moving points!");
        }
    }
}
