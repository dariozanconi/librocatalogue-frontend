package org.openjfx.BookCatalogueClient;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.openjfx.BookCatalogueClient.model.ApiResponse;
import org.openjfx.BookCatalogueClient.model.Book;
import org.openjfx.BookCatalogueClient.model.BookTable;
import org.openjfx.BookCatalogueClient.model.Patron;
import org.openjfx.BookCatalogueClient.model.PatronTable;
import org.openjfx.BookCatalogueClient.task.ApiTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class PatronTabController {
	
	@FXML
	ResourceBundle resources;
	
	@FXML
	TableView<PatronTable> patronTable;
	
	@FXML
	TableColumn<PatronTable, String> firstNameColumn;
	
	@FXML
	TableColumn<PatronTable, String> lastNameColumn;
	
	@FXML
	TableColumn<PatronTable, String> emailColumn;
	
	@FXML
	TableColumn<PatronTable, String> creationDateColumn;
	
	@FXML
	TableColumn<PatronTable, String> detailsColumn;
	
	@FXML
	TableView<BookTable> bookTable;
	
	@FXML
	TableColumn<BookTable, String> titleColumn;
	
	@FXML
	TableColumn<BookTable, String> authorColumn;
	
	@FXML
	TableColumn<BookTable, String> returnColumn;
	
	@FXML
	Label firstNameLabel;
	
	@FXML
	Label lastNameLabel;
	
	@FXML
	Label emailLabel;
	
	@FXML
	Label dateLabel;
	
	private List<Patron> patrons;
	private Patron currentPatron;
	private List<Book> books;
	private final ApiTask patronsTasks = new ApiTask();
	private ApiResponse<List<Patron>> response;
	private final ObservableList<PatronTable> data = FXCollections.observableArrayList();
	private ObservableList<BookTable> booksData = FXCollections.observableArrayList();
	private String token;
	 
	public void setTab() {
		data.clear();
		
		patrons.stream()
	    .map(p -> new PatronTable(
	        capitalize(p.getFirstName()),
	        capitalize(p.getLastName()),
	        p.getEmail(),
	        p.getCreationDate().toString(),
	        p
	    )).forEach(data::add);
		
		setPatronTable();
	}
	
	public void initialize(String token) {
		this.token=token;
		
		Task<ApiResponse<List<Patron>>> task = patronsTasks.getAllPatronsTask(token);
		task.setOnSucceeded(e -> {
			response = task.getValue();
			if (response.isSuccess()) {										
				this.patrons = response.getData();	
				setTab();
			} else {
				
			}
		});
		new Thread(task).start();
	}
	
	public void setPatronTable() {
		firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
		lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
		emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
		creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
		detailsColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
	 	
		Callback<TableColumn<PatronTable, String>, TableCell<PatronTable, String>> cellFactory =
				new Callback<TableColumn<PatronTable, String>, TableCell<PatronTable, String>>() {
			
			@Override
			public TableCell call(final TableColumn<PatronTable, String> param) {
				final TableCell<PatronTable, String> cell = new TableCell<PatronTable, String>() {

					final Button btn = new Button("Show Details");
					
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setText(null);
						} else {
							btn.setOnAction(event -> {
								PatronTable patronTable = getTableView().getItems().get(getIndex());
							    currentPatron = patronTable.getPatron(); 
								setPatronDetails(currentPatron);
							});
							setGraphic(btn);
							setText(null);
						}
					}
				};
				return cell;
			}
		};	

		detailsColumn.setCellFactory(cellFactory);
		patronTable.setItems(data);
		bookTable.refresh();
	}
	
	public void setPatronDetails(Patron patron) {
		booksData.clear();
		
		books = patron.getBooks();
		books.stream()
	    .map(b -> new BookTable(
	        b.getTitle(),
	        b.getAuthor(),	
	        b
	    )).forEach(booksData::add);
		
		firstNameLabel.setText(capitalize(patron.getFirstName()));
		lastNameLabel.setText(capitalize(patron.getLastName()));
		emailLabel.setText(patron.getEmail());
		dateLabel.setText(patron.getCreationDate().toString());
		
		setBookTable(booksData);
	}
	
	public void setBookTable(ObservableList<BookTable> booksData) {
		
		
		titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
		authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
		returnColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
		
		Callback<TableColumn<BookTable, String>, TableCell<BookTable, String>> cellFactory =
				new Callback<TableColumn<BookTable, String>, TableCell<BookTable, String>>() {
			
			@Override
			public TableCell call(final TableColumn<BookTable, String> param) {
				final TableCell<BookTable, String> cell = new TableCell<BookTable, String>() {

					final Button btn = new Button("Return");
					
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setText(null);
						} else {
							btn.setOnAction(event -> {
								Book currentBook = getTableView().getItems().get(getIndex()).getBook();
								returnBook(currentBook);
							});
							setGraphic(btn);
							setText(null);
						}
					}
				};
				return cell;
			}
		};	
		returnColumn.setCellFactory(cellFactory);
		bookTable.setItems(booksData);
		bookTable.refresh();
	}
	
	public void returnBook(Book book) {
		Task<ApiResponse<String>> task = patronsTasks.returnBookTask(book.getId(), token);
		task.setOnSucceeded(e -> {
			ApiResponse<String> returnResponse = task.getValue();
			if (returnResponse.isSuccess()) {
				createAlert(AlertType.INFORMATION, "alert.return", "alert.returnsuccess").showAndWait();
				data.clear();
	            booksData.clear();
	            patronTable.getItems().clear();
	            bookTable.getItems().clear();

	            initialize(token);
			} else {
				createAlert(AlertType.ERROR, "alert.return", "alert.returnfail").showAndWait();
			}
		});
		new Thread(task).start();
	}
	
	public String capitalize(String input) {
		return Character.toUpperCase(input.charAt(0)) + input.substring(1);
	}
	
	private Alert createAlert(AlertType type, String titleKey, String headerKey) {
	    Alert alert = new Alert(type);
	    alert.getDialogPane().getStylesheets().add(
	        getClass().getResource("AlertStyle.css").toExternalForm()
	    );
	    alert.setTitle(resources.getString(titleKey));
	    alert.setHeaderText(resources.getString(headerKey));
	    return alert;
	}
	
}
