package brandwatch.assessment.store.dto;

import brandwatch.assessment.store.model.ProcessedOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetryOrdersResult {
    private List<ProcessedOrder> processedOrders;
}
