package org.openjfx.BookCatalogueClient;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.openjfx.BookCatalogueClient.model.BookDto;
import org.openjfx.BookCatalogueClient.model.Collection;
import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.Book.Tag;
import org.openjfx.BookCatalogueClient.task.ApiTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;

public class AddBookController {
	
	@FXML 
	private ResourceBundle resources;
	
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
	private ApiResponse<List<Collection>> collectionResponse;
	private List<Collection> collectionList;
	private ObservableList<String> titles = FXCollections.observableArrayList();
	private File selectedFile = null;
	private Book book;
	private String token;
	

	public void init(String token) {
		this.token = token;	
		isbnField.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					loadFromOpenBook();
				}			
			}
			
		});
		
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
		messageLabel.setText("");
		book = new Book();
		book.setTitle(titleField.getText());
		book.setAuthor(authorField.getText());
		book.setIsbn(isbnField2.getText());
		book.setPublisher(publisherField.getText());
		book.setPublishPlace(publishPlaceField.getText());
		book.setPublishDate(publishDateField.getValue());
		if (!numberPagesField.getText().isEmpty())
			book.setPages(Integer.parseInt(numberPagesField.getText()));
		else book.setPages(0);		
		book.setAvailable(availableCheckBox.isSelected());
		book.setTags(readTags(tagsField.getText()));
		
		if(!book.getTitle().isBlank() && !book.getAuthor().isBlank() && !book.getIsbn().isBlank()) {
			if (selectedFile!=null)
				books.add(new BookDto(book, selectedFile));
			else 
				books.add(new BookDto(book));
			titles.add(book.getTitle());
			bookListView.setItems(titles);
		} else {
			 Alert alert = new Alert(AlertType.ERROR);
			 alert.getDialogPane().getStylesheets().add(
					    getClass().getResource("AlertStyle.css").toExternalForm()
					);
			 alert.setTitle(resources.getString("alert.warning"));
			 alert.setContentText(resources.getString("alert.manualadderror"));
			 alert.showAndWait();
		}
	}
	
	@FXML
	public void loadFromOpenBook() {
		messageLabel.setText("");		
		bookDto = new BookDto();
		
		if (!isbnField.getText().isEmpty()) {
			
			Task<BookDto> task = bookTasks.parseBookTask(isbnField.getText());
			task.setOnSucceeded(e -> {
				 bookDto = task.getValue();	
				 if (bookDto != null && !titles.stream().anyMatch(title -> title.equalsIgnoreCase(bookDto.getBook().getTitle()))) {
					 books.add(bookDto);
					 titles.add(bookDto.getBook().getTitle());
					 bookListView.setItems(titles);
					 isbnField.setText("");
					 isbnField.requestFocus();
				 } else if (titles.stream().anyMatch(title -> title.equalsIgnoreCase(bookDto.getBook().getTitle()))){
					 showError(resources.getString("alert.warning"), resources.getString("alert.alreadyinqueue"));					 
				 } else {
					 showError(resources.getString("alert.warning"), resources.getString("alert.booknotloaded"));
				 }			 
			});	 
			task.setOnFailed(e -> {
				showError(resources.getString("alert.warning"), resources.getString("alert.booknotloaded"));
			});
			new Thread(task).start();			
		}		
	}
	
	@FXML
	public void clear() {
		books.clear();
		titles.clear();
		bookListView.getItems().clear();
		messageLabel.setText("");
		isbnField.requestFocus();
	}
	
	@FXML
	public void addBook() {
		
		if (!books.isEmpty()) {
			for (BookDto bookToAdd : books) {
				if (bookToAdd!=null ) {
					runApiTask(
						bookTasks.addBookTask(bookToAdd.getBook(), bookToAdd.getCover(), token),
						result -> {
							if (result.isSuccess()) {
						        if (!collectionsBox.getValue().getName().equals("none")) {
						            addBookToCollection(collectionsBox.getValue(), result.getData());
						        }
						            messageLabel.setText(resources.getString("alert.booksaddtodatabase"));
						        } else {
						            showError(resources.getString("alert.bookaddfail"), 
						            		resources.getString("alert.bookaddfail")+ bookToAdd.getBook().getTitle());
						        }
						}
					);			
				}				
			}
		
			books.clear();
			titles.clear();
			bookListView.setItems(titles);
		}					
	}
	
	public void addBookToCollection(Collection collection, Book book) {
		runApiTask(
			collectionTasks.addBookCollectionTask(collection.getId(), book, token),
			   result -> {
			   if (!result.isSuccess()) {
			    	showError(resources.getString("alert.bookaddfail"), 
			            	resources.getString("alert.bookaddfail"));
			   }
			   }
		);
	}
	
	private <T> void runApiTask(Task<ApiResponse<T>> task, Consumer<ApiResponse<T>> onSuccess) {
	    task.setOnSucceeded(e -> {
	        ApiResponse<T> result = task.getValue();
	        onSuccess.accept(result);
	    });
	    task.setOnFailed(e -> {
	       
	    });
	    new Thread(task).start();
	}
	
	private void showError(String title, String header) {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.getDialogPane().getStylesheets().add(
	        getClass().getResource("AlertStyle.css").toExternalForm()
	    );
	    alert.setTitle(title);
	    alert.setHeaderText(header);
	    alert.showAndWait();
	}
	
	public List<Tag> readTags(String tags) {
	    return Arrays.stream(tags.split(","))
	            .map(String::trim)
	            .filter(s -> !s.isEmpty())
	            .map(Tag::new)
	            .collect(Collectors.toList());
	}
	
	@FXML
	public void loadImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(resources.getString("chooser.title"));
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
