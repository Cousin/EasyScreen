package net.blancodev.easyscreen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ScreenshotFrame extends JFrame {

    public ScreenshotFrame(BufferedImage bufferedImage) {

        // Setup frame
        setSize(bufferedImage.getWidth(), bufferedImage.getHeight());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setVisible(true);

        // Rectangle for keeping track of selection
        Rectangle rectangle = new Rectangle(0,0,0,0);

        // Rectangle that stores the currently pictured rectangle
        // (I probably could've combined the 2 but handling the dragging and click methods got confused so I settled with this way)
        Rectangle visibleRectangle = new Rectangle(0,0,0,0);

        // Store the renderable image buttons and their functions
        ImageButton[] imageButtons = {
                new ImageButton(0,0,0,0, () -> {
                    BufferedImage croppedImage = bufferedImage.getSubimage((int) visibleRectangle.getX(), (int) visibleRectangle.getY(), (int) visibleRectangle.getWidth(), (int) visibleRectangle.getHeight());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(croppedImage), null);
                })
        };

        JPanel jPanel;

        // Setup main JPanel which renders the whole selection area
        add(jPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponents(graphics);

                // Draws a copy of the screen with selection box
                graphics.drawImage(filteredImage(bufferedImage, rectangle), 0, 0, this);
                visibleRectangle.setRect(rectangle);

                if (!rectangle.getSize().equals(new Dimension(0,0))) {

                    // Draw the image buttons
                    try {
                        graphics.drawImage(ImageIO.read(new File("copyicon.png")),
                                (int) rectangle.getMaxX() - 20, (int) rectangle.getMaxY() + 5, 20, 20, this);
                        imageButtons[0].update(new ImageButton(
                                (int) rectangle.getMaxX() - 20, (int) rectangle.getMaxY() + 5, 20, 20
                        ));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        // Mouse listener for dragging selection box
        jPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                if (rectangle.getSize().equals(new Dimension(0,0))) {
                    rectangle.setRect(mouseEvent.getX(), mouseEvent.getY(), mouseEvent.getX(), mouseEvent.getY());
                } else {
                    double width = mouseEvent.getX() - rectangle.getMinX();
                    double height = mouseEvent.getY() - rectangle.getMinY();

                    // Check if dragging in correct direction
                    // TODO: support backwards selection
                    if (width >= 0 && height >= 0) {
                        rectangle.setRect(rectangle.getMinX(), rectangle.getMinY(), width, height);
                    } else {
                        rectangle.setRect(mouseEvent.getX(), mouseEvent.getY(), rectangle.getMaxX(), rectangle.getMaxY());
                    }
                }

                repaint();
            }

        });

        // Mouse listeners
        jPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                // Restart selection
                rectangle.setRect(0, 0, 0, 0);
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                // Check if image buttons were clicked
                Arrays.stream(imageButtons).forEach(i -> i.handleClick(mouseEvent.getX(), mouseEvent.getY()));
            }
        });

        // Key listener to check for escape to close
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                    dispatchEvent(new WindowEvent(ScreenshotFrame.this, WindowEvent.WINDOW_CLOSING));
                }
            }
        });

    }

    // Generate filtered image with highlighting selection
    private BufferedImage filteredImage(BufferedImage image, Rectangle rectangle) {

        BufferedImage copy = copyImage(image);

        for (int w = 0; w < copy.getWidth(); w++) {
           for (int h = 0; h < copy.getHeight(); h++) {

                if (!(w >= rectangle.getMinX() && w <= rectangle.getMaxX()
                                && h >= rectangle.getMinY() && h <= rectangle.getMaxY())) {

                    if (h == rectangle.getY() - 1 || w == rectangle.getX() - 1) {
                        copy.setRGB(Math.max(0, w - 1), Math.max(0, h - 1), Color.BLACK.getRGB());
                    } else if (w == (rectangle.getX() + (rectangle.getMaxX() - rectangle.getX()) + 1)
                            || h == (rectangle.getY() + (rectangle.getMaxY() - rectangle.getY())) + 1) {
                        copy.setRGB(Math.min(w, w + 1), Math.min(h, h + 1), Color.BLACK.getRGB());
                    } else {
                        copy.setRGB(w, h, new Color(copy.getRGB(w, h)).darker().darker().getRGB());
                    }

                }

            }
        }

        return copy;

    }

    // Creates a copy of a BufferedImage
    private BufferedImage copyImage(BufferedImage bufferedImage){
        BufferedImage copy = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
        Graphics graphics = copy.getGraphics();
        graphics.drawImage(bufferedImage, 0, 0, null);
        graphics.dispose();
        return copy;
    }

}
