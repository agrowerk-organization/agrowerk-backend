package tech.agrowerk.business.service.property;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;

@Service
public class PropertyService {
    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }
}
