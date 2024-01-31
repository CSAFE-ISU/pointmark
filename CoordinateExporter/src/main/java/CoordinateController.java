import ij.IJ;
import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import ij.measure.ResultsTable;
import org.json.simple.parser.ParseException;
import java.io.IOException;

/**
 * Controls reading and writing of imported and exported files respectively.
 */
public class CoordinateController {

    ImagePlus img;

    public CoordinateController(ImagePlus img) {
        this.img = img;
    }

    public void placeCoords(String filePath) {
        try {
            JSONReader reader = new JSONReader(img);
            RoiManager coords = reader.importCoordsFromJSON(filePath);
            reader.placeCoords(coords);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportCoords(String filePath) throws IOException {
        JSONWriter writer = new JSONWriter(img);
        writer.exportCoordsAsJSON(filePath);
    }
}