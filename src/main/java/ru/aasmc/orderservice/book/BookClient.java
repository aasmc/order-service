package ru.aasmc.orderservice.book;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BookClient {
    private static final String BOOKS_ROOT_API = "/books/";
    private final WebClient webClient;

    public BookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Book> getBookByIsbn(String isbn) {
        return webClient
                .get()
                .uri(BOOKS_ROOT_API + isbn)
                .retrieve()
                .bodyToMono(Book.class)
                .timeout(Duration.ofSeconds(3), Mono.empty())
                // return Mono.empty() in case of 404 error, and not
                // proceed with retries
                .onErrorResume(
                        WebClientResponseException.NotFound.class,
                        exception -> Mono.empty())
                // Placing retryWhen() after timeout(), means that the timeout is applied
                // to each retry attempt. Placing it after timeout() means the timeout
                // is applied to the overall operation (i.e. the whole sequence of initial
                // request and retries has to happen within the given timeout limit)
                .retryWhen(
                        // Exponential backoff is used as the retry strategy
                        // Three attempts are allowed with a 100ms initial backoff
                        // By default a jitter of at most 50% of the computed delay is used.
                        // When you have multiple instances of Order Service running, the jitter
                        // factor ensures that the replicas will not retry requests simultaneously
                        Retry.backoff(3, Duration.ofMillis(100))
                )
                // If any error happens after the 3 retry attempts, catch the exception and
                // return an empty object
                .onErrorResume(
                        Exception.class,
                        e -> Mono.empty()
                );
    }
}
