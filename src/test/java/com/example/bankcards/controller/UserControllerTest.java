package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@Test
	@WithMockUser(roles = "USER")
	void currentProfile_authenticated_shouldReturnUser() throws Exception {
		UserDTO user = new UserDTO();
		user.setId(1L);
		user.setUsername("user");
		user.setEmail("user@test.com");
		user.setFullName("Test User");
		user.setRoles(Set.of("USER"));

		when(userService.currentProfile()).thenReturn(user);

		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.username").value("user"))
				.andExpect(jsonPath("$.email").value("user@test.com"));

		verify(userService).currentProfile();
	}

	@Test
	void currentProfile_unauthorized_shouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isUnauthorized());

		verify(userService, never()).currentProfile();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void create_asAdmin_shouldReturnCreated() throws Exception {
		UserCreateRequest request = new UserCreateRequest();
		request.setUsername("newuser");
		request.setPassword("password123");
		request.setEmail("newuser@test.com");
		request.setFullName("New User");
		request.setRoles(Set.of("USER"));

		UserDTO response = new UserDTO();
		response.setId(1L);
		response.setUsername("newuser");

		when(userService.create(anyString(), anyString(), anyString(), anyString(), any()))
				.thenReturn(response);

		mockMvc.perform(post("/api/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.username").value("newuser"));

		verify(userService).create(eq("newuser"), eq("password123"), eq("newuser@test.com"), eq("New User"), any());
	}

	@Test
	@WithMockUser(roles = "USER")
	void create_asUser_shouldReturnForbidden() throws Exception {
		UserCreateRequest request = new UserCreateRequest();
		request.setUsername("newuser");
		request.setPassword("password123");
		request.setEmail("newuser@test.com");
		request.setFullName("New User");

		mockMvc.perform(post("/api/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());

		verify(userService, never()).create(any(), any(), any(), any(), any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void all_asAdmin_shouldReturnList() throws Exception {
		UserDTO user1 = new UserDTO();
		user1.setId(1L);
		user1.setUsername("user1");

		UserDTO user2 = new UserDTO();
		user2.setId(2L);
		user2.setUsername("user2");

		when(userService.all()).thenReturn(List.of(user1, user2));

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[1].id").value(2L));

		verify(userService).all();
	}

	@Test
	@WithMockUser(roles = "USER")
	void all_asUser_shouldReturnForbidden() throws Exception {
		mockMvc.perform(get("/api/users"))
				.andExpect(status().isForbidden());

		verify(userService, never()).all();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void byId_asAdmin_shouldReturnUser() throws Exception {
		UserDTO user = new UserDTO();
		user.setId(1L);
		user.setUsername("user");

		when(userService.byId(1L)).thenReturn(user);

		mockMvc.perform(get("/api/users/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.username").value("user"));

		verify(userService).byId(1L);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void delete_asAdmin_shouldReturnNoContent() throws Exception {
		doNothing().when(userService).delete(1L);

		mockMvc.perform(delete("/api/users/1")
						.with(csrf()))
				.andExpect(status().isNoContent());

		verify(userService).delete(1L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void delete_asUser_shouldReturnForbidden() throws Exception {
		mockMvc.perform(delete("/api/users/1")
						.with(csrf()))
				.andExpect(status().isForbidden());

		verify(userService, never()).delete(anyLong());
	}
}
