package br.com.brisacidada.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nível de alerta calculado a partir do índice de calor")
public enum AlertLevel {
    INSUFFICIENT_DATA("Dados insuficientes no período avaliado"),
    NORMAL("Sem alerta de calor"),
    ATTENTION("Atenção: fadiga possível com exposição prolongada"),
    ALERT("Alerta: risco de cãibras e exaustão pelo calor"),
    DANGER("Perigo: exaustão provável e risco de insolação"),
    EXTREME("Perigo extremo: alto risco de insolação");

    private final String description;

    AlertLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
