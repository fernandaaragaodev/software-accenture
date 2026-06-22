package com.accenture.officehub_v1.service.ia.layout;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class YoloEquipamentoMapper {

    private static final Map<String, String> MAPEAMENTO = Map.ofEntries(
            Map.entry("monitor", "MONITOR"),
            Map.entry("notebook", "NOTEBOOK"),
            Map.entry("cadeira", "CADEIRA"),
            Map.entry("dock", "DOCK"),
            Map.entry("telefone", "TELEFONE"),
            Map.entry("impressora", "IMPRESSORA"),
            Map.entry("mesa-digitalizadora", "MESA_DIGITALIZADORA"),
            Map.entry("projetor", "PROJETOR")
    );

    public String mapearTipoEquipamento(String classNameYolo) {
        if (classNameYolo == null || classNameYolo.isBlank()) {
            return null;
        }

        String chave = classNameYolo.trim().toLowerCase(Locale.ROOT);
        return MAPEAMENTO.get(chave);
    }

    public String descricaoPadrao(String tipoEquipamento) {
        return switch (tipoEquipamento) {
            case "MONITOR" -> "Monitor de vídeo padrão para estações de trabalho";
            case "NOTEBOOK" -> "Notebook detectado na estação de trabalho";
            case "CADEIRA" -> "Cadeira detectada na estação de trabalho";
            case "DOCK" -> "Dock de conexão detectado na estação";
            case "TELEFONE" -> "Telefone detectado na estação de trabalho";
            case "IMPRESSORA" -> "Impressora detectada no ambiente";
            case "MESA_DIGITALIZADORA" -> "Mesa digitalizadora detectada no ambiente";
            case "PROJETOR" -> "Projetor detectado no ambiente";
            default -> "Equipamento detectado automaticamente via IA";
        };
    }
}
