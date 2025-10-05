package org.openjfx.BookCatalogueClient;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.Book.Tag;
import org.openjfx.BookCatalogueClient.task.ApiTask;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;

public class UpdateBookController {
	
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
	Button updateButton;
	
	@FXML
	Label coverDetailsLabel;
	
	@FXML
	Label titleLabel;
	
	@FXML
	Label authorLabel;
	
	@FXML
	ImageView coverImageView;
	
	private final ApiTask bookTasks = new ApiTask();
	private ApiResponse<String> response;
	HomeController homeController;
	Book book;
	String token;
	File selectedFile;
	Book updatedBook;
	
	public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
	
	public void setBookToUpdate(Book book, String token) {
		
		formatDatePicker();
		updatedBook = new Book();
		this.token = token;
		this.book = book;
		titleLabel.setText(book.getTitle());
		authorLabel.setText(book.getAuthor());
		URL url;
		BufferedImage image;
		try {
			url = new URL(book.getImageUrl());
			image = ImageIO.read(url);
			coverImageView.setImage(SwingFXUtils.toFXImage(image, null));
			
			File tempFile = File.createTempFile("book-cover-", "."+getFormat());
		    ImageIO.write(image, getFormat(), tempFile);
		    this.selectedFile = tempFile;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		titleField.setText(book.getTitle());
		authorField.setText(book.getAuthor());
		isbnField.setText(book.getIsbn());
		publisherField.setText(book.getPublisher());
		publishPlaceField.setText(book.getPublishPlace());
		if (book.getPublishDate()!=null)
			publishDateField.setValue(book.getPublishDate());
		numberPagesField.setText(String.valueOf(book.getPages()));
		
		String tags = "";
		for (int i=0; i<book.getTags().size(); i++) {
			if (i==book.getTags().size()-1) 
				tags = tags.concat(book.getTags().get(i).getName());
			else
				tags = tags.concat(book.getTags().get(i).getName().concat(", "));
			
		}
		tagsField.setText(tags);		
		availableCheckBox.setSelected(book.isAvailable());
		
	}
	
	@FXML
	public void update() {
		
		updatedBook.setId(book.getId());
		updatedBook.setTitle(titleField.getText());
		updatedBook.setAuthor(authorField.getText());
		updatedBook.setIsbn(isbnField.getText());
		updatedBook.setPublisher(publisherField.getText());
		updatedBook.setPublishPlace(publishPlaceField.getText());
		updatedBook.setPublishDate(publishDateField.getValue());
		updatedBook.setPages(Integer.parseInt(numberPagesField.getText()));
		updatedBook.setAvailable(availableCheckBox.isSelected());
		updatedBook.setTags(readTags(tagsField.getText()));
		
		
		Task<ApiResponse<String>> task = bookTasks.updateBookTask(updatedBook.getId(), updatedBook, selectedFile, token);
			
		task.setOnSucceeded(e -> {
			response = task.getValue();			
				if (response.isSuccess()) {														
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.getDialogPane().getStylesheets().add(
							   getClass().getResource("AlertStyle.css").toExternalForm()
						);
					alert.setTitle(resources.getString("alert.updated"));
					alert.setHeaderText(resources.getString("alert.updated"));
					alert.showAndWait();
					Tab selected = homeController.getTabPane().getSelectionModel().getSelectedItem();
					homeController.getTabPane().getTabs().remove(selected);
						
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.getDialogPane().getStylesheets().add(
							   getClass().getResource("AlertStyle.css").toExternalForm()
						);
					alert.setTitle(resources.getString("alert.updatefail"));
					alert.setHeaderText(response.getData());
					alert.showAndWait();
				}
				
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
		fileChooser.setTitle(resources.getString("chooser.title"));
		fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
		selectedFile = fileChooser.showOpenDialog(rootNode.getScene().getWindow());
		coverDetailsLabel.setText(selectedFile.getName());
		coverImageView.setImage(new Image(selectedFile.toURI().toString()));
	}
	
	public String getFormat() {
		 String fileName = book.getImageUrl().toLowerCase();
		 String format = "jpg"; 
		 if (fileName.endsWith(".png")) {
		     return "png";
		 } else if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
		     return "jpg";
		 }
		 return format;
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
