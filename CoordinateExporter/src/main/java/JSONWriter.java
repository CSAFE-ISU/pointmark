import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
import java.awt.*;
import java.io.*;

/**
 * Exports the current iteration of the valid (index 0 in the ROI Manager) and invalid (index 1)
 * ROIs to a JSON file. Note that any changes must first be saved in the ROI Manager by having
 * the ROI selected and clicking "Update."
 */
public class JSONWriter {

    ImagePlus img;

    public JSONWriter(ImagePlus img) {
        this.img = img;
    }

    public void exportCoordsAsJSON(String filePath) {

        JSONObject jsonObject = new JSONObject();

        JSONArray validJSON = new JSONArray();
        JSONArray invalidJSON = new JSONArray();
        JSONArray tmp;

        RoiManager roiManager = RoiManager.getRoiManager();

        if (roiManager.getCount() < 1) {
            throw new RuntimeException("There are no RoIs for this image.");
        }

        PointRoi validPoints = (PointRoi)roiManager.getRoi(0);
        PointRoi invalidPoints = (PointRoi)roiManager.getRoi(1);

        for (Point p : validPoints) {
            tmp = new JSONArray();
            tmp.put(p.getY()); //row and then column
            tmp.put(p.getX());
            validJSON.put(tmp);
        }

        for (Point p : invalidPoints) {

            tmp = new JSONArray();
            tmp.put(p.getY());
            tmp.put(p.getX());
            invalidJSON.put(tmp);
        }

        jsonObject.put("filename", "./sample.tiff");
        jsonObject.put("etor", "FAST");
        jsonObject.put("valid", validJSON);
        jsonObject.put("invalid", invalidJSON);
        try {
            FileWriter file = new FileWriter(filePath);
            file.write(jsonObject.toString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
