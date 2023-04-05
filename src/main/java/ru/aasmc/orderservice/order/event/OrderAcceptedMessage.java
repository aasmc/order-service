package ru.aasmc.orderservice.order.event;

public record OrderAcceptedMessage(
        Long orderId
) {
}
