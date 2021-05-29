package net.blancodev.easyscreen.frame;

import net.blancodev.easyscreen.EasyScreen;
import net.sourceforge.tess4j.TesseractException;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;

public class OCRFrame extends JFrame {

    public OCRFrame(BufferedImage image) {

        setSize(500, 500);
        setResizable(false);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);

        setIconImage(EasyScreen.loadFromResource("icon.png"));
        setTitle("Reading image...");

        add(progressBar);

        setLocationRelativeTo(null);
        setVisible(true);

        new Thread(() -> {
            try {
                String text = EasyScreen.getTesseract().doOCR(image);
                SwingUtilities.invokeLater(() -> {
                    setResizable(true);
                    setTitle("Text Generated!");
                    remove(progressBar);
                    add(getSuccessJPanel(text));
                    revalidate();
                    repaint();
                });
            } catch (TesseractException e) {
                SwingUtilities.invokeLater(() -> {
                    setTitle("Failed to read!");
                    remove(progressBar);
                    add(getSuccessJPanel("ERROR | \n" + e.getMessage()));
                    revalidate();
                    repaint();
                });
            }

        }).start();

        toFront();

        EasyScreen.getOpenFrames().add(this);
    }

    private JPanel getSuccessJPanel(String text) {

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea(text);

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener((l) -> {
            StringSelection stringSelection = new StringSelection(textArea.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);
            EasyScreen.closeFrame(OCRFrame.this);
        });

        jPanel.add(textArea, BorderLayout.CENTER);
        jPanel.add(copyButton, BorderLayout.PAGE_END);

        return jPanel;

    }

}
