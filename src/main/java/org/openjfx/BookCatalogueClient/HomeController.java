package org.openjfx.BookCatalogueClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.Collection;
import org.openjfx.BookCatalogueClient.service.JwtUtils;
import org.openjfx.BookCatalogueClient.task.ApiTask;
import org.openjfx.BookCatalogueClient.task.TaskTracker;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class HomeController {
	
	@FXML
	private TabPane tabPane;
	
	@FXML
	private Button showButton;
	
	@FXML
	private TextField searchField;
	
	@FXML
	private Button searchButton;
	
	@FXML
	private Button addButton;
	
	@FXML
	private Button updateButton;
	
	@FXML
	private Button removeButton;
	
	@FXML
	private Button loginButton;
	
	@FXML
	private MenuButton profileMenu;
	
	@FXML
	private MenuItem logoutItem;
	
	@FXML 
	private MenuItem deleteItem;
	
	@FXML
	private MenuItem showCollectionsItem;
	
	@FXML
	private MenuItem addBooksCollectionItem;
	
	@FXML
	private MenuItem removeBooksCollectionItem;
	
	
	private String token;
	private JwtUtils jwtUtils = new JwtUtils();
	private List<Book> selectedBooks = new ArrayList<Book>();
	private List<BooksTabController> controllerList = new ArrayList<>(); 
	private final ApiTask bookTasks = new ApiTask();
	private final ApiTask userTasks = new ApiTask();
	private final ApiTask collectionsTasks = new ApiTask();
	private ApiResponse<String> response;
	private ApiResponse<?> collectionResponse;
	private Collection actualCollection;
	
	public TabPane getTabPane() {		
        return tabPane;   
    }			
	
	@FXML
	public void initialize() {
		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
			deselectAllBooks();
			if (newTab != null) {
		        if (!(newTab.getUserData() instanceof Collection)) {
		        	this.actualCollection = (Collection) newTab.getUserData();
		            removeBooksCollectionItem.setDisable(true);
		        } else if (newTab.getUserData() instanceof Collection && token!=null && !jwtUtils.isTokenExpired(token))
				removeBooksCollectionItem.setDisable(false);
			}		    
		});
		openBooksTabGeneric("All Books", BooksTabController::initNormal);
		searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					showSearchResult();
				}			
			}
			
		});
		
	    disableButtons();
	}
	
	public void openBooksTabGeneric(String header, Consumer<BooksTabController> initializer) {
		try {
			FXMLLoader booksLoader = new FXMLLoader(getClass().getResource("BooksTab.fxml"));
			Parent content = booksLoader.load();
	    	
			BooksTabController booksTabController = booksLoader.getController();
			controllerList.add(booksTabController);
			booksTabController.setHomeController(this);
			if (token!=null && !jwtUtils.isTokenExpired(token))
				booksTabController.setToken(token);			
			booksTabController.setHeader(header);
			
			initializer.accept(booksTabController); 
			
			Tab tab = new Tab(header, content);	
			tabPane.getTabs().add(tab);
			tabPane.getSelectionModel().select(tab);			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void openBooksTab(String header) {
		removeBooksCollectionItem.setDisable(true);
	    openBooksTabGeneric(header, BooksTabController::initNormal);  
	}
	
	@FXML
	public void showAll() {
		deselectAllBooks();
		removeBooksCollectionItem.setDisable(true);
		openBooksTabGeneric("Show All", BooksTabController::initNormal);
	}

	@FXML
	public void showSearchResult() {
		deselectAllBooks();
		removeBooksCollectionItem.setDisable(true);
	    String keyword = searchField.getText();
	    if (keyword != null && !keyword.isBlank()) {
	        openBooksTabGeneric("Search results '"+ keyword + "'", controller -> controller.initSearch(keyword));	   
	    }
	}
	
	@FXML
	public void openAddBookTab() {
		deselectAllBooks();
		if (token!=null && !jwtUtils.isTokenExpired(token)) {
			
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("AddBookTab.fxml"));
				Parent addBookRoot = loader.load();
				
				AddBookController controller = loader.getController();
				controller.init(token);
				Tab addBookTab = new Tab("Add Books", addBookRoot);
				
		        tabPane.getTabs().add(addBookTab);
		        tabPane.getSelectionModel().select(addBookTab);
		        removeBooksCollectionItem.setDisable(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			openLoginTab();
			disableButtons();
		}
	}
	
	@FXML
	public void updateSelectedBooks() {
		
		if (selectedBooks!=null) {
			List<Book> booksToUpdate = new ArrayList<Book>();
			for (Book book : selectedBooks) {
				booksToUpdate.add(book);
			}
			
			for (Book book : booksToUpdate) {
				openUpdateBookTab(book);
			}
			deselectAllBooks();
			removeBooksCollectionItem.setDisable(true);
		}		
	}
	
	public void openUpdateBookTab(Book book) {

		if (token!=null && !jwtUtils.isTokenExpired(token)) {
			
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("UpdateBookTab.fxml"));
				Parent updateBookRoot = loader.load();
				
				UpdateBookController controller = loader.getController();
				controller.setBookToUpdate(book, token);				
				controller.setHomeController(this);
				
				Tab updateBookTab = new Tab("Update "+ book.getTitle(), updateBookRoot);
				
		        tabPane.getTabs().add(updateBookTab);
		        tabPane.getSelectionModel().select(updateBookTab);
		        removeBooksCollectionItem.setDisable(true);

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			openLoginTab();
			disableButtons();
		}
		
	}
	
	@FXML
	public void removeSelectedBooks() {
		
		TaskTracker tracker = new TaskTracker(selectedBooks.size(), 
				() -> {
					Alert alert2 = new Alert(AlertType.INFORMATION);
					alert2.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
					alert2.setTitle("Books deleted");
					alert2.setHeaderText("Books deleted successfully");
					alert2.showAndWait();
					
					tabPane.getTabs().clear();
					openBooksTab("All books");
				},
				() -> { 
			        Alert alert = new Alert(Alert.AlertType.ERROR);
			        alert.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
			        alert.setTitle("Error");
			        alert.setHeaderText("Some books could not be deleted.");
			        alert.showAndWait();
			        
			        tabPane.getTabs().clear();
					openBooksTab("All books");
			        }
				);
		if (!selectedBooks.isEmpty()) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirm");
			alert.getDialogPane().getStylesheets().add(
				    getClass().getResource("AlertStyle.css").toExternalForm()
				);
			alert.setHeaderText("Are you sure you want to delete the selected books?");
			Optional<ButtonType> result = alert.showAndWait();
			ButtonType button = result.orElse(ButtonType.CANCEL);
		
			if (button==ButtonType.OK) {
			
				for (Book book : selectedBooks) {			
					Task<ApiResponse<String>> task = bookTasks.deleteBookTask(book.getId(), token);
					task.setOnSucceeded(e -> {
					response = task.getValue();			
					if (!response.isSuccess()) {														
						Alert alert2 = new Alert(AlertType.INFORMATION);
						alert2.getDialogPane().getStylesheets().add(
							    getClass().getResource("AlertStyle.css").toExternalForm()
							);
						alert2.setTitle("Book not found");
						alert2.setHeaderText(response.getData());
						alert2.showAndWait();
						tracker.taskFailed();
					} else
					tracker.taskSucceeded();
					});
					task.setOnFailed(e -> {
						Throwable ex = task.getException();
						ex.printStackTrace();
					});
					new Thread(task).start();
			
				}				
			}		
		}		
	}
	
	public void removeBook(Book book, String token) {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(
			    getClass().getResource("AlertStyle.css").toExternalForm()
			);
		alert.setTitle("Confirm");
		alert.setHeaderText("Are you sure you want to delete this book?");
		Optional<ButtonType> result = alert.showAndWait();
		ButtonType button = result.orElse(ButtonType.CANCEL);
	
		if (button==ButtonType.OK) {
			
			Task<ApiResponse<String>> task = bookTasks.deleteBookTask(book.getId(), token);
			task.setOnSucceeded(e -> {
				response = task.getValue();			
				if (!response.isSuccess()) {														
					Alert alert2 = new Alert(AlertType.INFORMATION);
					alert2.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
					alert2.setTitle("Book not found");
					alert2.setHeaderText(response.getData());
					alert2.showAndWait();
				} else {
					Alert alert3 = new Alert(AlertType.INFORMATION);
					alert3.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
					alert3.setTitle("Books deleted");
					alert3.setHeaderText("Book deleted successfully");
					alert3.showAndWait();
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
	public void openCollectionsTab() {
		deselectAllBooks();
		try {
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionsTab.fxml"));
			Parent collectionsRoot = loader.load();
			
			CollectionsTabController controller = loader.getController();
			Tab collectionsTab = new Tab("Collections", collectionsRoot);
			if (token!=null && !jwtUtils.isTokenExpired(token))
				controller.setToken(token);
			controller.setHomeController(this);
			
			controller.setOnCollectionOpened( collection -> {
				Collection selectedCollection = collection;
				tabPane.getTabs().remove(collectionsTab);
				openBooksCollection(selectedCollection);	
			});
						
			tabPane.getTabs().add(collectionsTab);
			tabPane.getSelectionModel().select(collectionsTab);
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	@FXML
	public void addBooksToCollection() {
		
		List<Book> booksToAdd = new ArrayList<Book>();
		if (selectedBooks!=null) {
			for (Book book : selectedBooks) {
				booksToAdd.add(book);
			}
		}
		
		try {
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionsTab.fxml"));
			Parent collectionsRoot = loader.load();
			
			CollectionsTabController controller = loader.getController();
			Tab collectionsTab = new Tab("Collections", collectionsRoot);
			controller.setHeader("Select the collection where you want to add the books:");			
			controller.setOnCollectionClicked( collection -> {
				
				TaskTracker tracker = new TaskTracker(booksToAdd.size(), 
						() -> {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.getDialogPane().getStylesheets().add(
								    getClass().getResource("AlertStyle.css").toExternalForm()
								);
							alert.setTitle("Books added");
							alert.setHeaderText("Book added successfully to the collection");
							alert.showAndWait();
				
							deselectAllBooks();
							tabPane.getTabs().remove(collectionsTab);
							openBooksCollection(collection);
					
						},
			
						() -> { 
							Alert alert = new Alert(Alert.AlertType.ERROR);
							alert.getDialogPane().getStylesheets().add(
								    getClass().getResource("AlertStyle.css").toExternalForm()
								);
							alert.setTitle("Error");
							alert.setHeaderText("Some books could not be added.");
							alert.showAndWait();
							
							deselectAllBooks();
							tabPane.getTabs().remove(collectionsTab);
							openBooksCollection(collection);
						}
						);	
				for (Book book : booksToAdd) {
					Task<ApiResponse<Book>> task = collectionsTasks.addBookCollectionTask(collection.getId(), book, token);
					task.setOnSucceeded(e -> {
						collectionResponse = task.getValue();			
							if (!collectionResponse.isSuccess()) {														
								Alert alert = new Alert(AlertType.INFORMATION);
								alert.getDialogPane().getStylesheets().add(
									    getClass().getResource("AlertStyle.css").toExternalForm()
									);
								alert.setTitle("Book not found");
								alert.setHeaderText(collectionResponse.getError().getMessage());
								alert.showAndWait();
								tracker.taskFailed();
							} else
							tracker.taskSucceeded();
						});
						task.setOnFailed(e -> {
							Throwable ex = task.getException();
				            ex.printStackTrace();
						});
						new Thread(task).start();
												
				}
							
			});
			
			tabPane.getTabs().add(collectionsTab);
			tabPane.getSelectionModel().select(collectionsTab);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@FXML
	public void removeBooksToCollection() throws InterruptedException {
		TaskTracker tracker = new TaskTracker(selectedBooks.size(),
		        () -> { 
		            Alert alert = new Alert(Alert.AlertType.INFORMATION);
		            alert.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
		            alert.setTitle("Books removed");
		            alert.setHeaderText("All books removed successfully from " + actualCollection.getName());
		            alert.showAndWait();
		            
		            deselectAllBooks();
		            removeBooksCollectionItem.setDisable(true);
					Tab selected = tabPane.getSelectionModel().getSelectedItem();
					tabPane.getTabs().remove(selected);
					//openBooksCollection(actualCollection); 
		            
		        },
		        () -> { 
		            Alert alert = new Alert(Alert.AlertType.ERROR);
		            alert.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
		            alert.setTitle("Error");
		            alert.setHeaderText("Some books could not be removed.");
		            alert.showAndWait();
		            
		            deselectAllBooks();
		            removeBooksCollectionItem.setDisable(true);
					Tab selected = tabPane.getSelectionModel().getSelectedItem();
					tabPane.getTabs().remove(selected);
					openBooksCollection(actualCollection); 
		        }
		    );
	    
			for (Book book : selectedBooks) {
				Task<ApiResponse<String>> task = collectionsTasks.removeBookCollectionTask(actualCollection.getId(), book.getId(), token);
				task.setOnSucceeded(e -> {
					
					collectionResponse = task.getValue();			
						if (!collectionResponse.isSuccess()) {														
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.getDialogPane().getStylesheets().add(
								    getClass().getResource("AlertStyle.css").toExternalForm()
								);
							alert.setTitle("Book not found");
							alert.setHeaderText(collectionResponse.getError().getMessage());
							alert.showAndWait();
							tracker.taskFailed();
						} else
						tracker.taskSucceeded();
					});
					task.setOnFailed(e -> {
						Throwable ex = task.getException();
				           ex.printStackTrace();

					});
					new Thread(task).start();												
			}									
	}

	public void openBooksCollection(Collection collection) {
		deselectAllBooks();				
		openBooksTabGeneric(collection.getName(), c -> c.initCollection(collection));
		if (!jwtUtils.isTokenExpired(token) && token!=null) {
			removeBooksCollectionItem.setDisable(false);
		}
		Tab currentTab = tabPane.getTabs().get(tabPane.getTabs().size() - 1);
		currentTab.setUserData(collection);
		this.actualCollection = collection;		
	}
	
	@FXML
	public void openLoginTab() {
		deselectAllBooks();
		if (jwtUtils.isTokenExpired(token) || token==null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginTab.fxml"));
				Parent loginRoot = loader.load();
			
				LoginController controller = loader.getController();
				Tab loginTab = new Tab("Login", loginRoot);
				controller.setOnLoginClicked(token -> {
					this.token = token;
					if (!jwtUtils.isTokenExpired(token)) {
						enableButtons();
						openLoggedTab("Logged in!");
						tabPane.getTabs().remove(loginTab);
						profileMenu.setText(controller.getUsername().substring(0, 1).toUpperCase() + controller.getUsername().substring(1));
					}
				});	
				controller.setOnRegisterClicked(() -> {
					openRegisterTab();
					tabPane.getTabs().remove(loginTab);
				});
				tabPane.getTabs().add(loginTab);
				tabPane.getSelectionModel().select(loginTab);
				removeBooksCollectionItem.setDisable(true);
			
				} catch (IOException e) {
					e.printStackTrace();
				}	
		} else {
			openLoggedTab("Logged in!");
		}		
	}
	
	
	@FXML
	public void openRegisterTab() {
		deselectAllBooks();
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterTab.fxml"));
			Parent registerRoot = loader.load();
			
			RegisterController controller = loader.getController();
			Tab registerTab = new Tab("Register", registerRoot);
			controller.setOnRegisterClicked(token -> {
				this.token = token;
				if (!jwtUtils.isTokenExpired(token)) {
					enableButtons();
					openLoggedTab("Registered");
					tabPane.getTabs().remove(registerTab);
				}
			});	
			controller.setOnLoginClicked(() -> {
				openLoginTab();
				tabPane.getTabs().remove(registerTab);
			});
			tabPane.getTabs().add(registerTab);
			tabPane.getSelectionModel().select(registerTab);
			removeBooksCollectionItem.setDisable(true);
			
			} catch (IOException e) {
				e.printStackTrace();
			}	
	}
	
	
	public void openLoggedTab(String header) {
		try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("LoggedTab.fxml"));
				Parent loggedRoot = loader.load();
			
				LoggedController controller = loader.getController();
				controller.setWelcomeUsername(header, jwtUtils.getUsernameToken(token));
				Tab loggedTab = new Tab("Login", loggedRoot);
				
				tabPane.getTabs().clear();
				tabPane.getTabs().add(loggedTab);
				tabPane.getSelectionModel().select(loggedTab);
			
				} catch (IOException e) {
					e.printStackTrace();
				}	
	}
	
	
	@FXML
	public void logout() {
		token=null;
		profileMenu.setText("Profile");
		disableButtons();
		tabPane.getTabs().clear();
	}
	
	
	@FXML
	public void deleteAccount() {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(
			    getClass().getResource("AlertStyle.css").toExternalForm()
			);
		alert.setTitle("Confirm");
		alert.setHeaderText("Are you sure you want to delete your account?");
		Optional<ButtonType> result = alert.showAndWait();
		ButtonType button = result.orElse(ButtonType.CANCEL);
		
		if (button==ButtonType.OK) {
			Task<ApiResponse<String>> task = userTasks.deleteAccountTask(token);
			task.setOnSucceeded(e -> {
				response = task.getValue();
				if (response.isSuccess()) {										
					Alert alert2 = new Alert(AlertType.INFORMATION);
					alert2.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
					alert2.setTitle("Account deleted");
					alert2.setHeaderText(response.getData());
					alert2.showAndWait();
					logout();					
				} 			
			});
			task.setOnFailed(e -> {
				System.out.println(response.getData());

			});
			new Thread(task).start();
		}
		
	}
	
	
	public void disableButtons() {
		addButton.setDisable(true);
		updateButton.setDisable(true);
		removeButton.setDisable(true);
		profileMenu.setDisable(true);
		addBooksCollectionItem.setDisable(true);
		removeBooksCollectionItem.setDisable(true);
	}
	
	
	public void enableButtons() {
		addButton.setDisable(false);
		updateButton.setDisable(false);
		removeButton.setDisable(false);
		profileMenu.setDisable(false);
		addBooksCollectionItem.setDisable(false);
	}
	
	
	public void addSelectedBooks (Book book) {
		selectedBooks.add(book);	
	}
	
	public void removeSelectedBooks (Book book) {
		selectedBooks.remove(book);		
	}
	
	public void deselectAllBooks() {
		selectedBooks.clear();
		for (int i=0; i<controllerList.size(); i++) {
			controllerList.get(i).deselectAllBooks();
		}
	}
	
	public String getToken() {
		return token;
	}
}
