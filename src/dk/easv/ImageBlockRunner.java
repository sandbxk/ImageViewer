package dk.easv;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static dk.easv.ColorRange.*;

public class ImageBlockRunner implements Runnable{
    private double startBlockX;
    private double startBlockY;
    private double endBlockX;
    private double endBlockY;
    private PixelReader pixelReader;
    private AtomicLong red;
    private AtomicLong green;
    private AtomicLong blue;
    private AtomicLong redGreen;
    private AtomicLong redBlue;
    private AtomicLong greenBlue;
    private AtomicLong monochrome;


    public ImageBlockRunner(double startBlockX, double startBlockY, double endBlockX, double endBlockY, PixelReader pixelReader, AtomicLong red, AtomicLong green, AtomicLong blue, AtomicLong redGreen, AtomicLong redBlue, AtomicLong greenBlue, AtomicLong monochrome) {
        this.startBlockX = startBlockX;
        this.startBlockY = startBlockY;
        this.endBlockX = endBlockX;
        this.endBlockY = endBlockY;
        this.pixelReader = pixelReader;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.redGreen = redGreen;
        this.redBlue = redBlue;
        this.greenBlue = greenBlue;
        this.monochrome = monochrome;

    }


    @Override
    public synchronized void run() {
        for (double x = startBlockX; x < endBlockX; x++) {
            for (double y = startBlockY; y < endBlockY; y++) {
                final Color col = pixelReader.getColor((int) x, (int) y);
                switch (rgbChecker(col)) {
                    case RED -> red.incrementAndGet();
                    case GREEN -> green.incrementAndGet();
                    case BLUE -> blue.incrementAndGet();
                    case RED_GREEN -> redGreen.incrementAndGet();
                    case RED_BLUE -> redBlue.incrementAndGet();
                    case GREEN_BLUE -> greenBlue.incrementAndGet();
                    case MONOCHROME -> monochrome.incrementAndGet();

                    default -> {
                        continue;
                    }
                }
            }
        }

        System.out.println("Red total count: " + red);
    }


    private ColorRange rgbChecker(Color color) {
        double red = color.getRed();
        double green = color.getGreen();
        double blue = color.getBlue();

        double[] values = {red, green, blue};
        Arrays.sort(values);
        double maxValue = values[values.length - 1];

        if (red == green && red == blue){
            return ColorRange.MONOCHROME;
        } else if (maxValue == red && maxValue == green) {
            return ColorRange.RED_GREEN;
        } else if (maxValue == red && maxValue == blue) {
            return ColorRange.RED_BLUE;
        } else if (maxValue == green && maxValue == blue) {
            return ColorRange.GREEN_BLUE;
        } else if (maxValue == red) {
            return RED;
        } else if (maxValue == green) {
            return ColorRange.GREEN;
        } else if (maxValue == blue) {
            return ColorRange.BLUE;
        }

        return ColorRange.NONE;
    }


}
