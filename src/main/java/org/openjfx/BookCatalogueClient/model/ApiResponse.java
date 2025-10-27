package org.openjfx.BookCatalogueClient.model;

public class ApiResponse<T> {
    private int status;
    private T data;
    private ApiError error;

    public ApiResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    public ApiResponse(ApiError error) {
        this.status = error.getStatus();
        this.error = error;
    }

    public boolean isSuccess() {
        return status ==200 || status == 201 || status == 204;
    }

    public int getStatus() { return status; }
    public T getData() { return data; }
    public ApiError getError() { return error; }
}
