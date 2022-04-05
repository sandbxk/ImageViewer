package dk.easv;

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
    private int currentImageIndex = 0;
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
            rgbCountPixelColor();
        }
        return null;
    }

    private void getNextImage() {
        if (!imageList.isEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % imageList.size();
            Image image = imageList.get(currentImageIndex);
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
        Image img = imageList.get(currentImageIndex);
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

    public void rgbCountPixelColor(){
        Image img = imageList.get(currentImageIndex);
        // Read through the pixels and count the number of occurrences of each color.

        final PixelReader pr = img.getPixelReader();
        final Map<Color, Long> redCount = new HashMap<>();
        final Map<Color, Long> greenCount = new HashMap<>();
        final Map<Color, Long> blueCount = new HashMap<>();


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

        System.out.println("RED: " + redTotalCount + "   GREEN: " + greenTotalCount +  "   BLUE: " + blueTotalCount);
    }

    private RGB rgbChecker(Color color){
        double red = color.getRed();
        double green = color.getGreen();
        double blue = color.getBlue();

        double[] values = {red, green, blue};
        Arrays.sort(values);

        if (values[values.length - 1] == red) {
            return RGB.RED;
        } else if (values[values.length - 1] == green) {
            return RGB.GREEN;
        } else if (values[values.length - 1] == blue) {
            return RGB.BLUE;
        }
        return null;
    }

    private enum RGB{
        RED, GREEN, BLUE;
    }
}
