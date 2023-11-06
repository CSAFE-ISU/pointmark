import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PointDragListener extends MouseAdapter {

    private static String address;

    RoiManager roiManager;

    ImagePlus img;

    boolean dragged;

    Point[] initialState;

    Point mouse;

    int selected;

    public PointDragListener() {
        dragged = false;
        address = this.toString();
        roiManager = RoiManager.getRoiManager();
        img = IJ.getImage();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (dragged || !this.toString().equals(address)) {
            return;
        }
        dragged = true;

        selected = roiManager.getSelectedIndex();

        //so that you don't have to check both rois
        PointRoi initialRoi = (PointRoi)roiManager.getRoi(selected);

        initialState = initialRoi.getContainedPoints();

        mouse = MouseInfo.getPointerInfo().getLocation();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!this.toString().equals(address)) {
            return;
        }

        dragged = false; //Need this?

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

            if (selected == 0) {
                initialStateROI.setStrokeColor(new Color(0, 0, 255));
                initialStateROI.setPointType(2);
                initialStateROI.setSize(3);
                initialStateROI.setName("valid");
            }
            else {
                initialStateROI.setStrokeColor(new Color(255, 255, 0));
                initialStateROI.setPointType(2);
                initialStateROI.setSize(3);
                initialStateROI.setName("invalid");
            }

            roiManager.setRoi(initialStateROI, selected);
            img.updateAndRepaintWindow();
            roiManager.select(roiManager.getSelectedIndex());
        }
    }
}
