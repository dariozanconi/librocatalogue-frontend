package org.openjfx.BookCatalogueClient;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Collection;
import org.openjfx.BookCatalogueClient.service.JwtUtils;
import org.openjfx.BookCatalogueClient.task.ApiTask;
import org.openjfx.BookCatalogueClient.task.TaskTracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class CollectionsTabController {
	
	@FXML 
	private ResourceBundle resources;
	
	@FXML
	private ListView<Collection> collectionList;
	
	@FXML
	private Button addButton;
	
	@FXML
	private Button deleteButton;
	
	@FXML
	private Label messageLabel;
	
	@FXML
	private TextField searchField;
	
	@FXML
	private Button searchButton;
	
	private HomeController homeController;
	private final ApiTask collectionTasks = new ApiTask();
	private ApiResponse<List<Collection>> response;
	private ApiResponse<Collection> responseCollection;
	private ApiResponse<String> responseRemoveCollection;
	private List<Collection> loadedCollections;
	private List<Collection> selectedCollections = new ArrayList<>();
	Consumer<Collection> onCollectionOpened;
	Consumer<Collection> onCollectionClicked;	
	JwtUtils jwtUtils = new JwtUtils();
	String token;
	
	@FXML
    public void initialize() {		
		addButton.setDisable(true);
		deleteButton.setDisable(true);	
		showList();		
	}
	
	public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
	
	public void showList() {
		
		loadCollections(
			    collections -> {
			       this.loadedCollections = collections;
			       for (Collection c : collections) {			            		      			            
			        }
			       ObservableList<Collection> observableCollection = FXCollections.observableList(loadedCollections);		
			       collectionList.setItems(observableCollection);
		
			       collectionList.setCellFactory(lv -> new ListCell<>() {
			    	   
		                private final Hyperlink hyperlink = new Hyperlink();
		                private final CheckBox checkBox = new CheckBox();
		                private final HBox hbox = new HBox(10, checkBox, hyperlink);

		                {	
		                	hbox.setAlignment(Pos.CENTER_LEFT);
		                	hbox.setStyle("-fx-background-color: transparent");
		                    hyperlink.setOnAction(e -> {
		                        Collection collection = getItem();
		                        if (collection != null) {
		                            if (onCollectionOpened != null)
		                                onCollectionOpened.accept(collection);
		                            if (onCollectionClicked != null)
		                                onCollectionClicked.accept(collection);
		                        }
		                    });
		                    
		                    checkBox.setOnAction(e -> {
		                        Collection collection = getItem();
		                        if (collection != null) {
		                            if (checkBox.isSelected()) {
		                                selectedCollections.add(collection);
		                            } else {
		                                selectedCollections.remove(collection);
		                            }
		                        }
		                    });
		                }

		                @Override
		                protected void updateItem(Collection item, boolean empty) {
		                    super.updateItem(item, empty);
		                    if (empty || item == null) {
		                        setGraphic(null);
		                    } else {
		                        hyperlink.setText(item.getName());
		                        hyperlink.setFont(new Font("Sans Serif", 15));
		                        setGraphic(hbox);
		                    }
		                }
		            });
		        },
		        error -> {
		            System.err.println("Error loading collections: " + error.getMessage());
		        }
		    );
		}
	
	public void loadCollections(Consumer<List<Collection>> onSuccess, Consumer<Throwable> onError) {
		String keyword = searchField.getText();
		if (keyword.isEmpty()) {
			Task<ApiResponse<List<Collection>>> task = collectionTasks.loadCollectionsTask();
			task.setOnSucceeded(e -> {
				response = task.getValue();
				if (response.isSuccess()) {										
					onSuccess.accept(response.getData());
				
				} 			
			});
			task.setOnFailed(e -> {
				onError.accept(task.getException());
			});
		
			new Thread(task).start();
		} else {
			Task<ApiResponse<List<Collection>>> task = collectionTasks.searchCollectionsTask(keyword);
			task.setOnSucceeded(e -> {
				response = task.getValue();
				if (response.isSuccess()) {										
					onSuccess.accept(response.getData());
				
				} 			
			});
			task.setOnFailed(e -> {
				onError.accept(task.getException());
			});
		
			new Thread(task).start();
		}
				
	}
	
	@FXML
	public void searchCollections() {
		showList();
	}
	
	@FXML
	public void addCollection() {
		
		TextField inputField = new TextField();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(
			    getClass().getResource("AlertStyle.css").toExternalForm()
			);
		alert.setTitle(resources.getString("alert.collectionname"));
		alert.setHeaderText(resources.getString("alert.insertname"));
		alert.getDialogPane().setContent(inputField);

		ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButton = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(okButton, cancelButton);

		Optional<ButtonType> result = alert.showAndWait();
		String userInput = null;
		if (result.isPresent() && result.get() == okButton) {
		    userInput = inputField.getText();
		    Collection collection = new Collection(userInput);
		    System.out.println(collection.getName());
		    
		    Task<ApiResponse<Collection>> task = collectionTasks.addCollectionTask(collection, token);
		    task.setOnSucceeded(e -> {
				responseCollection = task.getValue();
				if (responseCollection.isSuccess()) {										
					Tab selected = homeController.getTabPane().getSelectionModel().getSelectedItem();
					homeController.getTabPane().getTabs().remove(selected);
					homeController.openCollectionsTab();				
				} else {
					Alert alert2 = new Alert(AlertType.ERROR);
					alert.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
					alert2.setTitle(resources.getString("alert.addcollectionfail"));
					alert2.setHeaderText(resources.getString("alert.collectionexists"));
					System.out.println(responseCollection.getError().getMessage());
					alert2.showAndWait();
				}
			});
			task.setOnFailed(e -> {
				Throwable ex = task.getException();
	            ex.printStackTrace();
			});
			
			new Thread(task).start();
		} 
	}
	
	@FXML
	public void deleteCollection() {
			
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(
			    getClass().getResource("AlertStyle.css").toExternalForm()
			);
		alert.setTitle(resources.getString("alert.confirm"));
		alert.setHeaderText(resources.getString("alert.deletecollectionconfirm"));
		Optional<ButtonType> result = alert.showAndWait();
		ButtonType button = result.orElse(ButtonType.CANCEL);
	
		if (button==ButtonType.OK) {
			TaskTracker tracker = new TaskTracker(selectedCollections.size(), 
					() -> {
						Alert alert2 = new Alert(AlertType.INFORMATION);
						alert2.getDialogPane().getStylesheets().add(
							    getClass().getResource("AlertStyle.css").toExternalForm()
							);
						alert2.setTitle(resources.getString("alert.deletedcollection"));
						alert2.setHeaderText(resources.getString("alert.deletedcollectionsuccess"));
						alert2.showAndWait();
						
						Tab selected = homeController.getTabPane().getSelectionModel().getSelectedItem();
						homeController.getTabPane().getTabs().remove(selected);
						homeController.openCollectionsTab();						
					},
					() -> { 
				        Alert alert3 = new Alert(Alert.AlertType.ERROR);
				        alert3.getDialogPane().getStylesheets().add(
							    getClass().getResource("AlertStyle.css").toExternalForm()
							);
				        alert3.setTitle(resources.getString("alert.error"));
				        alert3.setHeaderText(resources.getString("alert.deletecollectionfail"));
				        alert3.showAndWait();
				        
				        Tab selected = homeController.getTabPane().getSelectionModel().getSelectedItem();
						homeController.getTabPane().getTabs().remove(selected);
						homeController.openCollectionsTab();				       
				        }
					);
			for (Collection collection : selectedCollections) {
				Task<ApiResponse<String>> task = collectionTasks.removeCollectionTask(collection.getId(), token);
				task.setOnSucceeded(e -> {
					responseRemoveCollection = task.getValue();
					if (!responseRemoveCollection.isSuccess()) {										
						Alert alert2 = new Alert(AlertType.INFORMATION);
						alert2.getDialogPane().getStylesheets().add(
							    getClass().getResource("AlertStyle.css").toExternalForm()
							);
						alert2.setTitle(resources.getString("alert.error"));
						alert2.setHeaderText(resources.getString("alert.collectionnotfound"));
						alert2.showAndWait();
						tracker.taskFailed();
					} else {
						tracker.taskSucceeded();
					}
				});
				task.setOnFailed(e -> {
					Throwable ex = task.getException();
		            ex.printStackTrace();
				});
				
				new Thread(task).start();
			}
		}
	}
	
	public void setOnCollectionOpened(Consumer<Collection> callback) {
		this.onCollectionOpened = callback;
	}
	
	public void setOnCollectionClicked(Consumer<Collection> callback) {
		this.onCollectionClicked = callback;
	}
	
	public void setHeader(String header) {
		messageLabel.setText(header);
	}
	
	public void setToken(String token) {
		this.token = token;
		if (token!=null && !jwtUtils.isTokenExpired(token)) {
			addButton.setDisable(false);
			deleteButton.setDisable(false);
		}
	}
}
