package com.solum.aims.springreactive.controllers;

import com.solum.aims.springreactive.config.WebClientProperties;
import com.solum.aims.springreactive.model.BeerDto;
import com.solum.aims.springreactive.model.BeerPagedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@Slf4j
@RequestMapping("/webclient")
public class WebClientTest {

    @Autowired
    private WebClient webClient;

    @GetMapping("/get")
    public Mono<BeerPagedList> listBeer() {

        final Mono<BeerPagedList> beerPagedListMono = webClient.get()
                .uri(WebClientProperties.BEER_V1_PATH)
                .retrieve()
                .bodyToMono(BeerPagedList.class);

        return beerPagedListMono;
    }

    @GetMapping("/getWithQuery")
    public Mono<BeerPagedList> getWithQueryParam() {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH)
                        .queryParamIfPresent("pageNumber", Optional.ofNullable(10))
                        .queryParamIfPresent("pageSize", Optional.ofNullable(1))
                        .build()
                )
                .retrieve()
                .bodyToMono(BeerPagedList.class);
    }

    @GetMapping("/getWithUrlBuilder")
    public Mono<BeerDto> getWithUrlBuilder(@RequestParam String upc) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_UPC_PATH)
                        .build(upc))
                .retrieve()
                .bodyToMono(BeerDto.class)
                .onErrorResume(throwable -> {
                    if (throwable instanceof WebClientResponseException) {
                        final WebClientResponseException exception = (WebClientResponseException) throwable;
                        log.error(exception.getStatusCode().toString());
                        BeerDto beerDto = new BeerDto();
                        beerDto.setStatus(exception.getStatusCode().value());
                        return Mono.just(beerDto);
                    } else {
                        throw new RuntimeException(throwable);
                    }
                });
    }

    @PostMapping("/postBeer")
    public Mono<ResponseEntity<Void>> postBeer() {

        BeerDto beerDto = BeerDto.builder()
                .beerName("Dogfishhead 90 Min IPA")
                .beerStyle("IPA")
                .upc("234848549559")
                .price(new BigDecimal("10.99"))
                .build();

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH).build())
                .body(BodyInserters.fromValue(beerDto))
                .retrieve()
                .toBodilessEntity();
    }

    @PutMapping("/putBeer")
    public Mono<ResponseEntity<Void>> putBeer() {

        BeerDto beerDto = BeerDto.builder()
                .beerName("Dog fish head 90 Min IPA")
                .beerStyle("IPA")
                .upc("234848549559")
                .price(new BigDecimal("10.99"))
                .build();

        return webClient.put()
                .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH_GET_BY_ID)
                        .build("43f6398b-b185-4f22-967b-514ea96f5a47"))
                .body(BodyInserters.fromValue(beerDto))
                .retrieve()
                .toBodilessEntity();
    }


    @GetMapping("/getUsingThread")
    public Mono<BeerPagedList> listBeerUsingThread() throws InterruptedException {

        final Mono<BeerPagedList> beerPagedListMono = webClient.get()
                .uri(WebClientProperties.BEER_V1_PATH)
                .retrieve()
                .bodyToMono(BeerPagedList.class);

        beerPagedListMono.subscribe(beerPagedList -> {
            System.out.println(beerPagedList.toList());
        });



        //Thread.sleep(100000);
/*
        AtomicReference<String> beerName = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        beerPagedListMono
                .map(beerPagedList -> {
                    final UUID id = beerPagedList.getContent().get(0).getId();
                    return id;
                })
                .map(beerId -> webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH_GET_BY_ID)
                                .queryParamIfPresent("showInventoryOnHand", Optional.ofNullable(true))
                                .build(beerId))
                        .retrieve()
                        .bodyToMono(BeerDto.class))
                .flatMap(mono -> mono)
                .subscribe(beerDto -> {
                    System.out.println(beerDto.getBeerName());
                    beerName.set(beerDto.getBeerName());
                    countDownLatch.countDown();
                });

        countDownLatch.await();*/
        return beerPagedListMono;
    }

}
