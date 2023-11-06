import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.plugin.PlugIn;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/**
 * Running class of the Plugin, takes in an import and export file.
 */
@Plugin(type = Command.class, menuPath = "Plugins>Coordinate_Controller")
public class File_Uploader implements PlugIn { //implements PlugIn

    CoordinateController coordControl;

    private JButton importFile;

    private final JFileChooser imported;

    private JPanel panel;

    private JButton exportFile;

    private JLabel importText;

    private JLabel exportText;

    private JLabel log;

    private final JFileChooser exported;

    public File_Uploader() {
        ImagePlus img = IJ.getImage();

        imported = new JFileChooser();
        exported = new JFileChooser();

        coordControl = new CoordinateController(img);

        importFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openForImport();
            }
        });

        exportFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openForExport();
            }
        });

        ImagePlus imp = IJ.getImage();

        ImageCanvas canvas = imp.getWindow().getCanvas();
        canvas.setFocusable(true);
        PointSwapListener p = new PointSwapListener(log);
        PointDragListener pd = new PointDragListener();
        canvas.addMouseListener(p);
        canvas.addKeyListener(p);
        canvas.addMouseListener(pd);

        importFile.setFocusable(false);
        exportFile.setFocusable(false);
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Q && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                        && ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)) {
                    openForExport();
                }
                else if (e.getKeyCode() == KeyEvent.VK_Q && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
                    openForImport();
                }
            }
        });
    }

    public void openForImport() {
        imported.setCurrentDirectory(new File(System.getProperty("user.home")));
        imported.setFileFilter(new FileNameExtensionFilter("JSON files","json"));
        int returnValue = imported.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = imported.getSelectedFile();
            String filePath = file.getAbsolutePath();
            coordControl.placeCoords(filePath);
            importText.setText("Imported: " + filePath);
            log.setText("Log: Importing...");
        }
    }

    public void openForExport() {
        exported.setCurrentDirectory(new File(System.getProperty("user.home")));
        exported.setFileFilter(new FileNameExtensionFilter("JSON files","json"));
        int returnValue = exported.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = exported.getSelectedFile();
            String filePath = file.getAbsolutePath();
            try {
                coordControl.exportCoords(filePath);
                exportText.setText("Exported: " + filePath);
                log.setText("Log: Exporting...");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void run(String arg) {
        JFrame frame = new JFrame("FileUploader");

        frame.setContentPane(new File_Uploader().panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack(); //sizes the window
        frame.setVisible(true);
    }
}
