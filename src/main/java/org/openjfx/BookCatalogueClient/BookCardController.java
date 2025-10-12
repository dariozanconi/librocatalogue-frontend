package org.openjfx.BookCatalogueClient;
import java.net.MalformedURLException;
import java.util.function.Consumer;
import org.openjfx.BookCatalogueClient.model.Book;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.concurrent.Task;

public class BookCardController {
	
	@FXML
	private ImageView coverImage;
	
	@FXML
	private Label titleLabel;
	
	@FXML
	private Label authorLabel;
	
	@FXML
	private CheckBox selectBox;
	
	Consumer<Book> onBookClicked;
	Consumer<Book> onBookSelected;
	Consumer<Book> onBookDeselected;
	
	private Book book;

	public void setData(Book book) throws MalformedURLException {
		
		this.book = book;
		titleLabel.setText(book.getTitle());
		if (authorLabel!=null)
			authorLabel.setText(book.getAuthorSort());
		
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
	    
	    loadImageTask.setOnSucceeded(e -> { if (coverImage!=null) coverImage.setImage(loadImageTask.getValue()); });
	    loadImageTask.setOnFailed(e -> {
	        System.out.println("Failed to load image");
	    });

	    new Thread(loadImageTask).start();
		
	}
	
	public void deselectBook() {
		selectBox.setSelected(false);
	}
	
	public void setOnBookClicked(Consumer<Book> callback) {
		this.onBookClicked = callback;
	}
	
	public void setOnBookSelected(Consumer<Book> callback) {
		this.onBookSelected = callback;
	}
	
	public void setOnBookDeselected(Consumer<Book> callback) {
		this.onBookDeselected = callback;
	}
	
	@FXML
	public void onCardClicked() {
		
		if (onBookClicked!=null)
			onBookClicked.accept(book);
	}
	
	@FXML
	public void onCardSelected() {		
		if (onBookSelected!=null && selectBox.isSelected())
			onBookSelected.accept(book);
		else if (onBookDeselected!=null && !selectBox.isSelected())
			onBookDeselected.accept(book);
	}
		
}
