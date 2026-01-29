package tech.agrowerk.business.service.supplier;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.supplier.SupplierSpecialtyRepository;

@Service
public class SupplierSpecialtyService {
    private final SupplierSpecialtyRepository supplierSpecialtyRepository;

    public SupplierSpecialtyService(SupplierSpecialtyRepository supplierSpecialtyRepository) {
        this.supplierSpecialtyRepository = supplierSpecialtyRepository;
    }
}
