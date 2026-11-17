package br.com.brisacidada.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI brisaCidadaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Brisa Cidadã API")
                .version("v1")
                .description("""
                        API REST da solução CitizenTech Brisa Cidadã para prevenção de riscos de calor extremo.
                        Gerencia zonas de calor, sensores, leituras de temperatura/umidade e pontos públicos de resfriamento,
                        e calcula alertas demonstrativos de calor por zona.

                        Os alertas são apenas retornados pela API: nenhuma notificação (SMS, WhatsApp, e-mail etc.) é enviada.
                        Leituras com origem SIMULATED são demonstrativas e não representam medições reais.
                        """)
                .license(new License().name("Uso acadêmico")));
    }
}
