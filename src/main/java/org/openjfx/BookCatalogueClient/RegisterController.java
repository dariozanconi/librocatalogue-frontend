package org.openjfx.BookCatalogueClient;

import java.util.function.Consumer;

import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.RegisterRequest;
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


public class RegisterController {
	
	@FXML
	TextField usernameField;
	
	@FXML
	TextField passwordField;
	
	@FXML
	TextField passwordField2;
	
	@FXML
	TextField codeField;
	
	@FXML
	Label errorLabel;
	
	@FXML
	Button loginButton;
	
	@FXML
	Button registerButton;
	
	Users user = new Users();
	RegisterRequest registerRequest = new RegisterRequest();
	private final ApiTask registerTasks = new ApiTask();
	private ApiResponse<Users> response;
	private final ApiTask loginTasks = new ApiTask();
	Consumer<String> onRegisterClicked;
	Runnable onLoginClicked;
	
	@FXML
	public void initialize() {
		
		passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					register();
				}			
			}
			
		});
		
		passwordField2.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					register();
				}			
			}
			
		});
		
		codeField.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					register();
				}			
			}
			
		});
	}
	
	@FXML
	public void register() {
		
		errorLabel.setText("");
		if (passwordField.getText().equals(passwordField2.getText())) {
			
			registerRequest.setUsername(usernameField.getText());
			registerRequest.setPassword(passwordField.getText());
			registerRequest.setCode(codeField.getText());
			
			if (!registerRequest.getUsername().isEmpty() && !registerRequest.getPassword().isEmpty() && !registerRequest.getCode().isEmpty()) {
				Task<ApiResponse<Users>> task = registerTasks.registerTask(registerRequest);
				task.setOnSucceeded(e -> {
					response = task.getValue();
					if (response.isSuccess()) {	
						
						Users user = response.getData();
			            Task<ApiResponse<String>> loginTask = loginTasks.loginTask(user);
			            loginTask.setOnSucceeded(ev -> {
			                ApiResponse<String> loginResponse = loginTask.getValue();
			                if (loginResponse.isSuccess()) {
			                    onRegisterClicked.accept(loginResponse.getData()); // Token weiterreichen
			                } else {
			                    errorLabel.setText(loginResponse.getError().getMessage());
			                }
			            });
			            loginTask.setOnFailed(ev -> {
			                errorLabel.setText(ev.getSource().getException().getMessage());
			            });
			            new Thread(loginTask).start();
						
					} else {
						errorLabel.setText(response.getError().getMessage());
					}				
				});
				task.setOnFailed(e -> {
					errorLabel.setText(response.getError().getMessage());

				});
				new Thread(task).start();				
			} else {
				errorLabel.setText("empty fields");
			}
		} else {
			errorLabel.setText("Passwords not matching");
		}
	}
	
	public void openLogin() {
		if (onLoginClicked!=null) {
			onLoginClicked.run();
		}
	}
	
	public void setOnRegisterClicked(Consumer<String> callback) {
		this.onRegisterClicked = callback;
	}
	
	public void setOnLoginClicked(Runnable callback) {
		this.onLoginClicked = callback;
	}
	
	
}
