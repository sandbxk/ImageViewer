package dk.easv;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;



public class ImageViewerWindowController implements Initializable
{
    private final List<Image> images = new ArrayList<>();
    public Slider sliderImageDuration;
    public Label lblDurationSeconds;
    public Label lblFileName;
    public Button btnStop;
    public Button btnStart;
    public AreaChart areaChartColors;
    private IntegerProperty currentImageIndex = new SimpleIntegerProperty(0);
    private int delay = 3;
    private ExecutorService executor;
    private SlideshowTask task;


    @FXML
    Parent root;

    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnStop.setDisable(true);

        task = new SlideshowTask(images, delay);


        sliderImageDuration.valueProperty().addListener((observable, oldValue, newValue) -> {
            lblDurationSeconds.setText(newValue.intValue() + "");
            delay = newValue.intValue();
            if (task != null && task.isRunning())
                task.setDelay(newValue.intValue());
        });
        sliderImageDuration.setValue(3);
        //currentImageIndex.bind(task.getCurrentImageIndexProperty());

        CategoryAxis xAxis = new CategoryAxis(FXCollections.observableArrayList(Arrays.asList("Red", "Red and Green", "Green", "Green and Blue", "Blue", "Blue and Red")));
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Pixels");

        areaChartColors.setTitle("Color distribution");

        currentImageIndex.addListener((observable, oldValue, newValue) -> {
            areaChartColors.getData().clear();
            XYChart.Series series = new XYChart.Series();
            XYChart.Series seriesNames = new XYChart.Series();
            Map<String, Long> colorMap = task.getColorData();

            ObservableList<Long> colorData = FXCollections.observableArrayList(colorMap.get("Red"),
                    colorMap.get("Red and Green"), colorMap.get("Green"),
                    colorMap.get("Green and Blue"), colorMap.get("Blue"), colorMap.get("Blue and Red"));
            ObservableList<String> colorNames = FXCollections.observableArrayList(colorMap.keySet().stream().toList());

            series.setData(colorData);
            seriesNames.setData(colorNames);

            areaChartColors.getData().add(new XYChart.Data<>(seriesNames, series));
        });

        Platform.runLater(() -> {
            btnStop.getScene().getWindow().setOnHidden(event -> {
                if (task.isRunning())
                    task.setRunning();
            });
        });
    }



    @FXML
    private void handleBtnLoadAction()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (!files.isEmpty())
        {
            files.forEach((File f) ->
            {
                images.add(new Image(f.toURI().toString()));
            });
            displayImage();
        }
    }

    @FXML
    private void handleBtnPreviousAction()
    {
        if (!images.isEmpty())
        {
            int index = (currentImageIndex.get() - 1 + images.size()) % images.size();
            currentImageIndex.set(index) ;
            displayImage();
        }
    }

    @FXML
    private void handleBtnNextAction()
    {
        if (!images.isEmpty())
        {
            int index = (currentImageIndex.get() + 1) % images.size();
            currentImageIndex.set(index);
            displayImage();
        }
    }

    private void displayImage()
    {
        if (!images.isEmpty())
        {
            imageView.setImage(images.get(currentImageIndex.get()));
            System.out.println(currentImageIndex.get());
        }
    }

    public void handleBtnStopAction(ActionEvent event) {
        btnStop.setDisable(true);
        btnStart.setDisable(false);

        task.setRunning();
        displayImage();
    }

    public void handleBtnStartAction(ActionEvent event) {
        btnStop.setDisable(false);
        btnStart.setDisable(true);

        //task = new SlideshowTask(images, delay);
        executor = Executors.newCachedThreadPool();

        task.valueProperty().addListener((observable, oldValue, newValue) -> {
            imageView.setImage(newValue);
        });

        lblFileName.textProperty().bind(task.messageProperty());

        task.setOnSucceeded((succeededEvent) -> {
            displayImage();
        });

        executor.execute(task);

    }

}