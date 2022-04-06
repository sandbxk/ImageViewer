package dk.easv;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

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
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ObservableList<XYChart.Data<String, Number>> colorData = FXCollections.observableArrayList(colorMap.keySet().stream().map(
                key -> new XYChart.Data<String, Number>(key, colorMap.get(key))).toList());

        series.setData(colorData);
        //Map<String, Long> map1 = getColorData(imageProperty.get());
        //Map<String, Long> map2 = getColorData(imageProperty.get());
        //map1.merge("Red", map2.get("Red"), (a, b) -> a + b);

        return series;
    }

    public Map<String, Long> getColorData(Image img){
        // Read through the pixels and count the number of occurrences of each color.
        int threadCount = 16;

        final Map<Color, Long> redCount = new ConcurrentHashMap<>(); // red
        final Map<Color, Long> greenCount = new ConcurrentHashMap<>(); // green
        final Map<Color, Long> blueCount = new ConcurrentHashMap<>(); // blue
        final Map<Color, Long> redGreenCount = new ConcurrentHashMap<>(); // Red and green mix
        final Map<Color, Long> redBlueCount = new ConcurrentHashMap<>(); // Red and blue mix
        final Map<Color, Long> greenBlueCount = new ConcurrentHashMap<>(); // Green and blue mix
        final Map<Color, Long> monochromeCount = new ConcurrentHashMap<>(); // blacks, greys, whites


        Map<String, Long> rgbCount = new HashMap<>();

        //divides the image size with 8 for 8 blocks.
        double blockSizeX = img.getWidth()/threadCount;
        double blockSizeY = img.getHeight()/threadCount;
        final PixelReader pr = img.getPixelReader();


        List<Thread> runners = new ArrayList<>();
        ThreadGroup group = new ThreadGroup("group");

        for (int i = 0; i < threadCount; i++) {
            ImageBlockRunner runnable = new ImageBlockRunner(blockSizeX*i, blockSizeY*i, blockSizeX*(i+1), blockSizeY*(i+1), pr, redCount, greenCount, blueCount, redGreenCount, redBlueCount, greenBlueCount, monochromeCount);
            runners.add(new Thread(group, runnable));
        }
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (Thread runner : runners) {
            executor.execute(runner);
            /*try {
                runner.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

             */
        }

        executor.shutdown();


        /*runners.parallelStream().forEach(runner -> runner.run());

        for (var runner: runners) {
            try {
                runner.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

         */

/*
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

 */

        while (group.activeCount() > 0) {
            try {
                this.wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
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


    public void setRunning(){
        running = !running;
    }

}
