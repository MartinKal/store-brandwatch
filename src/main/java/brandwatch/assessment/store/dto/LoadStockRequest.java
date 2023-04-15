package brandwatch.assessment.store.dto;

import brandwatch.assessment.store.model.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoadStockRequest {
    @NonNull
    private List<Item> items;
}
