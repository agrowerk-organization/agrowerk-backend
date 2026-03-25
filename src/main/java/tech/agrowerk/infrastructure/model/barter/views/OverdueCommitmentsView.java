package tech.agrowerk.infrastructure.model.barter.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Immutable
@Subselect("""
    SELECT 
        cc.id,
        cc.transaction_id,
        cc.farmer_id,
        cc.crop_id,
        cc.committed_quantity,
        cc.delivered_quantity,
        cc.committed_quantity - cc.delivered_quantity AS pending_quantity,
        cc.expected_delivery_date,
        CURRENT_DATE - cc.expected_delivery_date AS days_overdue,
        cc.status 
    FROM crop_commitments cc
    WHERE cc.expected_delivery_date < CURRENT_DATE
    AND cc.status NOT IN ('DELIVERED', 'CANCELLED')
""")
@Getter
public class OverdueCommitmentsView {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "farmer_id")
    private UUID farmerId;

    @Column(name = "crop_id")
    private UUID cropId;

    @Column(name = "committed_quantity")
    private BigDecimal committedQuantity;

    @Column(name = "delivered_quantity")
    private BigDecimal deliveredQuantity;

    @Column(name = "pending_quantity")
    private BigDecimal pendingQuantity;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "days_overdue")
    private Integer daysOverdue;

    @Column(name = "status")
    private String status;
}
