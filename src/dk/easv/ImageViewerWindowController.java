package dk.easv;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
    private int currentImageIndex = 0;
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

        sliderImageDuration.valueProperty().addListener((observable, oldValue, newValue) -> {
            lblDurationSeconds.setText(newValue.intValue() + "");
        });
        sliderImageDuration.setValue(3);
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
            currentImageIndex =
                    (currentImageIndex - 1 + images.size()) % images.size();
            displayImage();
        }
    }

    @FXML
    private void handleBtnNextAction()
    {
        if (!images.isEmpty())
        {
            currentImageIndex = (currentImageIndex + 1) % images.size();
            displayImage();
        }
    }

    private void displayImage()
    {
        if (!images.isEmpty())
        {
            imageView.setImage(images.get(currentImageIndex));
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

        task = new SlideshowTask(images, delay);
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


    public int onDelaySet(MouseEvent mouseEvent) {
        Double value = sliderImageDuration.getValue();
        delay = value.intValue();

        return value.intValue();

    }
}