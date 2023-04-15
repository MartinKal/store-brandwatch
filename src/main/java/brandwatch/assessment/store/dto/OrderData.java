package brandwatch.assessment.store.dto;

import brandwatch.assessment.store.model.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderData {
    private List<Item> items;
    private String orderReferenceId;
}
