package org.openjfx.BookCatalogueClient;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LoggedController {
	
	@FXML
	Label welcomeLabel;
	
	@FXML
	Label headerLabel;
	
	public void setWelcomeUsername(String header, String username) {
		headerLabel.setText(header);
		welcomeLabel.setText("                                   " + "Welcome " + username + "!");
	}
}
