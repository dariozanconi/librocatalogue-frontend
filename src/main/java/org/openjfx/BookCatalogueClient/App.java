package org.openjfx.BookCatalogueClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


/**  
 * JavaFX App
 */
public class App extends Application {
		
	@Override
	public void init() throws Exception {
	    super.init();	        
	    LoadingTasks();
	}
	
	private void LoadingTasks() throws InterruptedException {	        	        	    

        Thread.sleep(5000);
	}
	    
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Home.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root,1200,650);		
			
			primaryStage.setScene(scene);
			primaryStage.setTitle("LibroCatalogue");
			primaryStage.getIcons().add(new Image(getClass().getResource("LibroIcon.png").toExternalForm()));
			
			primaryStage.show();							  
		    		    		    			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.setProperty("javafx.preloader", MyPreloader.class.getCanonicalName());
		launch(args);
	}
	
}