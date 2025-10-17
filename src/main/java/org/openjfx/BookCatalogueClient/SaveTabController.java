package org.openjfx.BookCatalogueClient;


import java.io.File;
import java.io.FileOutputStream;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.BookTable;
import org.openjfx.BookCatalogueClient.model.PageResponse;
import org.openjfx.BookCatalogueClient.service.ExcelCreator;
import org.openjfx.BookCatalogueClient.task.ApiTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class SaveTabController {
	
	@FXML
	StackPane rootNode;
	
	@FXML 
	private ResourceBundle resources;
	
	@FXML
	Label headerLabel;
	
	@FXML
	CheckBox authorBox;
	
	@FXML
	CheckBox titleBox;
	
	@FXML
	CheckBox isbnBox;
	
	@FXML
	CheckBox publisherBox;
	
	@FXML
	CheckBox dateBox;
	
	@FXML
	CheckBox placeBox;
	
	@FXML
	CheckBox availableBox;
	
	@FXML
	MenuButton sortMenu;
	
	@FXML
	MenuItem titleItem;
	
	@FXML
	MenuItem authorItem;
	
	@FXML
	MenuItem publisherItem;
	
	@FXML
	TableView<BookTable> previewTable;
	
	ObservableList<BookTable> booksPreview = FXCollections.observableArrayList();		
	private final ApiTask bookTasks = new ApiTask();
	private ApiResponse<PageResponse<Book>> response;
	private ApiResponse<PageResponse<Book>> response2;
	private int totalElements;
	private List<Book> books = new ArrayList<Book>();
	private static Boolean[] settings = {true, true, true, true, true, true, true};
	private String currentSort = "authorSort";
	DateTimeFormatter uiFormatter = DateTimeFormatter.ofPattern("yyyy");
				
	@FXML
	public void initialize() {
		
		authorBox.setSelected(true);
		authorBox.setOnAction(e -> { showPreview(); settings[0]=authorBox.isSelected(); });
		titleBox.setSelected(true);
		titleBox.setOnAction(e -> { showPreview(); settings[1]=titleBox.isSelected(); });
		isbnBox.setSelected(true);
		isbnBox.setOnAction(e -> {showPreview(); settings[2]=isbnBox.isSelected(); });
		publisherBox.setSelected(true);
		publisherBox.setOnAction(e -> {showPreview(); settings[3]=publisherBox.isSelected(); });
		placeBox.setSelected(true);
		placeBox.setOnAction(e -> {showPreview(); settings[4]=placeBox.isSelected(); });
		dateBox.setSelected(true);
		dateBox.setOnAction(e -> {showPreview(); settings[5]=dateBox.isSelected(); });
		availableBox.setSelected(true);
		availableBox.setOnAction(e -> {showPreview(); settings[6]=availableBox.isSelected(); });
			  
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.loadBooksTask(1, 1, "authorSort");
		task.setOnSucceeded(e -> {		
			
			response = task.getValue();
			totalElements = (int) response.getData().getTotalElements();
			System.out.println(totalElements);
			loadBooks();
		});		
		
		new Thread(task).start();
	}
	
	@FXML
	public void loadBooks() {
		
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.loadBooksTask(0, totalElements, currentSort);
		
		task.setOnSucceeded(e -> {
			response2 = task.getValue();
			books = response2.getData().getContent();
			showPreview();
		});
		
		new Thread(task).start();
	}
	
	@FXML
	public void showPreview() {
	    booksPreview.clear();
	    previewTable.getColumns().clear();
	    
	    int colIndex = 0;
	    
	    TableColumn<BookTable, String> indexCol = new TableColumn<>("");
	    indexCol.setCellFactory(col -> {
		    TableCell<BookTable, String> cell = new TableCell<>() {
		        @Override
		        protected void updateItem(String item, boolean empty) {
		            super.updateItem(item, empty);
		            setText(empty ? null : item);
		        }
		    };

		    cell.getStyleClass().add("row-header-cell");
		    cell.setEditable(false);
		    cell.setAlignment(Pos.CENTER);
		    return cell;
		});
	    indexCol.setCellValueFactory(cellData -> cellData.getValue().indexProperty());
	    indexCol.setPrefWidth(30);
	    previewTable.getColumns().add(indexCol);

	    if (authorBox.isSelected()) {
	        TableColumn<BookTable, String> authorCol = new TableColumn<>(getColumnLetter(colIndex++));
	        authorCol.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
	        authorCol.setMaxWidth(100);
	        previewTable.getColumns().add(authorCol);
	    }

	    if (titleBox.isSelected()) {
	        TableColumn<BookTable, String> titleCol = new TableColumn<>(getColumnLetter(colIndex++));
	        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
	        titleCol.setMaxWidth(100);
	        previewTable.getColumns().add(titleCol);
	    }

	    if (isbnBox.isSelected()) {
	        TableColumn<BookTable, String> isbnCol = new TableColumn<>(getColumnLetter(colIndex++));
	        isbnCol.setCellValueFactory(cellData -> cellData.getValue().isbnProperty());
	        isbnCol.setMaxWidth(103);
	        previewTable.getColumns().add(isbnCol);
	    }

	    if (publisherBox.isSelected()) {
	        TableColumn<BookTable, String> publisherCol = new TableColumn<>(getColumnLetter(colIndex++));
	        publisherCol.setCellValueFactory(cellData -> cellData.getValue().publisherProperty());
	        publisherCol.setMaxWidth(80);
	        previewTable.getColumns().add(publisherCol);
	    }

	    if (placeBox.isSelected()) {
	        TableColumn<BookTable, String> placeCol = new TableColumn<>(getColumnLetter(colIndex++));
	        placeCol.setCellValueFactory(cellData -> cellData.getValue().placeProperty());
	        placeCol.setMaxWidth(80);
	        previewTable.getColumns().add(placeCol);
	    }

	    if (dateBox.isSelected()) {
	        TableColumn<BookTable, String> dateCol = new TableColumn<>(getColumnLetter(colIndex++));
	        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
	        dateCol.setMaxWidth(65);
	        previewTable.getColumns().add(dateCol);
	    }

	    if (availableBox.isSelected()) {
	        TableColumn<BookTable, String> availableCol = new TableColumn<>(getColumnLetter(colIndex++));
	        availableCol.setCellValueFactory(cellData -> cellData.getValue().availableProperty());
	        availableCol.setMaxWidth(75);
	        previewTable.getColumns().add(availableCol);
	    }
	    
	    for (int i=0; i<10; i++) {
	    	TableColumn<BookTable, String> colnext = new TableColumn<>(getColumnLetter(colIndex++));
	    	colnext.setPrefWidth(60);
	    	previewTable.getColumns().add(colnext);
	    }
	    
	    
	    for (int i = 0; i < Math.min(books.size(), 7); i++) {	
	    	
	    	Book b = books.get(i);
	    	if (i>0) {
	    		b = books.get(i-1);
	    	}	   
	        BookTable bt = new BookTable();

	        bt.setIndex(String.valueOf(i + 1));
	        
	        if (i==0) {
	        	bt.setAuthor(resources.getString("excel.author"));
	        	bt.setTitle(resources.getString("excel.title"));
	        	bt.setIsbn("ISBN");
	        	bt.setPublisher(resources.getString("excel.publisher"));
	        	bt.setPlace(resources.getString("excel.place"));
	        	bt.setDate(resources.getString("excel.date"));
	        	bt.setAvailable(resources.getString("excel.availability"));
	        } else if (i==6) {
	        	bt.setAuthor("...");
	        	bt.setTitle("...");
	        	bt.setIsbn("...");
	        	bt.setPublisher("...");
	        	bt.setPlace("...");
	        	bt.setDate("...");
	        	bt.setAvailable("...");
	        } else {
	        	bt.setAuthor(b.getAuthorSort());
	        	bt.setTitle(b.getTitle());
	        	bt.setIsbn(b.getIsbn());
	        	bt.setPublisher(b.getPublisher());
	        	bt.setPlace(b.getPublishPlace());
	        	bt.setDate(b.getPublishDate()!=null ? b.getPublishDate().format(uiFormatter) : "");
	        	bt.setAvailable(b.isAvailable() ? resources.getString("excel.available") : resources.getString("excel.notavailable"));
	        }
	        

	        booksPreview.add(bt);
	    }

	    previewTable.setItems(booksPreview);
	}
	
	@FXML
	public void save() {
		ExcelCreator creator = new ExcelCreator(books, settings, resources);

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save");
		fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Excel Dokument", "*.xlsx"));
		
		File fileToSave = fileChooser.showSaveDialog(rootNode.getScene().getWindow());
		if (fileToSave != null) {
            try {
            	FileOutputStream out = new FileOutputStream(fileToSave);
                creator.create().write(out);
                Alert alert = new Alert(AlertType.INFORMATION);
				alert.getDialogPane().getStylesheets().add(
					    getClass().getResource("AlertStyle.css").toExternalForm()
					);
				alert.setTitle(resources.getString("alert.save"));
				alert.setHeaderText(resources.getString("alert.savesuccess"));
				alert.showAndWait();
                out.close();
            } catch (Exception ex) {
            	Alert alert = new Alert(AlertType.INFORMATION);
 				alert.getDialogPane().getStylesheets().add(
 					    getClass().getResource("AlertStyle.css").toExternalForm()
 					);
 				alert.setTitle(resources.getString("alert.error"));
 				alert.setHeaderText(resources.getString("alert.savefail"));
 				alert.showAndWait();
            }
        }
		
	}
	
	@FXML
	public void reset() {
		previewTable.getItems().clear();
		initialize();
	}
		
	@FXML
	public void setTitleAsSort() {
		currentSort = "title";
		loadBooks();
	}
	
	@FXML
	public void setAuthorAsSort() {
		currentSort = "authorSort";
		loadBooks();
	}
	
	@FXML
	public void setPublisherAsSort() {
		currentSort = "publisher";
		loadBooks();
	}
	
	@FunctionalInterface
	interface ConsumerTwo<T, U> {
	    void accept(T t, U u);
	}
	
	private String getColumnLetter(int index) {
	    return String.valueOf((char) ('A' + index));
	}
}
