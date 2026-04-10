package com.humberto.carbigdata.ocorrencias.repository;

import com.humberto.carbigdata.ocorrencias.model.FotoOcorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoOcorrenciaRepository extends JpaRepository<FotoOcorrencia, Long> {
  List<FotoOcorrencia> findByOcorrencia_CodOcorrencia(Long codOcorrencia);
}
