import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads the imported JSON file and applies it to the ImagePlus window in two
 * ROIs, "set_1" and "set_2." In the imported JSON file, use labels "set_1"
 * and "set_2" with no modifications (i.e. "Set_1" or "set_2_points").
 */
class JSONReader {

    public JSONReader() {
    }

    public RoiManager initROIs(ImagePlus img, ImagePlus img2) throws NullPointerException {
        RoiManager coords = RoiManager.getRoiManager(); //Opens RoI Manager if not already open
        coords.reset(); //Clears the previously open RoIs

        PointRoi roi = new PointRoi();
        coords.add(fillROI(img, null, roi, true), 0); //identifier 0

        PointRoi roi2 = new PointRoi();
        coords.add(fillROI(img2, null, roi2, false), 1); //identifier 1

        coords.runCommand("Show All");

        return coords;
    }

    public RoiManager importCoordsFromJSON(ImagePlus img, ImagePlus img2, String filePath) throws IOException, NullPointerException {
        JSONObject reader = new JSONObject(new JSONTokener(new FileReader(filePath)));
        RoiManager coords = RoiManager.getRoiManager(); //Opens RoI Manager if not already open
        coords.reset(); //Clears the previously open RoIs

        JSONArray points = (JSONArray) reader.get("set_1");
        PointRoi roi = new PointRoi();
        coords.add(fillROI(img, points, roi, true), 0); //identifier 0

        points = (JSONArray) reader.get("set_2");
        PointRoi roi2 = new PointRoi();
        coords.add(fillROI(img2, points, roi2, false), 1); //identifier 1

        coords.runCommand("Show All");

        return coords;
    }

    public PointRoi fillROI(ImagePlus img, JSONArray points, PointRoi roi, boolean isSet1) {
        String title = isSet1 ? "set_1" : "set_2";

        if (points != null) {
            for (int i = 0; i < points.length(); i++) { //obtains point values
                JSONArray array = (JSONArray) points.get(i);
                double row = ((Number) array.get(1)).doubleValue();
                double col = ((Number) array.get(0)).doubleValue();
                roi.addUserPoint(img, row, col);
            }
        }

        roi.setPointType(2); //sets values for the ROI itself
        roi.setSize(3);
        roi.setName(title);
        if (isSet1) {
            roi.setStrokeColor(new Color(0, 0, 255));
            return roi;
        }
        roi.setStrokeColor(new Color(255, 255, 0));
        return roi;
    }

    public void placeCoords(ImagePlus img, ImagePlus img2, RoiManager coords) throws IOException {
        img.setRoi(coords.getRoi(0), true);
        img2.setRoi(coords.getRoi(1), true);
        coords.select(img2, 0);
    }

}