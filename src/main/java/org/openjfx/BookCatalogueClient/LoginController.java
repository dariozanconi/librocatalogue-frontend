package org.openjfx.BookCatalogueClient;

import java.util.function.Consumer;

import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Users;
import org.openjfx.BookCatalogueClient.task.ApiTask;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController {
	
	@FXML
	TextField usernameField;
	
	@FXML
	TextField passwordField;
	
	@FXML
	Label errorLabel;
	
	@FXML
	Button signinButton;
	
	@FXML
	Button registerButton;
	
	Users user = new Users();
	private final ApiTask loginTasks = new ApiTask();
	private ApiResponse<String> response;
	
	Consumer<String> onLoginClicked;
	Runnable onRegisterClicked;
	
	@FXML
	public void initialize() {
		passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					signIn();
				}			
			}
			
		});
		
		usernameField.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					signIn();
				}			
			}
			
		});
	}
	
	@FXML
	public void signIn() {
		
		user.setUsername(usernameField.getText());
		user.setPassword(passwordField.getText());
		errorLabel.setText("");
		
		if (!user.getUsername().isEmpty() && !user.getPassword().isEmpty()) {
			
			login(user);
			
		} else {
			errorLabel.setText("insert a valid username and password");
		}
	}
	
	public void setOnLoginClicked(Consumer<String> callback) {
		this.onLoginClicked = callback;
	}
	
	public void login(Users user) {
		
		Task<ApiResponse<String>> task = loginTasks.loginTask(user);
		task.setOnSucceeded(e -> {
			response = task.getValue();
			if (response.isSuccess()) {										
				onLoginClicked.accept(response.getData());							
			} else {
				errorLabel.setText(response.getData());
			}				
		});
		task.setOnFailed(e -> {
			errorLabel.setText(response.getData());

		});
		new Thread(task).start();
		
		
	}
	
	public void openRegister() {
		if (onRegisterClicked!=null) {
			onRegisterClicked.run();
		}
	}
	
	public String getUsername() {
		return user.getUsername();
	}
	
	public void setOnRegisterClicked(Runnable callback) {
		this.onRegisterClicked = callback;
	}
}
