package org.openjfx.BookCatalogueClient.model;

import java.io.File;

public class BookDto {
	
	private Book book;
	private File cover;
	
	public BookDto(Book book, File cover) {
		this.book = book;
		this.cover = cover;
	}
	
	public BookDto(Book book) {
		this.book = book;
	}
	
	public BookDto() {
		
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public File getCover() {
		return cover;
	}

	public void setCover(File cover) {
		this.cover = cover;
	}
	
	
	
}
