package com.solum.aims.springreactive.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Person {

    private Integer id;
    private String firstName;
    private String lastName;

    public String sayMyName() {
        return firstName + " " + lastName;
    }

}
