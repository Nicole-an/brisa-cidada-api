package br.com.brisacidada.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cooling_points",
        uniqueConstraints = @UniqueConstraint(name = "uk_cooling_points_name_address", columnNames = {"name", "address"}))
public class CoolingPoint extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 120)
    private String neighborhood;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "opening_hours", nullable = false, length = 120)
    private String openingHours;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "accessibility_info", length = 500)
    private String accessibilityInfo;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    // Opcional: pontos de resfriamento podem atender regiões ainda não mapeadas como zona de calor.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heat_zone_id", foreignKey = @ForeignKey(name = "fk_cooling_points_heat_zone"))
    private HeatZone heatZone;
}
