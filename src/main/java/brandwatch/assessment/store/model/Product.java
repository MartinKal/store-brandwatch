package brandwatch.assessment.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
@NoArgsConstructor
public class Product {
    @Id
    private ObjectId id;
    @Indexed
    private String productId;
    private Integer quantity;

    public Product(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
