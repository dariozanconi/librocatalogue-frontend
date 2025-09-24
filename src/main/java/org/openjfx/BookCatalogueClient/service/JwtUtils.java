package org.openjfx.BookCatalogueClient.service;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtUtils {
	
	public boolean isTokenExpired(String token) {
		if (token!= null) {
			DecodedJWT decodedJwt = JWT.decode(token);
			Date expiresAt = decodedJwt.getExpiresAt();
			return expiresAt.before(new Date());
		} else return true;
		
	}
	
	public String getUsernameToken(String token) {
		DecodedJWT decodedJwt = JWT.decode(token);
		return decodedJwt.getSubject();
	}
}
