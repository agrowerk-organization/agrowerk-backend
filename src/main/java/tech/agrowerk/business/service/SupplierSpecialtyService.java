package tech.agrowerk.business.service;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.SupplierSpecialtyRepository;

@Service
public class SupplierSpecialtyService {
    private final SupplierSpecialtyRepository supplierSpecialtyRepository;

    public SupplierSpecialtyService(SupplierSpecialtyRepository supplierSpecialtyRepository) {
        this.supplierSpecialtyRepository = supplierSpecialtyRepository;
    }
}
