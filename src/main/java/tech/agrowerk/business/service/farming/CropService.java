package tech.agrowerk.business.service.farming;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.farming.CropRepository;

@Service
public class CropService {
    private final CropRepository cropRepository;

    public CropService(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }
}
