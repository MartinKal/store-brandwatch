package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.model.Item;
import brandwatch.assessment.store.dto.LoadStockRequest;
import brandwatch.assessment.store.dto.LoadStockResult;
import brandwatch.assessment.store.model.Product;
import brandwatch.assessment.store.service.RedisProducerService;
import brandwatch.assessment.store.service.StockService;
import brandwatch.assessment.store.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;
    private final ValidationService validationService;
    private final RedisProducerService redisProducerService;

    public StockController(StockService stockService, ValidationService validationService, RedisProducerService redisProducerService) {
        this.stockService = stockService;
        this.validationService = validationService;
        this.redisProducerService = redisProducerService;
    }

    @GetMapping("/shortages")
    public ResponseEntity<List<Map<String, Integer>>> getAllStockShortages() {
        List<Map<String, Integer>> shortages = stockService.getAllStockShortages();
        return ResponseEntity.ok(shortages);
    }

    @PostMapping
    public ResponseEntity<LoadStockResult> loadStock(@RequestBody LoadStockRequest loadData) {
        validationService.validateLoadStockData(loadData);
        List<Product> products = stockService.addOrUpdateStock(loadData.getItems());
        Map<String, Integer> items = products
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getQuantity));
        redisProducerService.sendInStockMessage("stock:load", items);

        return ResponseEntity.ok(
                new LoadStockResult(
                        items
                                .entrySet()
                                .stream()
                                .map(entry -> new Item(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList())
                )
        );
    }
}
