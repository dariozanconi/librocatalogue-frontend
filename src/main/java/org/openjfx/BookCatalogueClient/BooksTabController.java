package org.openjfx.BookCatalogueClient;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.Collection;
import org.openjfx.BookCatalogueClient.model.PageResponse;
import org.openjfx.BookCatalogueClient.task.ApiTask;

public class BooksTabController {
	
	@FXML 
	private ResourceBundle resources;
	
	@FXML
	private StackPane rootPane;
	
	@FXML
	private FlowPane flowPane;
		
	@FXML
	private Button nextButton;
	
	@FXML
	private Button previousButton;
	
	@FXML
	private MenuButton sortMenu;
	
	@FXML
	private Label pageLabel;
	
	@FXML 
	private Label loadingLabel;
	
	@FXML
	private Button refreshButton;
	
	@FXML
	private Label headerLabel;
	
	private final ApiTask bookTasks = new ApiTask();
	private ApiResponse<PageResponse<Book>> response;	
	private int currentPage = 0;
	private int pageSize = 20;
	private String currentSort = "title";
	private HomeController homeController;
	private String token;
	private List<BookCardController> controllerList;
	List<Book> books;
	
	@FXML
	private MenuItem titleItem;
	
	@FXML
	private MenuItem authorItem;
	
	@FXML
	private MenuItem publisherItem;

	
    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    
	@FXML
    public void initialize() {}
	
	
	public void initNormal() {	
		sortMenu = new MenuButton(resources.getString("menu.sort"));    		
	    loadBooks(currentPage, pageSize, "title");
	    	    		
	}
	
	public void initSearch(String keyword) {
		searchBooks(keyword, 0, 20);
		sortMenu.setDisable(true);
    }
	
	public void initCollection(Collection collection) {
		loadCollection(collection.getId(), 0, 20);
		sortMenu.setDisable(true);
    }
	
	@FXML
	public void refresh() {
		sortMenu = new MenuButton(resources.getString("menu.sort"));    		
	    loadBooks(currentPage, pageSize, "title");
	}
	
	public void loadBooks(int page, int size, String sort) {
		
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.loadBooksTask(page, size, sort);
		loadingLabel.setText("Loading...");	
		pageLabel.setText("");
		previousButton.setDisable(true);
		nextButton.setDisable(true);
		task.setOnSucceeded(e -> {
			
			response = task.getValue();
			if (response.isSuccess()) {	
				loadingLabel.setText("");														
				pageLabel.setText(((currentPage*20)+1)
						+ "-" + ((currentPage*20)+response.getData().getNumberOfElements())
						+ " of "+ response.getData().getTotalElements());
				if (currentPage==0)
					previousButton.setDisable(true);		        
				else previousButton.setDisable(false);	        
		        if ((response.getData().getTotalPages()-currentPage==1)) {
	        		nextButton.setDisable(true);
	        	} else nextButton.setDisable(false);
		        showBooks(response.getData().getContent());	
		        
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.getDialogPane().getStylesheets().add(
					    getClass().getResource("AlertStyle.css").toExternalForm()
					);
				alert.setTitle(resources.getString("alert.errorload"));
				alert.setHeaderText(resources.getString("alert.connection"));
				alert.showAndWait();
				System.out.println(response.getStatus());
			}
		});
		
		task.setOnFailed(e -> {
			Throwable ex = task.getException();
            ex.printStackTrace();
		});
		
		new Thread(task).start();
	}

	private void showBooks(List<Book> books) {
		
		flowPane.getChildren().clear();
		controllerList = new ArrayList<>();
		this.books = books;
		for (Book book : books) {
			
			try {
				
				FXMLLoader loader = new FXMLLoader(getClass().getResource("BookCard.fxml"));
				Node cardNode = loader.load();				
				BookCardController controller = loader.getController();
				controllerList.add(controller);
				controller.setData(book);
				
				controller.setOnBookClicked(bookClicked -> {
					showBookDetails(bookClicked);
				});
				controller.setOnBookSelected(bookSelected -> {
					homeController.addSelectedBooks(bookSelected);
				});
				controller.setOnBookDeselected(bookSelected -> {
					homeController.removeSelectedBooks(bookSelected);
				});				
				
				flowPane.getChildren().add(cardNode);
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}						
		}
		
	}
	
	public void showBookDetails(Book book) {
	        try {
	        	ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("BookDetailsTab.fxml"), bundle);
	            Node detailsRoot = loader.load();

	            BookDetailsController controller = loader.getController();
	            
	            controller.setBook(book, homeController);	            
	            controller.setToken(token);
	            
	            controller.setOnReturn(() -> {
	            	backToBooks();
	            });
	            
	            controller.setOnBookUpdate(updatedBook -> {
	            	homeController.openUpdateBookTab(book);
	                loadBooks(0, 20, "title");
	            });
	            controller.setOnBookDelete(deletedBook -> {
	            	homeController.removeBook(deletedBook, token);
	            	backToBooks();
	            });

	            rootPane.getChildren().setAll(detailsRoot);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
	
	
	public void searchBooks(String input, int page, int size) {
		String keyword = parse(input);
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.searchBooksTask(keyword, page, size);
		loadingLabel.setText("Loading...");	
		pageLabel.setText("");
		previousButton.setDisable(true);
		nextButton.setDisable(true);
		refreshButton.setDisable(true);
		
		task.setOnSucceeded(e -> {
			
			response = task.getValue();
			if (response.isSuccess()) {	
				loadingLabel.setText("");														
				pageLabel.setText((response.getData().getNumber()*20)
						+ "-" + (response.getData().getTotalElements()-(response.getData().getNumber()) 
						+ " of "+ response.getData().getTotalElements()));
				if (response.getData().getNumber()==0)
					previousButton.setDisable(true);		        
				else previousButton.setDisable(false);	        
		        if ((response.getData().getTotalPages()-response.getData().getNumber()==1)) {
	        		nextButton.setDisable(true);
	        	} else nextButton.setDisable(false);
		        showBooks(response.getData().getContent());	
		        
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.getDialogPane().getStylesheets().add(
					    getClass().getResource("AlertStyle.css").toExternalForm()
					);
				alert.setTitle(resources.getString("alert.errorload"));
				alert.setHeaderText(resources.getString("alert.connection"));
				alert.showAndWait();
				System.out.println(response.getStatus());
			}
		});
		
		task.setOnFailed(e -> {
			Throwable ex = task.getException();
            ex.printStackTrace();
		});
		
		new Thread(task).start();
	}
	
	private String parse(String input) {
		String output = "";
		if (input!=null) {
			for(int i=0; i<input.length(); i++) {
				if (input.charAt(i)!=' ')
					output = output.concat(Character.toString(input.charAt(i)));
				else output = output.concat("_");
			}
			return output;
		} else return null;		
	}

	public void loadCollection(int id, int page, int size) {
		
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.loadCollectionBooksTask(id, page, size);
		
		loadingLabel.setText("Loading...");	
		pageLabel.setText("");
		previousButton.setDisable(true);
		nextButton.setDisable(true);
		refreshButton.setDisable(true);
		task.setOnSucceeded(e -> {
			
			response = task.getValue();
			if (response.isSuccess()) {	
				loadingLabel.setText("");														
				pageLabel.setText(((currentPage*20)+1)
						+ "-" + ((currentPage*20)+response.getData().getNumberOfElements())
						+ " of "+ response.getData().getTotalElements());
				if (currentPage==0)
					previousButton.setDisable(true);		        
				else previousButton.setDisable(false);	        
		        if ((response.getData().getTotalPages()-currentPage==1)) {
	        		nextButton.setDisable(true);
	        	} else nextButton.setDisable(false);
		        showBooks(response.getData().getContent());	
		        
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.getDialogPane().getStylesheets().add(
					    getClass().getResource("AlertStyle.css").toExternalForm()
					);
				alert.setTitle(resources.getString("alert.errorload"));
				alert.setHeaderText(resources.getString("alert.connection"));
				alert.showAndWait();
				System.out.println(response.getStatus());
			}
		});
		
		task.setOnFailed(e -> {
			Throwable ex = task.getException();
            ex.printStackTrace();
		});
		
		new Thread(task).start();
	}
	
	@FXML
	public void nextPage() {
	    currentPage++;
	    loadBooks(currentPage, pageSize, currentSort);

	}
			
	@FXML
	public void previousPage() {
		currentPage--;
		loadBooks(currentPage, pageSize, currentSort);

	}
	
	public void backToBooks() {
				
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
			FXMLLoader booksLoader = new FXMLLoader(getClass().getResource("BooksTab.fxml"), bundle);
			Parent content = booksLoader.load();
			BooksTabController controller = booksLoader.getController();	
			
			controller.setHomeController(homeController);
			token = homeController.getToken();
			controller.initNormal();			
			controller.setToken(token);
            controller.loadBooks(0, 20, "title");
            			
			rootPane.getChildren().setAll(content);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public void deselectAllBooks() {
		if (controllerList!=null)
		for (int i=0; i<controllerList.size(); i++) {
			controllerList.get(i).deselectBook();
		}
	}
	
	public void setHeader(String header) {
		headerLabel.setText(header);
	}

	public void setToken(String token) {
		this.token = token;
		
	}
	
	@FXML
	public void setTitleAsSort() {
		currentSort = "title";
		loadBooks(0, pageSize, currentSort);
	}
	
	@FXML
	public void setAuthorAsSort() {
		currentSort = "authorSort";
		loadBooks(currentPage, pageSize, currentSort);
	}
	
	@FXML
	public void setPublisherAsSort() {
		currentSort = "publisher";
		loadBooks(currentPage, pageSize, currentSort);
	}
}
