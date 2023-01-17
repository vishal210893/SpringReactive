package com.solum.aims.springreactive.bootstrap;

import com.solum.aims.springreactive.model.Movie;
import com.solum.aims.springreactive.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BootstrapCLR implements CommandLineRunner {

    private final MovieRepository movieRepository;

    @Override
    public void run(String... args) {
        //clear old data
        movieRepository.deleteAll()
                .thenMany(
                        Flux.just("Silence of the Lambdas", "AEon Flux", "Enter the Mono<Void>", "The Fluxxinator",
                                        "Back to the Future", "Meet the Fluxes", "Lord of the Fluxes")
                                .map(title -> new Movie(UUID.randomUUID().toString(), title))
                                .flatMap(movieRepository::save))
                .subscribe(null, null, () -> movieRepository.findAll().subscribe(System.out::println));
    }

}