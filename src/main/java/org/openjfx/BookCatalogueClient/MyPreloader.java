package org.openjfx.BookCatalogueClient;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MyPreloader extends Preloader{
	
	private Stage preloaderStage;
	
	private final StackPane parent = new StackPane();
	
	 @Override
	 public void init() throws Exception {

	        Image image = new Image(getClass().getResource("splash-screen.png").toExternalForm());
	        ImageView imageView = new ImageView(image);
	        imageView.setPreserveRatio(true);
	        imageView.setFitWidth(500);
	        this.parent.getChildren().add(imageView);
	 }
	 
	 @Override
	 public void start(Stage stage) throws Exception {
	        this.preloaderStage = stage;
	        Scene scene = new Scene(parent, 481, 280);
	        scene.setFill(Color.TRANSPARENT);
	        stage.setScene(scene);
	        stage.getIcons().add(new Image(getClass().getResource("LibroIcon.png").toExternalForm()));
	        stage.initStyle(StageStyle.TRANSPARENT);
	        stage.centerOnScreen();
	        stage.show();
	    }

	 @Override
	 public void handleStateChangeNotification(StateChangeNotification info) {

	        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
	            this.preloaderStage.close();
	        }
	    }

}
