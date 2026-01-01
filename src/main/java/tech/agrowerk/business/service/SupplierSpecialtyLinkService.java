package tech.agrowerk.business.service;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.SupplierSpecialtyLinkRepository;

@Service
public class SupplierSpecialtyLinkService {
    private final SupplierSpecialtyLinkRepository supplierSpecialtyLinkRepository;

    public SupplierSpecialtyLinkService(SupplierSpecialtyLinkRepository supplierSpecialtyLinkRepository) {
        this.supplierSpecialtyLinkRepository = supplierSpecialtyLinkRepository;
    }
}
