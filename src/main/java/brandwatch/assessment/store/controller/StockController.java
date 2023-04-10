package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.dto.ProductData;
import brandwatch.assessment.store.model.Product;
import brandwatch.assessment.store.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/store")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/shortages")
    public ResponseEntity<List<Map<String, Integer>>> getAllStockShortages() {
        List<Map<String, Integer>> shortages = stockService.getAllStockShortages();
        return ResponseEntity.ok(shortages);
    }

    @GetMapping("stock/{productId}")
    public ResponseEntity<Integer> getStockByProductId(@PathVariable String productId) {
        Integer shortage = stockService.getStockByProductId(productId);
        if (shortage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shortage);
    }

    @PostMapping("/stock")
    public ResponseEntity<List<Product>> loadStock(@RequestBody ProductData productData) {
        return ResponseEntity.ok(stockService.addOrUpdateStock(productData));
    }
}
