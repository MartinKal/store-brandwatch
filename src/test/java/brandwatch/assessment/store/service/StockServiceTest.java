package brandwatch.assessment.store.service;

import brandwatch.assessment.store.dto.CompleteOrderResult;
import brandwatch.assessment.store.dto.Item;
import brandwatch.assessment.store.model.Product;
import brandwatch.assessment.store.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {
    @Mock
    private ProductRepository repository;

    @InjectMocks
    private StockService stockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        stockService = new StockService(repository);
    }

    @Test
    void testGetAllStockShortages() {
        // Given
        Product product1 = new Product("product1", 10, 5);
        Product product2 = new Product("product2", 5, 3);

        when(repository.findShortages()).thenReturn(List.of(product1, product2));

        // When
        List<Map<String, Integer>> result = stockService.getAllStockShortages();

        // Then
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).get("product1"));
        assertEquals(3, result.get(0).get("product2"));
    }

    @Test
    void testProcessOrderStockSUCCESS() {
        // Given
        String orderReferenceId = "order1";
        List<Item> items = List.of(new Item("p1", 5), new Item("p2", 9));
        Set<Product> products = new HashSet<>(
                List.of(
                        new Product("p1", 10, 0),
                        new Product("p2", 15, 0)
                )
        );

        when(repository.findAllByProductId(any())).thenReturn(products);

        // When
        CompleteOrderResult result = stockService.ProcessOrderStock(items, orderReferenceId, false);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(orderReferenceId, result.getOrderReferenceId());
    }

    @Test
    void testProcessOrderStockNotEnoughStock() {
        // Given
        String orderReferenceId = "order1";
        List<Item> items = List.of(new Item("p1", 5), new Item("p2", 9));
        Set<Product> products = new HashSet<>(
                List.of(
                        new Product("p1", 10, 0),
                        new Product("p2", 4, 0)
                )
        );

        when(repository.findAllByProductId(any())).thenReturn(products);

        // When
        CompleteOrderResult result = stockService.ProcessOrderStock(items, orderReferenceId, false);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(orderReferenceId, result.getOrderReferenceId());
    }

    @Test
    void testProcessOrderStockItemNotFound() {
        // Given
        String orderReferenceId = "order1";
        List<Item> items = List.of(new Item("p1", 5), new Item("p2", 9));
        Set<Product> products = new HashSet<>(
                List.of(
                        new Product("p1", 10, 0)
                )
        );

        when(repository.findAllByProductId(any())).thenReturn(products);

        // When
        CompleteOrderResult result = stockService.ProcessOrderStock(items, orderReferenceId, false);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(orderReferenceId, result.getOrderReferenceId());
    }

    @Test
    void testStockUpdateSUCCESS() {
        // Given
        List<Item> items = List.of(new Item("p1", 5));
        Product product = new Product("p1", 10, 0);

        when(repository.findByProductId(anyString())).thenReturn(Optional.of(product));
        when(repository.save(any())).thenReturn(product);

        // When
        List<Product> updatedProducts = stockService.addOrUpdateStock(items);

        // Then
        assertEquals(1, updatedProducts.size());
        assertEquals(15, updatedProducts.get(0).getQuantity());
        assertEquals("p1", updatedProducts.get(0).getProductId());
    }

    @Test
    void testAddNewStockSUCCESS() {
        // Given
        List<Item> items = List.of(new Item("p1", 5), new Item("p2", 4));
        Product product = new Product("p1", 10, 0);
        Product newProduct = new Product("p2", 4, 0);

        when(repository.findByProductId("p1")).thenReturn(Optional.of(product));
        when(repository.save(product)).thenReturn(product);
        when(repository.save(newProduct)).thenReturn(newProduct);

        // When
        List<Product> updatedProducts = stockService.addOrUpdateStock(items);

        // Then
        assertEquals(2, updatedProducts.size());
        assertEquals(15, updatedProducts.get(0).getQuantity());
        assertEquals(4, updatedProducts.get(1).getQuantity());
        assertEquals("p1", updatedProducts.get(0).getProductId());
        assertEquals("p2", updatedProducts.get(1).getProductId());
    }
}
