package tech.agrowerk.application.controller.supplier;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.business.service.supplier.SupplierSpecialtyService;

@RestController
@RequestMapping("/supplier-specialties")
public class SupplierSpecialtyController {
    private final SupplierSpecialtyService supplierSpecialtyService;

    public SupplierSpecialtyController(SupplierSpecialtyService supplierSpecialtyService) {
        this.supplierSpecialtyService = supplierSpecialtyService;
    }
}
