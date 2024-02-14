import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Main class of the plugin, takes in an import and export file. Underscore required for ImageJ2 plugin convention.
 */
public class File_Uploader implements PlugIn {

    CoordinateController coordController;

    private JButton importFile;

    private final JFileChooser imported;

    private JPanel panel;

    private JButton exportFile;

    private JLabel importText;

    private JLabel exportText;

    private JLabel log;

    private final JFileChooser exported;

    ImagePlus img;

    ImagePlus img2;

    public File_Uploader() {
        String trace = "";
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            trace += "\n" + element.toString();
        }
        IJ.log("Stack trace: " + trace);

        coordController = new CoordinateController();
        int[] idList = WindowManager.getIDList();
        img = WindowManager.getImage(idList[0]);
        img2 = WindowManager.getImage(idList[1]);

        imported = new JFileChooser();
        exported = new JFileChooser();

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

        ImageCanvas canvas1 = img.getWindow().getCanvas();
        canvas1.setFocusable(true);
        //https://stackoverflow.com/questions/32205496/actionlistener-code-triggered-twice
        //for solution
        PointSwapListener p = new PointSwapListener(log, false);
        PointDragListener pd = new PointDragListener(log, false);
        if (canvas1.getMouseListeners().length < 1)
            canvas1.addMouseListener(p);
        if (canvas1.getMouseListeners().length < 2)
            canvas1.addMouseListener(pd);
        if (canvas1.getKeyListeners().length < 1)
            canvas1.addKeyListener(p);
        IJ.log("Canvas1 has " + canvas1.getMouseListeners().length + " mouse listeners and "
                + canvas1.getKeyListeners().length + " key listeners.");

        ImageCanvas canvas2 = img2.getWindow().getCanvas();
        canvas2.setFocusable(true);
        PointSwapListener p2 = new PointSwapListener(log, true);
        PointDragListener pd2 = new PointDragListener(log, true);
        if (canvas2.getMouseListeners().length < 1)
            canvas2.addMouseListener(p2);
        if (canvas2.getMouseListeners().length < 2)
            canvas2.addMouseListener(pd2);
        if (canvas1.getKeyListeners().length < 1)
            canvas2.addKeyListener(p2);
        IJ.log("Canvas2 has " + canvas2.getMouseListeners().length + " mouse listeners and "
                + canvas2.getKeyListeners().length + " key listeners.");

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
                } else if (e.getKeyCode() == KeyEvent.VK_Q && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
                    openForImport(); //ctrl + Q to open import
                }
            }
        });
    }

    public void openForImport() {
        log.setText("Log: Importing...");
        imported.setCurrentDirectory(new File(System.getProperty("user.home")));
        imported.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        int returnValue = imported.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = imported.getSelectedFile();
            String filePath = file.getAbsolutePath();
            coordController.importCoords(img, img2, filePath);
            importText.setText("Imported: " + filePath);
        }
    }

    public void openForExport() {
        log.setText("Log: Exporting...");
        exported.setCurrentDirectory(new File(System.getProperty("user.home")));
        exported.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        int returnValue = exported.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = exported.getSelectedFile();
            String filePath = file.getAbsolutePath();
            try {
                coordController.exportCoords(filePath);
                exportText.setText("Exported: " + filePath);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void run(String arg) {
        JFrame frame = new JFrame("FileUploader");

        //frame.setContentPane(new File_Uploader().panel);
        frame.setContentPane(this.panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack(); //sizes the window
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        importFile = new JButton();
        importFile.setText("Import");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 40;
        panel.add(importFile, gbc);
        exportFile = new JButton();
        exportFile.setText("Export");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 40;
        panel.add(exportFile, gbc);
        importText = new JLabel();
        importText.setText("Imported: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(importText, gbc);
        exportText = new JLabel();
        exportText.setText("Exported: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(exportText, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 40;
        panel.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 40;
        panel.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipady = 40;
        panel.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipady = 40;
        panel.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipady = 20;
        panel.add(spacer5, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipady = 20;
        panel.add(spacer6, gbc);
        log = new JLabel();
        log.setText("Log: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(log, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipady = 20;
        panel.add(spacer7, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipady = 40;
        panel.add(spacer8, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 40;
        panel.add(spacer9, gbc);
        final JPanel spacer10 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 40;
        panel.add(spacer10, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
