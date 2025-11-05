package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardUpdateRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CardService cardService;

	@Test
	@WithMockUser(roles = "USER")
	void create_asUser_shouldReturnCreated() throws Exception {
		CardCreateRequest request = new CardCreateRequest();
		request.setCardNumber("1234567890123456");
		request.setCardHolderName("John Doe");
		request.setExpirationDate(LocalDate.now().plusYears(1));

		CardDTO response = new CardDTO();
		response.setId(1L);
		response.setCardHolderName("John Doe");

		when(cardService.create(anyString(), anyString(), any(LocalDate.class), any(), eq(false)))
				.thenReturn(response);

		mockMvc.perform(post("/api/cards")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.cardHolderName").value("John Doe"));

		verify(cardService).create(anyString(), anyString(), any(LocalDate.class), isNull(), eq(false));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void create_asAdmin_shouldReturnCreated() throws Exception {
		CardCreateRequest request = new CardCreateRequest();
		request.setCardNumber("1234567890123456");
		request.setCardHolderName("John Doe");
		request.setExpirationDate(LocalDate.now().plusYears(1));
		request.setOwnerId(2L);

		CardDTO response = new CardDTO();
		response.setId(1L);

		when(cardService.create(anyString(), anyString(), any(LocalDate.class), eq(2L), eq(true)))
				.thenReturn(response);

		mockMvc.perform(post("/api/cards")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

		verify(cardService).create(anyString(), anyString(), any(LocalDate.class), eq(2L), eq(true));
	}

	@Test
	void create_unauthorized_shouldReturnUnauthorized() throws Exception {
		CardCreateRequest request = new CardCreateRequest();
		request.setCardNumber("1234567890123456");
		request.setCardHolderName("John Doe");
		request.setExpirationDate(LocalDate.now().plusYears(1));

		mockMvc.perform(post("/api/cards")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());

		verify(cardService, never()).create(any(), any(), any(), any(), anyBoolean());
	}

	@Test
	@WithMockUser(roles = "USER")
	void mine_shouldReturnPage() throws Exception {
		CardDTO card = new CardDTO();
		card.setId(1L);
		Page<CardDTO> page = new PageImpl<>(List.of(card));

		when(cardService.mine(any(), any())).thenReturn(page);

		mockMvc.perform(get("/api/cards"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content[0].id").value(1L));

		verify(cardService).mine(any(), any());
	}

	@Test
	@WithMockUser(roles = "USER")
	void byId_shouldReturnCard() throws Exception {
		CardDTO card = new CardDTO();
		card.setId(1L);
		card.setCardHolderName("John Doe");

		when(cardService.byId(1L)).thenReturn(card);

		mockMvc.perform(get("/api/cards/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.cardHolderName").value("John Doe"));

		verify(cardService).byId(1L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void block_shouldReturnCard() throws Exception {
		CardDTO card = new CardDTO();
		card.setId(1L);

		when(cardService.block(1L)).thenReturn(card);

		mockMvc.perform(put("/api/cards/1/block")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L));

		verify(cardService).block(1L);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void activate_asAdmin_shouldReturnCard() throws Exception {
		CardDTO card = new CardDTO();
		card.setId(1L);

		when(cardService.activate(1L)).thenReturn(card);

		mockMvc.perform(put("/api/cards/1/activate")
						.with(csrf()))
				.andExpect(status().isOk());

		verify(cardService).activate(1L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void activate_asUser_shouldReturnForbidden() throws Exception {
		mockMvc.perform(put("/api/cards/1/activate")
						.with(csrf()))
				.andExpect(status().isForbidden());

		verify(cardService, never()).activate(anyLong());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void update_asAdmin_shouldReturnCard() throws Exception {
		CardUpdateRequest request = new CardUpdateRequest();
		request.setCardHolderName("Jane Doe");
		request.setBalance(new BigDecimal("1000.00"));

		CardDTO response = new CardDTO();
		response.setId(1L);

		when(cardService.updateAdmin(eq(1L), any(), any(), any(), any(), any())).thenReturn(response);

		mockMvc.perform(put("/api/cards/1")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		verify(cardService).updateAdmin(eq(1L), any(), any(), any(), any(), any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void delete_asAdmin_shouldReturnNoContent() throws Exception {
		doNothing().when(cardService).delete(1L);

		mockMvc.perform(delete("/api/cards/1")
						.with(csrf()))
				.andExpect(status().isNoContent());

		verify(cardService).delete(1L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void delete_asUser_shouldReturnForbidden() throws Exception {
		mockMvc.perform(delete("/api/cards/1")
						.with(csrf()))
				.andExpect(status().isForbidden());

		verify(cardService, never()).delete(anyLong());
	}
}
