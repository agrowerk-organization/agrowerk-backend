package tech.agrowerk.infrastructure.model.market;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;
import tech.agrowerk.infrastructure.model.market.enums.ReportStatus;
import tech.agrowerk.infrastructure.model.market.enums.ReportType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_reports",
        indexes = {
            @Index(name = "idx_report_commodity_date", columnList = "commodity, period_start"),
            @Index(name = "idx_report_type_status", columnList = "reportType, reportStatus")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MarketReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    private Commodity commodity;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private ReportPayload reportPayload;

    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime generatedAt;


}
