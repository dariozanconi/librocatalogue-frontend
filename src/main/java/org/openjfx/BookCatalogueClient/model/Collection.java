package org.openjfx.BookCatalogueClient.model;

import java.util.List;

public class Collection {
	
	private int id;
	private String name;
	private List<Book> books;
	
	public Collection(String name) {
		this.name = name;
	}
	
	public Collection(String name, List<Book> books) {
		this.name = name;
		this.books = books;
	}
	
	public Collection() {}
	
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

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}
	
	@Override
	public String toString() {
	    return name;
	}
	
}
