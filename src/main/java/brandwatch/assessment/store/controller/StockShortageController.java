package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.model.StockShortage;
import brandwatch.assessment.store.service.StockShortageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock-shortages")
public class StockShortageController {

    private final StockShortageService stockShortageService;

    public StockShortageController(StockShortageService stockShortageService) {
        this.stockShortageService = stockShortageService;
    }

    @GetMapping
    public ResponseEntity<List<StockShortage>> getAllStockShortages() {
        List<StockShortage> shortages = stockShortageService.getAllStockShortages();
        return ResponseEntity.ok(shortages);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Integer> getStockShortageByProductId(@PathVariable String productId) {
        Integer shortage = stockShortageService.getStockShortageByProductId(productId);
        if (shortage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shortage);
    }
}
