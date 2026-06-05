package com.humberto.api.ocorrencias.controller;

import com.humberto.api.ocorrencias.dto.request.OcorrenciaRequest;
import com.humberto.api.ocorrencias.dto.response.FotoResponse;
import com.humberto.api.ocorrencias.dto.response.OcorrenciaResponse;
import com.humberto.api.ocorrencias.service.FotoOcorrenciaService;
import com.humberto.api.ocorrencias.service.OcorrenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ocorrencias")
@RequiredArgsConstructor
@Tag(name = "Ocorrências")
public class OcorrenciaController {

  private final OcorrenciaService ocorrenciaService;
  private final FotoOcorrenciaService fotoOcorrenciaService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Cadastrar ocorrência com cliente e endereço")
  public ResponseEntity<OcorrenciaResponse> cadastrar(
    @RequestPart("dados") @Valid OcorrenciaRequest request,
    @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos) {

    OcorrenciaResponse response = ocorrenciaService.cadastrar(request, fotos);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(response.getCodOcorrencia()).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping
  @Operation(summary = "Listar ocorrências com filtros e paginação")
  public ResponseEntity<Page<OcorrenciaResponse>> listar(
    @Parameter(description = "Filtrar por nome do cliente")
    @RequestParam(required = false) String nmeCliente,

    @Parameter(description = "Filtrar por CPF do cliente")
    @RequestParam(required = false) String nroCpf,

    @Parameter(description = "Filtrar por data da ocorrência (yyyy-MM-dd)")
    @RequestParam(required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaOcorrencia,

    @Parameter(description = "Filtrar por cidade da ocorrência")
    @RequestParam(required = false) String nmeCidade,

    @ParameterObject @PageableDefault(size = 20, sort = "dtaOcorrencia")
    Pageable pageable) {
      return ResponseEntity.ok(
        ocorrenciaService.listar(nmeCliente, nroCpf, dtaOcorrencia, nmeCidade, pageable)
      );
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar ocorrência por Id")
  public ResponseEntity<OcorrenciaResponse> buscarPorId(@PathVariable Long id) {
    return ResponseEntity.ok(ocorrenciaService.buscarPorId(id));
  }

  @PostMapping(value = "/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Adicionar fotos a uma ocorrência existente (não finalizada)")
  public ResponseEntity<List<FotoResponse>> adicionarFotos(
    @PathVariable Long id,
    @RequestPart("fotos") List<MultipartFile> fotos) {

    List<FotoResponse> response = fotoOcorrenciaService.adicionarFotos(id, fotos);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}/finalizar")
  @Operation(summary = "Finalizar ocorrência")
  public ResponseEntity<OcorrenciaResponse> finalizar(@PathVariable Long id) {
    return ResponseEntity.ok(ocorrenciaService.finalizar(id));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover ocorrência, caso não esteja finalizada")
  public ResponseEntity<Void> deletar(@PathVariable Long id) {
    ocorrenciaService.deletar(id);
    return ResponseEntity.noContent().build();
  }
}
