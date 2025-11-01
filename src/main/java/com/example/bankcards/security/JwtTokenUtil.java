package com.example.bankcards.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {
	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.accessExpiration}")
	private Long accessExpiration;

	@Value("${jwt.refreshExpiration}")
	private Long refreshExpiration;

	private static final String CLAIM_AUTHORITIES = "authorities";
	private static final String CLAIM_TOKEN_TYPE = "token_type";
	private static final String TOKEN_TYPE_ACCESS = "access";
	private static final String TOKEN_TYPE_REFRESH = "refresh";

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(Authentication authentication) {
		String username = authentication.getName();
		String authorities = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));
		return buildToken(username, authorities, accessExpiration, TOKEN_TYPE_ACCESS);
	}

	public String generateAccessToken(UserDetails userDetails) {
		String authorities = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));
		return buildToken(userDetails.getUsername(), authorities, accessExpiration, TOKEN_TYPE_ACCESS);
	}

	public String generateRefreshToken(String username) {
		return buildToken(username, null, refreshExpiration, TOKEN_TYPE_REFRESH);
	}

	private String buildToken(String username, String authorities, long ttlMillis, String tokenType) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + ttlMillis);

		JwtBuilder builder = Jwts.builder()
				.subject(username)
				.claim(CLAIM_TOKEN_TYPE, tokenType)
				.issuedAt(now)
				.expiration(expiryDate)
				.signWith(getSigningKey());
		if (authorities != null) {
			builder.claim(CLAIM_AUTHORITIES, authorities);
		}
		return builder.compact();
	}

	public String getUsernameFromToken(String token) {
		Claims claims = parseClaims(token);
		return claims.getSubject();
	}

	public boolean isAccessToken(String token) {
		Claims claims = parseClaims(token);
		return TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
	}

	public boolean isRefreshToken(String token) {
		Claims claims = parseClaims(token);
		return TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public Collection<? extends GrantedAuthority> getAuthorities(String token) {
		Claims claims = parseClaims(token);
		String authoritiesCsv = claims.get(CLAIM_AUTHORITIES, String.class);
		if (authoritiesCsv == null || authoritiesCsv.isEmpty()) {
			return java.util.List.of();
		}
		return java.util.Arrays.stream(authoritiesCsv.split(","))
				.map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
	}
}

