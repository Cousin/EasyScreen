package net.blancodev.easyscreen.frame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import net.blancodev.easyscreen.*;
import net.blancodev.easyscreen.render.ImageButton;
import net.blancodev.easyscreen.render.SelectionRectangle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class ScreenshotFrame extends JFrame {

    private SelectionRectangle selectionRectangle;

    private boolean painting;

    public ScreenshotFrame(BufferedImage bufferedImage) {

        this.painting = false;

        // Setup frame
        setSize(bufferedImage.getWidth(), bufferedImage.getHeight());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        selectionRectangle = new SelectionRectangle(this, bufferedImage, new Rectangle(100, 100, 500, 500));

        ImageButton[] imageButtons = EasyScreen.IMAGE_BUTTONS;

        JPanel jPanel;

        // Setup main JPanel which renders the whole selection area
        add(jPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics graphics) {
                Toolkit.getDefaultToolkit().sync();
                graphics.drawImage(selectionRectangle.render(), 0, 0, this);

                graphics.setColor(new Color(0,0,0,0.4f));
                graphics.fillRoundRect((int) selectionRectangle.getRectangle().getMinX(), Math.max(0, (int) selectionRectangle.getRectangle().getMinY() - 25), 80, 20, 15, 15);
                graphics.setColor(Color.WHITE);
                graphics.drawString((int) selectionRectangle.getRectangle().getWidth() + "x" + (int)selectionRectangle.getRectangle().getHeight() + "px", (int) selectionRectangle.getRectangle().getMinX() + 5, Math.max(15, (int) selectionRectangle.getRectangle().getMinY() - 10));

                for (int i = 0; i < imageButtons.length; i++) {
                    ImageButton imageButton = imageButtons[i];
                    imageButton.setX((int) selectionRectangle.getRectangle().getMaxX() - 25 - (i * 25));
                    imageButton.setY(Math.min(getHeight() - 25, (int) selectionRectangle.getRectangle().getMaxY() + 5));
                    graphics.drawImage(imageButton.isHovering() ? imageButton.getHoverImage() : imageButton.getImage(), imageButton.getX(), imageButton.getY(),ScreenshotFrame.this);

                    if (imageButton.isHovering()) {

                        int boxWidth = graphics.getFontMetrics().stringWidth(imageButton.getTooltip()) + 10;

                        graphics.setColor(Color.WHITE);
                        graphics.fillRect(imageButton.getX(), imageButton.getY() + 30, boxWidth, 20);

                        graphics.setColor(Color.BLACK);
                        graphics.drawString(imageButton.getTooltip(), imageButton.getX() + 5, imageButton.getY() + 44);

                    }
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);

        // Mouse listener for dragging selection box
        jPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                selectionRectangle.onMouseMove(mouseEvent.getPoint());
                Arrays.stream(imageButtons).forEach(i -> {
                    i.handleHover(mouseEvent.getX(), mouseEvent.getY());
                });
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                if (!isPainting()) {
                    selectionRectangle.onMouseDrag(mouseEvent.getPoint());
                }
                repaint();
            }
        });

        new Thread(() -> {

            while (true) {
                if (isPainting()) {
                    int brushSize = 10;
                    selectionRectangle.getPaintLayer().getGraphics().fillOval((int) MouseInfo.getPointerInfo().getLocation().getX(), (int) MouseInfo.getPointerInfo().getLocation().getY(), brushSize, brushSize);
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }, "Paint-Thread").start();

        // Mouse listeners
        jPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (!isPainting()) {
                    selectionRectangle.onMousePress(mouseEvent.getPoint());
                } else {
                    int brushSize = 10;
                    selectionRectangle.getPaintLayer().getGraphics().fillOval(mouseEvent.getX(), mouseEvent.getY(), brushSize, brushSize);
                    /*for (int i = -brushSize; i < brushSize; i++) {
                        selectionRectangle.getPaintLayer().setRGB(mouseEvent.getX() + i, mouseEvent.getY() + i, Color.RED.getRGB());
                    }*/
                }
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                Arrays.stream(imageButtons).forEach(i -> i.handleClick(ScreenshotFrame.this, mouseEvent.getX(), mouseEvent.getY()));
            }
        });

        // Key listener to check for escape to close
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    for (JFrame looped : EasyScreen.getOpenFrames()) {
                        EasyScreen.closeFrame(looped);
                    }
                    EasyScreen.getOpenFrames().clear();
                }
            }
        });

        toFront();

        EasyScreen.getOpenFrames().add(this);
    }

    public boolean isJavaFXStillUsable() {
        try {
            new JFXPanel(); // Initializes the Toolkit required by JavaFX, as stated in the docs of Platform.runLater()
        } catch (IllegalStateException ise) {
            return false;
        }
        return true;
    }

    public SelectionRectangle getSelectionRectangle() {
        return selectionRectangle;
    }

    public boolean isPainting() {
        return painting;
    }

    public void setPainting(boolean painting) {
        this.painting = painting;
    }
}
