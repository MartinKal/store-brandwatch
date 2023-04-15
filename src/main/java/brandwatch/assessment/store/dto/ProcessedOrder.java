package brandwatch.assessment.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrder {
    private String orderReferenceId;
    private boolean completed;
}
