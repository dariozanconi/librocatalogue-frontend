package org.openjfx.BookCatalogueClient.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BookTable {
	
	private Book book;
	
	public BookTable() {}
	
	public BookTable(String title, String author, Book book) {
		this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.book = book;
	}
	
	private StringProperty index;
	public void setIndex(String value) { indexProperty().set(value); }
    public String getIndex() { return indexProperty().get(); }
    public StringProperty indexProperty() { 
        if (index == null) index = new SimpleStringProperty(this, "index");
        return index; 
    }
    
    private StringProperty author;
    public void setAuthor(String value) { authorProperty().set(value); }
    public String getAuthor() { return authorProperty().get(); }
    public StringProperty authorProperty() { 
        if (author == null) author = new SimpleStringProperty(this, "author");
        return author; 
    }
	
    private StringProperty title;
    public void setTitle(String value) { titleProperty().set(value); }
    public String getTitle() { return titleProperty().get(); }
    public StringProperty titleProperty() { 
        if (title == null) title = new SimpleStringProperty(this, "title");
        return title; 
    }
	
    private StringProperty isbn;
    public void setIsbn(String value) { isbnProperty().set(value); }
    public String getIsbn() { return isbnProperty().get(); }
    public StringProperty isbnProperty() { 
        if (isbn == null) isbn = new SimpleStringProperty(this, "isbn");
        return isbn; 
    }
	
    private StringProperty publisher;
    public void setPublisher(String value) { publisherProperty().set(value); }
    public String getPublisher() { return publisherProperty().get(); }
    public StringProperty publisherProperty() { 
        if (publisher == null) publisher = new SimpleStringProperty(this, "publisher");
        return publisher; 
    }
	
    private StringProperty place;
    public void setPlace(String value) { placeProperty().set(value); }
    public String getPlace() { return placeProperty().get(); }
    public StringProperty placeProperty() { 
        if (place == null) place = new SimpleStringProperty(this, "place");
        return place; 
    }
    
    private StringProperty date;
    public void setDate(String value) { dateProperty().set(value); }
    public String getDate() { return dateProperty().get(); }
    public StringProperty dateProperty() { 
        if (date == null) date = new SimpleStringProperty(this, "date");
        return date; 
    }
    
    private StringProperty available;
    public void setAvailable(String value) { availableProperty().set(value); }
    public String getAvailable() { return availableProperty().get(); }
    public StringProperty availableProperty() { 
        if (available == null) available = new SimpleStringProperty(this, "available");
        return available; 
    }

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}
    
    
}
