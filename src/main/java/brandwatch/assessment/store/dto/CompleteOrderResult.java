package brandwatch.assessment.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompleteOrderResult {
    private boolean success;
    private String orderReferenceId;
}
