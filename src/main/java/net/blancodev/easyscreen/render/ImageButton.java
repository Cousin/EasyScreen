package net.blancodev.easyscreen.render;

import java.awt.image.BufferedImage;

// Class to encapsulate where images are rendered and handle clicks
public class ImageButton {

    private BufferedImage image;
    private BufferedImage hoverImage;

    private String tooltip;

    private int x,y;

    private Runnable runnable;

    private boolean hovering;

    public ImageButton(BufferedImage image, BufferedImage hoverImage, String tooltip, Runnable runnable) {
        this.image = image;
        this.hoverImage = hoverImage;
        this.tooltip = tooltip;
        this.x = x;
        this.y = y;
        this.runnable = runnable;
        this.hovering = false;
    }

    // Check if click was inside correct bounds
    public boolean handleClick(int clickedX, int clickedY) {

        if (withinBounds(clickedX, clickedY)) {
            runnable.run();
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

    public Runnable getRunnable() {
        return runnable;
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
