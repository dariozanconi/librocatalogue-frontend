package org.openjfx.BookCatalogueClient.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.Book.Tag;
import org.openjfx.BookCatalogueClient.model.BookDto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.embed.swing.SwingFXUtils;

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

public class OpenBookService {

	private Book book;
	private File cover;
	private BookDto bookDto;
	
	public BookDto parse(String isbn) throws ParseException, IOException {

		String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&jscmd=data&format=json";
		book = new Book();
		bookDto = new BookDto();
		cover = null;
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
		    HttpGet request = new HttpGet(url);
		    try (CloseableHttpResponse response = client.execute(request)) {
		        String body = EntityUtils.toString(response.getEntity());

		        ObjectMapper mapper = new ObjectMapper();

		        JsonNode root = mapper.readTree(body);		 
		        JsonNode bookData = root.path("ISBN:" + isbn);
		        
		        if (bookData.isMissingNode() || bookData.isEmpty()) {
		            return null;
		        }
		        book.setIsbn(isbn);	        
		        book.setTitle(bookData.has("title") ? bookData.path("title").asText() : null);
		        book.setPages(bookData.has("number_of_pages") ? bookData.get("number_of_pages").asInt() : 0);
		        book.setAuthor(bookData.has("authors")? bookData.path("authors").get(0).path("name").asText() : null); 
		        book.setPublisher(bookData.has("publishers") ? bookData.path("publishers").get(0).path("name").asText() : null);
		        book.setPublishPlace(bookData.has("publish_places") ? bookData.path("publish_places").get(0).path("name").asText() : null);
		        book.setAvailable(true);
		        
		        List<Tag> tags = new ArrayList<>();
		        if (bookData.has("subjects")) {
		        	for (JsonNode publisher : bookData.path("subjects")) {
		        		if (publisher.has("name")) {
		        			tags.add(new Tag(publisher.path("name").asText()));
		        		}
		        	}
		        	book.setTags(tags);
		        }
		        
		        if (bookData.has("publish_date")) {
		        	String date = bookData.path("publish_date").asText();		        
		        	book.setPublishDate(parseDate(date)); 
		        }
		        
		        if (bookData.has("cover")) {
		        	String coverUrl = bookData.path("cover").path("large").asText();
		        	cover = parseFile(coverUrl);
		        }
		        		        		    
		    }
		}
		
		bookDto.setBook(book);
	    bookDto.setCover(cover);
	    return bookDto;
		
	}

	private File parseFile(String coverUrl) throws MalformedURLException {	
		
		URL url = new URL(coverUrl);
		BufferedImage image;
				
		try {
			image = ImageIO.read(url);	
			if (image==null) return null;
			File tempFile = File.createTempFile("book-cover-", "."+getFormat(coverUrl));
		    ImageIO.write(image, getFormat(coverUrl), tempFile);
		    return tempFile;
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}		
	}

	private LocalDate parseDate(String date) {
		
		if (date == null || date.isBlank()) return null;
		
		DateTimeFormatter[] formatters = new DateTimeFormatter[] {
		        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),   // 12 December 1999
		        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),  // October 1, 1988
		        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),    // 12 Dec 1999
		        DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH),      // Dec 1999
		        DateTimeFormatter.ofPattern("yyyy MMM", Locale.ENGLISH),      // 1999 Dec
		        DateTimeFormatter.ofPattern("MMM. yyyy", Locale.ENGLISH),     // Feb. 2009
		        DateTimeFormatter.ofPattern("yyyy-MM-dd"),                    // 1987-12-03
		        DateTimeFormatter.ofPattern("dd-MM-yyyy"),                    // 03-12-1987
		    };
		
		for (DateTimeFormatter formatter : formatters) {
	        try {
	            return LocalDate.parse(date, formatter);
	        } catch (Exception ignored) {}
	    }
		
		if (date.matches("\\d{4}")) {
	        return LocalDate.of(Integer.parseInt(date), 1, 1);
	    }
		
		return null;
	}
	
	public String getFormat(String url) {
		if (url.toLowerCase().endsWith(".png")) return "png";
	    if (url.toLowerCase().endsWith(".jpeg") || url.toLowerCase().endsWith(".jpg")) return "jpg";
	    return "jpg";
		 
	}
}
