package tech.agrowerk.infrastructure.model.core;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.infrastructure.converter.BackupCodesConverter;
import tech.agrowerk.infrastructure.model.core.enums.DeletionReason;
import tech.agrowerk.infrastructure.model.core.enums.RoleType;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;
import tech.agrowerk.infrastructure.model.supplier.Supplier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_cpf", columnList = "cpf"),
        @Index(name = "idx_deleted", columnList = "is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 15)
    private String telephone;

    @Column(unique = true, length = 14)
    @CPF
    private String cpf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Instant lastLogin;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @Column(name = "refresh_token_hash", length = 60)
    private String refreshTokenHash;

    @Column(name = "refresh_token_expiry")
    private Instant refreshTokenExpiry;

    @Column(name = "refresh_token_family_id")
    private String refreshTokenFamilyId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_password_change")
    private Instant lastPasswordChange;

    @Column(name = "password_expiry_days")
    private Integer passwordExpiryDays = 90;

    @Column(name = "require_password_change", nullable = false)
    private boolean requirePasswordChange = false;

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret", length = 32)
    private String mfaSecret;

    @Column(name = "mfa_backup_codes")
    @Convert(converter = BackupCodesConverter.class)
    private List<String> mfaBackupCodes;

    @Column(name = "terms_accepted", nullable = false)
    private boolean termsAccepted = false;

    @Column(name = "terms_accepted_at")
    private Instant termsAcceptedAt;

    @Column(name = "terms_version")
    private String termsVersion;

    @Column(name = "privacy_policy_accepted", nullable = false)
    private boolean privacyPolicyAccepted = false;

    @Column(name = "privacy_policy_accepted_at")
    private Instant privacyPolicyAcceptedAt;

    @Column(name = "privacy_policy_version")
    private String privacyPolicyVersion;

    @Column(name = "marketing_consent", nullable = false)
    private boolean marketingConsent = false;

    @Column(name = "marketing_consent_at")
    private Instant marketingConsentAt;

    @Column(name = "data_sharing_consent", nullable = false)
    private boolean dataSharingConsent = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deletion_reason")
    @Enumerated(EnumType.STRING)
    private DeletionReason deletionReason;

    @Column(name = "anonymized", nullable = false)
    private boolean anonymized = false;

    @Column(name = "anonymized_at")
    private Instant anonymizedAt;

    @Column(name = "data_retention_until")
    private LocalDateTime dataRetentionUntil;

    @Column(name = "ip_address_registration", length = 45)
    private String ipAddressRegistration;

    @Column(name = "last_ip_address", length = 45)
    private String lastIpAddress;

    @Column(name = "user_agent_registration", length = 500)
    private String userAgentRegistration;

    @Column(name = "last_user_agent", length = 500)
    private String lastUserAgent;

    @Column(name = "geo_location_registration", length = 100)
    private String geoLocationRegistration;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_sent_at")
    private Instant emailVerificationSentAt;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_property",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "property_id")
    )
    private Set<Property> properties;

    @OneToOne(mappedBy = "administrator", fetch = FetchType.LAZY)
    private Supplier supplier;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<StockMovement> movements;

    public boolean isLoginCorrect(LoginRequest loginRequest, BCryptPasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(loginRequest.password(), this.password);
    }

    public boolean isProducer() {
        return role != null && role.getName() == RoleType.PRODUCER;
    }

    public boolean isSupplierAdmin() {
        return role != null && role.getName() == RoleType.SUPPLIER_ADMIN;
    }

    public void setRefreshToken(String rawToken, PasswordEncoder encoder, int expiryDays) {
        this.refreshTokenHash = encoder.encode(rawToken);
        this.refreshTokenExpiry = Instant.now().plus(expiryDays, ChronoUnit.DAYS);
        this.refreshTokenFamilyId = UUID.randomUUID().toString();
    }

    public boolean isRefreshTokenValid(String rawToken, PasswordEncoder encoder) {
        return refreshTokenExpiry != null
                && refreshTokenExpiry.isAfter(Instant.now())
                && encoder.matches(rawToken, refreshTokenHash);
    }

    public void invalidateRefreshToken() {
        this.refreshTokenHash = null;
        this.refreshTokenExpiry = null;
    }

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.isLocked = true;
            this.lockedUntil = Instant.now().plus(30, ChronoUnit.MINUTES);
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.isLocked = false;
        this.lockedUntil = null;
    }

    public boolean isAccountLocked() {
        if (isLocked && lockedUntil != null) {
            if (Instant.now().isAfter(lockedUntil)) {
                resetFailedLoginAttempts();
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isPasswordExpired() {
        if (lastPasswordChange == null || passwordExpiryDays == null) {
            return false;
        }
        Instant expiryDate = lastPasswordChange.plus(passwordExpiryDays, ChronoUnit.DAYS);
        return Instant.now().isAfter(expiryDate);
    }

    public void anonymize() {
        this.name = "Anonymous user #" + this.id;
        this.email = "anonymous " + this.id + "@deleted.local";
        this.telephone = null;
        this.cpf = null;
        this.password = UUID.randomUUID().toString();
        this.anonymizedAt = Instant.now();
        this.isActive = false;
        this.refreshTokenHash = null;
        this.mfaSecret = null;
        this.mfaBackupCodes = null;
    }

    @PrePersist
    protected void onCreate() {
        if (lastPasswordChange == null) {
            lastPasswordChange = Instant.now();
        }
        if (dataRetentionUntil == null) {
            dataRetentionUntil = LocalDateTime.now().plusYears(5);
        }
    }
}