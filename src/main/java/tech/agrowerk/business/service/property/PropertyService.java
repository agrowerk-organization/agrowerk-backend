package tech.agrowerk.business.service.property;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {
    private final PropertyRepository propertyRepository;



}
