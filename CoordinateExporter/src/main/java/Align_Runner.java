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
public class Align_Runner implements PlugIn {

    private JPanel panel;
    private JTextArea dummy;
    public Align_Runner() {
        this.panel = new JPanel(new GridLayout(2, 1));
        this.dummy = new JTextArea();
        panel.add(dummy);
        dummy.setText("align things!");
    }

    public void run(String arg) {
        int p = JOptionPane.showConfirmDialog(null, this.panel,
                "Save Image + Markup", JOptionPane.OK_CANCEL_OPTION);
        System.out.printf("option was %d\n", p);
    }
    public static void callFromMacro() {
        Align_Runner x = new Align_Runner();
        x.run("");
    }
}
