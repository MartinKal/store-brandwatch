package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.exception.ProductNotFoundException;
import brandwatch.assessment.store.exception.ProductOutOfStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.NoTransactionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class StockControllerAdvice {
    @ExceptionHandler(NoTransactionException.class)
    public ResponseEntity<String> handleNoTransactionException(NoTransactionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Transaction could not be completed.");
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Transaction could not be completed. Product not found in store.");
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<String> handleProductOutOfStockException(ProductOutOfStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Transaction could not be completed. Product out of stock.");
    }
}
