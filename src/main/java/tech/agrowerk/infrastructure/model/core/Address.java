package tech.agrowerk.infrastructure.model.core;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Address {

    private boolean rural;

    @Column(length = 9)
    private String code;

    @Column(length = 255)
    private String municipality;

    @Column(length = 255)
    private String locationName;

    @Column(length = 255)
    private String street;

    @Column(name = "address_number")
    private Integer number;

    @Column(length = 255)
    private String neighborhood;

    @Column(length = 500)
    private String landmark;
}