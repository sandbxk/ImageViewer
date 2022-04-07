package dk.easv;

import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import static dk.easv.ColorRange.RED;

public class ImageBlockLooper implements Callable<Map<String, Long>> {
    private int startBlockX;
    private int startBlockY;
    private int endBlockX;
    private int endBlockY;
    private PixelReader pixelReader;
    private AtomicLong red;
    private AtomicLong green;
    private AtomicLong blue;
    private AtomicLong redGreen;
    private AtomicLong redBlue;
    private AtomicLong greenBlue;
    private AtomicLong monochrome;
    private Map<String, Long> result;

    public ImageBlockLooper(int startBlockX, int startBlockY, int endBlockX, int endBlockY, PixelReader pixelReader) {
        this.startBlockX = startBlockX;
        this.startBlockY = startBlockY;
        this.endBlockX = endBlockX;
        this.endBlockY = endBlockY;
        this.pixelReader = pixelReader;
        this.red = new AtomicLong(0);
        this.green = new AtomicLong(0);
        this.blue = new AtomicLong(0);
        this.redGreen = new AtomicLong(0);
        this.redBlue = new AtomicLong(0);
        this.greenBlue = new AtomicLong(0);
        this.monochrome = new AtomicLong(0);
        this.result = new HashMap<>();
    }


    @Override
    public synchronized Map<String, Long> call() {
        for (int x = startBlockX; x < endBlockX; x++) {
            for (int y = startBlockY; y < endBlockY; y++) {
                final Color col = pixelReader.getColor(x, y);
                switch (rgbChecker(col)) {
                    case RED -> red.incrementAndGet();
                    case GREEN -> green.incrementAndGet();
                    case BLUE -> blue.incrementAndGet();
                    case RED_GREEN -> redGreen.incrementAndGet();
                    case RED_BLUE -> redBlue.incrementAndGet();
                    case GREEN_BLUE -> greenBlue.incrementAndGet();
                    case MONOCHROME -> monochrome.incrementAndGet();

                    default -> {}
                }
            }
        }

        System.out.println("Red total count: " + red);
        result.put("Red", red.get());
        result.put("Green", green.get());
        result.put("Blue", blue.get());
        result.put("Yellow", redGreen.get());
        result.put("Magenta", redBlue.get());
        result.put("Cyan", greenBlue.get());
        result.put("Monochrome", monochrome.get());
        return result;
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
