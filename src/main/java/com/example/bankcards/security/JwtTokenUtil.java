package com.example.bankcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
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

	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(Authentication authentication) {
		String username = authentication.getName();
		String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
		return buildToken(username, authorities, accessExpiration, TOKEN_TYPE_ACCESS);
	}
	public String generateAccessToken(UserDetails userDetails) {
		String authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
		return buildToken(userDetails.getUsername(), authorities, accessExpiration, TOKEN_TYPE_ACCESS);
	}
	public String generateRefreshToken(String username) {
		return buildToken(username, null, refreshExpiration, TOKEN_TYPE_REFRESH);
	}
	private String buildToken(String username, String authorities, long ttl, String type) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + ttl);
		JwtBuilder b = Jwts.builder().subject(username).claim(CLAIM_TOKEN_TYPE, type).issuedAt(now).expiration(exp).signWith(getKey());
		if (authorities != null) b.claim(CLAIM_AUTHORITIES, authorities);
		return b.compact();
	}
	public boolean validate(String token) {
		try { parse(token); return true; } catch (Exception e) { return false; }
	}
	public boolean isAccess(String token) { return TOKEN_TYPE_ACCESS.equals(parse(token).get(CLAIM_TOKEN_TYPE, String.class)); }
	public boolean isRefresh(String token) { return TOKEN_TYPE_REFRESH.equals(parse(token).get(CLAIM_TOKEN_TYPE, String.class)); }
	public String getUsername(String token) { return parse(token).getSubject(); }
	public Collection<? extends GrantedAuthority> getAuthorities(String token) {
		String csv = parse(token).get(CLAIM_AUTHORITIES, String.class);
		if (csv == null || csv.isEmpty()) return java.util.List.of();
		return java.util.Arrays.stream(csv.split(","))
				.map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
	}
	private Claims parse(String token) {
		return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
	}
}

