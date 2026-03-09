package tech.agrowerk.infrastructure.model.core;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Address {

    @Column(length = 255)
    private String municipality;

    @Column(length = 9)
    private String code;

    @Column(name = "address_number")
    private int number;

    @Column(length = 255)
    private String street;

    @Column(length = 255)
    private String neighborhood;
}
