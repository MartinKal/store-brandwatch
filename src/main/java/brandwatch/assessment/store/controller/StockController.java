package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.dto.LoadStockData;
import brandwatch.assessment.store.model.Product;
import brandwatch.assessment.store.service.StockService;
import brandwatch.assessment.store.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;
    private final ValidationService validationService;

    public StockController(StockService stockService, ValidationService validationService) {
        this.stockService = stockService;
        this.validationService = validationService;
    }

    @GetMapping("/shortages")
    public ResponseEntity<List<Map<String, Integer>>> getAllStockShortages() {
        List<Map<String, Integer>> shortages = stockService.getAllStockShortages();
        return ResponseEntity.ok(shortages);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Integer> getStockByProductId(@PathVariable String productId) {
        Integer shortage = stockService.getStockByProductId(productId);
        if (shortage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shortage);
    }

    @PostMapping
    public ResponseEntity<List<Product>> loadStock(@RequestBody LoadStockData loadData) {
        validationService.validateLoadStockData(loadData);
        return ResponseEntity.ok(stockService.addOrUpdateStock(loadData.getItems()));
    }
}
