package org.openjfx.BookCatalogueClient;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.Collection;
import org.openjfx.BookCatalogueClient.service.JwtUtils;
import org.openjfx.BookCatalogueClient.task.ApiTask;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

public class BookDetailsController {
	
	@FXML 
	private ResourceBundle resources;
	
	@FXML
	FlowPane tagPane;
	
	@FXML   
	FlowPane collectionPane;
	
	@FXML
	Label titleLabel;
	
	@FXML
	Label authorLabel;
	
	@FXML
	Label publisherLabel;
	
	@FXML
	Label publishPlaceLabel;
	
	@FXML
	Label publishDateLabel;
	
	@FXML
	Label pagesLabel;
	
	@FXML
	Label isbnLabel;
	
	@FXML
	Label availableLabel;
	
	@FXML
	ImageView coverImage;
	
	@FXML
	Button removeButton;
	
	@FXML
	Button updateButton;
	
	@FXML
	Button backButton;
	
	@FXML
	Button refreshButton;
	
	@FXML
	Button addToCollectionButton;
	
	@FXML
	ImageView availableImage;
	
	@FXML
	Label tagLabel = new Label();
	
	@FXML
	Label collectionLabel = new Label();
	
	private Book book;
	private String token;
	private List<Collection> collections;
	private JwtUtils jwtUtils = new JwtUtils();
	Consumer<Book> onBookUpdate;
	Consumer<Book> onBookDelete;
	private Runnable onReturn;
	private HomeController homeController;
	private final ApiTask collectionsTasks = new ApiTask();
	private ApiResponse<?> collectionResponse;
	private ApiResponse<List<Collection>> responseCollection;
	private ApiResponse<Book> response;
			
	public void showBook(Book book) {
		initializeToolTip();
		titleLabel.setText(book.getTitle());
		authorLabel.setText(book.getAuthor());
		if (book.getPublisher()!=null)
			publisherLabel.setText(book.getPublisher());
		if (book.getPublishPlace()!=null)
			publishPlaceLabel.setText(book.getPublishPlace());
		
		DateTimeFormatter uiFormatter = DateTimeFormatter.ofPattern("yyyy");
		if (book.getPublishDate()!=null)
			publishDateLabel.setText(book.getPublishDate().format(uiFormatter));
		isbnLabel.setText(book.getIsbn());
		pagesLabel.setText(String.valueOf(book.getPages()));
		if (book.isAvailable()) {
			availableLabel.setText("Available ");
			availableImage.setImage(new Image(getClass().getResource("icons/ready-stock.png").toExternalForm()));
		}
		else {
			availableLabel.setText("Not available ");
			availableImage.setImage(new Image(getClass().getResource("icons/out-of-stock.png").toExternalForm()));
			
		}

		
		for (int i=0; i<book.getTags().size(); i++) {
			if (! book.getTags().get(i).getName().isEmpty()) {
				Button button = new Button(book.getTags().get(i).getName());
				String tagName = book.getTags().get(i).getName();
				button.setOnAction(e -> {
					Tab selected = homeController.getTabPane().getSelectionModel().getSelectedItem();
					homeController.getTabPane().getTabs().remove(selected);
					homeController.openBooksTabGeneric(button.getText(), controller -> controller.searchBooks(tagName,0,20));
				});
				tagPane.getChildren().add(button);
			}		
		}		
		Task<ApiResponse<List<Collection>>> task = collectionsTasks.loadCollectionsByBookIdTask(book.getId());
		task.setOnSucceeded(e -> {
			responseCollection = task.getValue();
			if (responseCollection.isSuccess()) {
				collections = responseCollection.getData();
				for (int i=0; i<collections.size(); i++) {
					Button button = new Button(collections.get(i).getName());
					Collection selectedCollection = collections.get(i);
					button.setOnAction(action -> {
						
						Tab selected = homeController.getTabPane().getSelectionModel().getSelectedItem();
						homeController.getTabPane().getTabs().remove(selected);
						homeController.openBooksTabGeneric(button.getText(), controller -> controller.initCollection(selectedCollection));
						
					});
					collectionPane.getChildren().add(button);
				}
			}
		});
		task.setOnFailed(e -> {
			
		});
		new Thread(task).start();
		
		Task<Image> loadImageTask = new Task<>() {
	        @Override
	        protected Image call() throws Exception {
	            try {
	                return new Image(book.getImageUrl(), true); 
	            } catch (Exception e) {
	                return null;
	            }
				
	        }
	    };

	    loadImageTask.setOnSucceeded(e -> coverImage.setImage(loadImageTask.getValue()));
	    loadImageTask.setOnFailed(e -> {
	        System.out.println("Failed to load image");
	    });

	    new Thread(loadImageTask).start();
	}
	
	@FXML
	public void refresh() {
		tagPane.getChildren().clear();
		tagLabel.setText(" Tag: ");
		tagPane.getChildren().add(tagLabel);
		collectionPane.getChildren().clear();
		collectionLabel.setText(" Collection: ");
		collectionPane.getChildren().add(collectionLabel);
		setBook(book,homeController);
	}
	
	public void setBook(Book book, HomeController homeController) {
		this.homeController = homeController;
		this.book = book;
		removeButton.setDisable(true);
		updateButton.setDisable(true);
		addToCollectionButton.setDisable(true);
				 
		Task<ApiResponse<Book>> task = collectionsTasks.loadBookTask(book.getIsbn());
		task.setOnSucceeded(e -> {
			response = task.getValue();
			if (response.isSuccess()) {
				Book bookLoaded = response.getData();
				this.book = response.getData();
				showBook(bookLoaded);
			} else showBook(book);
			
		});
		task.setOnFailed(e -> {
			showBook(book);
		});
		new Thread(task).start();		
	}

	
	public void addToCollection() {
	
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
			FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionsTab.fxml"),bundle);
			Parent collectionsRoot = loader.load();
		
			CollectionsTabController controller = loader.getController();
			Tab collectionsTab = new Tab(resources.getString("label.header4"), collectionsRoot);
			
			controller.setHeader(resources.getString("label.selectcollection"));
			controller.setOnCollectionClicked( collection -> {	
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
						} else {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.getDialogPane().getStylesheets().add(
								    getClass().getResource("AlertStyle.css").toExternalForm()
								);
							alert.setTitle(resources.getString("alert.bookadd"));
							alert.setHeaderText(resources.getString("alert.addedtocollection")+collection.getName());
							alert.showAndWait();
						}
						homeController.getTabPane().getTabs().remove(collectionsTab);
						Tab selected = homeController.getTabPane().getSelectionModel().getSelectedItem();
						homeController.getTabPane().getTabs().remove(selected);
						homeController.openBooksCollection(collection);
						
					});
					task.setOnFailed(e -> {
						Throwable ex = task.getException();
			            ex.printStackTrace();
					});
					new Thread(task).start();
											
			
			});
			homeController.deselectAllBooks();
			homeController.getTabPane().getTabs().add(collectionsTab);
			homeController.getTabPane().getSelectionModel().select(collectionsTab);
			
		
	} catch (IOException e) {
		e.printStackTrace();
	}	
	}
	
	public void setOnBookUpdate(Consumer<Book> callback) {
		this.onBookUpdate = callback;
	}
	
	public void setOnBookDelete(Consumer<Book> callback) {
		this.onBookDelete = callback;
	}
	
	public void setOnReturn(Runnable callback) {
		this.onReturn = callback; 
	}
	
	@FXML
	public void deleteBook() {
		if (onBookDelete!=null)
			onBookDelete.accept(book);
	}
	
	@FXML
	public void updateBook() {
		if (onBookUpdate!=null)
			onBookUpdate.accept(book);
	}
	
	@FXML
	public void back() {
		if (onReturn!=null)
			onReturn.run();            
	}
	               

	public void setToken(String token) {
		this.token = token;
		if (token!=null && !jwtUtils.isTokenExpired(token)) {
			removeButton.setDisable(false);
			updateButton.setDisable(false);	
			addToCollectionButton.setDisable(false);
		}
	}
	
	private void initializeToolTip() {
		backButton.setTooltip(new Tooltip(resources.getString("tooltip.back")));
		refreshButton.setTooltip(new Tooltip(resources.getString("tooltip.refresh")));
	}
	
	
}

