package com.solum.aims.springreactive;

import com.solum.aims.springreactive.model.Person;
import com.solum.aims.springreactive.repository.PersonRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@Slf4j
class PersonRepositoryImplTest {

    private PersonRepositoryImpl personRepository;

    @BeforeEach
    void setUp() {
        personRepository = new PersonRepositoryImpl();
    }

    @Test
    void getByIdBlock() {
        Mono<Person> personMono = personRepository.getById(1);
        Person person = personMono.block();
        log.info(person.toString());
    }

    @Test
    void getByIdSubscribe() {
        Mono<Person> personMono = personRepository.getById(21);
        StepVerifier.create(personMono).expectNextCount(1).verifyComplete();
        personMono.subscribe(person -> {
            log.info(person.toString());
        });
    }

    @Test
    void getByIdSubscribeNotFound() {
        Mono<Person> personMono = personRepository.getById(9);
        personMono.subscribe(person -> {
            log.info(person.toString());
        });
    }

    @Test
    void getByIdMapFunction() {
        Mono<Person> personMono = personRepository.getById(1);
        personMono.map(person -> {
            log.info(person.toString());
            return person.getFirstName();
        }).subscribe(firstName -> {
            log.info("from map: " + firstName);
        });
    }

    @Test
    void fluxTestBlockFirst() {
        Flux<Person> personFlux = personRepository.findAll();
        StepVerifier.create(personFlux).expectNextCount(2).verifyComplete();
        Person person = personFlux.blockFirst();
        log.info(person.toString());
    }

    @Test
    void testFluxSubscribe() {
        Flux<Person> personFlux = personRepository.findAll();
        personFlux.subscribe(person -> {
            log.info(person.toString());
        });
    }

    @Test
    void testFluxToListMono() {
        Flux<Person> personFlux = personRepository.findAll();
        Mono<List<Person>> personListMono = personFlux.collectList();
        personListMono.subscribe(list -> {
            list.forEach(person -> {
                log.info(person.toString());
            });
        });
    }

    @Test
    void testFindPersonById() {
        Flux<Person> personFlux = personRepository.findAll();
        final Integer id = 2;
        Mono<Person> personMono = personFlux.filter(person -> person.getId() == id).next();
        personMono.subscribe(person -> {
            log.info(person.toString());
        });
    }

    @Test
    void testFindPersonByIdNotFound() {
        Flux<Person> personFlux = personRepository.findAll();
        final Integer id = 10;
        Mono<Person> personMono = personFlux.filter(person -> person.getId() == id).next();
        personMono.subscribe(person -> {
            log.info(person.toString());
        });
    }

    @Test
    void testFindPersonByIdNotFoundWithException() {
        Flux<Person> personFlux = personRepository.findAll();
        final Integer id = 1;

        Mono<Person> personMono = personFlux.filter(person -> {
            System.out.println(person.getId());
            return person.getId() == id;
        }).single();

        personMono.doOnError(throwable -> log.info(throwable.getMessage(), throwable))
                .onErrorReturn(Person.builder().id(id).build())
                .subscribe(person -> log.info(person.toString()));
    }

}
