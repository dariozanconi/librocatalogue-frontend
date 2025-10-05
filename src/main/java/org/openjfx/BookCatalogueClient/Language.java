package org.openjfx.BookCatalogueClient;

import java.util.Locale;

public class Language {
	
	private static Locale locale = Locale.GERMAN;
	
	public static Locale getLocale() {
		return locale;
	}

	public static void setLocale(Locale locale) {
		Language.locale = locale;
	}
	
	
}
