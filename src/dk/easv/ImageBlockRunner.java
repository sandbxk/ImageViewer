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
    private Map<Color, Long> redMap;
    private Map<Color, Long> greenMap;
    private Map<Color, Long> blueMap;
    private Map<Color, Long> redGreenMap;
    private Map<Color, Long> redBlueMap;
    private Map<Color, Long> greenBlueMap;
    private Map<Color, Long> monochromeMap;
    private boolean isDone = false;


    public ImageBlockRunner(double startBlockX, double startBlockY, double endBlockX, double endBlockY, PixelReader pixelReader, Map<Color, Long> redMap, Map<Color, Long> greenMap, Map<Color, Long> blueMap, Map<Color, Long> redGreenMap, Map<Color, Long> redBlueMap, Map<Color, Long> greenBlueMap, Map<Color, Long> monochromeMap) {
        this.startBlockX = startBlockX;
        this.startBlockY = startBlockY;
        this.endBlockX = endBlockX;
        this.endBlockY = endBlockY;
        this.pixelReader = pixelReader;
        this.redMap = redMap;
        this.greenMap = greenMap;
        this.blueMap = blueMap;
        this.redGreenMap = redGreenMap;
        this.redBlueMap = redBlueMap;
        this.greenBlueMap = greenBlueMap;
        this.monochromeMap = monochromeMap;
    }


    @Override
    public synchronized void run() {
        for (double x = startBlockX; x < endBlockX; x++) {
            for (double y = startBlockY; y < endBlockY; y++) {
                final Color col = pixelReader.getColor((int) x, (int) y);
                switch (rgbChecker(col)) {
                    case RED -> {
                        if (redMap.containsKey(col)) {
                            redMap.put(col, redMap.get(col) + 1); //Use atomic long instead of map. Skip maps and use atomic longs
                        } else redMap.put(col, 1L);
                    }
                    case GREEN -> {
                        if (greenMap.containsKey(col)) {
                            greenMap.put(col, greenMap.get(col) + 1);
                        } else greenMap.put(col, 1L);
                    }
                    case BLUE -> {
                        if (blueMap.containsKey(col)) {
                            blueMap.put(col, blueMap.get(col) + 1);
                        } else blueMap.put(col, 1L);
                    }
                    case RED_GREEN -> {
                        if (redGreenMap.containsKey(col)) {
                            redGreenMap.put(col, redGreenMap.get(col) + 1);
                        } else redGreenMap.put(col, 1L);
                    }
                    case RED_BLUE -> {
                        if (redBlueMap.containsKey(col)) {
                            redBlueMap.put(col, redBlueMap.get(col) + 1);
                        } else redBlueMap.put(col, 1L);
                    }
                    case GREEN_BLUE -> {
                        if (greenBlueMap.containsKey(col)) {
                            greenBlueMap.put(col, greenBlueMap.get(col) + 1);
                        } else greenBlueMap.put(col, 1L);
                    }
                    case MONOCHROME -> {
                        if (monochromeMap.containsKey(col)) {
                            monochromeMap.put(col, monochromeMap.get(col) + 1);
                        } else monochromeMap.put(col, 1L);
                    }

                    default -> {
                        continue;
                    }
                }
            }
        }
        AtomicLong redTotalCount = new AtomicLong();
        redMap.keySet().forEach(color -> redTotalCount.addAndGet(redMap.get(color)));
        System.out.println("Red total count: " + redTotalCount);
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

    public boolean getIsDone() {
        return isDone;
    }

}
