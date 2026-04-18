package tech.agrowerk.business.service.supplier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.supplier.CreateSupplierRatingRequest;
import tech.agrowerk.application.dto.response.supplier.SupplierRatingResponse;
import tech.agrowerk.business.mapper.supplier.SupplierRatingMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.supplier.Supplier;
import tech.agrowerk.infrastructure.model.supplier.SupplierRating;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.BatchRepository;
import tech.agrowerk.infrastructure.repository.property.UserPropertyRepository;
import tech.agrowerk.infrastructure.repository.supplier.SupplierRatingRepository;
import tech.agrowerk.infrastructure.repository.supplier.SupplierRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class SupplierRatingService {

    private final SupplierRatingRepository supplierRatingRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final SupplierRatingMapper supplierRatingMapper;
    private final AuthUtil authUtil;


    public SupplierRatingService(SupplierRatingRepository supplierRatingRepository,
                                 SupplierRepository supplierRepository,
                                 UserRepository userRepository,
                                 BatchRepository batchRepository,
                                 UserPropertyRepository userPropertyRepository,
                                 SupplierRatingMapper supplierRatingMapper,
                                 AuthUtil authUtil) {
        this.supplierRatingRepository = supplierRatingRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
        this.userPropertyRepository = userPropertyRepository;
        this.supplierRatingMapper = supplierRatingMapper;
        this.authUtil = authUtil;
    }

    @Transactional
    public SupplierRatingResponse rateSupplier(CreateSupplierRatingRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        User producer = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        validatePurchaseHistory(supplier.getId(), auth.id());

        if (supplierRatingRepository.existsBySupplier_IdAndRatedBy_Id(supplier.getId(), producer.getId())) {
            throw new AccessDeniedException("You have already rated this supplier");
        }

        SupplierRating rating = SupplierRating.builder()
                .supplier(supplier)
                .ratedBy(producer)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        SupplierRating saved = supplierRatingRepository.save(rating);
        log.info("Rating created for supplier={} by producer={}", supplier.getId(), producer.getId());

        return supplierRatingMapper.toRatingResponse(saved);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageRating(UUID supplierId) {
        return supplierRatingRepository.calculateAverageRating(supplierId)
                .orElse(BigDecimal.ZERO);
    }

    private void validatePurchaseHistory(UUID supplierId, UUID userId) {
        boolean hasAcquired = userPropertyRepository.hasUserPurchasedFromSupplier(supplierId, userId);

        if (!hasAcquired) {
            throw new AccessDeniedException("You can only rate suppliers you have acquired inputs from");
        }
    }
}
