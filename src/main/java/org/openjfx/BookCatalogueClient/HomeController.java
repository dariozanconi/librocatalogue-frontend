package org.openjfx.BookCatalogueClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HomeController {
	
	@FXML 
	private ResourceBundle resources;
	
	@FXML
	private BorderPane rootPane;
	
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
	
	@FXML
	private Hyperlink switchLanguage;
	
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
		openBooksTabGeneric(resources.getString("label.header2"), BooksTabController::initNormal);
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
			ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
			FXMLLoader booksLoader = new FXMLLoader(getClass().getResource("BooksTab.fxml"), bundle);
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
		openBooksTabGeneric(resources.getString("label.header2"), BooksTabController::initNormal);
	}

	@FXML
	public void showSearchResult() {
		deselectAllBooks();
		removeBooksCollectionItem.setDisable(true);
	    String keyword = searchField.getText();
	    if (keyword != null && !keyword.isBlank()) {
	        openBooksTabGeneric(resources.getString("label.header3")+ keyword + "'", controller -> controller.initSearch(keyword));	   
	    }
	}
	
	@FXML
	public void openAddBookTab() {
		deselectAllBooks();
		if (token!=null && !jwtUtils.isTokenExpired(token)) {
			
			try {
				ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
				FXMLLoader loader = new FXMLLoader(getClass().getResource("AddBookTab.fxml"), bundle);
				Parent addBookRoot = loader.load();
				
				AddBookController controller = loader.getController();
				controller.init(token);
				Tab addBookTab = new Tab(resources.getString("label.header5"), addBookRoot);
				
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
				ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
				FXMLLoader loader = new FXMLLoader(getClass().getResource("UpdateBookTab.fxml"),bundle);
				Parent updateBookRoot = loader.load();
				
				UpdateBookController controller = loader.getController();
				controller.setBookToUpdate(book, token);				
				controller.setHomeController(this);
				
				Tab updateBookTab = new Tab(resources.getString("tab.update")+ book.getTitle(), updateBookRoot);
				
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
					alert2.setTitle(resources.getString("alert.booksdeleted"));
					alert2.setHeaderText(resources.getString("alert.deletesuccesses"));
					alert2.showAndWait();
					
					tabPane.getTabs().clear();
					openBooksTab(resources.getString("label.header2"));
				},
				() -> { 
			        Alert alert = new Alert(Alert.AlertType.ERROR);
			        alert.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
			        alert.setTitle(resources.getString("alert.error"));
			        alert.setHeaderText(resources.getString("alert.deletefail"));
			        alert.showAndWait();
			        
			        tabPane.getTabs().clear();
					openBooksTab("label.header2");
			        }
				);
		if (!selectedBooks.isEmpty()) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle(resources.getString("alert.confirm"));
			alert.getDialogPane().getStylesheets().add(
				    getClass().getResource("AlertStyle.css").toExternalForm()
				);
			alert.setHeaderText(resources.getString("alert.deleteconfirm"));
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
						alert2.setTitle(resources.getString("alert.booknotfound"));
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
		alert.setTitle(resources.getString("alert.confirm"));
		alert.setHeaderText(resources.getString("alert.deleteconfirm"));
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
					alert2.setTitle(resources.getString("alert.booknotfound"));
					alert2.setHeaderText(response.getData());
					alert2.showAndWait();
				} else {
					Alert alert3 = new Alert(AlertType.INFORMATION);
					alert3.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
					alert3.setTitle(resources.getString("alert.bookdeleted"));
					alert3.setHeaderText(resources.getString("alert.deletesuccess"));
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
			ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
			FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionsTab.fxml"), bundle);
			Parent collectionsRoot = loader.load();
			
			CollectionsTabController controller = loader.getController();
			Tab collectionsTab = new Tab(resources.getString("label.header4"), collectionsRoot);
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
		if (!selectedBooks.isEmpty()) {
			for (Book book : selectedBooks) {
				booksToAdd.add(book);
			}
			try {
				ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
				FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionsTab.fxml"), bundle);
				Parent collectionsRoot = loader.load();
				
				CollectionsTabController controller = loader.getController();
				Tab collectionsTab = new Tab(resources.getString("label.header4"), collectionsRoot);
				controller.setHeader(resources.getString("label.selectcollection"));			
				controller.setOnCollectionClicked( collection -> {
					
					TaskTracker tracker = new TaskTracker(booksToAdd.size(), 
							() -> {
								Alert alert = new Alert(AlertType.INFORMATION);
								alert.getDialogPane().getStylesheets().add(
									    getClass().getResource("AlertStyle.css").toExternalForm()
									);
								alert.setTitle(resources.getString("alert.booksadd"));
								alert.setHeaderText(resources.getString("alert.addedbookstocollection"));
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
								alert.setTitle(resources.getString("alert.error"));
								alert.setHeaderText(resources.getString("alert.addtocollectionfail"));
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
									alert.setTitle(resources.getString("alert.booknotfound"));
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
		
		
	}
	
	@FXML
	public void removeBooksToCollection() throws InterruptedException {
		TaskTracker tracker = new TaskTracker(selectedBooks.size(),
		        () -> { 
		            Alert alert = new Alert(Alert.AlertType.INFORMATION);
		            alert.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
		            alert.setTitle(resources.getString("alert.booksremove"));
		            alert.setHeaderText(resources.getString("alert.booksremovefrom") + actualCollection.getName());
		            alert.showAndWait();
		            
		            deselectAllBooks();
		            removeBooksCollectionItem.setDisable(true);
					Tab selected = tabPane.getSelectionModel().getSelectedItem();
					tabPane.getTabs().remove(selected);
		            
		        },
		        () -> { 
		            Alert alert = new Alert(Alert.AlertType.ERROR);
		            alert.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
		            alert.setTitle(resources.getString("alert.error"));
		            alert.setHeaderText(resources.getString("alert.booksremovefail"));
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
							alert.setTitle(resources.getString("alert.booknotfound"));
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
				ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
				FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginTab.fxml"),bundle);
				Parent loginRoot = loader.load();
			
				LoginController controller = loader.getController();
				Tab loginTab = new Tab(resources.getString("label.header8"), loginRoot);
				controller.setOnLoginClicked(token -> {
					this.token = token;
					if (!jwtUtils.isTokenExpired(token)) {
						enableButtons();
						tabPane.getTabs().clear();
						openLoggedTab(resources.getString("label.header9"));
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
			ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
			FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterTab.fxml"), bundle);
			Parent registerRoot = loader.load();
			
			RegisterController controller = loader.getController();
			Tab registerTab = new Tab(resources.getString("label.header7"), registerRoot);
			controller.setOnRegisterClicked(token -> {
				this.token = token;
				if (!jwtUtils.isTokenExpired(token)) {
					enableButtons();
					openLoggedTab(resources.getString("label.header10"));
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
			ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LoggedTab.fxml"),bundle);
			Parent loggedRoot = loader.load();
			
			LoggedController controller = loader.getController();
			controller.setWelcomeUsername(header, jwtUtils.getUsernameToken(token));
			Tab loggedTab = new Tab(resources.getString("label.header9"), loggedRoot);
							
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
		alert.setTitle(resources.getString("alert.confirm"));
		alert.setHeaderText(resources.getString("alert.confirmdeleteaccount"));
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
					alert2.setTitle(resources.getString("alert.deletedaccount"));
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
	
	public void setToken(String token) {
		this.token=token;
	}
	
	@FXML
	public void switchLanguage () {
		if (Language.getLocale().equals(Locale.GERMAN)) 
			Language.setLocale(Locale.ENGLISH);
		 else Language.setLocale(Locale.GERMAN);
		
		Stage stage = (Stage) rootPane.getScene().getWindow();
		controllerList.clear();
		ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
	    FXMLLoader loader = new FXMLLoader(getClass().getResource("Home.fxml"),bundle);
	    	   	    
	    try {
	    	Parent root = loader.load();
		    HomeController controller = loader.getController();
		    if (token!=null && !jwtUtils.isTokenExpired(token)) {
		    	controller.setToken(this.token);
		    	controller.enableButtons();
		    }
	        stage.setScene(new Scene(root));
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
