package tech.agrowerk.application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.business.service.SupplierSpecialtyService;

@RestController
@RequestMapping("/supplier-specialty-links")
public class SupplierSpecialtyLinkController {
    private final SupplierSpecialtyService supplierSpecialtyService;

    public SupplierSpecialtyLinkController(SupplierSpecialtyService supplierSpecialtyService) {
        this.supplierSpecialtyService = supplierSpecialtyService;
    }
}
