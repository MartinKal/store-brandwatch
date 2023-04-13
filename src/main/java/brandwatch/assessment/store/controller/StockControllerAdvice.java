package brandwatch.assessment.store.controller;

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
}
