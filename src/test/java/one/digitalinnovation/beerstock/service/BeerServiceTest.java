package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;
    private static final int INVALID_INCREMENT_BEER_QUANTITY = 41;
    private static final int VALID_INCREMENT_BEER_QUANTITY = 40;
    private static final int INVALID_DECREMENT_BEER_QUANTITY = 11;
    private static final int VALID_DECREMENT_BEER_QUANTITY = 10;

    @Mock
    BeerRepository beerRepository;

    BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    BeerService beerService;

    @Test
    void whenValidBeerIsGivenThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer mockBeer = beerMapper.toModel(mockBeerDTO);

        // when
        when(beerRepository.findByName(mockBeerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(mockBeer)).thenReturn(mockBeer);

        // then
        BeerDTO beerCreatedDTO = beerService.createBeer(mockBeerDTO);
        assertThat(beerCreatedDTO).isEqualTo(mockBeerDTO);
    }

    @Test
    void whenAlreadyRegisteredBeerIsGivenThenItShouldThrowException() {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer mockBeer = beerMapper.toModel(mockBeerDTO);

        // when
        when(beerRepository.findByName(mockBeerDTO.getName())).thenReturn(Optional.of(mockBeer));

        // then
        assertThatExceptionOfType(BeerAlreadyRegisteredException.class)
                .isThrownBy(() -> beerService.createBeer(mockBeerDTO));
    }

    @Test
    void whenExistingBeerNameIsInformedThenItShouldBeFoundAndReturned() throws BeerNotFoundException {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer mockBeer = beerMapper.toModel(mockBeerDTO);

        // when
        when(beerRepository.findByName(mockBeerDTO.getName())).thenReturn(Optional.of(mockBeer));

        // then
        BeerDTO beerFoundDTO = beerService.findByName(mockBeerDTO.getName());
        assertThat(beerFoundDTO).isEqualTo(mockBeerDTO);
    }

    @Test
    void whenNonExistingBeerNameIsInformedThenItShouldThrowException() {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        when(beerRepository.findByName(mockBeerDTO.getName())).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(BeerNotFoundException.class)
                .isThrownBy(() -> beerService.findByName(mockBeerDTO.getName()));
    }

    @Test
    void whenListAllBeersInvokedThenListOfBeersShouldBeReturned() {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer mockBeer = beerMapper.toModel(mockBeerDTO);

        // when
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(mockBeer));

        // then
        List<BeerDTO> listOfBeersReturned = beerService.listAll();
        assertThat(listOfBeersReturned.get(0)).isEqualTo(mockBeerDTO);
    }

    @Test
    void whenExistingIdOfBeerIsInformedThenBeerShouldBeDeleted() throws BeerNotFoundException {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer mockBeer = beerMapper.toModel(mockBeerDTO);

        // when
        when(beerRepository.findById(mockBeerDTO.getId())).thenReturn(Optional.of(mockBeer));
        doNothing().when(beerRepository).deleteById(mockBeerDTO.getId());

        // then
        beerService.deleteById(mockBeerDTO.getId());
        verify(beerRepository, times(1)).findById(mockBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(mockBeerDTO.getId());
    }

    @Test
    void whenNonExistingIdOfBeerIsInformedThenExceptionShouldBeThrown() {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        when(beerRepository.findById(mockBeerDTO.getId())).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(BeerNotFoundException.class)
                .isThrownBy(() -> beerService.deleteById(mockBeerDTO.getId()));
    }

    @Test
    void whenQuantityOfBeerToIncrementIsInformedThenItShouldBeAddedToExistingQuantity() throws BeerNotFoundException, BeerStockExceededException {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer mockBeer = beerMapper.toModel(mockBeerDTO);

        // when
        when(beerRepository.findById(mockBeerDTO.getId())).thenReturn(Optional.of(mockBeer));
        when(beerRepository.save(mockBeer)).thenReturn(mockBeer);
        int expectedIncrementedQuantity = mockBeerDTO.getQuantity() + VALID_INCREMENT_BEER_QUANTITY;

        // then
        BeerDTO incrementedBeer = beerService.increment(mockBeerDTO.getId(), VALID_INCREMENT_BEER_QUANTITY);
        assertThat(incrementedBeer.getQuantity()).isEqualTo(expectedIncrementedQuantity);
        assertThat(incrementedBeer.getQuantity()).isLessThanOrEqualTo(incrementedBeer.getMax());
    }

    @Test
    void whenQuantityOfBeerToIncrementIsInformedAndIdDoesNotExistThenItShouldThrowException() {
        // given
        // when
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(BeerNotFoundException.class)
                .isThrownBy(() -> beerService.increment(INVALID_BEER_ID, VALID_INCREMENT_BEER_QUANTITY));
    }

    @Test
    void whenQuantityAfterIncrementGreaterMaxQuantityThenExceptionShouldBeThrown() {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer mockBeer = beerMapper.toModel(mockBeerDTO);

        // when
        when(beerRepository.findById(mockBeerDTO.getId())).thenReturn(Optional.of(mockBeer));

        // then
        assertThatExceptionOfType(BeerStockExceededException.class)
                .isThrownBy(() -> beerService.increment(mockBeerDTO.getId(), INVALID_INCREMENT_BEER_QUANTITY));
    }

    @Test
    void whenQuantityOfBeerToDecrementIsInformedThenItShouldBeSubtractedFromExistingQuantity() throws BeerNotFoundException, BeerStockExceededException {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer mockBeer = beerMapper.toModel(mockBeerDTO);

        // when
        when(beerRepository.findById(mockBeerDTO.getId())).thenReturn(Optional.of(mockBeer));
        when(beerRepository.save(mockBeer)).thenReturn(mockBeer);
        int expectedDecrementedQuantity = mockBeerDTO.getQuantity() - VALID_DECREMENT_BEER_QUANTITY;

        // then
        BeerDTO decrementedBeer = beerService.decrement(mockBeerDTO.getId(), VALID_DECREMENT_BEER_QUANTITY);
        assertThat(decrementedBeer.getQuantity()).isEqualTo(expectedDecrementedQuantity);
        assertThat(decrementedBeer.getQuantity()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void whenQuantityOfBeerToDecrementIsInformedAndIdDoesNotExistThenItShouldThrowException() {
        // given
        // when
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(BeerNotFoundException.class)
                .isThrownBy(() -> beerService.decrement(INVALID_BEER_ID, VALID_DECREMENT_BEER_QUANTITY));
    }

    @Test
    void whenQuantityAfterDecrementLessZeroThenExceptionShouldBeThrown() {
        // given
        BeerDTO mockBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer mockBeer = beerMapper.toModel(mockBeerDTO);

        // when
        when(beerRepository.findById(mockBeerDTO.getId())).thenReturn(Optional.of(mockBeer));

        // then
        assertThatExceptionOfType(BeerStockExceededException.class)
                .isThrownBy(() -> beerService.decrement(mockBeerDTO.getId(), INVALID_DECREMENT_BEER_QUANTITY));
    }
}
