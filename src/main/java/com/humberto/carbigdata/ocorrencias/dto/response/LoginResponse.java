package com.humberto.carbigdata.ocorrencias.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
  private String token;
  private String tipo;
  private long expiracaoMs;
}
