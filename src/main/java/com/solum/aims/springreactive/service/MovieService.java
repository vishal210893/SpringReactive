package com.solum.aims.springreactive.service;

import com.solum.aims.springreactive.model.Movie;
import com.solum.aims.springreactive.model.MovieEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieService {

    Flux<MovieEvent> events(String movieId);

    Mono<Movie> getMovieById(String id);

    Flux<Movie> getAllMovies();

}