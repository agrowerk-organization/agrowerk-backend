package tech.agrowerk.business.mapper.farming;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.farming.CreateBatchRequest;
import tech.agrowerk.application.dto.response.farming.BatchResponse;
import tech.agrowerk.infrastructure.model.farming.Batch;
import tech.agrowerk.infrastructure.model.farming.enums.BatchReceiptStatus;
import tech.agrowerk.infrastructure.model.farming.enums.BatchStatus;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.supplier.Supplier;

@Component
public class BatchMapper {

    public Batch toEntity(CreateBatchRequest request, Input input, Supplier supplier) {
        Batch batch = new Batch();
        batch.setBatchNumber(request.batchNumber());
        batch.setInvoiceNumber(request.invoiceNumber());
        batch.setInput(input);
        batch.setSupplier(supplier);
        batch.setInitialQuantity(request.initialQuantity());
        batch.setCurrentQuantity(request.initialQuantity());
        batch.setManufacturingDate(request.manufacturingDate());
        batch.setExpirationDate(request.expirationDate());
        batch.setEntryDate(request.entryDate());
        batch.setUnitPrice(request.unitPrice());
        batch.setTotalValue(request.initialQuantity().multiply(request.unitPrice()));
        batch.setStatus(BatchStatus.AVAILABLE);
        batch.setReceiptStatus(BatchReceiptStatus.PENDING);
        batch.setNotes(request.notes());
        return batch;
    }

    public BatchResponse toResponse(Batch batch) {
        return new BatchResponse(
                batch.getId(),
                batch.getBatchNumber(),
                batch.getInvoiceNumber(),
                batch.getInput().getId(),
                batch.getInput().getName(),
                batch.getSupplier().getId(),
                batch.getSupplier().getCorporateReason(),
                batch.getProperty() != null ? batch.getProperty().getId() : null,
                batch.getProperty() != null ? batch.getProperty().getName() : null,
                batch.getInitialQuantity(),
                batch.getCurrentQuantity(),
                batch.getManufacturingDate(),
                batch.getExpirationDate(),
                batch.getEntryDate(),
                batch.getUnitPrice(),
                batch.getTotalValue(),
                batch.getStatus().name(),
                batch.getReceiptStatus().name(),
                batch.getReceivedAt(),
                batch.getNotes(),
                batch.isNearExpiration(15),
                batch.isExpired(),
                batch.getCreatedAt(),
                batch.getUpdatedAt()
        );
    }
}