package com.example.hotel.BookingService;

import com.example.hotel.BookingService.Clients.AvailabilityClient;
import com.example.hotel.BookingService.rabbitmq.BookingProducer;
import com.example.hotel.BookingService.rabbitmq.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingServiceApplicationTests {

    @Autowired MockMvc mockMvc;
    @MockBean
    RabbitTemplate rabbitTemplate;
    @Autowired Queue notificationQueue;
    @Autowired ObjectMapper objectMapper;

    @MockBean
    private AvailabilityClient availabilityClient;

    @Autowired
    private BookingProducer bookingProducer;

    @BeforeEach
    void purgeQueue() {
        rabbitTemplate.execute(ch -> { ch.queuePurge(RabbitMQConfig.BOOKING_QUEUE); return null; });
    }

    @Test
    void whenRoomAvailable_thenBookingCreated_andMessageSent() throws Exception {
        // arrange: availability returns true
        given(availabilityClient.check("DOUBLE", 2)).willReturn(true);

        // act: call POST /bookings?roomType=DOUBLE&nights=2
        String bookingId =
                mockMvc.perform(post("/bookings")
                                .param("roomType", "DOUBLE")
                                .param("nights", "2")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        // assert: the bookingId was sent to RabbitMQ
        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.BOOKING_ROUTING,
                bookingId
        );
    }

    @Test
    void whenRoomNotAvailable_thenBadRequest_andNoMessage() throws Exception {
        // arrange: availability returns false
        given(availabilityClient.check("SINGLE", 5)).willReturn(false);
        // act & assert: POST returns 400
        mockMvc.perform(post("/bookings")
                        .param("roomType", "SINGLE")
                        .param("nights", "5"))
                .andExpect(status().isBadRequest());

        // assert: queue remains empty
        Object msg = rabbitTemplate.receiveAndConvert(RabbitMQConfig.BOOKING_QUEUE);
        assertThat(msg).as("no message should be sent when availability fails")
                .isNull();
    }
}
