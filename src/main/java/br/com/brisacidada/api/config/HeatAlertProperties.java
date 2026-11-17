package br.com.brisacidada.api.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "brisa.heat-alert")
public class HeatAlertProperties {

    @Min(1)
    @Max(72)
    private int lookbackHours = 3;

    @NotNull
    private BigDecimal attentionHeatIndex = new BigDecimal("27");

    @NotNull
    private BigDecimal alertHeatIndex = new BigDecimal("32");

    @NotNull
    private BigDecimal dangerHeatIndex = new BigDecimal("41");

    @NotNull
    private BigDecimal extremeHeatIndex = new BigDecimal("54");

    @Min(0)
    @Max(50)
    private int maxCoolingPoints = 5;

    @AssertTrue(message = "os limiares devem ser crescentes: attention < alert < danger < extreme")
    public boolean isThresholdsAscending() {
        if (attentionHeatIndex == null || alertHeatIndex == null || dangerHeatIndex == null || extremeHeatIndex == null) {
            return true;
        }
        return attentionHeatIndex.compareTo(alertHeatIndex) < 0
                && alertHeatIndex.compareTo(dangerHeatIndex) < 0
                && dangerHeatIndex.compareTo(extremeHeatIndex) < 0;
    }
}
