package com.solum.aims.springreactive.repository;

import com.solum.aims.springreactive.model.Movie;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MovieRepository extends ReactiveMongoRepository<Movie, String> {

}