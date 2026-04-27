package tech.agrowerk.business.service.supplier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.supplier.CreateSupplierRequest;
import tech.agrowerk.application.dto.request.supplier.UpdateSupplierRequest;
import tech.agrowerk.application.dto.response.supplier.SupplierResponse;
import tech.agrowerk.business.mapper.supplier.SupplierMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.supplier.Supplier;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.supplier.SupplierRepository;

import java.util.UUID;

@Service
@Slf4j
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final SupplierMapper supplierMapper;
    private final AuthUtil authUtil;

    public SupplierService(SupplierRepository supplierRepository,
                           UserRepository userRepository,
                           SupplierMapper supplierMapper,
                           AuthUtil authUtil) {
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.supplierMapper = supplierMapper;
        this.authUtil = authUtil;
    }

    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        if (supplierRepository.findByAdministrator_Id(auth.id()).isPresent()) {
            throw new EntityAlreadyExistsException("Supplier administrator already has a supplier");
        }

        if (supplierRepository.existsByCnpj(request.cnpj())) {
            throw new EntityAlreadyExistsException("CNPJ already registered");
        }

        User admin = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Supplier supplier = supplierMapper.toEntity(request);
        supplier.setAdministrator(admin);

        Supplier saved = supplierRepository.save(supplier);

        log.info("Supplier created id={}", saved.getId());

        return supplierMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(UUID supplierId) {
        return supplierRepository.findById(supplierId)
                .map(supplierMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
    }

    @Transactional(readOnly = true)
    public SupplierResponse findByCnpj(String cnpj) {
        return supplierRepository.findByCnpj(cnpj)
                .map(supplierMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
    }

    @Transactional(readOnly = true)
    public SupplierResponse getMySupplier() {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        return supplierRepository.findByAdministrator_Id(auth.id())
                .map(supplierMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found for this administrator"));
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> listAll(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(supplierMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> listByState(String state, Pageable pageable) {
        return supplierRepository.findByAddress_MunicipalityContainingIgnoreCaseAndIsActiveTrue(
                state, pageable).map(supplierMapper::toResponse);
    }

    @Transactional
    public SupplierResponse updateSupplier(UpdateSupplierRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Supplier supplier = supplierRepository.findByAdministrator_Id(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        if (request.corporateReason()    != null) supplier.setCorporateReason(request.corporateReason());
        if (request.fantasyName()        != null) supplier.setFantasyName(request.fantasyName());
        if (request.stateRegistration()  != null) supplier.setStateRegistration(request.stateRegistration());
        if (request.email()              != null) supplier.setEmail(request.email());
        if (request.telephone()          != null) supplier.setTelephone(request.telephone());
        if (request.nameContact()        != null) supplier.setNameContact(request.nameContact());
        if (request.observations()       != null) supplier.setObservations(request.observations());
        if (request.acceptsBarterDeals() != null) supplier.setAcceptsBarterDeals(request.acceptsBarterDeals());
        if (request.barterTerms()        != null) supplier.setBarterTerms(request.barterTerms());
        if (request.address() != null)
            supplier.setAddress(supplierMapper.toAddress(request.address()));

        log.info("Supplier updated id={}", supplier.getId());
        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public void toggleActive(UUID supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        supplier.setIsActive(!supplier.getIsActive());
        log.info("Supplier {} isActive={}", supplierId, supplier.getIsActive());
    }
}
