package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.model.Item;
import brandwatch.assessment.store.dto.LoadStockRequest;
import brandwatch.assessment.store.model.Product;
import brandwatch.assessment.store.service.RedisProducerService;
import brandwatch.assessment.store.service.StockService;
import brandwatch.assessment.store.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class StockControllerTest {
    @Mock
    private StockService stockService;

    @Mock
    private ValidationService validationService;

    @Mock
    private RedisProducerService redisProducerService;

    @InjectMocks
    private StockController stockController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockController).build();
        objectMapper = new ObjectMapper();
        stockController = new StockController(stockService, validationService, redisProducerService);
    }

    @Test
    public void getAllStockShortagesTest() throws Exception {
        // Given
        Map<String, Integer> shortages = new HashMap<>() {{
            put("item1", 5);
            put("item2", 15);
        }};
        when(stockService.getAllStockShortages()).thenReturn(List.of(shortages));

        // When-Then
        mockMvc.perform(get("/stock/shortages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].item1").value(5))
                .andExpect(jsonPath("$[0].item2").value(15));
    }

    @Test
    public void loadStockTest() throws Exception {
        // Given
        List<Item> items = List.of(
                new Item("p1", 5),
                new Item("p2", 10)
        );

        LoadStockRequest loadData = new LoadStockRequest(items);

        List<Product> products = items.stream()
                .map(item -> new Product(item.getProductId(), item.getQuantity(), 0))
                .collect(Collectors.toList());

        when(stockService.addOrUpdateStock(items)).thenReturn(products);

        // When-Then
        mockMvc.perform(post("/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"))
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.items[1].productId").value("p2"))
                .andExpect(jsonPath("$.items[1].quantity").value(10));

        verify(validationService).validateLoadStockData(loadData);
        verify(stockService).addOrUpdateStock(items);
    }
}
