package one.digitalinnovation.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockExceededException extends Exception {

    public BeerStockExceededException(Long id, int quantityToIncrement, int maxQuantity, int currentQuantity) {
        super(String.format("%s Beers informed with ID %s to increment, exceeds the max stock capacity: %s. Current Amount: %s", quantityToIncrement, id, maxQuantity, currentQuantity));
    }
}
