package brandwatch.assessment.store.service;

import brandwatch.assessment.store.dto.Item;
import brandwatch.assessment.store.dto.CompleteOrderResult;
import brandwatch.assessment.store.model.Product;
import brandwatch.assessment.store.repository.ProductRepository;
import org.springframework.data.util.Pair;
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
        return Collections.singletonList(productRepository.findShortages()
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getNeeded)));
    }

    public Integer getStockByProductId(String productId) {
        Optional<Integer> shortage = productRepository.findStockByProductId(productId)
                .map(Product::getQuantity);
        return shortage.orElse(null);
    }

    public CompleteOrderResult ProcessOrderStock(List<Item> items, String orderReferenceId) {
        boolean updateQuantity = true;
        Map<String, Integer> itemHashSet = items
                .stream()
                .collect(Collectors.toMap(Item::getProductId, Item::getQuantity));

        Set<Product> products = productRepository
                .findAllByProductId(items.stream().map(Item::getProductId).collect(Collectors.toSet()));
        if (products.isEmpty()) {
            return new CompleteOrderResult(false, orderReferenceId);
        }

        // key - product 2. value (new quantity, needed)
        Map<Product, Pair<Integer, Integer>> productsMap = new HashMap<>();

        for (Product product : products) {
            int itemQuantity = itemHashSet.get(product.getProductId());
            int productNeeded = product.getNeeded();
            int productQuantity = product.getQuantity();
            int quantityDiff = productQuantity - itemQuantity;

            if (productNeeded > 0) {
                productNeeded += itemQuantity;
                updateQuantity = false;
            } else if (quantityDiff >= 0) {
                productQuantity = quantityDiff;
            } else {
                productNeeded = Math.abs(quantityDiff);
                updateQuantity = false;
            }
            productsMap.put(product, Pair.of(productQuantity, productNeeded));
        }

        updateProductQuantities(productsMap);
        if (updateQuantity) {
            return new CompleteOrderResult(true, orderReferenceId);
        }

        return new CompleteOrderResult(false, orderReferenceId);
    }

    private void updateProductQuantities(Map<Product, Pair<Integer, Integer>> productsMap) {
        List<Product> products = new ArrayList<>();
        for (Map.Entry<Product, Pair<Integer, Integer>> entry : productsMap.entrySet()) {
            Product p = entry.getKey();
            int quantity = entry.getValue().getFirst();
            int needed = entry.getValue().getSecond();
            p.setQuantity(quantity);
            p.setNeeded(needed);
            products.add(p);
        }
        productRepository.saveAll(products);
    }

    public List<Product> addOrUpdateStock(List<Item> items) {
        List<Product> products = new ArrayList<>();
        items.forEach(item -> {
            Product p = new Product(item.getProductId(), item.getQuantity(), 0);
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
            existingProduct.setNeeded(Math.max(0, existingProduct.getNeeded() - newProduct.getQuantity()));
            return productRepository.save(existingProduct);
        } else {
            return productRepository.save(newProduct);
        }
    }
}
