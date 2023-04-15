package brandwatch.assessment.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteOrderResult2 {
    private List<ProcessedOrder> processedOrders;
}
