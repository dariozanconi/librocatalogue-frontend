package org.openjfx.BookCatalogueClient.model;


import java.time.LocalDate;
import java.util.List;

public class Book {
		
	private int id;
    private String isbn;
    private String title;
    private String author;
    private String authorSort;
    private boolean available;
    
    private LocalDate publishDate;
    private String publishPlace;
    private String publisher;
    private int pages;

    private String imageName;
    private String imageUrl;
   
    private List<Tag> tags;
    
    public Book() {}
    
	public Book(int id, String isbn, String title, String author, boolean available, LocalDate publishDate,
			String publishPlace, String publisher, int pages, String imageName, String imageUrl, List<Tag> tags) {		
		this.id = id;
		this.isbn = isbn;
		this.title = title;
		this.author = author;
		this.available = available;
		this.publishDate = publishDate;
		this.publishPlace = publishPlace;
		this.publisher = publisher;
		this.pages = pages;
		this.imageName = imageName;
		this.imageUrl = imageUrl;
		this.tags = tags;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getAuthorSort() {
		return authorSort;
	}

	public void setAuthorSort(String authorSort) {
		this.authorSort = authorSort;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public LocalDate getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(LocalDate publishDate) {
		this.publishDate = publishDate;
	}

	public String getPublishPlace() {
		return publishPlace;
	}

	public void setPublishPlace(String publishPlace) {
		this.publishPlace = publishPlace;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	
	public static class Tag {
		
		private int id;
		private String name;
		
		public Tag() {}
			
		public Tag(String name) {
			this.name = name;
		}

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		
	}
	
    
}
