import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Image_Loader implements PlugIn {
    private final JButton imgLoadButton;
    private final JTextArea imgPath;
    private final JCheckBox markupAvailable;
    private final JButton markupLoadButton;
    private final JTextArea markupPath;
    private final JPanel panel;
    private final JFileChooser chooser;
    ImagePlus img;
    String markup;
    private boolean img_valid;
    private boolean markup_valid;

    public Image_Loader() {
        this.panel = new JPanel(new GridLayout(6, 2));
        this.chooser = new JFileChooser();

        this.imgLoadButton = new JButton();
        imgLoadButton.setText("Load Image...");
        this.imgPath = new JTextArea();
        imgPath.setEditable(false);
        imgPath.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        this.markupAvailable = new JCheckBox();
        markupAvailable.setText("I have already marked up this image");
        this.markupLoadButton = new JButton();
        markupLoadButton.setText("Markup File Location...");
        this.markupPath = new JTextArea();
        markupPath.setEditable(false);
        markupPath.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        panel.add(new JLabel());
        panel.add(new JLabel());
        panel.add(imgLoadButton);
        panel.add(imgPath);
        panel.add(markupAvailable);
        panel.add(new JLabel());
        panel.add(markupLoadButton);
        panel.add(markupPath);

        imgLoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String validPath = checkFileLoad("tiff", "jpg", "png");
                if (validPath == null || validPath.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Invalid File!");
                    img_valid = false;
                } else {
                    imgPath.setText(validPath);
                    img_valid = true;
                }
            }
        });

        markupAvailable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (markupAvailable.isSelected()) {
                    markupPath.setEnabled(true);
                    markupLoadButton.setEnabled(true);
                } else {
                    markupPath.setEnabled(false);
                    markupLoadButton.setEnabled(false);
                }
            }
        });

        markupLoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String validPath = checkFileLoad("json", "txt");
                if (validPath == null || validPath.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Invalid File!");
                    markup_valid = false;
                } else {
                    markupPath.setText(validPath);
                    markup_valid = true;
                }
            }
        });

        this.img = null;
        this.markup = null;
        markupAvailable.setSelected(false);
        markupLoadButton.setEnabled(false);
        markupPath.setEnabled(false);
    }

    String checkFileLoad(String... fileTypes) {
        chooser.setFileFilter(new FileNameExtensionFilter("Load Image Data", fileTypes));
        int returnValue = chooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            return file.getAbsolutePath();
        }
        return "";
    }

    public void run(String arg) {
        int p = JOptionPane.showConfirmDialog(null, this.panel,
                "Load Image and Markup", JOptionPane.OK_CANCEL_OPTION);

        if (p == JOptionPane.OK_OPTION && img_valid) {
            this.img = IJ.openImage(imgPath.getText());
            this.img.show();
            if (markup_valid) {
                try {
                    this.markup = new String(Files.readAllBytes(Paths.get(markupPath.getText())));
                } catch (IOException e) {
                    // might need to show an error here
                    this.markup = "";
                }
                System.out.println(this.markup);
            }
        }
    }

    public static void callFromMacro() {
        Image_Loader x = new Image_Loader();
        x.run("");
    }
}
