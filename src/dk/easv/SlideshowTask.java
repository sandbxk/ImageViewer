package dk.easv;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class SlideshowTask extends Task<Image> {

    private List<Image> imageList;
    private int currentImageIndex = 0;
    private int sleepTime;
    private boolean running = true;

    public SlideshowTask(List<Image> images, int sleepTime) {
        this.imageList = images;
        this.sleepTime = sleepTime;
    }

    @Override
    protected Image call() throws Exception {
        while (running){
            Thread.sleep(sleep(sleepTime));
            getNextImage();
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
}
