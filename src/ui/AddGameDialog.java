package ui;

import database.Database;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddGameDialog extends JDialog {
    private final JTextField nameField = new JTextField();
    private final JTextField executableField = new JTextField();

    private final DefaultListModel<File> imageModel = new DefaultListModel<>();
    private final JList<File> imageList = new JList<>(imageModel);

    public AddGameDialog(Frame parent) {
        super(parent, "Add Game", true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);


        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(new JLabel("Title:"), BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);


        executableField.setEditable(false);

        JButton selectExecutableButton = new JButton("Browse...");
        selectExecutableButton.addActionListener(l -> {
            JFileChooser chooser = new JFileChooser();

            chooser.setDialogTitle("Select executable");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                executableField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel executablePanel = new JPanel(new BorderLayout());
        executablePanel.add(new JLabel("Executable:"), BorderLayout.WEST);
        executablePanel.add(executableField, BorderLayout.CENTER);
        executablePanel.add(selectExecutableButton, BorderLayout.EAST);


        imageList.setVisibleRowCount(5);
        imageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane imageScrollPane = new JScrollPane(imageList);

        JButton addImagesButton = new JButton("Add images...");
        JButton clearImagesButton = new JButton("Clear images");

        addImagesButton.addActionListener(l -> {
            JFileChooser chooser = new JFileChooser();

            chooser.setDialogTitle("Select images");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(true);

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                for (File file : chooser.getSelectedFiles())
                    imageModel.addElement(file);
            }
        });

        clearImagesButton.addActionListener(l -> imageModel.clear());

        JPanel imageButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imageButtons.add(addImagesButton);
        imageButtons.add(clearImagesButton);

        JPanel imagesPanel = new JPanel(new BorderLayout());
        imagesPanel.add(new JLabel("Images:"), BorderLayout.NORTH);
        imagesPanel.add(imageScrollPane, BorderLayout.CENTER);
        imagesPanel.add(imageButtons, BorderLayout.SOUTH);


        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(l -> {
            String name = nameField.getText();

            File executable = executableField.getText().isBlank() ? null : new File(executableField.getText());

            List<File> images = new ArrayList<>();
            for (int i = 0; i < imageModel.size(); i++) {
                images.add(imageModel.getElementAt(i));
            }

            if (executable == null) return;

            Database.AddGame(name, executable, images);

            dispose();
        });

        cancelButton.addActionListener(l -> dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(cancelButton);
        bottomPanel.add(confirmButton);


        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder());

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.add(namePanel);
        fieldsPanel.add(Box.createVerticalStrut(10));
        fieldsPanel.add(executablePanel);

        content.add(fieldsPanel, BorderLayout.NORTH);
        content.add(imagesPanel, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(650, 400);
        setMinimumSize(new Dimension(550, 350));
        setLocationRelativeTo(parent);
    }
}
