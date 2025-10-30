package org.openjfx.BookCatalogueClient;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
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
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
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
	private Button saveButton;
	
	@FXML
	private Button patronsButton;
	
	@FXML
	private Button statisticsButton;
	
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
	
	@FXML
	private TreeView<String> treeView;
	
	TreeItem<String> accountItem;
		
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
	private Set<String> disabledItems = new HashSet<>();
	
	public TabPane getTabPane() {		
        return tabPane;   
    }			
	
	@FXML
	public void initialize() {
		
		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
		    deselectAllBooks();
		    actualCollection = (newTab != null && newTab.getUserData() instanceof Collection)
		        ? (Collection) newTab.getUserData() : null;
		    updateButtonStates();
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
		initializeTree();
		initializeToolTip();
		updateButtonStates();
	}
	
	public void openBooksTabGeneric(String header, Consumer<BooksTabController> initializer) {

		BooksTabController booksTabController = openTab("BooksTab.fxml", header);
		controllerList.add(booksTabController);
		booksTabController.setHomeController(this);
		if (token!=null && !jwtUtils.isTokenExpired(token))
			booksTabController.setToken(token);			
		booksTabController.setHeader(header);			
		initializer.accept(booksTabController); 
			
	}
	
	public void openBooksTab(String header) {
	    openBooksTabGeneric(header, BooksTabController::initNormal);  
	}
	
	@FXML
	public void showAll() {
		deselectAllBooks();
		openBooksTabGeneric(resources.getString("label.header2"), BooksTabController::initNormal);
	}

	@FXML
	public void showSearchResult() {
		deselectAllBooks();
	    String keyword = searchField.getText();
	    if (keyword != null && !keyword.isBlank()) {
	        openBooksTabGeneric(resources.getString("label.header3")+ keyword + "'", controller -> controller.initSearch(keyword));	   
	    }
	}
	
	@FXML
	public void openAddBookTab() {
		
		deselectAllBooks();
		if (token!=null && !jwtUtils.isTokenExpired(token)) {
			
			AddBookController controller = openTab("AddBookTab.fxml", resources.getString("label.header5"));
			controller.init(token);		
				
		} else {
			openLoginTab();
			updateButtonStates();
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
		}		
	}
	
	public void openUpdateBookTab(Book book) {

		if (token!=null && !jwtUtils.isTokenExpired(token)) {			
			UpdateBookController controller = openTab("UpdateBookTab.fxml", resources.getString("tab.update")+ book.getTitle());
			controller.setBookToUpdate(book, token);				
			controller.setHomeController(this);		
		} else {
			openLoginTab();
			updateButtonStates();
		}
		
	}
	
	@FXML
	public void removeSelectedBooks() {
		
		TaskTracker tracker = new TaskTracker(selectedBooks.size(), 
				() -> {
					createAlert(AlertType.INFORMATION, "alert.booksdeleted", "alert.deletesuccesses").showAndWait();
					tabPane.getTabs().clear();
					openBooksTab(resources.getString("label.header2"));
				},
				() -> { 
					createAlert(AlertType.ERROR, "alert.error", "alert.deletefail").showAndWait();
			        tabPane.getTabs().clear();
					openBooksTab("label.header2");
			        }
				);
		if (!selectedBooks.isEmpty()) {
			
			Alert alert = createAlert(AlertType.CONFIRMATION, "alert.confirm", "alert.deleteconfirm");
			Optional<ButtonType> result = alert.showAndWait();
			ButtonType button = result.orElse(ButtonType.CANCEL);
		
			if (button==ButtonType.OK) {
			
				for (Book book : selectedBooks) {			
					Task<ApiResponse<String>> task = bookTasks.deleteBookTask(book.getId(), token);
					task.setOnSucceeded(e -> {
					response = task.getValue();			
					if (!response.isSuccess()) {
						createAlert(AlertType.INFORMATION, "alert.booknotfound", "alert.booknotfound").showAndWait();
						tracker.taskFailed();
					} else
						tracker.taskSucceeded();
					});
					task.setOnFailed(e -> {
						createAlert(AlertType.INFORMATION, "alert.booknotfound", "alert.booknotfound").showAndWait();
					});
					new Thread(task).start();
			
				}				
			}		
		}		
	}
	
	public void removeBook(Book book, String token) {
		
		Alert alert = createAlert(AlertType.CONFIRMATION, "alert.confirm", "alert.deleteconfirm");	
		Optional<ButtonType> result = alert.showAndWait();
		ButtonType button = result.orElse(ButtonType.CANCEL);
	
		if (button==ButtonType.OK) {
			
			Task<ApiResponse<String>> task = bookTasks.deleteBookTask(book.getId(), token);
			task.setOnSucceeded(e -> {
				response = task.getValue();	
				
				if (!response.isSuccess()) {	
					createAlert(AlertType.INFORMATION, "alert.booknotfound", "alert.booknotfound").showAndWait();
				} else {
					createAlert(AlertType.INFORMATION, "alert.bookdeleted", "alert.deletesuccess").showAndWait();
				}
			});
			task.setOnFailed(e -> {
				createAlert(AlertType.INFORMATION, "alert.booknotfound", "alert.booknotfound").showAndWait();
			});
			new Thread(task).start();
		
		}
	}		
	
	@FXML
	public void openCollectionsTab() {
		deselectAllBooks();
		CollectionsTabController controller = openTab("CollectionsTab.fxml", resources.getString("label.header4"));		
		if (token!=null && !jwtUtils.isTokenExpired(token))
			controller.setToken(token);
		controller.setHomeController(this);
			
		controller.setOnCollectionOpened( collection -> {
			Collection selectedCollection = collection;
			Tab collectionsTab = tabPane.getSelectionModel().getSelectedItem();
			tabPane.getTabs().remove(collectionsTab);
			openBooksCollection(selectedCollection);	
		});
							
	}
		
	@FXML
	public void addBooksToCollection() {
		
		List<Book> booksToAdd = new ArrayList<Book>();
		if (!selectedBooks.isEmpty()) {
			for (Book book : selectedBooks) {
				booksToAdd.add(book);
			}

			CollectionsTabController controller = openTab("CollectionsTab.fxml", resources.getString("label.header4"));				
			controller.setHeader(resources.getString("label.selectcollection"));			
			controller.setOnCollectionClicked( collection -> {
					
				TaskTracker tracker = new TaskTracker(booksToAdd.size(), 
						() -> {
							createAlert(AlertType.INFORMATION, "alert.booksadd", "alert.addedbookstocollection").showAndWait();				
							deselectAllBooks();
							Tab collectionsTab = tabPane.getSelectionModel().getSelectedItem();
							tabPane.getTabs().remove(collectionsTab);
							openBooksCollection(collection);
						
							},
				
						() -> { 
							createAlert(AlertType.ERROR, "alert.error", "alert.addtocollectionfail").showAndWait();							
							deselectAllBooks();
							Tab collectionsTab = tabPane.getSelectionModel().getSelectedItem();
							tabPane.getTabs().remove(collectionsTab);
							openBooksCollection(collection);
							}
						);	
					for (Book book : booksToAdd) {
						Task<ApiResponse<Book>> task = collectionsTasks.addBookCollectionTask(collection.getId(), book, token);
						task.setOnSucceeded(e -> {
							collectionResponse = task.getValue();			
								if (!collectionResponse.isSuccess()) {
									createAlert(AlertType.INFORMATION, "alert.booknotfound", "alert.booknotfound").showAndWait();
									tracker.taskFailed();
								} else
								tracker.taskSucceeded();
							});
							task.setOnFailed(e -> {
								createAlert(AlertType.INFORMATION, "alert.booknotfound", "alert.booknotfound").showAndWait();
							});
							new Thread(task).start();
													
					}
								
				});								
		}
				
	}
	
	@FXML
	public void removeBooksToCollection() throws InterruptedException {
		TaskTracker tracker = new TaskTracker(selectedBooks.size(),
		        () -> { 
		        	createAlert(AlertType.INFORMATION, "alert.booksremove", "alert.booksremovefrom").showAndWait();
		            Collection updatedCollection = actualCollection;
		            Tab selected = tabPane.getSelectionModel().getSelectedItem();
					tabPane.getTabs().remove(selected);
					openBooksCollection(updatedCollection);
		            
		        },
		        () -> { 
		        	createAlert(AlertType.ERROR, "alert.error", "alert.booksremovefail").showAndWait();
		            Collection updatedCollection = actualCollection;
		            Tab selected = tabPane.getSelectionModel().getSelectedItem();
					tabPane.getTabs().remove(selected);
					openBooksCollection(updatedCollection);
		        }
		    );
	    
			for (Book book : selectedBooks) {
				Task<ApiResponse<String>> task = collectionsTasks.removeBookCollectionTask(actualCollection.getId(), book.getId(), token);
				task.setOnSucceeded(e -> {
					
					collectionResponse = task.getValue();			
						if (!collectionResponse.isSuccess()) {		
							createAlert(AlertType.INFORMATION, "alert.booknotfound", "alert.booknotfound").showAndWait();
							tracker.taskFailed();
						} else
						tracker.taskSucceeded();
					});
					task.setOnFailed(e -> {
						createAlert(AlertType.INFORMATION, "alert.booknotfound", "alert.booknotfound").showAndWait();

					});
					new Thread(task).start();												
			}									
	}

	public void openBooksCollection(Collection collection) {
		deselectAllBooks();				
		openBooksTabGeneric(collection.getName(), c -> c.initCollection(collection));
		if (!jwtUtils.isTokenExpired(token) && token!=null) {
		}
		Tab currentTab = tabPane.getTabs().get(tabPane.getTabs().size() - 1);
		currentTab.setUserData(collection);
		this.actualCollection = collection;		
	}
	
	@FXML
	public void openLoginTab() {
		deselectAllBooks();
		if (jwtUtils.isTokenExpired(token) || token==null) {

			LoginController controller = openTab("LoginTab.fxml", resources.getString("label.header8"));				
			controller.setOnLoginClicked(token -> {
			this.token = token;
			if (!jwtUtils.isTokenExpired(token)) {
				updateButtonStates();
				tabPane.getTabs().clear();
				openLoggedTab(resources.getString("label.header9"));
				profileMenu.setText(controller.getUsername().substring(0, 1).toUpperCase() + controller.getUsername().substring(1));
			}
			});	
			controller.setOnRegisterClicked(() -> {
				Tab loginTab = tabPane.getSelectionModel().getSelectedItem();
				tabPane.getTabs().remove(loginTab);
				openRegisterTab();
			});
	
		} else {
			openLoggedTab("Logged in!");
		}		
	}
	
	
	@FXML
	public void openRegisterTab() {
		
		deselectAllBooks();		
		RegisterController controller = openTab("RegisterTab.fxml", resources.getString("label.header7"));
		controller.setOnRegisterClicked(token -> {
			this.token = token;
			if (!jwtUtils.isTokenExpired(token)) {
				updateButtonStates();
				Tab registerTab = tabPane.getSelectionModel().getSelectedItem();
				openLoggedTab(resources.getString("label.header10"));
				tabPane.getTabs().remove(registerTab);
			}
			});	
		controller.setOnLoginClicked(() -> {
			openLoginTab();
			Tab registerTab = tabPane.getSelectionModel().getSelectedItem();
			tabPane.getTabs().remove(registerTab);
		});

	}
	
	
	public void openLoggedTab(String header) {	
		LoggedController controller = openTab("LoggedTab.fxml", resources.getString("label.header9"));
		controller.setWelcomeUsername(header, jwtUtils.getUsernameToken(token));
	}
	
	
	@FXML
	public void logout() {
		token=null;
		profileMenu.setText("Profile");
		updateButtonStates();
		tabPane.getTabs().clear();
	}
	
	
	@FXML
	public void deleteAccount() {
		
		Alert alert = createAlert(AlertType.CONFIRMATION, "alert.confirm", "alert.confirmdeleteaccount");
		Optional<ButtonType> result = alert.showAndWait();
		ButtonType button = result.orElse(ButtonType.CANCEL);
		
		if (button==ButtonType.OK) {
			Task<ApiResponse<String>> task = userTasks.deleteAccountTask(token);
			task.setOnSucceeded(e -> {
				response = task.getValue();
				if (response.isSuccess()) {										
					createAlert(AlertType.INFORMATION, "alert.deletedaccount", "alert.deletedaccount").showAndWait();
					logout();					
				} 			
			});
			task.setOnFailed(e -> {
				
			});
			new Thread(task).start();
		}
		
	}
	
	@FXML
	public void saveBooks() {	
		openTab("SaveTab.fxml", resources.getString("label.header11"));
		updateButtonStates();
	}
	
	@FXML
	public void openStatisticsTab() {	
		StatisticsController controller = openTab("StatisticsTab.fxml", resources.getString("label.statistics"));
		controller.setToken(token, this);
		controller.initializeTab();
		updateButtonStates();
	}
	
	@FXML
	public void openPatronTab() {
		PatronTabController controller = openTab("PatronsTab.fxml", resources.getString("label.header12"));
		controller.initialize(token);
		updateButtonStates();
	}
	
	private <T> T openTab(String fxmlName, String headerKey) {
	    try {
	        ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
	        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlName), bundle);
	        Parent root = loader.load();
	        Tab tab = new Tab(headerKey, root);
	        tabPane.getTabs().add(tab);
	        tabPane.getSelectionModel().select(tab);
	        return loader.getController();
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private Alert createAlert(AlertType type, String titleKey, String headerKey) {
	    Alert alert = new Alert(type);
	    alert.getDialogPane().getStylesheets().add(
	        getClass().getResource("AlertStyle.css").toExternalForm()
	    );
	    alert.setTitle(resources.getString(titleKey));
	    alert.setHeaderText(resources.getString(headerKey));
	    return alert;
	}
	
	private void updateButtonStates() {
	    boolean loggedIn = token != null && !jwtUtils.isTokenExpired(token);
	    boolean hasSelection = !selectedBooks.isEmpty();

	    addButton.setDisable(!loggedIn);
	    patronsButton.setDisable(!loggedIn);
	    statisticsButton.setDisable(!loggedIn);
	    profileMenu.setDisable(!loggedIn);
	    updateButton.setDisable(!loggedIn || !hasSelection);
	    removeButton.setDisable(!loggedIn || !hasSelection);
	    addBooksCollectionItem.setDisable(!loggedIn || !hasSelection);
	    removeBooksCollectionItem.setDisable(!loggedIn || actualCollection == null);
	    
	    setItemDisabled(resources.getString("tree.add"), !loggedIn || !hasSelection);
	    setItemDisabled(resources.getString("tree.remove"), !loggedIn || actualCollection == null);
	    setItemDisabled(resources.getString("item.logout"), !loggedIn);
	    setItemDisabled(resources.getString("item.delete"), !loggedIn);
	    setItemDisabled(resources.getString("item.addbooks"), !loggedIn);
	    setItemDisabled(resources.getString("label.statistics"), !loggedIn);
	    setItemDisabled(resources.getString("tree.patron"), !loggedIn);
	    
	}
	
	private void initializeToolTip() {
		updateButton.setTooltip(new Tooltip(resources.getString("tooltip.update")));
		saveButton.setTooltip(new Tooltip(resources.getString("tooltip.export")));
		addButton.setTooltip(new Tooltip(resources.getString("tooltip.add")));
		updateButton.setTooltip(new Tooltip(resources.getString("tooltip.update")));
		removeButton.setTooltip(new Tooltip(resources.getString("tooltip.delete")));
	}
	
	@SuppressWarnings("unchecked")
	private void initializeTree() {
		TreeItem<String> menuItem = new TreeItem<>("Menu");
		TreeItem<String> resourcesItem = new TreeItem<>(" Resources", 
				new ImageView(new Image(getClass().getResource("icons/tree1.png").toExternalForm())));
		TreeItem<String> showItem = new TreeItem<>(resources.getString("item.all"));
		TreeItem<String> statisticsItem = new TreeItem<>(resources.getString("label.statistics"));
		TreeItem<String> patronsItem = new TreeItem<>(resources.getString("tree.patron"));
		resourcesItem.getChildren().addAll(showItem, patronsItem, statisticsItem);
		
		TreeItem<String> actionsItem = new TreeItem<>("Actions", 
				new ImageView(new Image(getClass().getResource("icons/tree2.png").toExternalForm())));
		TreeItem<String> saveItem = new TreeItem<>(resources.getString("button.save"));
		TreeItem<String> addItem = new TreeItem<>(resources.getString("item.addbooks"));
		actionsItem.getChildren().addAll(addItem, saveItem);
		
		TreeItem<String> collectionsItem = new TreeItem<>(resources.getString("menu.collections"), 
				new ImageView(new Image(getClass().getResource("icons/tree3.png").toExternalForm())));
		TreeItem<String> showCollectionsItem = new TreeItem<>(resources.getString("item.show"));
		TreeItem<String> addToCollectionsItem = new TreeItem<>(resources.getString("tree.add"));
		TreeItem<String> removeFromCollectionsItem = new TreeItem<>(resources.getString("tree.remove"));
		collectionsItem.getChildren().addAll(showCollectionsItem, addToCollectionsItem, removeFromCollectionsItem);
		
		TreeItem<String> accountItem = new TreeItem<>("Account", 
				new ImageView(new Image(getClass().getResource("icons/tree4.png").toExternalForm())));
		TreeItem<String> loginItem = new TreeItem<>(resources.getString("label.login"));
		TreeItem<String> registerItem = new TreeItem<>(resources.getString("label.register"));
		TreeItem<String> signoutItem = new TreeItem<>(resources.getString("item.logout"));
		TreeItem<String> deleteAccountItem = new TreeItem<>(resources.getString("item.delete"));
		accountItem.getChildren().addAll(loginItem, registerItem, signoutItem, deleteAccountItem);

		menuItem.getChildren().addAll(resourcesItem, actionsItem, collectionsItem, accountItem);
		treeView.setShowRoot(false);
		treeView.setRoot(menuItem);
		
		treeView.setCellFactory(tv -> new TreeCell<>() {
	        @Override
	        protected void updateItem(String item, boolean empty) {
	            super.updateItem(item, empty);
	            if (empty || item == null) {
	                setText(null);
	                setStyle("");
	                setGraphic(null);
	            } else {
	                setText(item);
	                TreeItem<String> treeItem = getTreeItem();
	                setGraphic(treeItem.getGraphic());
	                if (disabledItems.contains(item)) {
	                   setTextFill(Color.GRAY);
	                   setStyle("-fx-text-fill: GRAY;");
	                } else {
	                    setTextFill(Color.BLACK);
	                    setStyle("");
	                }
	            }
	        }
	    });

	    treeView.setOnMouseClicked(event -> {
	        TreeItem<String> selected = treeView.getSelectionModel().getSelectedItem();
	        if (selected != null && !disabledItems.contains(selected.getValue())) {
	            try {
					selectTreeItem();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        } else {
	            event.consume(); 
	        }
	    });
	
	}
	
	public void selectTreeItem() throws InterruptedException {
		
		TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
		
		if (item!=null) {
			String selection = item.getValue();
			
			if (selection.equals(resources.getString("item.all"))) showAll(); 
			if (selection.equals(resources.getString("label.statistics"))) openStatisticsTab();
			if (selection.equals(resources.getString("tree.patron"))) openPatronTab();
			if (selection.equals(resources.getString("item.addbooks"))) openAddBookTab();
			if (selection.equals(resources.getString("button.save"))) saveBooks(); 
			if (selection.equals(resources.getString("item.show"))) openCollectionsTab(); 
			if (selection.equals(resources.getString("tree.add"))) addBooksToCollection(); 
			if (selection.equals(resources.getString("tree.remove"))) removeBooksToCollection();
			if (selection.equals(resources.getString("label.login"))) openLoginTab();
			if (selection.equals(resources.getString("label.register"))) openRegisterTab();
			if (selection.equals(resources.getString("item.logout"))) logout();
			if (selection.equals(resources.getString("item.delete"))) deleteAccount();
			
			treeView.getSelectionModel().clearSelection();			
		}
	}
	
	public void setItemDisabled(String itemName, boolean disabled) {
	    if (disabled) {
	        disabledItems.add(itemName);
	    } else {
	        disabledItems.remove(itemName);
	    }
	    treeView.refresh();
	}
	
	public void addSelectedBooks (Book book) {
		selectedBooks.add(book);	
		updateButtonStates();
	}
	
	public void removeSelectedBooks (Book book) {
		selectedBooks.remove(book);		
		updateButtonStates();
	}
	
	public void deselectAllBooks() {
		selectedBooks.clear();
		for (int i=0; i<controllerList.size(); i++) {
			controllerList.get(i).deselectAllBooks();
		}
		updateButtonStates();
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
		    	controller.updateButtonStates();
		    }
	        stage.setScene(new Scene(root));
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
