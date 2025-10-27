package org.openjfx.BookCatalogueClient;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.Collection;
import org.openjfx.BookCatalogueClient.model.PageResponse;
import org.openjfx.BookCatalogueClient.model.Patron;
import org.openjfx.BookCatalogueClient.task.ApiTask;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class StatisticsController {
	
	@FXML 
	private ResourceBundle resources;

	@FXML
	Label booksLabel;
	
	@FXML
	Label patronsLabel;
	
	@FXML
	Label collectionsLabel;
	
	@FXML
	private LineChart<String, Number> booksChart;
	
	@FXML
	private CategoryAxis xAxis;

	@FXML
	private NumberAxis yAxis;
	
	@FXML
	ComboBox<GroupBy> comboBox;
	
	private String token;
	private int totalBooks;
	private int totalPatrons;
	private int totalCollections;
	private List<Book> books;
	private final ApiTask bookTasks = new ApiTask();
	private final ApiTask collectionTasks = new ApiTask();
	private ApiResponse<PageResponse<Book>> response;
	private ApiResponse<List<Collection>> response2;
	private ApiResponse<List<Patron>> response3;
	
	public enum GroupBy {
	    DAY, MONTH, YEAR
	}
		
	public void initializeTab() {		
		getAllBooks();	
		comboBox.getItems().addAll(GroupBy.values());
		comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> setChart(newV));
	}
	
	public void getAllBooks() {
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.loadBooksTask(1, 1, "authorSort");
		task.setOnSucceeded(e -> {					
			response = task.getValue();
			totalBooks = (int) response.getData().getTotalElements();
			getTotalBooks();
		});			
		new Thread(task).start();
	}
	
	public void getTotalBooks() {
		Task<ApiResponse<PageResponse<Book>>> task = bookTasks.loadBooksTask(0, totalBooks, "title");
		task.setOnSucceeded(e -> {				
			response = task.getValue();
			System.out.println(totalBooks);
			books = response.getData().getContent();
			getTotalCollections();
		});				
		new Thread(task).start();
	}
	
	public void getTotalCollections() {
		Task<ApiResponse<List<Collection>>> task = collectionTasks.loadCollectionsTask();
		task.setOnSucceeded(e -> {
			response2 = task.getValue();
			if (response2.isSuccess()) {										
				totalCollections=response2.getData().size();
				getTotalPatrons();
			} else {
				setData();
			}
		});
		new Thread(task).start();
	}
	
	public void getTotalPatrons() {
		
		Task<ApiResponse<List<Patron>>> task = collectionTasks.getAllPatronsTask(token);
		task.setOnSucceeded(e -> {
			response3 = task.getValue();
			if (response3.isSuccess()) {										
				totalPatrons=response3.getData().size();	
				setData();
			} else {
				setData();
			}
		});
		new Thread(task).start();
	}
	
	public void setData() {
		booksLabel.setText(resources.getString("label.totalbooks") + totalBooks);
		collectionsLabel.setText(resources.getString("label.totalcollections")+totalCollections);
		patronsLabel.setText(resources.getString("label.totalpatrons")+totalPatrons);
		Platform.runLater(() -> {setChart(GroupBy.DAY);});
	}
	
	public void setChart(GroupBy groupBy) {
		
	    Platform.runLater(() -> {

	        Map<String, Long> booksGrouped = null;

	        switch (groupBy) {
	            case DAY:
	                booksGrouped = books.stream()
	                        .collect(Collectors.groupingBy(
	                                book -> book.getCreationDate().toString(), // yyyy-MM-dd
	                                TreeMap::new,
	                                Collectors.counting()
	                        ));
	                break;

	            case MONTH:
	                booksGrouped = books.stream()
	                        .collect(Collectors.groupingBy(
	                                book -> book.getCreationDate().getYear() + "-" +
	                                        String.format("%02d", book.getCreationDate().getMonthValue()), // yyyy-MM
	                                TreeMap::new,
	                                Collectors.counting()
	                        ));
	                break;

	            case YEAR:
	                booksGrouped = books.stream()
	                        .collect(Collectors.groupingBy(
	                                book -> String.valueOf(book.getCreationDate().getYear()), // yyyy
	                                TreeMap::new,
	                                Collectors.counting()
	                        ));
	                break;

	            default:
	                throw new IllegalStateException("Unexpected value: " + groupBy);
	        }

	        XYChart.Series<String, Number> series = new XYChart.Series<>();
	        

	        for (Map.Entry<String, Long> entry : booksGrouped.entrySet()) {
	            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().intValue()));
	        }

	        booksChart.getData().clear();
	        booksChart.getData().add(series);

	        switch (groupBy) {
	            case DAY:
	                xAxis.setLabel("Tag");
	                break;
	            case MONTH:
	                xAxis.setLabel("Monat");
	                break;
	            case YEAR:
	                xAxis.setLabel("Jahr");
	                break;
	        }
	        yAxis.setLabel("Anzahl Bücher");
	    });
	}
	
	public void setToken(String token) {
		this.token = token;
	}
}
