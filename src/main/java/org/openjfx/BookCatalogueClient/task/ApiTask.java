package org.openjfx.BookCatalogueClient.task;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.hc.core5.http.ParseException;
import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.BookDto;
import org.openjfx.BookCatalogueClient.model.Collection;
import org.openjfx.BookCatalogueClient.model.PageResponse;
import org.openjfx.BookCatalogueClient.model.RegisterRequest;
import org.openjfx.BookCatalogueClient.model.Users;
import org.openjfx.BookCatalogueClient.service.BookServiceClient;
import org.openjfx.BookCatalogueClient.service.CollectionServiceClient;
import org.openjfx.BookCatalogueClient.service.GoogleBooksService;
import org.openjfx.BookCatalogueClient.service.OpenBookService;
import org.openjfx.BookCatalogueClient.service.UserServiceClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.concurrent.Task;


public class ApiTask {
	
	private final BookServiceClient bookClient = new BookServiceClient();
	private final OpenBookService openBook = new OpenBookService();
	private final GoogleBooksService googleBooks = new GoogleBooksService();
    private final UserServiceClient userClient = new UserServiceClient();
    private final CollectionServiceClient collectionClient = new CollectionServiceClient();
    
    public Task<ApiResponse<PageResponse<Book>>> loadBooksTask(int page, int size, String sort) {
        return new Task<>() {
            @Override
            protected ApiResponse<PageResponse<Book>> call() {
                return bookClient.getBooks(page, size, sort);
            }
        };
    }
    
    public Task<ApiResponse<Book>> loadBookTask(String isbn) {
    	return new Task<>() {
            @Override
            protected ApiResponse<Book> call() throws ParseException {
                return bookClient.getBookByIsbn(isbn);
            }
        };
    }
    
    public Task<ApiResponse<List<Collection>>> loadCollectionsByBookIdTask(int id) {
    	return new Task<>() {
            @Override
            protected ApiResponse<List<Collection>> call() throws ParseException {
                return bookClient.getCollectionsByBookId(id);
            }
        };
    }
    
    public Task<ApiResponse<Book>> addBookTask(Book book, File image, String token) {
    	return new Task<>() {
            @Override
            protected ApiResponse<Book> call() throws ParseException, JsonProcessingException {
                return bookClient.addBook(book, image, token);
            }
        };
    }
    
    public Task<ApiResponse<String>> updateBookTask(int id, Book book, File image, String token) {
    	return new Task<>() {
            @Override
            protected ApiResponse<String> call() throws ParseException, JsonProcessingException {
                return bookClient.updateBook(id, book, image, token);
            }
        };
    }
    
    public Task<ApiResponse<String>> deleteBookTask(int id, String token) {
    	return new Task<>() {
            @Override
            protected ApiResponse<String> call() throws ParseException, JsonProcessingException {
                return bookClient.deleteBook(id, token);
            }
        };
    }
    
    public Task<ApiResponse<PageResponse<Book>>> searchBooksTask(String keyword, int page, int size) {
        return new Task<>() {
            @Override
            protected ApiResponse<PageResponse<Book>> call() {
                return bookClient.searchBooks(keyword, page, size);
            }
        };
    }
    
    public Task<BookDto> parseBookTask(String isbn) {
    	return new Task<>() {
            @Override
            protected BookDto call() throws ParseException, IOException {
            	BookDto bookData = openBook.parse(isbn);
            	if (bookData!=null) {
            		System.out.println("book from Open Library");
            		return bookData;
            	}
            	bookData = googleBooks.parse(isbn);
            	if (bookData!=null) {
            		System.out.println("book from Google Books");
            		return bookData;
            	}
            	return null;
            }
        };
    }
    
    public Task<ApiResponse<String>> loginTask(Users user) {
    	return new Task<>() {
            @Override
            protected ApiResponse<String> call() throws ParseException, JsonProcessingException {
                return userClient.login(user);
            }
    	};
    }
    
    public Task<ApiResponse<Users>> registerTask(RegisterRequest registerRequest) {
    	return new Task<>() {
            @Override
            protected ApiResponse<Users> call() throws ParseException, JsonProcessingException {
                return userClient.register(registerRequest);
            }
    	};
    }
    
    public Task<ApiResponse<String>> deleteAccountTask(String token) {
    	return new Task<>() {
            @Override
            protected ApiResponse<String> call() throws JsonProcessingException  {
                return userClient.deleteAccount(token);
            }
    	};
    } 
    
    public Task<ApiResponse<List<Collection>>> loadCollectionsTask() {
    	return new Task<>() {
            @Override
            protected ApiResponse<List<Collection>> call() {
                return collectionClient.getAllCollections();
            }
    	};
    }
    
    public Task<ApiResponse<PageResponse<Book>>> loadCollectionBooksTask(int id, int page, int size) {
    	return new Task<>() {
            @Override
            protected ApiResponse<PageResponse<Book>> call() {
                return collectionClient.getCollectionBooks(id, page, size);
            }
        };  	
    }
    
    public Task<ApiResponse<Collection>> addCollectionTask(Collection collection, String token) {
    	return new Task<>() {
            @Override
            protected ApiResponse<Collection> call() throws JsonProcessingException {
                return collectionClient.addCollection(collection, token);
            }
        };  	
    }
    
    public Task<ApiResponse<Book>> addBookCollectionTask(int id, Book book, String token) {
    	return new Task<>() {
            @Override
            protected ApiResponse<Book> call() throws JsonProcessingException {
                return collectionClient.addBook(id, book, token);
            }
        };  	
    }
    
    public Task<ApiResponse<String>> removeBookCollectionTask(int collectionId, int bookId, String token) {
    	return new Task<>() {
            @Override
            protected ApiResponse<String> call() {
                return collectionClient.removeBook(collectionId, bookId, token);
            }
        };  	
    }
    
    public Task<ApiResponse<String>> removeCollectionTask(int id, String token) {
    	return new Task<>() {
            @Override
            protected ApiResponse<String> call() {
                return collectionClient.removeCollection(id, token);
            }
        };  	
    } 
    
    public Task<ApiResponse<List<Collection>>> searchCollectionsTask(String keyword) {
    	return new Task<>() {
            @Override
            protected ApiResponse<List<Collection>> call() throws ParseException {
                return collectionClient.searchCollections(keyword);
            }
        };
    }
       
}
