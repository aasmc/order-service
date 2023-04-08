package ru.aasmc.orderservice.order.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.aasmc.orderservice.config.SecurityConfig;
import ru.aasmc.orderservice.order.domain.Order;
import ru.aasmc.orderservice.order.domain.OrderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static ru.aasmc.orderservice.order.domain.OrderStatus.REJECTED;

@WebFluxTest(OrderController.class)
@Import(SecurityConfig.class)
public class OrderControllerWebFluxTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private OrderService orderService;

    /**
     * Ap part of the OAuth2 Access Token validation, Spring Security relies on the
     * public keys provided by Keycloak to verify the JWT signature. Internally, the
     * framework configures a ReactiveJwtDecoder bean to decode and verify a JWT using
     * those keys. In the context of a web slice test, we can provide a mock ReactiveJwtDecoder
     * bean so that Spring Security skips the interaction with Keycloak.
     */
    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Test
    void whenBookNotAvailableThenRejectOrder() {
        var orderRequest = new OrderRequest("1234567890", 3);
        var expectedOrder = OrderService.buildRejectedOrder(orderRequest.isbn(), orderRequest.quantity());
        given(orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity()))
                .willReturn(Mono.just(expectedOrder));

        webClient
                // Mutates the HTTP request with a mock, JWT-formatted Access Token
                // for a user with the "customer" role.
                .mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_customer")))
                .post()
                .uri("/orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class).value(actualOrder -> {
                    assertThat(actualOrder).isNotNull();
                    assertThat(actualOrder.status()).isEqualTo(REJECTED);
                });

    }
}
