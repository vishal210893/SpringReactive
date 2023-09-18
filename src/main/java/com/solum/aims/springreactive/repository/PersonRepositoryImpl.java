package com.solum.aims.springreactive.repository;

import com.solum.aims.springreactive.model.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PersonRepositoryImpl implements PersonRepository {

    @Override
    public Mono<Person> getById(Integer id) {
        //final Person singlePerson = Person.builder().id(1).firstName("Java").lastName("Springboot Reactive").build();
        //return Mono.just(singlePerson);

        return findAll().filter(person -> person.getId() == id).next();
    }

    @Override
    public Flux<Person> findAll() {
        final Person person1 = Person.builder().id(1).firstName("Java").lastName("Springboot Reactive").build();
        final Person person2 = Person.builder().id(2).firstName("Oracle").lastName("Java 17").build();
        Person michael = new Person(3, "Michael", "Weston");
        Person fiona = new Person(5, "Fiona", "Glenanne");
        Person sam = new Person(6, "Sam", "Axe");
        Person jesse = new Person(1, "Jesse", "Porter");
        return Flux.just(person1, person2, michael, fiona, sam, jesse);
    }

}
