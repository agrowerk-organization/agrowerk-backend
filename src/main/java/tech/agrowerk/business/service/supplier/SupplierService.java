package tech.agrowerk.business.service.supplier;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.supplier.SupplierRepository;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }
}
