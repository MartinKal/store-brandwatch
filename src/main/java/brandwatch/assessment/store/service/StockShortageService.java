package brandwatch.assessment.store.service;

import brandwatch.assessment.store.model.StockShortage;
import brandwatch.assessment.store.repository.StockShortageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockShortageService {

    private final StockShortageRepository stockShortageRepository;

    public StockShortageService(StockShortageRepository stockShortageRepository) {
        this.stockShortageRepository = stockShortageRepository;
    }

    public List<StockShortage> getAllStockShortages() {
        return stockShortageRepository.findAll();
    }

    public Integer getStockShortageByProductId(String productId) {
        Optional<Integer> shortage = stockShortageRepository.findQuantityByProductId(productId);
        return shortage.orElse(null);
    }
}
