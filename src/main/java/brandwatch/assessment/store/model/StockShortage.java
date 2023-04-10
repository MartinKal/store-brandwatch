package brandwatch.assessment.store.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stockShortages")
@Data
public class StockShortage {
    @Id
    private ObjectId id;
    private String productId;
    private String productName;
    private Integer quantity;
}
