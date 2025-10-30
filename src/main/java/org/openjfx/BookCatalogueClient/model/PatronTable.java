package org.openjfx.BookCatalogueClient.model;

import javafx.beans.property.SimpleStringProperty;

public class PatronTable {
	
	private final SimpleStringProperty firstName;
    private final SimpleStringProperty lastName;
    private final SimpleStringProperty email;
    private final SimpleStringProperty creationDate;
    private Patron patron;
    

    public PatronTable(String fName, String lName, String email, String creationDate, Patron patron) {
        this.firstName = new SimpleStringProperty(fName);
        this.lastName = new SimpleStringProperty(lName);
        this.email = new SimpleStringProperty(email);
        this.creationDate = new SimpleStringProperty(creationDate);
        this.patron = patron;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String fName) {
        firstName.set(fName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String fName) {
        lastName.set(fName);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String mail) {
        email.set(mail);
    }
    
    public String getCreationDate() {
        return creationDate.get();
    }

    public void setCreationDate(String cDate) {
        creationDate.set(cDate);
    }

	public Patron getPatron() {
		return patron;
	}

	public void setPatron(Patron patron) {
		this.patron = patron;
	}
    
    
}
