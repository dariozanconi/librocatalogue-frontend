module org.openjfx.BookCatalogueClient {
    requires transitive javafx.controls;
    requires javafx.fxml;
	requires javafx.graphics;
	requires org.apache.httpcomponents.client5.httpclient5;
	requires org.apache.httpcomponents.core5.httpcore5; 
	requires org.apache.httpcomponents.core5.httpcore5.h2;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires javafx.base;
	requires javafx.swing;
	requires transitive java.desktop;
	requires com.auth0.jwt;
	requires com.fasterxml.jackson.datatype.jsr310;

	requires com.fasterxml.jackson.annotation;
	requires org.apache.poi.ooxml;

	
    opens org.openjfx.BookCatalogueClient to javafx.fxml;
    exports org.openjfx.BookCatalogueClient;
    
    exports org.openjfx.BookCatalogueClient.model to com.fasterxml.jackson.databind;
    

}
