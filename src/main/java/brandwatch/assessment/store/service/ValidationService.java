package brandwatch.assessment.store.service;

import brandwatch.assessment.store.dto.OrderData;
import brandwatch.assessment.store.model.Item;
import brandwatch.assessment.store.dto.LoadStockRequest;
import brandwatch.assessment.store.exception.IllegalItemData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidationService {

    public void validateLoadStockData(LoadStockRequest data) {
        validateListOfItems(data.getItems());
    }

    public void validateShopOrderData(OrderData data) {
        validateListOfItems(data.getItems());
    }

    private void validateListOfItems(List<Item> items) {
        items.forEach(lineItem -> {
            if (lineItem.getProductId().isBlank()) {
                throw new IllegalItemData("Product name cannot be blank.");
            }

            if (lineItem.getQuantity() < 0) {
                throw new IllegalItemData("Quantity cannot be negative.");
            }
        });
    }
}
