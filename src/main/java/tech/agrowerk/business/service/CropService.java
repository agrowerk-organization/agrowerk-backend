package tech.agrowerk.business.service;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.CropRepository;

@Service
public class CropService {
    private final CropRepository cropRepository;

    public CropService(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }
}
