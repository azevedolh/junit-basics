package one.digitalinnovation.beerstock.controller;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.dto.QuantityDTO;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.service.BeerService;
import one.digitalinnovation.beerstock.utils.JsonConvertionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

    private static final String BEER_API_URL_PATH = "/api/v1/beers";
    private static final long VALID_BEER_ID = 1L;
    private static final long INVALID_BEER_ID = 2L;
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";
    @Mock
    BeerService beerService;
    @InjectMocks
    BeerController beerController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
    void whenPOSTisInvokedThenBeerIsCreated() throws Exception {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        when(beerService.createBeer(mockBeerDTO)).thenReturn(mockBeerDTO);

        // then
        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(mockBeerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(mockBeerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(mockBeerDTO.getBrand())))
                .andExpect(jsonPath("$.max", is(mockBeerDTO.getMax())))
                .andExpect(jsonPath("$.quantity", is(mockBeerDTO.getQuantity())))
                .andExpect(jsonPath("$.type", is(mockBeerDTO.getType().toString())));
    }

    @Test
    void whenPOSTIsInvokedWithoutMandatoryFieldThenBadRequestShouldBeReturned() throws Exception {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        mockBeerDTO.setName(null);

        //then
        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString((mockBeerDTO))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGETisInvokedWithAValidNameThenStatusOKShouldBeReturned() throws Exception {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        when(beerService.findByName(mockBeerDTO.getName())).thenReturn(mockBeerDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + mockBeerDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(mockBeerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(mockBeerDTO.getBrand())))
                .andExpect(jsonPath("$.max", is(mockBeerDTO.getMax())))
                .andExpect(jsonPath("$.quantity", is(mockBeerDTO.getQuantity())))
                .andExpect(jsonPath("$.type", is(mockBeerDTO.getType().toString())));
    }

    @Test
    void whenGETisInvokedWithAInvalidNameThenStatusNotFoundShouldBeReturned() throws Exception {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        when(beerService.findByName(mockBeerDTO.getName())).thenThrow(BeerNotFoundException.class);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + mockBeerDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGETListIsInvokedAndListContainsItemsThenStatusOKShouldBeReturned() throws Exception {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        when(beerService.listAll()).thenReturn(Collections.singletonList(mockBeerDTO));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(mockBeerDTO.getName())))
                .andExpect(jsonPath("$[0].brand", is(mockBeerDTO.getBrand())))
                .andExpect(jsonPath("$[0].max", is(mockBeerDTO.getMax())))
                .andExpect(jsonPath("$[0].quantity", is(mockBeerDTO.getQuantity())))
                .andExpect(jsonPath("$[0].type", is(mockBeerDTO.getType().toString())));
    }

    @Test
    void whenGETListIsInvokedAndListDoesNotContainItemsThenStatusOKShouldBeReturned() throws Exception {
        // when
        when(beerService.listAll()).thenReturn(new ArrayList<>());

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenDELETEIsInvokedWithValidIDThenNoContentStatusIsReturned() throws Exception {
        // when
        doNothing().when(beerService).deleteById(VALID_BEER_ID);

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + VALID_BEER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDELETEIsInvokedWithInvalidIDThenNotFoundStatusIsReturned() throws Exception {
        // when
        doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHToIncrementIsInvokedThenStatusOkShouldBeReturned() throws Exception {
        // given
        QuantityDTO mockQuantityDTO = QuantityDTO.builder().quantity(40).build();

        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        int expectedIncrementedQuantity = mockBeerDTO.getQuantity() + mockQuantityDTO.getQuantity();
        mockBeerDTO.setQuantity(expectedIncrementedQuantity);

        // when
        when(beerService.increment(VALID_BEER_ID, mockQuantityDTO.getQuantity())).thenReturn(mockBeerDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders
                .patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(mockQuantityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(mockBeerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(mockBeerDTO.getBrand())))
                .andExpect(jsonPath("$.max", is(mockBeerDTO.getMax())))
                .andExpect(jsonPath("$.quantity", is(expectedIncrementedQuantity)))
                .andExpect(jsonPath("$.type", is(mockBeerDTO.getType().toString())));
    }

    @Test
    void whenPATCHToIncrementIsInvokedWithInvalidIdThenStatusNotFoundShouldBeReturned() throws Exception {
        // given
        QuantityDTO mockQuantityDTO = QuantityDTO.builder().quantity(40).build();

        // when
        doThrow(BeerNotFoundException.class).when(beerService).increment(INVALID_BEER_ID, mockQuantityDTO.getQuantity());

        // then
        mockMvc.perform(MockMvcRequestBuilders
                .patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(mockQuantityDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHToIncrementIsInvokedWithQuantityGreaterThanMaxThenStatusBadRequestIsReturned() throws Exception {
        // given
        QuantityDTO mockQuantityDTO = QuantityDTO.builder().quantity(41).build();

        // when
        doThrow(BeerStockExceededException.class).when(beerService).increment(VALID_BEER_ID, mockQuantityDTO.getQuantity());

        // then
        mockMvc.perform(MockMvcRequestBuilders
                .patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(mockQuantityDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHToDecrementIsInvokedThenStatusOkShouldBeReturned() throws Exception {
        // given
        QuantityDTO mockQuantityDTO = QuantityDTO.builder().quantity(10).build();

        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        int expectedDecrementedQuantity = mockBeerDTO.getQuantity() + mockQuantityDTO.getQuantity();
        mockBeerDTO.setQuantity(expectedDecrementedQuantity);

        // when
        when(beerService.decrement(VALID_BEER_ID, mockQuantityDTO.getQuantity())).thenReturn(mockBeerDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders
                .patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(mockQuantityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(mockBeerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(mockBeerDTO.getBrand())))
                .andExpect(jsonPath("$.max", is(mockBeerDTO.getMax())))
                .andExpect(jsonPath("$.quantity", is(expectedDecrementedQuantity)))
                .andExpect(jsonPath("$.type", is(mockBeerDTO.getType().toString())));
    }

    @Test
    void whenPATCHToDecrementIsInvokedWithInvalidIdThenStatusNotFoundShouldBeReturned() throws Exception {
        // given
        QuantityDTO mockQuantityDTO = QuantityDTO.builder().quantity(10).build();

        // when
        doThrow(BeerNotFoundException.class).when(beerService).decrement(INVALID_BEER_ID, mockQuantityDTO.getQuantity());

        // then
        mockMvc.perform(MockMvcRequestBuilders
                .patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(mockQuantityDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHToDecrementIsInvokedWithQuantityLessZeroThenStatusBadRequestIsReturned() throws Exception {
        // given
        QuantityDTO mockQuantityDTO = QuantityDTO.builder().quantity(11).build();

        // when
        doThrow(BeerStockExceededException.class).when(beerService).decrement(VALID_BEER_ID, mockQuantityDTO.getQuantity());

        // then
        mockMvc.perform(MockMvcRequestBuilders
                .patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConvertionUtils.asJsonString(mockQuantityDTO)))
                .andExpect(status().isBadRequest());
    }
}
