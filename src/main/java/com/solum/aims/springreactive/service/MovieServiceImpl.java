package com.solum.aims.springreactive.service;

import com.solum.aims.springreactive.model.Movie;
import com.solum.aims.springreactive.model.MovieEvent;
import com.solum.aims.springreactive.repository.MovieRepository;
import com.solum.aims.springreactive.service.interfaces.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Override
    public Flux<MovieEvent> events(String movieId) {
        return Flux.<MovieEvent>generate(movieEventSynchronousSink -> movieEventSynchronousSink.next(new MovieEvent(movieId, new Date())))
                .delayElements(Duration.ofSeconds(1));
    }

    @Override
    public Mono<Movie> getMovieById(String id) {
        return this.movieRepository.findById(id);
    }

    @Override
    public Flux<Movie> getAllMovies() {
        return this.movieRepository.findAll();
    }

}