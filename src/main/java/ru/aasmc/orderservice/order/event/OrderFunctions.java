package ru.aasmc.orderservice.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import ru.aasmc.orderservice.order.domain.OrderService;

import java.util.function.Consumer;

@Configuration
public class OrderFunctions {
    private static final Logger log = LoggerFactory.getLogger(OrderFunctions.class);

    @Bean
    public Consumer<Flux<OrderDispatchedMessage>> dispatchOrder(
            OrderService orderService
    ) {
        // for each dispatched message, we update the related order in the database
        return flux -> orderService.consumeOrderDispatchedEvent(flux)
                // always log a message
                .doOnNext(order -> log.info("The order with id {} is dispatched", order.id()))
                // must subscribe to the reactive stream. Without a subscriber, no data flows
                // through the stream.
                .subscribe();
    }

}
