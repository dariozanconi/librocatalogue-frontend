package org.openjfx.BookCatalogueClient.model;

import java.time.LocalDate;
import java.util.List;

public class Patron {
	
	private int id;

	private String firstName;
	private String lastName;
	private String email;

	private LocalDate creationDate;
	private List<Book> books;
	
	public Patron() {}

	public Patron(int id, String firstName, String lastName, String email, LocalDate creationDate, List<Book> books) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.creationDate = creationDate;
		this.books = books;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}
	
	
}
