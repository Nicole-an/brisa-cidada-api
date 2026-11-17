package br.com.brisacidada.api.service;

/**
 * Índice de calor (sensação térmica) pela regressão de Rothfusz, usada pelo NWS/NOAA,
 * incluindo os ajustes para umidade muito baixa ou muito alta.
 */
public final class HeatIndexCalculator {

    private HeatIndexCalculator() {
    }

    public static double heatIndexCelsius(double temperatureCelsius, double humidityPercent) {
        double t = temperatureCelsius * 9.0 / 5.0 + 32.0;
        double rh = humidityPercent;

        double simple = 0.5 * (t + 61.0 + (t - 68.0) * 1.2 + rh * 0.094);
        if ((simple + t) / 2.0 < 80.0) {
            return toCelsius(simple);
        }

        double hi = -42.379
                + 2.04901523 * t
                + 10.14333127 * rh
                - 0.22475541 * t * rh
                - 0.00683783 * t * t
                - 0.05481717 * rh * rh
                + 0.00122874 * t * t * rh
                + 0.00085282 * t * rh * rh
                - 0.00000199 * t * t * rh * rh;

        if (rh < 13 && t >= 80 && t <= 112) {
            hi -= ((13 - rh) / 4.0) * Math.sqrt((17 - Math.abs(t - 95.0)) / 17.0);
        } else if (rh > 85 && t >= 80 && t <= 87) {
            hi += ((rh - 85) / 10.0) * ((87 - t) / 5.0);
        }
        return toCelsius(hi);
    }

    private static double toCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }
}
