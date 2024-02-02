import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.plugin.PlugIn;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/**
 * Main class of the plugin, takes in an import and export file. Underscore required for ImageJ2 plugin convention.
 */
public class File_Uploader implements PlugIn {

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
        PointDragListener pd = new PointDragListener(log);
        canvas.addMouseListener(p);
        canvas.addKeyListener(p);
        canvas.addMouseListener(pd);

        importFile.setFocusable(false);
        exportFile.setFocusable(false);
        panel.setFocusable(true); //sets focus to the log panel itself
        panel.requestFocusInWindow();

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Q && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                        && ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)) {
                    openForExport(); //ctrl + shift + Q to open export
                }
                else if (e.getKeyCode() == KeyEvent.VK_Q && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
                    openForImport(); //ctrl + Q to open import
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
            coordControl.importCoords(filePath);
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

    public static void main(String[] args) {

    }
}
