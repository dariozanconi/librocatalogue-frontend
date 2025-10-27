package org.openjfx.BookCatalogueClient;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.Color;
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
	
	private Node booksViewContent;
	
	@FXML
	private StackPane rootPane;
	
	@FXML
	private HBox hBox;
	
	@FXML
	private FlowPane flowPane;
		
	@FXML
	private Button nextButton;
	
	@FXML
	private Button previousButton;
	
	@FXML
	private Button iconButton;
	
	@FXML
	private Button selectAllButton;
	
	@FXML
	private Button allBooksButton;
	
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
		
	@FXML
	private MenuItem titleItem;
	
	@FXML
	private MenuItem authorItem;
	
	@FXML
	private MenuItem publisherItem;
	
	@FXML
	private MenuItem dateItem;

	private final ApiTask bookTasks = new ApiTask();
	private ApiResponse<PageResponse<Book>> response;	
	private int currentPage = 0;
	private int pageSize = 20;
	private String currentSort = "title";
	private HomeController homeController;
	private String token;
	private List<BookCardController> controllerList;
	private enum IconMode { NORMAL, SMALL, XSMALL}
	private IconMode iconStatus = IconMode.NORMAL;
	private int totElement;
	private enum Mode { NORMAL, COLLECTION, SEARCH }
	private Mode currentMode = Mode.NORMAL;
	private Collection currentCollection;
	private String currentSearchKeyword;
	private Boolean allSelected = false;
	List<Book> books;
	
    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    
	@FXML
    public void initialize() {
		initializeToolTip();
	}
	
	
	public void initNormal() {	
		currentMode = Mode.NORMAL;
		sortMenu = new MenuButton(resources.getString("menu.sort"));    		
	    loadBooks(currentPage, pageSize, "title");	    	    		
	}
	
	public void initSearch(String keyword) {
		currentMode = Mode.SEARCH;
		currentSearchKeyword = keyword;
		searchBooks(keyword, 0, 20);
		disableButtons();
    }
	
	public void initCollection(Collection collection) {
		currentMode = Mode.COLLECTION;
	    currentCollection = collection;
		loadCollection(collection.getId(), 0, 20);
		disableButtons();
    }
	
	@FXML
	public void refresh() {
		sortMenu = new MenuButton(resources.getString("menu.sort")); 		
	    loadBooks(currentPage, pageSize, "title");
	}
	
	public void loadBooks(int page, int size, String sort) {
		
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.loadBooksTask(page, size, sort);
		runBookLoadingTask(task);
	}
	
	public void loadCollection(int id, int page, int size) {		
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.loadCollectionBooksTask(id, page, size);
		runBookLoadingTask(task);
	}
	
	public void searchBooks(String input, int page, int size) {
		currentMode = Mode.SEARCH;
		String keyword = parse(input);
		currentSearchKeyword = keyword;
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.searchBooksTask(keyword, page, size);
		runBookLoadingTask(task);
		disableButtons();
	}

	private void showBooks(List<Book> books, String fxml) {
		
		flowPane.getChildren().clear();
		controllerList = new ArrayList<>();
		this.books = books;
		for (Book book : books) {
			
			try {
				
				FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
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
	
	@FXML
	public void showAllElements() {
		pageSize=totElement;
		loadBooks(currentPage, pageSize, currentSort);
	}
	
	@FXML
	public void changeIcon() {
		if (iconStatus==IconMode.NORMAL) {
			iconStatus=IconMode.SMALL;
			pageSize=40;
			loadBooks(currentPage, pageSize, currentSort);
		} else if (iconStatus==IconMode.SMALL) {
			iconStatus=IconMode.XSMALL;
			pageSize=100;
			loadBooks(currentPage, pageSize, currentSort);
		} else if (iconStatus==IconMode.XSMALL) {
			iconStatus=IconMode.NORMAL;
			pageSize=20;
			loadBooks(currentPage, pageSize, currentSort);
		}
	}
	
	public void showBookDetails(Book book) {
			booksViewContent = rootPane.getChildren().get(0);
	        try {
	        	//booksViewContent = rootPane.getChildren().get(0);
	        	ResourceBundle bundle = ResourceBundle.getBundle("messages", Language.getLocale());
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("BookDetailsTab.fxml"), bundle);
	            Node detailsRoot = loader.load();

	            BookDetailsController controller = loader.getController();
	            
	            controller.setBook(book, homeController, token);	            
	            
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
	
	private void runBookLoadingTask(Task<ApiResponse<PageResponse<Book>>> task) {
	    loadingLabel.setText("Loading...");
	    pageLabel.setText("");
	    previousButton.setDisable(true);
	    nextButton.setDisable(true);

	    task.setOnSucceeded(e -> {
	        ApiResponse<PageResponse<Book>> result = task.getValue();
	        if (result.isSuccess()) {
	            loadingLabel.setText("");
	            PageResponse<Book> page = result.getData();
	            totElement = (int) result.getData().getTotalElements();
	            updatePagination(page);
	            if (iconStatus==IconMode.NORMAL)  
	            	showBooks(page.getContent(), "BookCard.fxml");
	            else if (iconStatus==IconMode.SMALL)
	            	showBooks(page.getContent(), "BookCardSmall.fxml");
	            else if (iconStatus==IconMode.XSMALL)
	            	showBooks(page.getContent(), "BookCardXSmall.fxml");
	        } else {
	            showErrorAlert(resources.getString("alert.errorload"), resources.getString("alert.connection"));
	        }
	    });

	    task.setOnFailed(e -> task.getException().printStackTrace());
	    new Thread(task).start();
	}
	
	private void updatePagination(PageResponse<Book> page) {
		if (page.getNumberOfElements()==0 && page.getTotalElements()>0) {
			pageLabel.setText("0-0 of "+ page.getTotalElements());
			previousButton.setDisable(false); 
			nextButton.setDisable(true);
		} else {
			pageLabel.setText((page.getNumber()*pageSize+1) +
					"-" + ((page.getNumber()*pageSize)+page.getNumberOfElements()) +
					" of "+ page.getTotalElements()); 
			if (page.getNumber()==0) 
				previousButton.setDisable(true); 
			else 
				previousButton.setDisable(false); 
			if ((page.getTotalPages()-page.getNumber()<=1)) 
				nextButton.setDisable(true); 
			else 
				nextButton.setDisable(false);
		}						
	}
	
	private void showErrorAlert(String title, String header) {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.getDialogPane().getStylesheets().add(getClass().getResource("AlertStyle.css").toExternalForm());
	    alert.setTitle(title);
	    alert.setHeaderText(header);
	    alert.showAndWait();
	}
	
	@FXML
	public void nextPage() {
	    currentPage++;
	    loadCurrentModePage();

	}
			
	@FXML
	public void previousPage() {
		currentPage--;
		loadCurrentModePage();

	}
	
	private void loadCurrentModePage() {
		switch (currentMode) {
        case NORMAL:
            loadBooks(currentPage, pageSize, currentSort);
            break;

        case COLLECTION:
            if (currentCollection != null)
                loadCollection(currentCollection.getId(), currentPage, pageSize);
            break;

        case SEARCH:
            if (currentSearchKeyword != null)
                searchBooks(currentSearchKeyword, currentPage, pageSize);
            break;
		}
	}
	
	public void backToBooks() {
				
		if (booksViewContent != null) {
			rootPane.getChildren().setAll(booksViewContent);
	    } else {
	        loadBooks(currentPage, pageSize, currentSort);
	    }
	}
	
	public void selectAllBooks() {
		if (allSelected) {
			deselectAllBooks();
			homeController.deselectAllBooks();
			allSelected=false;
		} else if (controllerList!=null) {
			for (int i=0; i<controllerList.size(); i++) {			
				homeController.addSelectedBooks(controllerList.get(i).selectBook());			
			}
			allSelected=true;
		}
		
	}
	
	public void deselectAllBooks() {
		if (controllerList!=null) {
			for (int i=0; i<controllerList.size(); i++) {
				controllerList.get(i).deselectBook();
			}		
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
	
	@FXML
	public void setDateAsSort() {
		currentSort = "creationDate";
		loadBooks(currentPage, pageSize, currentSort);
	}
	
	private void initializeToolTip() {
		iconButton.setTooltip(new Tooltip(resources.getString("tooltip.icon")));
		selectAllButton.setTooltip(new Tooltip(resources.getString("tooltip.select")));
		refreshButton.setTooltip(new Tooltip(resources.getString("tooltip.refresh")));
		allBooksButton.setTooltip(new Tooltip(resources.getString("tooltip.all")));

	}
	
	public void disableButtons() {
		iconButton.setDisable(true);
		iconButton.setVisible(false);
		allBooksButton.setDisable(true);
		allBooksButton.setVisible(false);
		refreshButton.setDisable(true);
		refreshButton.setVisible(false);
		hBox.getChildren().remove(sortMenu);

	}
}
