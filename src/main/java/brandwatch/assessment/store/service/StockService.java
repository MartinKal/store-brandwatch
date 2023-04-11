package brandwatch.assessment.store.service;

import brandwatch.assessment.store.dto.Item;
import brandwatch.assessment.store.dto.ProductData;
import brandwatch.assessment.store.dto.StockCheckResult;
import brandwatch.assessment.store.exception.ProductNotFoundException;
import brandwatch.assessment.store.exception.ProductOutOfStockException;
import brandwatch.assessment.store.model.Product;
import brandwatch.assessment.store.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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

    @Transactional
    public StockCheckResult processOrderRequest(List<Item> items) {
        StockCheckResult result = new StockCheckResult();
        try {
            for (Item item : items) {
                Product product = productRepository.findByProductId(item.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException("Product not found"));

                if (product.getQuantity() >= item.getQuantity()) {
                    product.setQuantity(product.getQuantity() - item.getQuantity());
                    productRepository.save(product);
                } else {
                    throw new ProductOutOfStockException("Insufficient stock for product: " + product.getProductId());
                }
            }
            result.setSuccess(true);
            result.setMessage("Products are in stock.");
        } catch (ProductNotFoundException | ProductOutOfStockException ex) {
            result.setSuccess(false);
            result.setMessage(ex.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return result;
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
        Optional<Product> product = productRepository.findByProductId(newProduct.getProductId());

        if (product.isPresent()) {
            Product existingProduct = product.get();
            existingProduct.setQuantity(existingProduct.getQuantity() + newProduct.getQuantity());
            return productRepository.save(existingProduct);
        } else {
            return productRepository.save(newProduct);
        }
    }
}
