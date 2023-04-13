package brandwatch.assessment.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopOrderData {
    private String orderReferenceId;
    @NonNull
    private List<Item> items;
    boolean retriedOrder;
}
