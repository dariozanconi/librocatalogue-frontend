package org.openjfx.BookCatalogueClient.service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openjfx.BookCatalogueClient.model.Book;

public class ExcelCreator {
	
	ResourceBundle resources;
	List<Book> books;
	Boolean[] settings;
	List<FunctionTwo<List<Book>, Integer, String>> functionList = List.of(
				(b,i) -> b.get(i).getAuthorSort(),
				(b,i) -> b.get(i).getTitle(),
				(b,i) -> b.get(i).getIsbn(),
				(b,i) -> b.get(i).getPublisher(),
				(b,i) -> b.get(i).getPublishPlace(),
				(b,i) -> b.get(i).getPublishDate()!=null ? b.get(i).getPublishDate().toString() : "",
				(b,i) -> b.get(i).isAvailable() ? resources.getString("excel.available") : resources.getString("excel.notavailable")
			);
	
	public ExcelCreator (List<Book> books, Boolean[] settings, ResourceBundle resources) {
		this.books = books;
		this.settings = settings;
		this.resources = resources;
	}
	
	public XSSFWorkbook create() {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFCreationHelper createHelper = workbook.getCreationHelper();
		XSSFSheet sheet = workbook.createSheet(resources.getString("excel.header"));
		
		XSSFCellStyle headerStyle = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		headerStyle.setFont(font);
		
		XSSFCellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy"));
		
		String[] labels = {resources.getString("excel.author"), 
				resources.getString("excel.title"),
				"ISBN",
				resources.getString("excel.publisher"),
				resources.getString("excel.place"),
				resources.getString("excel.date"),
				resources.getString("excel.availability")};
		
		XSSFRow headerRow = sheet.createRow(0);
		XSSFCell cell = headerRow.createCell(0);
		LocalDate time = LocalDate.now();
		cell.setCellValue(resources.getString("excel.header") +" created at "+ time);
		cell.setCellStyle(headerStyle);
		
		int index = 0;
		XSSFRow row = sheet.createRow(index+1);
		for (int j = 0; j < 7; j++) { 
			if (settings[j]) {                
				cell = row.createCell(index++);	
				cell.setCellValue(labels[j]);
			    cell.setCellStyle(headerStyle);
			}   
		}
		
		for (int i = 0; i< books.size(); i++) {
			
			row = sheet.createRow(i+2);
			index = 0;
			
			for (int c = 0; c < 7; c++) {
			    if (settings[c]) {               
			        cell = row.createCell(index++);			       
			        cell.setCellValue(functionList.get(c).apply(books, i));		        
			    }			    			
			}
		}
		
		for (int i = 0; i < 8; i++) {
		    sheet.autoSizeColumn(i);
		}
			
		return workbook;
	}
	
	@FunctionalInterface
	interface FunctionTwo<T, U, R> {
	    R apply(T t, U u);
	}
	
	
}
