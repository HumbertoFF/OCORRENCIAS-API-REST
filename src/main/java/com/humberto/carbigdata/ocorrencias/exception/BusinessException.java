package com.humberto.carbigdata.ocorrencias.exception;

public class BusinessException extends RuntimeException {
  public BusinessException(String mensagem) {
    super(mensagem);
  }
}

