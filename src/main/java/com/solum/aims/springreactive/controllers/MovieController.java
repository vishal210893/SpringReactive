package com.solum.aims.springreactive.controllers;

import com.solum.aims.springreactive.model.Movie;
import com.solum.aims.springreactive.model.MovieEvent;
import com.solum.aims.springreactive.service.interfaces.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    @GetMapping(value = "/{id}/events")
    Flux<MovieEvent> streamMovieEvents(@PathVariable String id) {
        return movieService.events(id);
    }

    @GetMapping(value = "/{id}")
    Mono<Movie> getMovieById(@PathVariable String id) {
        return movieService.getMovieById(id);
    }

    @GetMapping
    Flux<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

}