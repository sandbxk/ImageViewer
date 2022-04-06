package dk.easv;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.*;
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
        //TODO: Separate Series for each color.
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ObservableList<XYChart.Data<String, Number>> colorData = FXCollections.observableArrayList(colorMap.keySet().stream().map(
                key -> new XYChart.Data<String, Number>(key, colorMap.get(key))).toList());
        series.setData(colorData);
        return series;
    }

    public Map<String, Long> getColorData(Image img){
        // Read through the pixels and count the number of occurrences of each color.
        int threadCount = 16;

        //IDEAS:
        // https://stackoverflow.com/questions/2190787/dividing-the-image-into-four-equal-parts-in-jai
        // https://stackoverflow.com/questions/2405898/how-to-split-an-image-into-four-equal-parts-in-java
        // http://kalanir.blogspot.com/2010/02/how-to-split-image-into-chunks-java.html


        //divides the image size with 8 for 8 blocks.
        //BufferedImage bufferedImage = SwingFXUtils.fromFXImage(img, null);
        int width = img.widthProperty().intValue();
        int height = img.heightProperty().intValue();

        //TODO: Fix lossy double conversion on division - currently loosing pixels.
        int blockSizeX = Math.round(width / threadCount);
        int blockSizeY = Math.round(height / threadCount);
        final PixelReader pr = img.getPixelReader();

        List<Callable<Map<String, Long>>> runners = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            ImageBlockLooper callable = new ImageBlockLooper(blockSizeX*i, blockSizeY*i, blockSizeX*(i+1), blockSizeY*(i+1), pr);
            runners.add(callable);
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        Map<String, Long> totalColorResult = new ConcurrentHashMap<>(); //Total result - all results will be merged into this map.


        try {
            Map<String, Long> result = executor.submit(runners.get(0)).get();
            result.forEach((key, value) ->
                    totalColorResult.merge(key, value, (v1, v2) -> v1 + v2) );
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        //The results of each thread is added to the total result.
        for (Callable<Map<String, Long>> runner : runners) {
            try {
                Map<String, Long> result = executor.submit(runner).get();
                result.forEach((key, value) ->
                        totalColorResult.merge(key, value, (v1, v2) -> v1 + v2) );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        executor.shutdown();

        System.out.println("RED: " + totalColorResult.get("Red") + "   GREEN: " + totalColorResult.get("Green") +  "   BLUE: " + totalColorResult.get("Blue"));
        return totalColorResult;
    }

    public void setRunning(){
        running = !running;
    }

}
