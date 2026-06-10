package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;

public interface MotorAlocacao {

    AlocacaoAgenteSaidaDto executar(AlocacaoAgenteEntradaDto entrada);
}
