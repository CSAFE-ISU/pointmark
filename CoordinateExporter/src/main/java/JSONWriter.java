import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exports the current iteration of the set_1 (index 0 in the ROI Manager) and set_2 (index 1)
 * ROIs to a JSON file. Note that any changes must first be saved in the ROI Manager by having
 * the ROI selected and clicking "Update."
 */
public class JSONWriter {

    public JSONWriter() {
    }

    public void exportCoordsAsJSON(String filePath) {

        JSONObject jsonObject = new JSONObject();

        JSONArray set1_JSON = new JSONArray();
        JSONArray set2_JSON = new JSONArray();
        JSONArray tmp;

        RoiManager roiManager = RoiManager.getRoiManager();

        if (roiManager.getCount() < 1) {
            throw new RuntimeException("There are no RoIs for this image.");
        }

        PointRoi set1_Points = (PointRoi) roiManager.getRoi(0);
        PointRoi set2_Points = (PointRoi) roiManager.getRoi(1);

        for (Point p : set1_Points) {
            tmp = new JSONArray();
            tmp.put(p.getY()); //row and then column
            tmp.put(p.getX());
            set1_JSON.put(tmp);
        }

        for (Point p : set2_Points) {

            tmp = new JSONArray();
            tmp.put(p.getY());
            tmp.put(p.getX());
            set2_JSON.put(tmp);
        }

        jsonObject.put("filename", "./sample.tiff");
        jsonObject.put("etor", "FAST");
        jsonObject.put("set_1", set1_JSON);
        jsonObject.put("set_2", set2_JSON);
        try {
            FileWriter file = new FileWriter(filePath);
            file.write(jsonObject.toString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
