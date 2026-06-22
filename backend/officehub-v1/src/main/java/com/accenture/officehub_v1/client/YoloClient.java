package com.accenture.officehub_v1.client;

import com.accenture.officehub_v1.config.YoloProperties;
import com.accenture.officehub_v1.dto.ia.yolo.YoloResponseDto;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class YoloClient {

    private final YoloProperties yoloProperties;

    public YoloResponseDto detect(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new RegraNegocioException("A imagem da planta baixa é obrigatória.");
        }

        byte[] bytes;
        try {
            bytes = imagem.getBytes();
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler o arquivo da planta baixa.");
        }

        String filename = imagem.getOriginalFilename() != null ? imagem.getOriginalFilename() : "planta.jpg";
        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(yoloProperties.connectTimeoutMs());
        requestFactory.setReadTimeout(yoloProperties.readTimeoutMs());

        RestClient restClient = RestClient.builder()
                .baseUrl(yoloProperties.baseUrl())
                .requestFactory(requestFactory)
                .build();

        try {
            YoloResponseDto response = restClient.post()
                    .uri("/detect")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(YoloResponseDto.class);

            if (response == null) {
                throw new RegraNegocioException("O serviço YOLO retornou uma resposta vazia.");
            }

            return response;
        } catch (RestClientException ex) {
            throw new RegraNegocioException(
                    "Falha ao comunicar com o serviço YOLO. Verifique se a API Python está em execução em "
                            + yoloProperties.baseUrl());
        }
    }
}
