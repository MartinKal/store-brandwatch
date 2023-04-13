package brandwatch.assessment.store.controller;

import brandwatch.assessment.store.dto.CompleteOrderResult;
import brandwatch.assessment.store.dto.Item;
import brandwatch.assessment.store.dto.ShopOrderData;
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

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class OrderProcessorControllerTest {
    @Mock
    private StockService stockService;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private OrderProcessorController orderProcessorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderProcessorController).build();
        objectMapper = new ObjectMapper();
        orderProcessorController = new OrderProcessorController(stockService, validationService);
    }

    @Test
    void processShopOrderStockTest() throws Exception {
        // Given
        ShopOrderData orderData = new ShopOrderData(
                "order1",
                List.of(new Item("p1", 5)),
                false
        );
        CompleteOrderResult expectedResult = new CompleteOrderResult(true, "order1");

        doNothing().when(validationService).validateShopOrderData(orderData);
        when(stockService.ProcessOrderStock(orderData.getItems(), orderData.getOrderReferenceId(), false)).thenReturn(expectedResult);

        // When-Then
        mockMvc.perform(post("/products/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(expectedResult.isSuccess()))
                .andExpect(jsonPath("$.orderReferenceId").value(expectedResult.getOrderReferenceId()));
    }
}
