package net.blancodev.easyscreen;

// Class to encapsulate where images are rendered and handle clicks
public class ImageButton {

    private int x,y,width,height;

    private Runnable runnable;

    public ImageButton(int x, int y, int width, int height, Runnable runnable) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.runnable = runnable;
    }

    public ImageButton(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Check if click was inside correct bounds
    public boolean handleClick(int clickedX, int clickedY) {

        if (x == 0 && y == 0 && height == 0 && width == 0) {
            return false;
        }

        if (clickedX >= x && clickedX <= x + width
                && clickedY >= y && clickedY <= y + height) {
            runnable.run();
            return true;
        }

        return false;

    }

    // For easy mutable updates inside anonymous classes
    public void update(ImageButton button) {
        this.x = button.x;
        this.y = button.y;
        this.width = button.width;
        this.height = button.height;
    }

}
