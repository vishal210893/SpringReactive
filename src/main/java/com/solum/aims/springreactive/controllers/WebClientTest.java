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
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

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

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<Void>> deleteBeerById() {

        Mono<BeerPagedList> beerPagedListMono = listBeer();
        final BeerDto beerDto = beerPagedListMono
                .publishOn(Schedulers.parallel())
                .flatMap(beerDto1 -> Mono.just(beerDto1))
                .map(beerDtos -> beerDtos.getContent().get(0))
                .block();
        //BeerDto beerDto = pagedList.getContent().get(0);

        return webClient.delete()
                .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH_GET_BY_ID)
                        .build(beerDto.getId()))
                .retrieve()
                .toBodilessEntity();

    }


    @GetMapping("/getUsingThread")
    public Mono<BeerPagedList> listBeerUsingThread() {

        /*
        map:
        -----
        • map is used to transform each element of a stream into another element. It applies a given function to each element and produces a new stream with the transformed elements.
        • The result of map is a one-to-one mapping, meaning each input element corresponds to exactly one output element.
        • It's suitable for simple transformations, such as changing the type of elements or extracting a property from each element.

        flatMap:
        --------
        • flatMap is used when you want to transform each element of a stream into zero, one, or multiple elements. It applies a function that returns a stream for each element and flattens the resulting streams into a single stream.
        • The result of flatMap is a many-to-many mapping, meaning one input element can produce multiple output elements, or even none if the function returns an empty stream.
        • It's suitable for situations where you need to perform more complex transformations, such as filtering, expanding, or unwrapping nested data structures.
        */

        ArrayList<List<String>> ar = new ArrayList<>();
        ar.add(Arrays.asList("a", "b"));
        ar.add(Arrays.asList("c", "d"));

        final Stream<String> stringStream = ar.stream().flatMap(arl -> arl.stream());
        stringStream.forEach(s -> System.out.println(s));

        final Stream<Stream<String>> streamStream = ar.stream().map(arl -> arl.stream());

        final Mono<BeerPagedList> beerPagedListMono = webClient.get()
                .uri(WebClientProperties.BEER_V1_PATH)
                .retrieve()
                .bodyToMono(BeerPagedList.class);

        beerPagedListMono.subscribe(beerPagedList -> {
            System.out.println(beerPagedList.toList());
        });


        //Thread.sleep(100000);

        AtomicReference<String> beerName = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        beerPagedListMono
                .map(beerPagedList -> {
                    final UUID id = beerPagedList.getContent().get(0).getId();
                    return id;
                })
                .map(beerId -> webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH_GET_BY_ID)
                                .queryParamIfPresent("showInventoryOnHand", Optional.ofNullable(false))
                                .build(beerId))
                        .retrieve()
                        .bodyToMono(BeerDto.class))
                .flatMap(mono -> mono)
                .subscribe(beerDto -> {
                    System.out.println(beerDto.getBeerName());
                    beerName.set(beerDto.getBeerName());
                    countDownLatch.countDown();
                });

        //countDownLatch.await();
        return beerPagedListMono;
    }

}
