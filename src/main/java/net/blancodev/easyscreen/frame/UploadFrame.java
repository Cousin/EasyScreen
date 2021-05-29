package net.blancodev.easyscreen.frame;

import javafx.scene.control.ProgressBar;
import net.blancodev.easyscreen.ConnectionBuilder;
import net.blancodev.easyscreen.EasyScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLEncoder;

public class UploadFrame extends JFrame {

    public UploadFrame(String base64, boolean searchGoogle) {

        System.out.println(base64);

        setSize(500, 75);
        setResizable(false);

        setIconImage(EasyScreen.loadFromResource("icon.png"));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);

        setTitle("Uploading screenshot...");

        add(progressBar);

        setLocationRelativeTo(null);
        setVisible(true);

        new Thread(() -> {
            ConnectionBuilder uploadRequest = new ConnectionBuilder(
                    "https://blancodev.net/easyscreen/upload.php"
            ).https(true).method(ConnectionBuilder.ConnectionMethod.POST).data("base64=" + URLEncoder.encode(base64)).send();

            String url = uploadRequest.getResponse();

            if (searchGoogle) {
                try {
                    Desktop.getDesktop().browse(new URL("https://www.google.com/searchbyimage?image_url=" + url).toURI());
                } catch (Exception e) { }
                EasyScreen.closeFrame(UploadFrame.this);
            } else {
                SwingUtilities.invokeLater(() -> {
                    setTitle("Uploaded!");
                    remove(progressBar);
                    add(getSuccessJPanel(url));
                    revalidate();
                    repaint();
                });
            }
        }).start();

        //

        toFront();

        EasyScreen.getOpenFrames().add(this);
    }

    private JPanel getSuccessJPanel(String url) {

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));

        JTextField urlTextField = new JTextField(url);
        urlTextField.setCaretPosition(0);
        urlTextField.setEditable(false);
        urlTextField.setMaximumSize(new Dimension(450, 27));

        JButton openButton = new JButton("Open");
        openButton.addActionListener((l) -> {
            try {
                Desktop.getDesktop().browse(new URL(urlTextField.getText()).toURI());
            } catch (Exception e) { }
            EasyScreen.closeFrame(UploadFrame.this);
        });

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener((l) -> {
            StringSelection stringSelection = new StringSelection(urlTextField.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);
            EasyScreen.closeFrame(UploadFrame.this);
        });

        jPanel.add(openButton);
        jPanel.add(copyButton);

        jPanel.add(urlTextField);

        return jPanel;

    }

}
