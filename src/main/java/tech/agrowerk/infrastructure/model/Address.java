package tech.agrowerk.infrastructure.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private String municipality;

    @Column(length = 9)
    private String code;

    private int number;

    private String street;

    private String neighborhood;
}
