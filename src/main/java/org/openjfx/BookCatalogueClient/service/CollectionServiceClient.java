package org.openjfx.BookCatalogueClient.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.openjfx.BookCatalogueClient.model.ApiError;
import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.Collection;
import org.openjfx.BookCatalogueClient.model.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CollectionServiceClient {
	
	String urlDomain = DomainConstant.DOMAIN_PUBLIC;
	
	public ApiResponse<List<Collection>> getAllCollections() {
		
		String url = urlDomain + "/collections";
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpGet request = new HttpGet(url);
			
			try(CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				List<Collection> collections = mapper.readValue(body, new TypeReference<List<Collection>>() {});
				
				return new ApiResponse<>(statusCode, collections);
			}
		}
		
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<> (new ApiError(500, e.getMessage()));
		}		
	}
	
	public ApiResponse<PageResponse<Book>> getCollectionBooks(int id, int page, int size) {
		
		String url = urlDomain + "/collections/" + id + "/books?page=" + page + "&size=" + size;
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpGet request = new HttpGet(url);
			
			try(CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());				
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				
				if (statusCode == 200 || statusCode == 201) {
					PageResponse<Book> pageResponse = mapper.readValue(body, new TypeReference<PageResponse<Book>>() {});               
					return new ApiResponse<>(statusCode, pageResponse);
				} else {
					return new ApiResponse<>(new ApiError(statusCode, body));
				}
			}
		}	
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<> (new ApiError(500, e.getMessage()));
		}			
	}
	
	public ApiResponse<Collection> addCollection(Collection collection, String token) throws JsonProcessingException {
		
		String url = urlDomain + "/collections";
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		String json = mapper.writeValueAsString(collection);
		ContentType jsonType = ContentType.create("application/json", StandardCharsets.UTF_8);
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpPost request = new HttpPost(url);
			StringEntity entity = new StringEntity(json, jsonType);
			request.setEntity(entity);
			request.addHeader("Authorization", "Bearer " + token);
			
			try(CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				
				if (statusCode == 200 || statusCode == 201) {
					return new ApiResponse<>(statusCode, mapper.readValue(body, Collection.class));
				} else {
					return new ApiResponse<>(new ApiError(statusCode, body));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<> (new ApiError(500, e.getMessage()));
		}	
	}
	
	public ApiResponse<Book> addBook(int id, Book book, String token) throws JsonProcessingException {
		
		String url = urlDomain + "/collections/" + id + "/books";
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		String json = mapper.writeValueAsString(book);
		ContentType jsonType = ContentType.create("application/json", StandardCharsets.UTF_8);
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpPost request = new HttpPost(url);
			StringEntity entity = new StringEntity(json, jsonType);
			request.setEntity(entity);
			request.addHeader("Authorization", "Bearer " + token);
			
			try(CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				
				if (statusCode == 200 || statusCode == 201) {
					return new ApiResponse<>(statusCode, mapper.readValue(body, Book.class));
				} else {
					return new ApiResponse<>(new ApiError(statusCode, body));
				}
			}
		}	
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<> (new ApiError(500, e.getMessage()));
		}
	}
	
	public ApiResponse<String> removeBook(int collectionId, int bookId, String token) {
		
		String url = urlDomain + "/collections/" + collectionId + "/books/" + bookId;
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpDelete request = new HttpDelete(url);
			request.addHeader("Authorization", "Bearer " + token);
			
			try (CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				return new ApiResponse<>(statusCode, body);
				}
		}	
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<> (new ApiError(500, e.getMessage()));
		}
	}
	
	public ApiResponse<String> removeCollection(int id, String token) {
		
		String url = urlDomain + "/collections/id/" + id;
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpDelete request = new HttpDelete(url);
			request.addHeader("Authorization", "Bearer " + token);
			
			try (CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				return new ApiResponse<>(statusCode, body);
				}
		}	
		
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<> (new ApiError(500, e.getMessage()));
		}
	}
	
	public ApiResponse<List<Collection>> searchCollections(String keyword) {
		
		String url = urlDomain + "/collections/search?keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpGet request = new HttpGet(url);
			 try (CloseableHttpResponse response = client.execute(request)) {
				 
				 int statusCode = response.getCode();
				 String body = EntityUtils.toString(response.getEntity());
				 
				 ObjectMapper mapper = new ObjectMapper();
				 mapper.registerModule(new JavaTimeModule());				 
				 return new ApiResponse<List<Collection>>(statusCode, mapper.readValue(body, new TypeReference<List<Collection>>() {}));
			 }			 		 
		}
		
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<>(new ApiError(500, e.getMessage()));
		}			
	}
	
}
