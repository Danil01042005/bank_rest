package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCardController.class)
class AdminCardControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CardService cardService;

	@Test
	@WithMockUser(roles = "ADMIN")
	void all_asAdmin_shouldReturnPage() throws Exception {
		CardDTO card = new CardDTO();
		card.setId(1L);
		Page<CardDTO> page = new PageImpl<>(List.of(card));

		when(cardService.adminList(any(), any())).thenReturn(page);

		mockMvc.perform(get("/api/admin/cards"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content[0].id").value(1L));

		verify(cardService).adminList(any(), any());
	}

	@Test
	@WithMockUser(roles = "USER")
	void all_asUser_shouldReturnForbidden() throws Exception {
		mockMvc.perform(get("/api/admin/cards"))
				.andExpect(status().isForbidden());

		verify(cardService, never()).adminList(any(), any());
	}

	@Test
	void all_unauthorized_shouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/api/admin/cards"))
				.andExpect(status().isUnauthorized());

		verify(cardService, never()).adminList(any(), any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void all_withSearch_shouldReturnFilteredPage() throws Exception {
		CardDTO card = new CardDTO();
		card.setId(1L);
		Page<CardDTO> page = new PageImpl<>(List.of(card));

		when(cardService.adminList(eq("test"), any())).thenReturn(page);

		mockMvc.perform(get("/api/admin/cards")
						.param("search", "test"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());

		verify(cardService).adminList(eq("test"), any());
	}
}
