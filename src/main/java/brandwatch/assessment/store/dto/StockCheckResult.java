package brandwatch.assessment.store.dto;

import lombok.Data;

@Data
public class StockCheckResult {
    private boolean success;
    private String message;
}
