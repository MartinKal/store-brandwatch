package brandwatch.assessment.store.service;

import brandwatch.assessment.store.dto.ProductData;
import brandwatch.assessment.store.model.Product;
import brandwatch.assessment.store.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Map<String, Integer>> getAllStockShortages() {
        return Collections.singletonList(productRepository.findOutOfStock()
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getQuantity)));
    }

    public Integer getStockByProductId(String productId) {
        Optional<Integer> shortage = productRepository.findStockByProductId(productId)
                .map(Product::getQuantity);
        return shortage.orElse(null);
    }


    public List<Product> addOrUpdateStock(ProductData productData) {
        List<Product> products = new ArrayList<>();
        productData.getProducts().forEach(product -> {
            Product p = new Product();
            p.setQuantity(product.getQuantity());
            p.setProductId(product.getProductId());
            Product saved = addOrReplenishProduct(p);
            products.add(saved);
        });
        return products;
    }


    private Product addOrReplenishProduct(Product newProduct) {
        Optional<Product> existingProductOptional = productRepository.findByProductId(newProduct.getProductId());

        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setQuantity(existingProduct.getQuantity() + newProduct.getQuantity());
            return productRepository.save(existingProduct);
        } else {
            return productRepository.save(newProduct);
        }
    }
}
