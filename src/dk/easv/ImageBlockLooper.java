package dk.easv;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class ImageBlockLooper implements Callable<ColorRange> {
    private int startBlockX;
    private int startBlockY;
    private int endBlockX;
    private int endBlockY;
    private Image image;
    private PixelReader pixelReader;

    public ImageBlockLooper(int startBlockX, int startBlockY, int endBlockX, int endBlockY, PixelReader pixelReader) {
        this.startBlockX = startBlockX;
        this.startBlockY = startBlockY;
        this.endBlockX = endBlockX;
        this.endBlockY = endBlockY;
        this.pixelReader = pixelReader;
    }

    @Override
    public ColorRange call() throws Exception {
        for (int x = startBlockX; x < endBlockX; x++) {
            for (int y = startBlockY; y < endBlockY; y++) {
                final Color col = pixelReader.getColor(x, y);
                 ColorRange colorRange = rgbChecker(col);
                 return colorRange;
            }
        }
        return ColorRange.NONE;
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
            return ColorRange.RED;
        } else if (maxValue == green) {
            return ColorRange.GREEN;
        } else if (maxValue == blue) {
            return ColorRange.BLUE;
        }

        return ColorRange.NONE;
    }

}
