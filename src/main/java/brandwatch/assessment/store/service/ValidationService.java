package brandwatch.assessment.store.service;

import brandwatch.assessment.store.dto.Item;
import brandwatch.assessment.store.dto.LoadStockData;
import brandwatch.assessment.store.dto.ShopOrderData;
import brandwatch.assessment.store.exception.IllegalItemData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidationService {

    public void validateLoadStockData(LoadStockData data) {
        validateListOfItems(data.getItems());
    }

    public void validateShopOrderData(ShopOrderData data) {
        validateListOfItems(data.getItems());
    }

    private void validateListOfItems(List<Item> items) {
        items.forEach(lineItem -> {
            if (lineItem.getProductId().isBlank()) {
                throw new IllegalItemData("Product name cannot be blank.");
            }

            if (lineItem.getQuantity() < 1) {
                throw new IllegalItemData("Quantity should be at least 1.");
            }
        });
    }
}
