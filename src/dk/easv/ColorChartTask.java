package dk.easv;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

public class ColorChartTask extends Task<ObservableList<XYChart.Series<String, Number>>> {

    private ObjectProperty<Image> imageProperty;
    private boolean running = true;

    public ColorChartTask(ObjectProperty<Image> imageProperty) {
        this.imageProperty = imageProperty;

    }

    @Override
    protected ObservableList<XYChart.Series<String, Number>> call() throws Exception {
            Image currentImage = imageProperty.get();
        while (running) {
            if (imageProperty.get() != currentImage) {
                currentImage = imageProperty.get();
                updateValue(getSeriesData(getColorData(currentImage)));
            }
        }
        return null;
    }

    private ObservableList<XYChart.Series<String, Number>> getSeriesData(Map<String, Long> colorMap) {
        BigInteger totalcount = BigInteger.valueOf(0);
        for (Long count : colorMap.values()) {
            totalcount = totalcount.add(BigInteger.valueOf(count));
        }


        ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();
        for (Map.Entry<String, Long> entry : colorMap.entrySet()) {
            XYChart.Series<String, Number> seriesData = new XYChart.Series<>();
            seriesData.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            series.add(seriesData);
        }

        return series;
    }

    /**
     * Returns a map of colors and their number of occurrences.
     * Uses 4 threads/callables to speed up the process, and merge the results
     * of each callable into a single map that is returned.
     * @param img
     * @return
     */
    public Map<String, Long> getColorData(Image img){
        /*4 threads. Each thread will get a part of the image.
        Using more threads makes it harder to divide the image properly without lossy double to int conversion, which
        causes pixel loss and therefore innacuracies in the color readings.   */
        int threadCount = 4;

        //Divides the image into 4 equal parts
        int width = img.widthProperty().intValue();
        int height = img.heightProperty().intValue();
        int centerX = Math.round(width/2);
        int centerY = Math.round(height/2);

        //The reader for each individual pixel. Reads the color value in the Callable.
        final PixelReader pr = img.getPixelReader();

        List<Callable<Map<String, Long>>> runners = new ArrayList<>();

        //Thread for the top left
        ImageBlockLooper callable1 = new ImageBlockLooper(0, 0, centerX, centerY, pr);
        runners.add(callable1);

        //Thread for the top right
        ImageBlockLooper callable2 = new ImageBlockLooper(centerX, 0, width, centerY, pr);
        runners.add(callable2);

        //Thread for the bottom left
        ImageBlockLooper callable3 = new ImageBlockLooper(0, centerY, centerX, height, pr);
        runners.add(callable3);

        //Thread for the bottom right
        ImageBlockLooper callable4 = new ImageBlockLooper(centerX, centerY, width, height, pr);
        runners.add(callable4);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        //Total result - all results will be merged into this map.
        Map<String, Long> totalColorResult = new ConcurrentHashMap<>();

        //The results of each thread is added to the total result using Futures from the Callable.
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
