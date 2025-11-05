package com.example.bankcards.service;

import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private Authentication authentication;

	@Mock
	private SecurityContext securityContext;

	@InjectMocks
	private UserService userService;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setUsername("testuser");
		user.setEmail("test@test.com");
		user.setFullName("Test User");
		user.setRoles(Set.of(UserRole.USER));

		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void create_validData_shouldReturnUserDTO() {
		when(userRepository.existsByUsername("newuser")).thenReturn(false);
		when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User u = invocation.getArgument(0);
			u.setId(2L);
			return u;
		});

		UserDTO result = userService.create("newuser", "password123", "newuser@test.com", "New User", null);

		assertNotNull(result);
		assertEquals("newuser", result.getUsername());
		assertEquals("newuser@test.com", result.getEmail());
		assertEquals("New User", result.getFullName());
		assertTrue(result.getRoles().contains("USER"));

		verify(userRepository).existsByUsername("newuser");
		verify(userRepository).existsByEmail("newuser@test.com");
		verify(passwordEncoder).encode("password123");
		verify(userRepository).save(any(User.class));
	}

	@Test
	void create_duplicateUsername_shouldThrowException() {
		when(userRepository.existsByUsername("existinguser")).thenReturn(true);

		assertThrows(BadRequestException.class, () -> {
			userService.create("existinguser", "password123", "email@test.com", "User", null);
		});

		verify(userRepository).existsByUsername("existinguser");
		verify(userRepository, never()).save(any());
	}

	@Test
	void create_duplicateEmail_shouldThrowException() {
		when(userRepository.existsByUsername("newuser")).thenReturn(false);
		when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

		assertThrows(BadRequestException.class, () -> {
			userService.create("newuser", "password123", "existing@test.com", "User", null);
		});

		verify(userRepository).existsByEmail("existing@test.com");
		verify(userRepository, never()).save(any());
	}

	@Test
	void create_withRoles_shouldSetRoles() {
		when(userRepository.existsByUsername("admin")).thenReturn(false);
		when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
		when(passwordEncoder.encode("password")).thenReturn("encoded");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User u = invocation.getArgument(0);
			u.setId(1L);
			return u;
		});

		UserDTO result = userService.create("admin", "password", "admin@test.com", "Admin", Set.of("ADMIN", "USER"));

		assertNotNull(result);
		assertTrue(result.getRoles().contains("ADMIN"));
		assertTrue(result.getRoles().contains("USER"));
	}

	@Test
	void all_shouldReturnListOfUsers() {
		User user2 = new User();
		user2.setId(2L);
		user2.setUsername("user2");

		when(userRepository.findAll()).thenReturn(List.of(user, user2));

		List<UserDTO> result = userService.all();

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("testuser", result.get(0).getUsername());
		assertEquals("user2", result.get(1).getUsername());

		verify(userRepository).findAll();
	}

	@Test
	void byId_existingUser_shouldReturnUserDTO() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		UserDTO result = userService.byId(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("testuser", result.getUsername());

		verify(userRepository).findById(1L);
	}

	@Test
	void byId_nonExistingUser_shouldThrowException() {
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			userService.byId(99L);
		});

		verify(userRepository).findById(99L);
	}

	@Test
	void currentProfile_authenticatedUser_shouldReturnUserDTO() {
		when(authentication.getName()).thenReturn("testuser");
		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		UserDTO result = userService.currentProfile();

		assertNotNull(result);
		assertEquals("testuser", result.getUsername());
		assertEquals("test@test.com", result.getEmail());

		verify(userRepository).findByUsername("testuser");
	}

	@Test
	void currentProfile_userNotFound_shouldThrowException() {
		when(authentication.getName()).thenReturn("nonexistent");
		when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			userService.currentProfile();
		});

		verify(userRepository).findByUsername("nonexistent");
	}

	@Test
	void delete_existingUser_shouldDelete() {
		when(userRepository.existsById(1L)).thenReturn(true);
		doNothing().when(userRepository).deleteById(1L);

		assertDoesNotThrow(() -> userService.delete(1L));

		verify(userRepository).existsById(1L);
		verify(userRepository).deleteById(1L);
	}

	@Test
	void delete_nonExistingUser_shouldThrowException() {
		when(userRepository.existsById(99L)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> {
			userService.delete(99L);
		});

		verify(userRepository).existsById(99L);
		verify(userRepository, never()).deleteById(anyLong());
	}
}
