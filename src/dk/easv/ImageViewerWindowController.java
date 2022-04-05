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
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    public AreaChart<String, Number> areaChartColors;
    private IntegerProperty currentImageIndex = new SimpleIntegerProperty(0);
    private int delay = 3;
    private ExecutorService executor;
    private SlideshowTask slideshowTask;
    private ColorChartTask colorChartTask;


    @FXML
    Parent root;

    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnStop.setDisable(true);

        slideshowTask = new SlideshowTask(images, delay);
        colorChartTask = new ColorChartTask(imageView.imageProperty());


        sliderImageDuration.valueProperty().addListener((observable, oldValue, newValue) -> {
            lblDurationSeconds.setText(newValue.intValue() + "");
            delay = newValue.intValue();
            if (slideshowTask != null && slideshowTask.isRunning())
                slideshowTask.setDelay(newValue.intValue());
        });
        sliderImageDuration.setValue(3);

        areaChartColors.setTitle("Color distribution");
        areaChartColors.setLegendVisible(false);
        areaChartColors.getYAxis().setLabel("Pixels");

        initColorChartTaskListener();
        initStopTask();
    }

    private void initStopTask(){
        Platform.runLater(() -> {
            btnStop.getScene().getWindow().setOnHidden(event -> {
                if (slideshowTask.isRunning()) {
                    slideshowTask.setRunning();
                    slideshowTask.cancel();
                }
            });
        });
    }

    private void initColorChartTaskListener(){
        executor = Executors.newCachedThreadPool();

        colorChartTask.valueProperty().addListener((observable, oldValue, newValue) -> {
            areaChartColors.getData().clear();
            areaChartColors.getData().add(newValue);
        });

        executor.execute(colorChartTask);
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

        slideshowTask.setRunning();
        displayImage();
    }

    public void handleBtnStartAction(ActionEvent event) {
        btnStop.setDisable(false);
        btnStart.setDisable(true);


        executor = Executors.newCachedThreadPool();

        slideshowTask.valueProperty().addListener((observable, oldValue, newValue) -> {
            imageView.setImage(newValue);
        });

        lblFileName.textProperty().bind(slideshowTask.messageProperty());

        slideshowTask.setOnSucceeded((succeededEvent) -> {
            displayImage();
        });

        executor.execute(slideshowTask);
    }

}