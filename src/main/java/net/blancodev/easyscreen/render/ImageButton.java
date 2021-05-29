package net.blancodev.easyscreen.render;

import net.blancodev.easyscreen.frame.ScreenshotFrame;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

// Class to encapsulate where images are rendered and handle clicks
public class ImageButton {

    private BufferedImage image;
    private BufferedImage hoverImage;

    private String tooltip;

    private int x,y;

    private Consumer<ScreenshotFrame> consumer;

    private boolean hovering;

    public ImageButton(BufferedImage image, BufferedImage hoverImage, String tooltip, Consumer<ScreenshotFrame> consumer) {
        this.image = image;
        this.hoverImage = hoverImage;
        this.tooltip = tooltip;
        this.x = x;
        this.y = y;
        this.consumer = consumer;
        this.hovering = false;
    }

    // Check if click was inside correct bounds
    public boolean handleClick(ScreenshotFrame jFrame, int clickedX, int clickedY) {

        if (withinBounds(clickedX, clickedY)) {
            consumer.accept(jFrame);
            return true;
        }

        return false;

    }

    public boolean handleHover(int hoverX, int hoverY) {
        return hovering = withinBounds(hoverX, hoverY);
    }

    private boolean withinBounds(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + image.getWidth()
                && mouseY > y && mouseY < y + image.getHeight();
    }

    // For easy mutable updates inside anonymous classes
    public void update(ImageButton button) {
        this.x = button.x;
        this.y = button.y;
    }

    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage getHoverImage() {
        return hoverImage;
    }

    public String getTooltip() {
        return tooltip;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Consumer<ScreenshotFrame> getConsumer() {
        return consumer;
    }

    public boolean isHovering() {
        return hovering;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setHovering(boolean hovering) {
        this.hovering = hovering;
    }
}
