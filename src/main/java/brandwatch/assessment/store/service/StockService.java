package brandwatch.assessment.store.service;

import brandwatch.assessment.store.dto.*;
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
        Map<String, Pair<Integer, Integer>> productsMap = new HashMap<>();

        for (Product product : products) {
            int itemQuantity = itemHashSet.get(product.getProductId());
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

            productsMap.put(product.getProductId(), Pair.of(productQuantity, productNeeded));
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

    public CompleteOrderResult2 ProcessRetriedOrderStock2(OrdersForProcessing orders) {
        Map<String, Boolean> processedOrders = new HashMap<>();
        for (OrderData orderData : orders.getOrders()) {
            Map<String, Integer> itemsWanted = orderData
                    .getItems()
                    .stream()
                    .collect(Collectors.toMap(Item::getProductId, Item::getQuantity));

            Set<Product> productsOfTypeInStock = productRepository.findAllByProductId(itemsWanted.keySet());
            Map<String, Pair<Integer, Integer>> productsMap = new HashMap<>();

            if (productsOfTypeInStock.isEmpty() || productsOfTypeInStock.size() != itemsWanted.keySet().size()) {
                processedOrders.put(orderData.getOrderReferenceId(), false);
            } else {
                boolean updateQuantity = true;
                for (Product product : productsOfTypeInStock) {

                    int itemQuantity = itemsWanted.get(product.getProductId());
                    int productNeeded = product.getNeeded();
                    int productQuantity = product.getQuantity();
                    int quantityDiff = productQuantity - itemQuantity;

                    if (quantityDiff >= 0) {
                        productQuantity = quantityDiff;
                    } else {
                        updateQuantity = false;
                    }

                    productsMap.put(product.getProductId(), Pair.of(productQuantity, productNeeded));
                }

                if (updateQuantity) {
                    updateProductQuantities(productsOfTypeInStock, productsMap);
                    processedOrders.put(orderData.getOrderReferenceId(), true);
                } else {
                    processedOrders.put(orderData.getOrderReferenceId(), false);
                }
            }
        }
        return new CompleteOrderResult2(
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

    private void updateProductQuantities(Set<Product> products, Map<String, Pair<Integer, Integer>> productsMap) {
        for (Product product : products) {
            int quantity = productsMap.get(product.getProductId()).getFirst();
            product.setQuantity(quantity);
        }
        productRepository.saveAll(products);
    }

    private void updateProductNeeded(Set<Product> products, Map<String, Pair<Integer, Integer>> productsMap) {
        for (Product product : products) {
            int needed = productsMap.get(product.getProductId()).getSecond();
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
