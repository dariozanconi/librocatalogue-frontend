package org.openjfx.BookCatalogueClient.model;

public class ApiError {
	
    private int status;
    private String message;

    public ApiError() {}

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
}