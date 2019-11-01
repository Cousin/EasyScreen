package net.blancodev.easyscreen.render;

import oracle.jrockit.jfr.JFR;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.Arrays;
import java.util.Currency;

public class SelectionRectangle {

    private JFrame jFrame;
    private BufferedImage originalImage;
    private BufferedImage darkerImage;
    private Rectangle rectangle;

    //private Point topLeft, topMiddle, topRight, left, right, bottomLeft, bottomMiddle, bottomRight;

    private Point[] selectorPoints;

    private Point lastDragPoint;

    private int lastPressedPoint;

    public SelectionRectangle(JFrame jFrame, BufferedImage originalImage, Rectangle rectangle) {
        this.jFrame = jFrame;
        this.rectangle = rectangle;
        this.selectorPoints = new Point[8];

        this.lastPressedPoint = 8;
        this.lastDragPoint = new Point((int) (rectangle.getMaxX() - (rectangle.getWidth() / 2)), (int) (rectangle.getMaxY() - (rectangle.getHeight() / 2)));

        this.originalImage = originalImage;
        this.darkerImage = copyImage(originalImage);

        for (int w = 0; w < darkerImage.getWidth(); w++) {
            for (int h = 0; h < darkerImage.getHeight(); h++) {
                darkerImage.setRGB(w, h, new Color(darkerImage.getRGB(w, h)).darker().darker().getRGB());
            }
        }

        recalibratePoints();
    }

    public void recalibratePoints() {

        if (rectangle.getWidth() >= 0 && rectangle.getHeight() >= 0) {

            rectangle.x = Math.max(rectangle.x, 0);
            rectangle.y = Math.max(rectangle.y, 0);

            if (rectangle.getMaxY() >= originalImage.getHeight()) {
                rectangle = new Rectangle((int) rectangle.getX(), (int) rectangle.getY(), (int) rectangle.getWidth(), (int) (originalImage.getHeight() - rectangle.getMinY() - 1));
            }

            if (rectangle.getMaxX() >= originalImage.getWidth()) {
                rectangle = new Rectangle((int) rectangle.getX(), (int) rectangle.getY(), (int) (originalImage.getWidth() - rectangle.getMinX() - 1), (int) rectangle.getHeight());
            }

            selectorPoints[0] = rectangle.getLocation();
            selectorPoints[1] = addPoint(selectorPoints[0], (int) rectangle.getWidth() / 2, 0);
            selectorPoints[2] = addPoint(selectorPoints[0], (int) rectangle.getWidth(), 0);

            selectorPoints[3] = addPoint(selectorPoints[0], 0, (int) rectangle.getHeight() / 2);
            selectorPoints[4] = addPoint(selectorPoints[2], 0, (int) rectangle.getHeight() / 2);

            selectorPoints[5] = addPoint(selectorPoints[0], 0, (int) rectangle.getHeight());
            selectorPoints[6] = addPoint(selectorPoints[1], 0, (int) rectangle.getHeight());
            selectorPoints[7] = addPoint(selectorPoints[2], 0, (int) rectangle.getHeight());

        } else {

            // dragging horizontally
            if (rectangle.getWidth() < 0 && rectangle.getHeight() >= 0) {

                Point[] newArray = new Point[8];
                newArray[0] = selectorPoints[2];
                newArray[1] = selectorPoints[1];
                newArray[2] = selectorPoints[0];
                newArray[3] = selectorPoints[4];
                newArray[4] = selectorPoints[3];
                newArray[5] = selectorPoints[7];
                newArray[6] = selectorPoints[6];
                newArray[7] = selectorPoints[5];

                selectorPoints = newArray;
                rectangle = new Rectangle(selectorPoints[0].x, selectorPoints[0].y, Math.abs(rectangle.width), Math.abs(rectangle.height));

                lastPressedPoint =
                        lastPressedPoint == 0 ? 2 :
                        lastPressedPoint == 2 ? 0 :
                        lastPressedPoint == 3 ? 4 :
                        lastPressedPoint == 4 ? 3 :
                        lastPressedPoint == 5 ? 7 :
                        lastPressedPoint == 7 ? 5 : 0;

            } else if (rectangle.getWidth() >= 0 && rectangle.getHeight() < 0) {

                Point[] newArray = new Point[8];
                newArray[0] = selectorPoints[5];
                newArray[1] = selectorPoints[6];
                newArray[2] = selectorPoints[7];
                newArray[3] = selectorPoints[3];
                newArray[4] = selectorPoints[4];
                newArray[5] = selectorPoints[0];
                newArray[6] = selectorPoints[1];
                newArray[7] = selectorPoints[2];

                selectorPoints = newArray;
                rectangle = new Rectangle(selectorPoints[0].x, selectorPoints[0].y, Math.abs(rectangle.width), Math.abs(rectangle.height));

                lastPressedPoint =
                        lastPressedPoint == 0 ? 5 :
                        lastPressedPoint == 1 ? 6 :
                        lastPressedPoint == 2 ? 7 :
                        lastPressedPoint == 5 ? 0 :
                        lastPressedPoint == 6 ? 1 :
                        lastPressedPoint == 7 ? 2 : 0;

            } else {
                lastPressedPoint = 7 - lastPressedPoint;
                Point[] newArray = new Point[8];
                for (int i = 0; i < 8; i++) {
                    newArray[i] = selectorPoints[7 - i];
                }
                selectorPoints = newArray;
                rectangle = new Rectangle(selectorPoints[0].x, selectorPoints[0].y, Math.abs(rectangle.width), Math.abs(rectangle.height));
            }

        }

    }

    public BufferedImage render() {

        BufferedImage copy = copyImage(darkerImage);

        try {
            copy.getGraphics().drawImage(
                    originalImage.getSubimage((int) rectangle.getX(), (int) rectangle.getY(), (int) rectangle.getWidth(), (int) rectangle.getHeight()), (int) rectangle.getMinX(), (int) rectangle.getMinY(), jFrame
            );
        } catch (RasterFormatException x) {}

        // Draw border
        for (int w = (int) rectangle.getMinX(); w <= rectangle.getMaxX(); w++) {
            for (int h = (int) rectangle.getMinY(); h <= rectangle.getMaxY(); h++) {
                if (w == rectangle.getMinX() || w == rectangle.getMaxX() || h == rectangle.getMinY() || h == rectangle.getMaxY()) {
                    copy.setRGB(w, h, w % 2 == 0 && h % 2 == 0 ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
        }

        // Reloop to render points after everything else, to ensure they're on top.
        // So far no noticable performance hit by relooping
        for (int w = (int) rectangle.getMinX(); w <= rectangle.getMaxX(); w++) {
            for (int h = (int) rectangle.getMinY(); h <= rectangle.getMaxY(); h++) {
                for (Point point : selectorPoints) {
                    if (point.x == w && point.y == h) {

                        // Draw square for selection point
                        for (int x = w - 3; x < w + 3; x++) {
                            for (int y = h - 3; y < h + 3; y++) {
                                if (x >= 0 && y >= 0 && x < originalImage.getWidth() && y < originalImage.getHeight()) {
                                    if (x == w - 3 || x == w + 2 || y == h - 3 || y == h + 2) {
                                        copy.setRGB(x, y, Color.WHITE.getRGB());
                                    } else {
                                        copy.setRGB(x, y, Color.BLACK.getRGB());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return copy;

    }

    public BufferedImage getSelectionImage() {
        return originalImage.getSubimage(
                (int) rectangle.getX(),
                (int) rectangle.getY(),
                (int) rectangle.getWidth(),
                (int) rectangle.getHeight());
    }

    public void onMouseDrag(Point mousePoint) {

        int newWidth, newHeight;

        switch (this.lastPressedPoint) {
            case 0:
                newWidth = (int) (rectangle.getWidth() - (mousePoint.getX() - rectangle.getMinX()));
                newHeight = (int) (rectangle.getHeight() - (mousePoint.getY() - rectangle.getMinY()));
                rectangle = new Rectangle((int) mousePoint.getX(), (int) mousePoint.getY(), newWidth, newHeight);
                recalibratePoints();
                break;
            case 1:
               // cursorNumber = Cursor.N_RESIZE_CURSOR;
                newWidth = (int) rectangle.getWidth();
                newHeight = (int) (rectangle.getHeight() - (mousePoint.getY() - rectangle.getMinY()));
                rectangle = new Rectangle((int) rectangle.getX(), (int) (mousePoint.getY()), newWidth, newHeight);
                recalibratePoints();
                break;
            case 2:
              //  cursorNumber = Cursor.NE_RESIZE_CURSOR;
                newWidth = (int) (rectangle.getWidth() + (mousePoint.getX() - rectangle.getMaxX()));
                newHeight = (int) (rectangle.getHeight() - (mousePoint.getY() - rectangle.getMinY()));
                rectangle = new Rectangle((int) rectangle.getX(), (int) mousePoint.getY(), newWidth, newHeight);
                recalibratePoints();
                break;
            case 3:
               // cursorNumber = Cursor.W_RESIZE_CURSOR;
                newWidth = (int) (rectangle.getWidth() - (mousePoint.getX() - rectangle.getMinX()));
                newHeight = (int) rectangle.getHeight();
                rectangle = new Rectangle((int) mousePoint.getX(), (int) rectangle.getY(), newWidth, newHeight);
                recalibratePoints();
                break;
            case 4:
              //  cursorNumber = Cursor.E_RESIZE_CURSOR;
                newWidth = (int) (rectangle.getWidth() + (mousePoint.getX() - rectangle.getMaxX()));
                newHeight = (int) rectangle.getHeight();
                rectangle = new Rectangle((int) rectangle.getX(), (int) rectangle.getY(), newWidth, newHeight);
                recalibratePoints();
                break;
            case 5:
              //  cursorNumber = Cursor.SW_RESIZE_CURSOR;
                newWidth = (int) (rectangle.getWidth() - (mousePoint.getX() - rectangle.getMinX()));
                newHeight = (int) (rectangle.getHeight() + (mousePoint.getY() - rectangle.getMaxY()));
                rectangle = new Rectangle((int) mousePoint.getX(), (int) (mousePoint.getY() - newHeight), newWidth, newHeight);
                recalibratePoints();
                break;
            case 6:
             //   cursorNumber = Cursor.S_RESIZE_CURSOR;
                newWidth = (int) rectangle.getWidth();
                newHeight = (int) (rectangle.getHeight() + (mousePoint.getY() - rectangle.getMaxY()));
                rectangle = new Rectangle((int) rectangle.getX(), (int) rectangle.getY(), newWidth, newHeight);
                recalibratePoints();
                break;
            case 7:
              //  cursorNumber = Cursor.SE_RESIZE_CURSOR;
                newWidth = (int) (rectangle.getWidth() + (mousePoint.getX() - rectangle.getMaxX()));
                newHeight = (int) (rectangle.getHeight() + (mousePoint.getY() - rectangle.getMaxY()));
                rectangle = new Rectangle((int) rectangle.getX(), (int) rectangle.getY(), newWidth, newHeight);
                recalibratePoints();
                break;
            case 8:
                int xDiff = lastDragPoint.x - mousePoint.x;
                int yDiff = lastDragPoint.y - mousePoint.y;
                rectangle = new Rectangle((int) (rectangle.getX() - xDiff), (int) (rectangle.getY() - yDiff), (int) rectangle.getWidth(), (int) rectangle.getHeight());
                recalibratePoints();
                this.lastDragPoint = mousePoint;
                break;
        }

    }

    public void onMousePress(Point mousePoint) {
        boolean foundPoint = false;

        for (int i = 0; i < selectorPoints.length; i++) {
            Point selectorPoint = selectorPoints[i];

            if (mousePoint.x >= selectorPoint.x - 5 && mousePoint.x < selectorPoint.x + 5
                    && mousePoint.y >= selectorPoint.y - 5 && mousePoint.y < selectorPoint.y + 5) {
                this.lastPressedPoint = i;
                foundPoint = true;
                break;
            }
        }

        if (!foundPoint) {
            if (mousePoint.getX() >= rectangle.getMinX() && mousePoint.getX() <= rectangle.getMaxX()
                    && mousePoint.getY() >= rectangle.getMinY() && mousePoint.getY() <= rectangle.getMaxY()) {
                this.lastPressedPoint = 8;
                this.lastDragPoint = mousePoint;
            }
        }
    }

    public void onMouseMove(Point mousePoint) {

        boolean cursorEdited = false;

        for (int i = 0; i < selectorPoints.length; i++) {
            Point point = selectorPoints[i];

            if (mousePoint.x >= point.x - 5 && mousePoint.x < point.x + 5
                    && mousePoint.y >= point.y - 5 && mousePoint.y < point.y + 5) {

                int cursorNumber;

                switch (i) {
                    case 0:
                        cursorNumber = Cursor.NW_RESIZE_CURSOR;
                        break;
                    case 1:
                        cursorNumber = Cursor.N_RESIZE_CURSOR;
                        break;
                    case 2:
                        cursorNumber = Cursor.NE_RESIZE_CURSOR;
                        break;
                    case 3:
                        cursorNumber = Cursor.W_RESIZE_CURSOR;
                        break;
                    case 4:
                        cursorNumber = Cursor.E_RESIZE_CURSOR;
                        break;
                    case 5:
                        cursorNumber = Cursor.SW_RESIZE_CURSOR;
                        break;
                    case 6:
                        cursorNumber = Cursor.S_RESIZE_CURSOR;
                        break;
                    case 7:
                        cursorNumber = Cursor.SE_RESIZE_CURSOR;
                        break;
                    default:
                           cursorNumber = Cursor.N_RESIZE_CURSOR;
                }

                jFrame.setCursor(new Cursor(cursorNumber));
                cursorEdited = true;

            }
        }

        if (!cursorEdited) {
            if (mousePoint.getX() >= rectangle.getMinX() && mousePoint.getX() <= rectangle.getMaxX()
                    && mousePoint.getY() >= rectangle.getMinY() && mousePoint.getY() <= rectangle.getMaxY()) {
                jFrame.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            } else {
                jFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }

    }

    private Point addPoint(Point point, int x, int y) {
        return new Point((int) point.getX() + x, (int) point.getY() + y);
    }

    private Point subtractPoint(Point point, int x, int y) {
        return new Point((int) point.getX() - x, (int) point.getY() - y);
    }

    private BufferedImage copyImage(BufferedImage bufferedImage){
        BufferedImage copy = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
        Graphics graphics = copy.getGraphics();
        graphics.drawImage(bufferedImage, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}
