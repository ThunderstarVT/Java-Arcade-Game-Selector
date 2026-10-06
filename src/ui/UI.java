package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class UI extends JFrame {

    private final JTextArea nameArea = new JTextArea();

    private final BufferedImage myImage = ImageIO.read(new File("C:\\Users\\roblo\\Downloads\\Sprite-0001.jpg"));

    private final JLabel imageLabel = new JLabel();


    static private final Font defaultFont = new Font("Consolas", Font.BOLD, 28);

    public UI () throws IOException {
        super("Test");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setUndecorated(true);
        BuildUI();
        setVisible(true);
    }

    private void BuildUI(){

        nameArea.setEditable(false);
        nameArea.setLineWrap(false);
        nameArea.setFont(defaultFont);
        nameArea.setText("Sploingus");

        imageLabel.setIcon(new ImageIcon(myImage));

        JSplitPane theSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, imageLabel, nameArea);
        add(theSplitter);

    }

}
