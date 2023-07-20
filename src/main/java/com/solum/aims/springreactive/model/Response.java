package com.solum.aims.springreactive.model;

import lombok.Data;

@Data
public class Response {

    private BeerDto beerDto;

    private int statusCode;

}
