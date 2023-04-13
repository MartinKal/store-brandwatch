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

    public CompleteOrderResult ProcessOrderStock(List<Item> items, String orderReferenceId, boolean isRetried) {
        boolean updateQuantity = true;
        Map<String, Integer> itemHashSet = items
                .stream()
                .collect(Collectors.toMap(Item::getProductId, Item::getQuantity));

        Set<Product> products = productRepository
                .findAllByProductId(items.stream().map(Item::getProductId).collect(Collectors.toSet()));
        if (products.isEmpty() || products.size() != items.size()) {
            return new CompleteOrderResult(false, orderReferenceId);
        }

        // key - product 2. value (new quantity, needed)
        Map<Product, Pair<Integer, Integer>> productsMap = new HashMap<>();

        for (Product product : products) {
            int itemQuantity = itemHashSet.get(product.getProductId());
            int productNeeded = product.getNeeded();
            int productQuantity = product.getQuantity();
            int quantityDiff = productQuantity - itemQuantity;

            if (quantityDiff >= 0) {
                productQuantity = quantityDiff;
            } else if (productNeeded > 0) {
                productNeeded += itemQuantity;
                updateQuantity = false;
            } else {
                productNeeded = Math.abs(quantityDiff);
                updateQuantity = false;
            }

            productsMap.put(product, Pair.of(productQuantity, productNeeded));
        }
        if (!isRetried) {
            updateProductNeeded(products, productsMap);
        }

        if (updateQuantity) {
            updateProductQuantities(products, productsMap);
            return new CompleteOrderResult(true, orderReferenceId);
        }

        return new CompleteOrderResult(false, orderReferenceId);
    }

    public List<Product> addOrUpdateStock(List<Item> items) {
        List<Product> products = new ArrayList<>();
        for (Item item : items) {
            Product p = new Product(item.getProductId(), item.getQuantity(), 0);
            Product saved = addOrReplenishProduct(p);
            products.add(saved);
        }
        return products;
    }

    private void updateProductQuantities(Set<Product> products, Map<Product, Pair<Integer, Integer>> productsMap) {
        for (Product product:  products) {
            int quantity = productsMap.get(product).getFirst();
            product.setQuantity(quantity);
        }
        productRepository.saveAll(products);
    }

    private void updateProductNeeded(Set<Product> products, Map<Product, Pair<Integer, Integer>> productsMap) {
        for (Product product : products) {
            int needed = productsMap.get(product).getSecond();
            product.setNeeded(needed);
        }
        productRepository.saveAll(products);
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
