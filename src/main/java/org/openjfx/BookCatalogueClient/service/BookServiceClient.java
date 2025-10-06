package org.openjfx.BookCatalogueClient.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ContentType;

import org.openjfx.BookCatalogueClient.model.ApiError;
import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.PageResponse;
import org.openjfx.BookCatalogueClient.model.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class BookServiceClient {
	
	private final File defaultImage;
	String urlDomain = DomainConstant.DOMAIN_LOCAL;

    public BookServiceClient() {

        try (InputStream is = getClass().getResourceAsStream("/default-image.jpg")) {
            if (is == null) {
                throw new IllegalStateException("Default image not found in resources!");
            }
            defaultImage = File.createTempFile("default-cover-", ".jpg");
            Files.copy(is, defaultImage.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not load default image", e);
        }
    }
	
	
	public ApiResponse<PageResponse<Book>> getBooks(int page, int size, String sort) {
			
		String url = urlDomain + "/books?page=" + page + "&size=" + size + "&sortBy=" + sort;
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();
                String body = EntityUtils.toString(response.getEntity());
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                PageResponse<Book> pageResponse = mapper.readValue(body, new TypeReference<PageResponse<Book>>() {});
                
                return new ApiResponse<>(statusCode, pageResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(new ApiError(500, e.getMessage()));	
        }		
	}
	
	public ApiResponse<Book> getBookByIsbn(String isbn) throws ParseException {
		
		String url = urlDomain + "/books/isbn/" + isbn;
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpGet request = new HttpGet(url);			
		
			try (CloseableHttpResponse response = client.execute(request)) {
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
			
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				if (statusCode == 200 || statusCode == 201) {
					return new ApiResponse<Book>(statusCode, mapper.readValue(body, Book.class));
				} else {
					return new ApiResponse<>(new ApiError(statusCode, body));
				}
			}
		} catch (IOException e) {			
			e.printStackTrace();
			return new ApiResponse<>(new ApiError(500, e.getMessage()));
		} 
		
	}
	
	public ApiResponse<List<Collection>> getCollectionsByBookId(int id) throws ParseException {
		
		String url = urlDomain + "/book/" + id + "/collections";
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpGet request = new HttpGet(url);			
		
			try (CloseableHttpResponse response = client.execute(request)) {
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
			
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				if (statusCode == 200 || statusCode == 201) {
					return new ApiResponse<List<Collection>>(statusCode, mapper.readValue(body, new TypeReference<List<Collection>>() {}));
				} else {
					return new ApiResponse<>(new ApiError(statusCode, body));
				}
			}
		} catch (IOException e) {			
			e.printStackTrace();
			return new ApiResponse<>(new ApiError(500, e.getMessage()));
		} 
		
	}
	
	public ApiResponse<Book> addBook(Book book, File image, String token) throws JsonProcessingException {
		
        String url = urlDomain + "/books";

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(book);
        ContentType jsonType = ContentType.create("application/json", StandardCharsets.UTF_8);

        File imageToSend = (image != null) ? image : defaultImage;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("book", json, jsonType)
                .addBinaryBody("image", imageToSend, getType(imageToSend), imageToSend.getName())
                .build();

            HttpPost request = new HttpPost(url);
            request.addHeader("Authorization", "Bearer " + token);
            request.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();
                String body = EntityUtils.toString(response.getEntity());

                if (statusCode == 200 || statusCode == 201) {
                    return new ApiResponse<>(statusCode, mapper.readValue(body, Book.class));
                } else {
                    return new ApiResponse<>(new ApiError(statusCode, body));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(new ApiError(500, e.getMessage()));
        }
    }
	
	public ApiResponse<String> updateBook(int id, Book book, File image, String token) throws JsonProcessingException {
		
		String url = urlDomain + "/books/id/" + id;
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(book);
		ContentType jsonType = ContentType.create("application/json", StandardCharsets.UTF_8);
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpEntity entity = MultipartEntityBuilder.create()
					.addTextBody("book", json, jsonType)
					.addBinaryBody("image", image, getType(image), image.getName())
					.build();
			
			HttpPut request = new HttpPut(url);
			request.addHeader("Authorization", "Bearer " + token);
			request.setEntity(entity);
			try (CloseableHttpResponse response = client.execute(request)) {
			
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				return new ApiResponse<>(statusCode, body);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<>(new ApiError(500, e.getMessage()));
		}
		
	}
	
	public ApiResponse<String> deleteBook(int id, String token) {
		
		String url = urlDomain + "/books/id/" + id;
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
					
			HttpDelete request = new HttpDelete(url);
			request.addHeader("Authorization", "Bearer " + token);
			
			try (CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				return new ApiResponse<>(statusCode, body);
				}
						
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<>(new ApiError(500, e.getMessage()));
		}
				
	}
	
	public ApiResponse<PageResponse<Book>> searchBooks(String keyword, int page, int size) {
		
		String url = urlDomain + "/books/search?keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8) + "&page=" + page + "&size=" + size;
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpGet request = new HttpGet(url);
			 try (CloseableHttpResponse response = client.execute(request)) {
				 
				 int statusCode = response.getCode();
				 String body = EntityUtils.toString(response.getEntity());
				 
				 ObjectMapper mapper = new ObjectMapper();
				 mapper.registerModule(new JavaTimeModule());
				 PageResponse<Book> pageResponse = mapper.readValue(body, new TypeReference<PageResponse<Book>>() {});
				 
				 return new ApiResponse<PageResponse<Book>>(statusCode, pageResponse);
			 }			 		 
		}
		
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<>(new ApiError(500, e.getMessage()));
		}			
	}
	
	public static ContentType getType(File image) {
		if (image!=null) {
			if (image.getName().endsWith(".png")) {
				return ContentType.IMAGE_PNG;
			} else if (image.getName().endsWith(".jpg")||image.getName().endsWith(".jpeg")) {
				return ContentType.IMAGE_JPEG;
			} 
		}
			return null;		
	}
}
