package tech.agrowerk.business.mapper.farming;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.farming.CreatePrescriptionItemRequest;
import tech.agrowerk.application.dto.request.farming.CreatePrescriptionRequest;
import tech.agrowerk.application.dto.response.farming.PrescriptionItemResponse;
import tech.agrowerk.application.dto.response.farming.PrescriptionResponse;
import tech.agrowerk.infrastructure.model.farming.AgronomicPrescription;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.PrescriptionItem;
import tech.agrowerk.infrastructure.model.inventory.Input;

import java.util.ArrayList;
import java.util.List;

@Component
public class PrescriptionMapper {

    public AgronomicPrescription toEntity(
            CreatePrescriptionRequest request,
            Planting planting,
            String documentUrl) {

        AgronomicPrescription prescription = new AgronomicPrescription();
        prescription.setPlanting(planting);
        prescription.setField(planting.getField());
        prescription.setAgronomistName(request.agronomistName());
        prescription.setAgronomistCrea(request.agronomistCrea());
        prescription.setIssuedAt(request.issuedAt());
        prescription.setValidUntil(request.validUntil());
        prescription.setDocumentUrl(documentUrl);
        prescription.setActive(true);
        return prescription;
    }

    public PrescriptionItem toItemEntity(
            CreatePrescriptionItemRequest request,
            AgronomicPrescription prescription,
            Input input) {

        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null when mapping PrescriptionItem");
        }

        PrescriptionItem item = new PrescriptionItem();
        item.setPrescription(prescription);
        item.setInput(input);
        item.setAuthorizedQuantity(request.authorizedQuantity());
        item.setUnit(request.unit());
        item.setUsageInstructions(request.usageInstructions());

        if (prescription.getItems() == null) {
            prescription.setItems(new ArrayList<>());
        }
        prescription.getItems().add(item);

        return item;
    }

    public PrescriptionResponse toResponse(AgronomicPrescription prescription) {
        List<PrescriptionItemResponse> items = prescription.getItems() != null
                ? prescription.getItems().stream()
                .map(this::toItemResponse)
                .toList()
                : List.of();

        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPlanting().getId(),
                prescription.getPlanting()
                        .getCropVariety().getCrop().getName(),
                prescription.getField().getName(),
                prescription.getField().getProperty().getName(),
                prescription.getAgronomistName(),
                prescription.getAgronomistCrea(),
                prescription.getIssuedAt(),
                prescription.getValidUntil(),
                prescription.getDocumentUrl(),
                prescription.getActive(),
                !prescription.isValid(),
                items,
                prescription.getCreatedAt()
        );
    }

    private PrescriptionItemResponse toItemResponse(PrescriptionItem item) {
        return new PrescriptionItemResponse(
                item.getId(),
                item.getInput().getId(),
                item.getInput().getName(),
                item.getAuthorizedQuantity(),
                item.getUnit().name(),
                item.getUsageInstructions()
        );
    }
}
