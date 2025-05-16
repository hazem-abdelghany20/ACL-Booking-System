package com.example.hotel.NotificationService;

import com.example.hotel.NotificationService.command.CommandHandler;
import com.example.hotel.NotificationService.dto.NotificationRequest;
import com.example.hotel.NotificationService.factory.NotificationFactory;
import com.example.hotel.NotificationService.factory.NotificationBuilder;
import com.example.hotel.NotificationService.models.Notification;
import com.example.hotel.NotificationService.repositories.NotificationRepository;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceApplicationTests {
	@Test
	void contextLoads() {
		// Simple test to verify that the Spring context loads
	}
	// 🔹 Test 1: Factory Pattern - Signup
	@Test
	void factory_createsSignupNotification() {
		NotificationBuilder builder = NotificationFactory.getBuilder("Signup");
		Notification notification = builder.build(101L, "Welcome", "You signed up!");

		assertNotNull(notification);
		assertEquals("Signup", notification.getType());
		assertEquals("unread", notification.getStatus());
		assertEquals(101L, notification.getUserId());
	}

	// 🔹 Test 2: Factory Pattern - Invalid Type
	@Test
	void factory_throwsOnInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> {
			NotificationFactory.getBuilder("UnknownType");
		});
	}

	// 🔹 Test 3: Command Pattern - Mark as Read
	@Test
	void command_executesReadCommandSuccessfully() throws Exception {
		NotificationRepository mockRepo = mock(NotificationRepository.class);
		Notification mockNotif = new Notification();
		mockNotif.setStatus("unread");

		when(mockRepo.findById("abc123")).thenReturn(Optional.of(mockNotif));

		CommandHandler handler = new CommandHandler(mockRepo);
		handler.execute("read", "abc123");

		assertEquals("read", mockNotif.getStatus());
		verify(mockRepo, times(1)).save(mockNotif);
	}

	// 🔹 Test 4: Command Pattern - Invalid Command
	@Test
	void command_throwsOnInvalidCommand() {
		NotificationRepository mockRepo = mock(NotificationRepository.class);
		CommandHandler handler = new CommandHandler(mockRepo);

		assertThrows(IllegalArgumentException.class, () -> {
			handler.execute("invalid", "xyz");
		});
	}

	// 🔹 Test 5: DTO Structure (Bonus)
	@Test
	void dto_notificationRequestFieldsAreSetCorrectly() {
		NotificationRequest req = new NotificationRequest(1L, "Signup", "Hi", "msg", Map.of("key", "val"));

		assertEquals(1L, req.getUserId());
		assertEquals("Signup", req.getType());
		assertEquals("Hi", req.getTitle());
		assertEquals("msg", req.getMessage());
		assertEquals("val", req.getMetadata().get("key"));
	}
}
