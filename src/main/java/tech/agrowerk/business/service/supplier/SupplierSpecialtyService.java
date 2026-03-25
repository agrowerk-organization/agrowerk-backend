package tech.agrowerk.business.service.supplier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.supplier.CreateSupplierSpecialtyRequest;
import tech.agrowerk.application.dto.response.supplier.SupplierSpecialtyResponse;
import tech.agrowerk.business.mapper.supplier.SupplierMapper;
import tech.agrowerk.business.mapper.supplier.SupplierSpecialtyMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.supplier.Supplier;
import tech.agrowerk.infrastructure.model.supplier.SupplierSpecialty;
import tech.agrowerk.infrastructure.model.supplier.SupplierSpecialtyLink;
import tech.agrowerk.infrastructure.repository.supplier.SupplierRepository;
import tech.agrowerk.infrastructure.repository.supplier.SupplierSpecialtyLinkRepository;
import tech.agrowerk.infrastructure.repository.supplier.SupplierSpecialtyRepository;

import java.util.UUID;

@Service
@Slf4j
public class SupplierSpecialtyService {

    private final SupplierSpecialtyRepository supplierSpecialtyRepository;
    private final SupplierSpecialtyLinkRepository supplierSpecialtyLinkRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierSpecialtyMapper supplierSpecialtyMapper;
    private final AuthUtil authUtil;

    public SupplierSpecialtyService(SupplierSpecialtyRepository supplierSpecialtyRepository,
                                    SupplierSpecialtyLinkRepository supplierSpecialtyLinkRepository,
                                    SupplierRepository supplierRepository,
                                    SupplierSpecialtyMapper supplierSpecialtyMapper,
                                    AuthUtil authUtil) {
        this.supplierSpecialtyRepository = supplierSpecialtyRepository;
        this.supplierSpecialtyLinkRepository = supplierSpecialtyLinkRepository;
        this.supplierRepository = supplierRepository;
        this.supplierSpecialtyMapper = supplierSpecialtyMapper;
        this.authUtil = authUtil;
    }

    @Transactional(readOnly = true)
    public Page<SupplierSpecialtyResponse> listCatalog(Pageable pageable) {
        return supplierSpecialtyRepository.findAll(pageable)
                .map(s -> new SupplierSpecialtyResponse(s.getId(), s.getName(), s.getDescription(), true));
    }

    @Transactional
    public SupplierSpecialtyResponse createSpecialty(CreateSupplierSpecialtyRequest request) {
        SupplierSpecialty supplierSpecialty = new SupplierSpecialty();

        supplierSpecialty.setName(request.name());
        supplierSpecialty.setDescription(request.description());

        SupplierSpecialty saved = supplierSpecialtyRepository.save(supplierSpecialty);

        log.info("Supplier specialty created id={}", saved.getId());

        return new SupplierSpecialtyResponse(saved.getId(), saved.getName(), saved.getDescription(), true);
    }

    @Transactional
    public SupplierSpecialtyResponse addToSupplier(UUID supplierId, UUID specialtyId) {
        Supplier supplier = findSupplierAndValidateOwner(supplierId);

        SupplierSpecialty supplierSpecialty = supplierSpecialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier specialty not found"));

        SupplierSpecialtyLink link = new SupplierSpecialtyLink();
        link.setSupplier(supplier);
        link.setSpecialty(supplierSpecialty);
        link.setIsActive(true);

        return supplierSpecialtyMapper.toSpecialtyResponse(supplierSpecialtyLinkRepository.save(link));
    }

    @Transactional
    public void removeFromSupplier(UUID supplierId, UUID specialtyId) {
        findSupplierAndValidateOwner(supplierId);

        SupplierSpecialtyLink link = supplierSpecialtyLinkRepository
                .findBySupplier_IdAndSpecialty_Id(supplierId, specialtyId)
                .orElseThrow(() -> new EntityNotFoundException("Specialty link not found"));

        supplierSpecialtyLinkRepository.delete(link);
        log.info("Specialty {} removed from supplier {}", specialtyId, supplierId);
    }

    @Transactional
    public SupplierSpecialtyResponse toggleLinkActive(UUID supplierId, UUID specialtyId) {
        findSupplierAndValidateOwner(supplierId);

        SupplierSpecialtyLink link = supplierSpecialtyLinkRepository
                .findBySupplier_IdAndSpecialty_Id(supplierId, specialtyId)
                .orElseThrow(() -> new EntityNotFoundException("Specialty link not found"));

        link.setIsActive(!link.getIsActive());
        log.info("Specialty supplier = {} specialty = {} isActive = {}", supplierId, specialtyId, link.getIsActive());

        return supplierSpecialtyMapper.toSpecialtyResponse(link);
    }

    private Supplier findSupplierAndValidateOwner(UUID supplierId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        if (!supplier.getAdministrator().getId().equals(auth.id())) {
            throw new AccessDeniedException("Only the supplier administrator can manage specialties");
        }

        return supplier;
    }
}
