package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtTokenUtil;
import com.example.bankcards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
class TransferControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private TransferService transferService;

	@MockBean
	private UserDetailsService userDetailsService;

	@MockBean
	private JwtTokenUtil jwtTokenUtil;

	@MockBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Test
	@WithMockUser(roles = "USER")
	void transfer_asUser_shouldReturnOk() throws Exception {
		TransferRequest request = new TransferRequest();
		request.setFromCardId(1L);
		request.setToCardId(2L);
		request.setAmount(new BigDecimal("100.00"));

		doNothing().when(transferService).betweenOwn(anyLong(), anyLong(), any());

		mockMvc.perform(post("/api/transfers")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("OK"));

		verify(transferService).betweenOwn(eq(1L), eq(2L), eq(new BigDecimal("100.00")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void transfer_asAdmin_shouldReturnForbidden() throws Exception {
		TransferRequest request = new TransferRequest();
		request.setFromCardId(1L);
		request.setToCardId(2L);
		request.setAmount(new BigDecimal("100.00"));

		mockMvc.perform(post("/api/transfers")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());

		verify(transferService, never()).betweenOwn(anyLong(), anyLong(), any());
	}

	@Test
	void transfer_unauthorized_shouldReturnUnauthorized() throws Exception {
		TransferRequest request = new TransferRequest();
		request.setFromCardId(1L);
		request.setToCardId(2L);
		request.setAmount(new BigDecimal("100.00"));

		mockMvc.perform(post("/api/transfers")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());

		verify(transferService, never()).betweenOwn(anyLong(), anyLong(), any());
	}

	@Test
	@WithMockUser(roles = "USER")
	void transfer_invalidRequest_shouldReturnBadRequest() throws Exception {
		TransferRequest request = new TransferRequest();
		// Не заполняем обязательные поля

		mockMvc.perform(post("/api/transfers")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());

		verify(transferService, never()).betweenOwn(anyLong(), anyLong(), any());
	}
}
