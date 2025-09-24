package org.openjfx.BookCatalogueClient;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.BookCatalogueClient.model.BookDto;
import org.openjfx.BookCatalogueClient.model.Collection;
import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.Book.Tag;
import org.openjfx.BookCatalogueClient.task.ApiTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;

public class AddBookController {
	
	@FXML
	BorderPane rootNode;
	
	@FXML
	TextField titleField;
	
	@FXML
	TextField authorField;
	
	@FXML
	TextField isbnField;
	
	@FXML
	TextField isbnField2;
	
	@FXML
	TextField publisherField;
	
	@FXML
	TextField publishPlaceField;
	
	@FXML
	DatePicker publishDateField;
	
	@FXML
	TextField tagsField;
	
	@FXML
	TextField numberPagesField;
	
	@FXML
	CheckBox availableCheckBox;
	
	@FXML
	Button addCoverButton;
	
	@FXML
	Button loadButton;
	
	@FXML
	Button loadButton2;
	
	@FXML
	Button addButton;
	
	@FXML
	Label coverDetailsLabel;
	
	@FXML
	Label messageLabel;
	
	@FXML
	ComboBox<Collection> collectionsBox;
	
	@FXML 
	ListView<String> bookListView;
			
	List<BookDto> books = new ArrayList<>();
	private final ApiTask bookTasks = new ApiTask();
	private final ApiTask collectionTasks = new ApiTask();
	private BookDto bookDto;
	private ApiResponse<Book> response;
	private ApiResponse<List<Collection>> collectionResponse;
	
	private List<Collection> collectionList;
	
	ObservableList<String> titles = FXCollections.observableArrayList();
	File selectedFile = null;
	Book book;
	String token;
	

	public void init(String token) {
		this.token = token;	
		Task<ApiResponse<List<Collection>>> task = collectionTasks.loadCollectionsTask();
		task.setOnSucceeded(e -> {
			collectionResponse = task.getValue();
			if (collectionResponse.isSuccess()) {										
				collectionList = collectionResponse.getData();
				collectionList.add(new Collection("none"));
				collectionsBox.setItems(FXCollections
                        .observableArrayList(collectionList));
				collectionsBox.getSelectionModel().selectLast();
			} 			
		});
		task.setOnFailed(e -> {			
		});
		new Thread(task).start();
	}
	
	@FXML
	public void loadManual() {
		
		book = new Book();
		book.setTitle(titleField.getText());
		book.setAuthor(authorField.getText());
		book.setIsbn(isbnField2.getText());
		book.setPublisher(publisherField.getText());
		book.setPublishPlace(publishPlaceField.getText());
		book.setPublishDate(publishDateField.getValue());
		book.setPages(Integer.parseInt(numberPagesField.getText()));
		book.setAvailable(availableCheckBox.isSelected());
		book.setTags(readTags(tagsField.getText()));
		
		if(!book.getTitle().isBlank() && !book.getAuthor().isBlank() && !book.getIsbn().isBlank()) {
			books.add(new BookDto(book, selectedFile));
			titles.add(book.getTitle());
			bookListView.setItems(titles);
		}
	}
	
	@FXML
	public void loadFromOpenBook() {

		bookDto = new BookDto();
		Task<BookDto> task = bookTasks.parseBookTask(isbnField.getText());
		task.setOnSucceeded(e -> {
			 bookDto = task.getValue();	
			 if (bookDto != null) {
				 books.add(bookDto);
				 titles.add(bookDto.getBook().getTitle());
				 System.out.println(bookDto.getBook().getTitle());
				 bookListView.setItems(titles);
			 } else {
				 Alert alert = new Alert(AlertType.ERROR);
				 alert.getDialogPane().getStylesheets().add(
						    getClass().getResource("AlertStyle.css").toExternalForm()
						);
				 alert.setTitle("Warning");
				 alert.setHeaderText("Book not found! \nPlease insert it manually");
				 alert.showAndWait();
			 }
			 
		});	 
		task.setOnFailed(e -> {
			Throwable ex = task.getException();
            ex.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR);
			alert.getDialogPane().getStylesheets().add(
				    getClass().getResource("AlertStyle.css").toExternalForm()
				);
			alert.setTitle("Warning");
			alert.setHeaderText("Insert a correct ISBN!");
			alert.showAndWait();
		});
		new Thread(task).start();
		
	}
	
	@FXML
	public void addBook() {
		
		if (!books.isEmpty()) {
			for (BookDto bookToAdd : books) {
				if (bookToAdd!=null ) {
					Task<ApiResponse<Book>> task = bookTasks.addBookTask(bookToAdd.getBook(), bookToAdd.getCover(), token);
					task.setOnSucceeded(e -> {
						response = task.getValue();
						if (response.isSuccess()) {	
							if (!collectionsBox.getValue().getName().equals("none")) {
								addBookToCollection(collectionsBox.getValue(), response.getData());
							}							
						} else {
							Alert alert = new Alert(AlertType.ERROR);
							alert.getDialogPane().getStylesheets().add(
								    getClass().getResource("AlertStyle.css").toExternalForm()
								);
							alert.setTitle("Cannot add the book");
							alert.setHeaderText("Cannot add the book" + bookToAdd.getBook().getTitle());
							System.out.println(response.getStatus() + " "+ response.getError().getMessage());
							alert.showAndWait();
						}
					});
					task.setOnFailed(e -> {
						Throwable ex = task.getException();
						ex.printStackTrace();
					});
					new Thread(task).start();
				}				
			}
			
			messageLabel.setText("Books added in the database");
			books.clear();
			titles.clear();
			bookListView.setItems(titles);
		}					
	}
	
	public void addBookToCollection(Collection collection, Book book) {
		
		Task<ApiResponse<Book>> task = collectionTasks.addBookCollectionTask(collection.getId(), book, token);
		task.setOnSucceeded(e -> {
			response = task.getValue();			
				
			});
			task.setOnFailed(e -> {
				Throwable ex = task.getException();
	            ex.printStackTrace();
			});
			new Thread(task).start();
	}
	
	public List<Tag> readTags(String tags) {
		List<Tag> tagList = new ArrayList<>();
		String tag = "";
		
		for (int i=0; i<tags.length(); i++) {
			if (tags.charAt(i)==' ' && i>0) {	
				if (tags.charAt(i-1)==',') {} else { tag = tag.concat(String.valueOf(tags.charAt(i))); }}
			else if (tags.charAt(i)!=',')
				tag = tag.concat(String.valueOf(tags.charAt(i)));
			else if (tags.charAt(i)==',' && i!=tags.length()-1) {
				tagList.add(new Tag(tag));
				tag="";
			} 
		}
		tagList.add(new Tag(tag));
		return tagList;
	}
	
	@FXML
	public void loadImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
		selectedFile = fileChooser.showOpenDialog(rootNode.getScene().getWindow());
		coverDetailsLabel.setText(selectedFile.getName());
		
	}
	
	public void formatDatePicker() {
		
		DateTimeFormatter uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	    publishDateField.setConverter(new StringConverter<LocalDate>() {
	        @Override
	        public String toString(LocalDate date) {
	            return date != null ? uiFormatter.format(date) : "";
	        }

	        @Override
	        public LocalDate fromString(String string) {
	            return (string != null && !string.isEmpty()) 
	                ? LocalDate.parse(string, uiFormatter) 
	                : null;
	        }
	    });
	}
}
