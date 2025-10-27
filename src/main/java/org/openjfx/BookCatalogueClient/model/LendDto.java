package org.openjfx.BookCatalogueClient.model;

public class LendDto {

    private Patron patron;
    private String description;
    
    public LendDto() {}
    
	public LendDto(Patron patron, String description) {
		this.patron = patron;
		this.description = description;
	}

	public Patron getPatron() {
		return patron;
	}

	public void setPatron(Patron patron) {
		this.patron = patron;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
    
    
}


