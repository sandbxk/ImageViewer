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


}
