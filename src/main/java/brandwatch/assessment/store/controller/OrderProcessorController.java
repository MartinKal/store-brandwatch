package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.dto.*;
import brandwatch.assessment.store.model.ProcessedOrder;
import brandwatch.assessment.store.service.StockService;
import brandwatch.assessment.store.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderProcessorController {

    private final StockService stockService;

    public OrderProcessorController(StockService stockService, ValidationService validationService) {
        this.stockService = stockService;
    }

    @PostMapping("/process")
    public ResponseEntity<ProcessedOrder> processShopOrderStock(@RequestBody OrderData orderData) {
        ProcessedOrder inStock = stockService
                .ProcessOrderStock(orderData.getItems(), orderData.getOrderReferenceId());
        return ResponseEntity.ok(inStock);
    }

    @PostMapping("/retry")
    public ResponseEntity<RetryOrdersResult> processShopOrderStock2(@RequestBody RetryOrdersRequest orders) {
        RetryOrdersResult processedOrders = stockService
                .ProcessRetriedOrdersStock(orders.getOrders());
        return ResponseEntity.ok(processedOrders);
    }
}
