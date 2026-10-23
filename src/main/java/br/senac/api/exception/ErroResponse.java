package br.senac.api.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String mensagem,
        Map<String, String> campos
) {
    public static ErroResponse de(int status, String mensagem) {
        return new ErroResponse(OffsetDateTime.now(), status, mensagem, Map.of());
    }
}
