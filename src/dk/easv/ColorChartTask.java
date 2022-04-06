package dk.easv;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ColorChartTask extends Task<XYChart.Series<String, Number>> {

    private ObjectProperty<Image> imageProperty;
    private boolean running = true;

    public ColorChartTask(ObjectProperty<Image> imageProperty) {
        this.imageProperty = imageProperty;

    }

    @Override
    protected XYChart.Series<String, Number> call() throws Exception {
            Image currentImage = imageProperty.get();
        while (running) {
            if (imageProperty.get() != currentImage) {
                currentImage = imageProperty.get();
                updateValue(getSeriesData(getColorData(currentImage)));
            }
        }
        return null;
    }

    private XYChart.Series<String, Number> getSeriesData(Map<String, Long> colorMap) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ObservableList<XYChart.Data<String, Number>> colorData = FXCollections.observableArrayList(colorMap.keySet().stream().map(
                key -> new XYChart.Data<String, Number>(key, colorMap.get(key))).toList());

        series.setData(colorData);
        return series;
    }

    public Map<String, Long> getColorData(Image img){
        // Read through the pixels and count the number of occurrences of each color.

        final Map<Color, Long> redCount = new HashMap<>(); // red
        final Map<Color, Long> greenCount = new HashMap<>(); // green
        final Map<Color, Long> blueCount = new HashMap<>(); // blue
        final Map<Color, Long> redGreenCount = new HashMap<>(); // Red and green mix
        final Map<Color, Long> redBlueCount = new HashMap<>(); // Red and blue mix
        final Map<Color, Long> greenBlueCount = new HashMap<>(); // Green and blue mix
        final Map<Color, Long> monochromeCount = new HashMap<>(); // blacks, greys, whites


        Map<String, Long> rgbCount = new HashMap<>();

        final PixelReader pr = img.getPixelReader();

        for(int x = 0; x < img.getWidth(); x++) {
            for(int y = 0; y < img.getHeight(); y++) {
                final Color col = pr.getColor(x, y);

                switch (rgbChecker(col)){
                    case RED -> {
                        if (redCount.containsKey(col))
                        {
                            redCount.put(col, redCount.get(col)+1);
                        }
                        else redCount.put(col, 1L);
                    }
                    case GREEN -> {
                        if (greenCount.containsKey(col))
                        {
                            greenCount.put(col, greenCount.get(col)+1);
                        }
                        else greenCount.put(col, 1L);
                    }
                    case BLUE -> {
                        if (blueCount.containsKey(col))
                        {
                            blueCount.put(col, blueCount.get(col)+1);
                        }
                        else blueCount.put(col, 1L);
                    }
                    case RED_GREEN -> {
                        if (redGreenCount.containsKey(col))
                        {
                            redGreenCount.put(col, redGreenCount.get(col)+1);
                        }
                        else redGreenCount.put(col, 1L);
                    }
                    case RED_BLUE -> {
                        if (redBlueCount.containsKey(col))
                        {
                            redBlueCount.put(col, redBlueCount.get(col)+1);
                        }
                        else redBlueCount.put(col, 1L);
                    }
                    case GREEN_BLUE -> {
                        if (greenBlueCount.containsKey(col))
                        {
                            greenBlueCount.put(col, greenBlueCount.get(col)+1);
                        }
                        else greenBlueCount.put(col, 1L);
                    }
                    case MONOCHROME -> {
                        if (monochromeCount.containsKey(col))
                        {
                            monochromeCount.put(col, monochromeCount.get(col)+1);
                        }
                        else monochromeCount.put(col, 1L);
                    }
                    
                    default -> { continue; }
                }
            }
        }

        AtomicLong redTotalCount = new AtomicLong();
        redCount.keySet().forEach(color -> redTotalCount.addAndGet(redCount.get(color)));

        AtomicLong greenTotalCount = new AtomicLong();
        greenCount.keySet().forEach(color -> greenTotalCount.addAndGet(greenCount.get(color)));

        AtomicLong blueTotalCount = new AtomicLong();
        blueCount.keySet().forEach(color -> blueTotalCount.addAndGet(blueCount.get(color)));

        AtomicLong redGreenTotalCount = new AtomicLong();
        redGreenCount.keySet().forEach(color -> redGreenTotalCount.addAndGet(redGreenCount.get(color)));

        AtomicLong redBlueTotalCount = new AtomicLong();
        redBlueCount.keySet().forEach(color -> redBlueTotalCount.addAndGet(redBlueCount.get(color)));

        AtomicLong greenBlueTotalCount = new AtomicLong();
        greenBlueCount.keySet().forEach(color -> greenBlueTotalCount.addAndGet(greenBlueCount.get(color)));

        AtomicLong monochromeTotalCount = new AtomicLong();
        monochromeCount.keySet().forEach(color -> monochromeTotalCount.addAndGet(monochromeCount.get(color)));

        rgbCount.put("Monochrome", monochromeTotalCount.get());
        rgbCount.put("Red", redTotalCount.get());
        rgbCount.put("Red-Green", redGreenTotalCount.get());
        rgbCount.put("Green", greenTotalCount.get());
        rgbCount.put("Green-Blue", greenBlueTotalCount.get());
        rgbCount.put("Blue", blueTotalCount.get());
        rgbCount.put("Red-Blue", redBlueTotalCount.get());


        System.out.println("RED: " + redTotalCount + "   GREEN: " + greenTotalCount +  "   BLUE: " + blueTotalCount);
        return rgbCount;
    }

    private RGB rgbChecker(Color color) {
        double red = color.getRed();
        double green = color.getGreen();
        double blue = color.getBlue();

        double[] values = {red, green, blue};
        Arrays.sort(values);
        double maxValue = values[values.length - 1];

        if (red == green && red == blue){
            return RGB.MONOCHROME;
        } else if (maxValue == red && maxValue == green) {
            return RGB.RED_GREEN;
        } else if (maxValue == red && maxValue == blue) {
            return RGB.RED_BLUE;
        } else if (maxValue == green && maxValue == blue) {
            return RGB.GREEN_BLUE;
        } else if (maxValue == red) {
            return RGB.RED;
        } else if (maxValue == green) {
            return RGB.GREEN;
        } else if (maxValue == blue) {
            return RGB.BLUE;
        }

        return RGB.NONE;
    }

    private enum RGB{
        RED, GREEN, BLUE, RED_GREEN, RED_BLUE, GREEN_BLUE, MONOCHROME, NONE;
    }

    public void setRunning(){
        running = !running;
    }

}
