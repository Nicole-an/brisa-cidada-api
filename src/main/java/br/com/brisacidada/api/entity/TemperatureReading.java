package br.com.brisacidada.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "temperature_readings",
        uniqueConstraints = @UniqueConstraint(name = "uk_temperature_readings_sensor_measured",
                columnNames = {"sensor_id", "measured_at"}))
public class TemperatureReading extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_temperature_readings_sensor"))
    private Sensor sensor;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "temperature_celsius", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "humidity_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal humidityPercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReadingSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReadingQuality quality;
}
