import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import java.io.IOException;

/**
 * Controls reading and writing of imported and exported files respectively.
 */
public class CoordinateController {

    public CoordinateController() {
    }

    public void importCoords(ImagePlus img, ImagePlus img2, String filePath) {
        try {
            JSONReader reader = new JSONReader();
            RoiManager coords = reader.importCoordsFromJSON(img, img2, filePath);
            reader.placeCoords(img, img2, coords);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportCoords(String filePath) throws IOException {
        JSONWriter writer = new JSONWriter();
        writer.exportCoordsAsJSON(filePath);
    }
}