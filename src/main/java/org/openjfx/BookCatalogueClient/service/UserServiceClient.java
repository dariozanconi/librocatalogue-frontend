package org.openjfx.BookCatalogueClient.service;

import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.openjfx.BookCatalogueClient.model.ApiError;
import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.RegisterRequest;
import org.openjfx.BookCatalogueClient.model.Users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserServiceClient {
	
	String urlDomain = DomainConstant.DOMAIN_PUBLIC;
	
	public ApiResponse<String> login(Users user) throws JsonProcessingException {
		
		String url = urlDomain + "/login";
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(user);
		ContentType jsonType = ContentType.create("application/json", StandardCharsets.UTF_8);
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost request = new HttpPost(url);
			
			StringEntity entity = new StringEntity(json, jsonType);
			request.setEntity(entity);
			
			try (CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				
				return new ApiResponse<String>(statusCode, body);
			}
		}
		
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<>(new ApiError(500, e.getMessage()));
		}		
	}
	
	public ApiResponse<Users> register(RegisterRequest registerRequest) throws JsonProcessingException {
		
		String url = urlDomain + "/register";
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(registerRequest);
		ContentType jsonType = ContentType.create("application/json", StandardCharsets.UTF_8);
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			
			HttpPost request = new HttpPost(url);
			StringEntity entity = new StringEntity(json, jsonType);
			request.setEntity(entity);
			
			try (CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				
				if (statusCode == 200 || statusCode == 201) {
					return new ApiResponse<>(statusCode, mapper.readValue(body, Users.class));
				} else {
					return new ApiResponse<>(new ApiError(statusCode, body));
				}
				
			}			
		}
		catch (Exception e) {
			
			e.printStackTrace();
			return new ApiResponse<>(new ApiError(500, e.getMessage()));
		}
		
	}
	
	public ApiResponse<String> deleteAccount(String token) throws JsonProcessingException {
		
		String url = urlDomain + "/deleteaccount";
			
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpDelete request = new HttpDelete(url);
			
			request.setHeader("Authorization", "Bearer " + token);
			
			try (CloseableHttpResponse response = client.execute(request)) {
				
				int statusCode = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				
				return new ApiResponse<String>(statusCode, body);
			}
		}
		
		catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<>(new ApiError(500, e.getMessage()));
		}		
	}
	
}
