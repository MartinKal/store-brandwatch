package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.dto.CompleteOrderResult2;
import brandwatch.assessment.store.dto.OrdersForProcessing;
import brandwatch.assessment.store.dto.ShopOrderData;
import brandwatch.assessment.store.dto.CompleteOrderResult;
import brandwatch.assessment.store.service.StockService;
import brandwatch.assessment.store.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class OrderProcessorController {

    private final StockService stockService;
    private final ValidationService validationService;

    public OrderProcessorController(StockService stockService, ValidationService validationService) {
        this.stockService = stockService;
        this.validationService = validationService;
    }

    @PostMapping("/process")
    public ResponseEntity<CompleteOrderResult> processShopOrderStock(@RequestBody ShopOrderData orderData) {
        validationService.validateShopOrderData(orderData);
        CompleteOrderResult inStock = stockService
                .ProcessOrderStock(orderData.getItems(), orderData.getOrderReferenceId(), orderData.isRetriedOrder());
        return ResponseEntity.ok(inStock);
    }

    @PostMapping("/process2")
    public ResponseEntity<CompleteOrderResult2> processShopOrderStock2(@RequestBody OrdersForProcessing orders) {
        //validationService.validateShopOrderData(orders);
        CompleteOrderResult2 processedOrders = stockService
                .ProcessRetriedOrderStock2(orders);
        return ResponseEntity.ok(processedOrders);
    }
}
