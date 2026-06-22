package com.accenture.officehub_v1.service.ia.layout;

import com.accenture.officehub_v1.client.YoloClient;
import com.accenture.officehub_v1.dto.ia.yolo.YoloDetectionDto;
import com.accenture.officehub_v1.dto.ia.yolo.YoloResponseDto;
import com.accenture.officehub_v1.dto.request.CriarLayoutRequest;
import com.accenture.officehub_v1.dto.request.CriarPosicaoRequest;
import com.accenture.officehub_v1.dto.response.GerarLayoutPorIaResponse;
import com.accenture.officehub_v1.dto.response.LayoutResponse;
import com.accenture.officehub_v1.dto.response.PosicaoComEquipamentosResponse;
import com.accenture.officehub_v1.dto.response.PosicaoEquipamentoResponse;
import com.accenture.officehub_v1.dto.response.PosicaoResponse;
import com.accenture.officehub_v1.dto.response.SalaResponse;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.security.SecurityUtils;
import com.accenture.officehub_v1.service.LayoutService;
import com.accenture.officehub_v1.service.PosicaoEquipamentoService;
import com.accenture.officehub_v1.service.PosicaoService;
import com.accenture.officehub_v1.service.SalaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiLayoutGenerationService {

    private final YoloClient yoloClient;
    private final CoordinateTransformationService coordinateTransformationService;
    private final StationGroupingService stationGroupingService;
    private final SpatialValidationService spatialValidationService;
    private final YoloEquipamentoMapper yoloEquipamentoMapper;
    private final SalaService salaService;
    private final LayoutService layoutService;
    private final PosicaoService posicaoService;
    private final PosicaoEquipamentoService posicaoEquipamentoService;

    @Transactional
    public GerarLayoutPorIaResponse gerar(String nomeSala, BigDecimal largura, BigDecimal altura, MultipartFile imagem) {
        validarEntrada(nomeSala, largura, altura, imagem);

        DimensaoImagem dimensaoImagem = lerDimensoesImagem(imagem);
        YoloResponseDto respostaYolo = yoloClient.detect(imagem);
        List<YoloDetectionDto> deteccoes = respostaYolo.detections() != null
                ? respostaYolo.detections()
                : List.of();

        List<DetectedObject> objetosNormalizados = coordinateTransformationService.transform(
                deteccoes,
                dimensaoImagem.largura(),
                dimensaoImagem.altura(),
                largura,
                altura);

        List<WorkstationGroup> estacoes = stationGroupingService.agrupar(objetosNormalizados);
        int capacidadeMaxima = Math.max(estacoes.size(), 1);
        spatialValidationService.validarEstacoes(estacoes, largura, altura, capacidadeMaxima);

        UUID usuarioId = SecurityUtils.getUsuarioIdAtual();
        SalaResponse sala = salaService.criarParaIa(nomeSala, largura, altura, capacidadeMaxima, usuarioId);
        LayoutResponse layout = layoutService.criar(new CriarLayoutRequest(sala.id(), "1"));

        List<PosicaoComEquipamentosResponse> posicoesCriadas = new ArrayList<>();
        int indice = 1;

        for (WorkstationGroup estacao : estacoes) {
            String identificador = String.format("P%02d", indice++);
            PosicaoResponse posicao = posicaoService.criarNoLayout(
                    new CriarPosicaoRequest(
                            sala.id(),
                            identificador,
                            "ESTACAO_TRABALHO",
                            estacao.centerRoomX(),
                            estacao.centerRoomY(),
                            estacao.centerPixelX(),
                            estacao.centerPixelY(),
                            null,
                            null),
                    layout.id());

            List<PosicaoEquipamentoResponse> equipamentos = vincularEquipamentos(posicao.id(), estacao);
            posicoesCriadas.add(new PosicaoComEquipamentosResponse(posicao, equipamentos));
        }

        LayoutResponse layoutAprovado = layoutService.aprovar(layout.id(), usuarioId);

        return new GerarLayoutPorIaResponse(
                sala,
                layoutAprovado,
                posicoesCriadas,
                deteccoes.size(),
                estacoes.size());
    }

    private List<PosicaoEquipamentoResponse> vincularEquipamentos(UUID posicaoId, WorkstationGroup estacao) {
        List<PosicaoEquipamentoResponse> equipamentos = new ArrayList<>();
        Set<String> tiposVinculados = new LinkedHashSet<>();

        for (DetectedObject objeto : estacao.objetos()) {
            String tipoEquipamento = yoloEquipamentoMapper.mapearTipoEquipamento(objeto.className());
            if (tipoEquipamento == null || !tiposVinculados.add(tipoEquipamento)) {
                continue;
            }

            PosicaoEquipamentoResponse vinculo = posicaoEquipamentoService.vincularPorTipoNome(
                    posicaoId,
                    tipoEquipamento,
                    yoloEquipamentoMapper.descricaoPadrao(tipoEquipamento));
            equipamentos.add(vinculo);
        }

        return equipamentos;
    }

    private void validarEntrada(String nomeSala, BigDecimal largura, BigDecimal altura, MultipartFile imagem) {
        if (nomeSala == null || nomeSala.isBlank()) {
            throw new RegraNegocioException("O nome da sala é obrigatório.");
        }
        if (largura == null || altura == null
                || largura.compareTo(BigDecimal.ZERO) <= 0
                || altura.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Largura e altura da sala devem ser maiores que zero.");
        }
        if (imagem == null || imagem.isEmpty()) {
            throw new RegraNegocioException("A imagem da planta baixa é obrigatória.");
        }
    }

    private DimensaoImagem lerDimensoesImagem(MultipartFile imagem) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagem.getBytes()));
            if (bufferedImage == null) {
                throw new RegraNegocioException("O arquivo enviado não é uma imagem válida.");
            }
            return new DimensaoImagem(bufferedImage.getWidth(), bufferedImage.getHeight());
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler as dimensões da imagem enviada.");
        }
    }

    private record DimensaoImagem(int largura, int altura) {
    }
}
