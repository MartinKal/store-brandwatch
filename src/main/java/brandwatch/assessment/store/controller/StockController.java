package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.dto.LoadStockData;
import brandwatch.assessment.store.model.Product;
import brandwatch.assessment.store.service.RedisService;
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
    private final RedisService redisService;

    public StockController(StockService stockService, ValidationService validationService, RedisService redisService) {
        this.stockService = stockService;
        this.validationService = validationService;
        this.redisService = redisService;
    }

    @GetMapping("/shortages")
    public ResponseEntity<List<Map<String, Integer>>> getAllStockShortages() {
        List<Map<String, Integer>> shortages = stockService.getAllStockShortages();
        return ResponseEntity.ok(shortages);
    }

    @PostMapping
    public ResponseEntity<List<Product>> loadStock(@RequestBody LoadStockData loadData) {
        validationService.validateLoadStockData(loadData);
        List<Product> products = stockService.addOrUpdateStock(loadData.getItems());
        Map<String, Integer> items = products
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getQuantity));
        redisService.sendInStockMessage("stock:load", items);
        return ResponseEntity.ok(products);
    }
}
