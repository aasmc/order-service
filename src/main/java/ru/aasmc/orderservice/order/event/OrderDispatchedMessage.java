package ru.aasmc.orderservice.order.event;

public record OrderDispatchedMessage(
        Long orderId
) {
}
