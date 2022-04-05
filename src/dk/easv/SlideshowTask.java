package dk.easv;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SlideshowTask extends Task<Image> {

    private List<Image> imageList;
    private IntegerProperty currentImageIndex = new SimpleIntegerProperty(0);
    private int delay;
    private boolean running = true;

    public SlideshowTask(List<Image> images, int sleepTime) {
        this.imageList = images;
        this.delay = sleepTime;
    }

    @Override
    protected Image call() throws Exception {
        while (running){
            Thread.sleep(sleep(delay));
            getNextImage();
            getColorData();
        }
        return null;
    }


    private void getNextImage() {
        if (!imageList.isEmpty()) {
            int index = (currentImageIndex.get() + 1) % imageList.size();
            currentImageIndex.set(index);
            Image image = imageList.get(currentImageIndex.get());
            updateValue(image);
            updateMessage(new File(image.getUrl()).getName());
        }
    }


    private long sleep(int seconds){
        return seconds * 1000;
    }

    public void setRunning(){
        this.running = !running;
    }

    public void setDelay(int delay){
        this.delay = delay;
    }

    public void countPixelColor(){
        Image img = imageList.get(currentImageIndex.get());
        // Read through the pixels and count the number of occurrences of each color.

        final PixelReader pr = img.getPixelReader();
        final Map<Color, Long> colCount = new HashMap<>();


        for(int x = 0; x < img.getWidth(); x++) {
            for(int y = 0; y < img.getHeight(); y++) {
                final Color col = pr.getColor(x, y);
                if(colCount.containsKey(col)) {
                    colCount.put(col, colCount.get(col) + 1);
                } else {
                    colCount.put(col, 1L);
                }

            }
        }

        // Get the color with the highest number of occurrences .

        final Color dominantCol = colCount.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        System.out.println(dominantCol.toString());
    }

    public Map<String, Long> getColorData(){
        Image img = imageList.get(currentImageIndex.get());
        // Read through the pixels and count the number of occurrences of each color.

        final PixelReader pr = img.getPixelReader();
        final Map<Color, Long> redCount = new HashMap<>();
        final Map<Color, Long> greenCount = new HashMap<>();
        final Map<Color, Long> blueCount = new HashMap<>();
        final Map<Color, Long> redGreenCount = new HashMap<>();
        final Map<Color, Long> redBlueCount = new HashMap<>();
        final Map<Color, Long> greenBlueCount = new HashMap<>();

        Map<String, Long> rgbCount = new HashMap<>();

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

        rgbCount.put("Red", redTotalCount.get());
        rgbCount.put("Green", greenTotalCount.get());
        rgbCount.put("Blue", blueTotalCount.get());
        rgbCount.put("Red-Green", redGreenTotalCount.get());
        rgbCount.put("Red-Blue", redBlueTotalCount.get());
        rgbCount.put("Green-Blue", greenBlueTotalCount.get());

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


        if (maxValue == red && maxValue == green) {
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
        RED, GREEN, BLUE, RED_GREEN, RED_BLUE, GREEN_BLUE, NONE;
    }

    public IntegerProperty getCurrentImageIndexProperty() {
        return currentImageIndex;
    }
}
