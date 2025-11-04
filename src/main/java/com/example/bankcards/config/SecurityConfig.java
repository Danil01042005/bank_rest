package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
	private final UserDetailsService userDetailsService;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean 
	public PasswordEncoder passwordEncoder() { 
		log.debug("Инициализация BCryptPasswordEncoder");
		return new BCryptPasswordEncoder(); 
	}
	
	@Bean 
	public DaoAuthenticationProvider authenticationProvider() {
		log.debug("Создание DaoAuthenticationProvider");
		DaoAuthenticationProvider p = new DaoAuthenticationProvider();
		p.setUserDetailsService(userDetailsService);
		p.setPasswordEncoder(passwordEncoder());
		return p;
	}
	
	@Bean 
	public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception { 
		log.debug("Создание AuthenticationManager");
		return c.getAuthenticationManager(); 
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info("Настройка SecurityFilterChain: JWT аутентификация, CORS включен, CSRF отключен");
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/login", "/api/auth/refresh", "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
						// Административные эндпоинты
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						// Эндпоинты пользователей
						.requestMatchers("/api/users/me").authenticated()
						.requestMatchers("/api/users/**").hasRole("ADMIN")
						// Эндпоинты карт (детальная проверка прав через @PreAuthorize в контроллерах)
						.requestMatchers("/api/cards/**").authenticated()
						// Эндпоинты переводов (детальная проверка через @PreAuthorize)
						.requestMatchers("/api/transfers/**").authenticated()
						// Все остальные - требуют аутентификации
						.anyRequest().authenticated())
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		log.info("SecurityFilterChain успешно настроен");
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		log.debug("Настройка CORS: разрешены все origins, методы, headers");
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(List.of("*"));
		cfg.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
		cfg.setAllowedHeaders(List.of("*"));
		cfg.setExposedHeaders(List.of("Authorization"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}
}
