package brandwatch.assessment.store.service;

import brandwatch.assessment.store.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StockShortageService {

    private final StockRepository stockRepository;

    public StockShortageService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public List<Map<String, Integer>> getAllStockShortages() {
        return stockRepository.findOutOfStock();
    }

    public Integer getStockByProductId(String productId) {
        Optional<Integer> shortage = stockRepository.findStockByProductId(productId);
        return shortage.orElse(null);
    }
}
