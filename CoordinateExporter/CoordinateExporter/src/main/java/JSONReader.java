import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.awt.*;
import java.io.*;

/**
 * Reads the imported JSON file and applies it to the ImagePlus window in two
 * ROIs, "valid" and "invalid." In the imported JSON file, use labels "valid"
 * and "invalid" with no modifications.
 */
class JSONReader {

    ImagePlus img;

    public JSONReader(ImagePlus img) {
        this.img = img;
    }

    public RoiManager importCoordsFromJSON(String filePath) throws IOException, ParseException, NullPointerException {
        JSONObject reader = (JSONObject)(new JSONParser().parse(new FileReader(filePath)));
        RoiManager coords = RoiManager.getRoiManager(); //Opens RoI Manager if not already open
        coords.reset(); //Clears the previously open RoIs

        JSONArray points = (JSONArray)reader.get("valid");
        PointRoi roi = new PointRoi();
        coords.add(fillROI(points, roi, true), 0); //identifier 0

        points = (JSONArray)reader.get("invalid");
        PointRoi roi2 = new PointRoi();
        coords.add(fillROI(points, roi2, false), 1); //identifier 1

        coords.runCommand("Show All");

        return coords;
    }

    public PointRoi fillROI(JSONArray points, PointRoi roi, boolean isValid) {
        String title = isValid ? "valid" : "invalid";

        for (int i = 0; i < points.size(); i++) {
            JSONArray obj = (JSONArray) points.get(i);
            double x = ((Number) obj.get(0)).doubleValue();
            double y = ((Number) obj.get(1)).doubleValue();
            roi.addUserPoint(img, x, y);
        }
        roi.setPointType(2);
        roi.setSize(3);
        roi.setName(title);
        if (isValid) {
            roi.setStrokeColor(new Color(0, 0, 255));
            return roi;
        }
        roi.setStrokeColor(new Color(255, 255, 0));
        return roi;
    }

    public void placeCoords(RoiManager coords) throws IOException {
        for (Roi entry : coords) {
            img.setRoi(entry, true);
        }
    }

}