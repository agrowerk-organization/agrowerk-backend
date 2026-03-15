package tech.agrowerk.business.mapper.property;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.farming.CreateSeasonRequest;
import tech.agrowerk.application.dto.response.farming.SeasonResponse;
import tech.agrowerk.infrastructure.model.farming.Season;
import tech.agrowerk.infrastructure.model.farming.enums.SeasonStatus;
import tech.agrowerk.infrastructure.model.property.Property;

@Component
public class SeasonMapper {
    public Season toEntity(CreateSeasonRequest request, Property property) {
        Season season = new Season();
        season.setName(request.name());
        season.setStartDate(request.startDate());
        season.setEndDate(request.endDate());
        season.setSeasonStatus(SeasonStatus.PLANNED);
        season.setProperty(property);
        return season;
    }

    public SeasonResponse toResponse(Season season) {
        return new SeasonResponse(
                season.getId(),
                season.getName(),
                season.getProperty().getId(),
                season.getProperty().getName(),
                season.getStartDate(),
                season.getEndDate(),
                season.getSeasonStatus(),
                season.getCreatedAt()
        );
    }
}
