package brandwatch.assessment.store.service;

import brandwatch.assessment.store.dto.*;
import brandwatch.assessment.store.model.Item;
import brandwatch.assessment.store.model.ProcessedOrder;
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
        return Collections.singletonList(productRepository.findShortages()
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getNeeded)));
    }

    public ProcessedOrder ProcessOrderStock(List<Item> items, String orderReferenceId) {
        boolean updateQuantity = true;
        Map<String, Integer> itemsWanted = items
                .stream()
                .collect(Collectors.toMap(Item::getProductId, Item::getQuantity));
        Set<Product> products = productRepository.findAllByProductId(itemsWanted.keySet());

        if (products.isEmpty() || products.size() != items.size()) {
            return new ProcessedOrder(orderReferenceId,false);
        }

        Map<String, Integer> quantitiesMap = new HashMap<>();
        Map<String, Integer> neededMap = new HashMap<>();

        for (Product product : products) {
            int itemQuantity = itemsWanted.get(product.getProductId());
            int productNeeded = product.getNeeded();
            int productQuantity = product.getQuantity();
            int quantityDiff = productQuantity - itemQuantity;

            if (quantityDiff >= 0) {
                productQuantity = quantityDiff;
                if (productNeeded > 0) {
                    productNeeded += itemQuantity;
                }
            } else if (productNeeded > 0) {
                productNeeded += itemQuantity;
                updateQuantity = false;
            } else {
                productNeeded = Math.abs(quantityDiff);
                updateQuantity = false;
            }

            quantitiesMap.put(product.getProductId(), productQuantity);
            neededMap.put(product.getProductId(), productNeeded);
        }
        updateProductNeeded(products, neededMap);

        if (updateQuantity) {
            updateProductQuantities(products, quantitiesMap);
            return new ProcessedOrder(orderReferenceId, true);
        }

        return new ProcessedOrder(orderReferenceId, false);
    }

    public RetryOrdersResult ProcessRetriedOrdersStock(List<OrderData> orders) {
        Map<String, Boolean> processedOrders = new HashMap<>();

        for (OrderData orderData : orders) {
            Map<String, Integer> itemsWanted = orderData
                    .getItems()
                    .stream()
                    .collect(Collectors.toMap(Item::getProductId, Item::getQuantity));
            Set<Product> productsOfTypeInStock = productRepository.findAllByProductId(itemsWanted.keySet());
            Map<String, Integer> productsMap = new HashMap<>();

            if (productsOfTypeInStock.size() == itemsWanted.keySet().size()) {
                boolean updateQuantity = true;
                for (Product product : productsOfTypeInStock) {
                    int itemQuantity = itemsWanted.get(product.getProductId());
                    int productQuantity = product.getQuantity();
                    int quantityDiff = productQuantity - itemQuantity;

                    if (quantityDiff >= 0) {
                        productQuantity = quantityDiff;
                    } else {
                        updateQuantity = false;
                    }
                    productsMap.put(product.getProductId(), productQuantity);
                }

                if (updateQuantity) {
                    updateProductQuantities(productsOfTypeInStock, productsMap);
                    processedOrders.put(orderData.getOrderReferenceId(), true);
                } else {
                    processedOrders.put(orderData.getOrderReferenceId(), false);
                }
            } else {
                processedOrders.put(orderData.getOrderReferenceId(), false);
            }
        }
        return new RetryOrdersResult(
                processedOrders
                        .entrySet()
                        .stream()
                        .map(order -> new ProcessedOrder(order.getKey(), order.getValue()))
                        .collect(Collectors.toList()));
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

    private void updateProductQuantities(Set<Product> products, Map<String, Integer> quantities) {
        for (Product product : products) {
            int quantity = quantities.get(product.getProductId());
            product.setQuantity(quantity);
        }
        productRepository.saveAll(products);
    }

    private void updateProductNeeded(Set<Product> products, Map<String, Integer> shortages) {
        for (Product product : products) {
            int needed = shortages.get(product.getProductId());
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
