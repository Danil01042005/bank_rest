package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
	private final UserRepository userRepository;
	
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> {
						log.warn("Попытка загрузки несуществующего пользователя: {}", username);
						return new UsernameNotFoundException("User not found: " + username);
					});
			log.debug("Пользователь успешно загружен: {} (роли: {})", username, user.getRoles());
			return user;
		} catch (UsernameNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Ошибка при загрузке пользователя: {}", username, ex);
			throw new UsernameNotFoundException("Ошибка загрузки пользователя: " + username, ex);
		}
	}
}

