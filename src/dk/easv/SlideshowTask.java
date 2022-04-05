package dk.easv;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            countPixelColor();
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
}
