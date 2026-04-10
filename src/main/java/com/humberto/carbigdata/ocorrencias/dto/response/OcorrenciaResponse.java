package com.humberto.carbigdata.ocorrencias.dto.response;

import com.humberto.carbigdata.ocorrencias.model.enums.StatusOcorrencia;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class OcorrenciaResponse {
  private Long codOcorrencia;
  private ClienteResponse cliente;
  private EnderecoResponse endereco;
  private LocalDate dtaOcorrencia;
  private StatusOcorrencia staOcorrencia;
  private List<FotoResponse> fotos;

  public OcorrenciaResponse(){}
}
