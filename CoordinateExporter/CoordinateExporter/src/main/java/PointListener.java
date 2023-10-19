import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

/**
 * Listens for a double click, and will search out the nearest point within a certain range to swap
 * its ROI between the "valid" and "invalid" ROIs.
 */
public class PointListener implements MouseListener, KeyListener {

    private static String address;

    private RoiManager roiManager;

    private boolean qKeyPressed;

    public PointListener() {
        address = this.toString();
        roiManager = RoiManager.getRoiManager();
        qKeyPressed = false;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        IJ.log(qKeyPressed + ", " + (qKeyPressed && this.toString().equals(address)));
        if (qKeyPressed && this.toString().equals(address)) {
            changeClosestPoint(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    public void changeClosestPoint(int x, int y) {
        int selected = roiManager.getSelectedIndex();
        PointRoi pr = (PointRoi)roiManager.getRoi(selected);
        ImagePlus img = IJ.getImage();
        double magnification = img.getCanvas().getMagnification();
        x = (int)(x/magnification);
        y = (int)(y/magnification);

        if (pr == null || pr.size() == 0) {
            IJ.log("There is nothing here!");
            return;
        }

        IJ.log("Clicked at (" + x + ", " + y + ")");

        Point[] points = pr.getContainedPoints();
        Point closest = null;
        double closestDist = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < points.length; i++) {
            IJ.log("Point " + index + " at (" + points[i].getX() + ", " + points[i].getY() + ")");
            double xDiff = Math.abs(x - points[i].getX());
            double yDiff = Math.abs(y - points[i].getY());;
            double distance = Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
            if (closestDist > distance) {
                closest = points[i];
                closestDist = distance;
                index = i;
                IJ.log("Closest point is " + i + ": (" + points[i].getX() + ", " + points[i].getY() + ")");
            }
        }

        IJ.log("Closest distance: " + closestDist);
        //50 gotten by testing relative values, may need to adjust for measurement plugin.
        //50 pixels is the standard limit for distance when the ImagePlus window is full-screen 100% size.
        if (closestDist > 50) {IJ.log("No point selected!"); return;}
        IJ.log(closestDist + "");

        //No direct way to delete point from ROI
        PointRoi newPR = new PointRoi();
        for (int i = 0; i < points.length; i++) {
            if (i != index) {
                newPR.addPoint(points[i].getX(), points[i].getY());
                //IJ.log("Restoring point at index " + i + ": (" + points[i].getX() + ", " + points[i].getY() + ")");
            }
        }

        if (selected == 0) {
            newPR.setStrokeColor(new Color(0, 0, 255));
            newPR.setPointType(2);
            newPR.setSize(3);
            newPR.setName("valid");
        }
        else {
            newPR.setStrokeColor(new Color(255, 255, 0));
            newPR.setPointType(2);
            newPR.setSize(3);
            newPR.setName("invalid");
        }

        roiManager.setRoi(newPR, selected);

        //Now add the minDistance Point to the other Roi
        int otherIndex = (selected == 0) ? 1 : 0;
        PointRoi otherPR = (PointRoi) roiManager.getRoi(otherIndex);
        otherPR.addPoint(closest.getX(), closest.getY());
        roiManager.setRoi(otherPR, otherIndex);

        roiManager.select(selected);
        img.updateAndDraw();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        IJ.log("Hmm?");
        if (!qKeyPressed && (e.getKeyChar() == 'q' || e.getKeyChar() == 'Q')) {
            qKeyPressed = true;
            IJ.log("hit!");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (qKeyPressed) {
            qKeyPressed = false;
            IJ.log("released!");
        }
    }
}
