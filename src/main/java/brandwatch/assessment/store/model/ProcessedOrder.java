package brandwatch.assessment.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedOrder {
    private String orderReferenceId;
    private boolean completed;
}
