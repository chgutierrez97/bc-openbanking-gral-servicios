package com.ve.bc.openbanking.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ve.bc.openbanking.dto.ContratoResponse;
import com.ve.bc.openbanking.dto.ErrorDetalles;
import com.ve.bc.openbanking.dto.ErrorResponse;
import com.ve.bc.openbanking.dto.ResponseContratoCts;
import com.ve.bc.openbanking.dto.ContratoRequest;
import com.ve.bc.openbanking.service.ContratoServices;
import com.ve.bc.openbanking.utils.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/afiliacionValidarContrato")

@Tag(name = "Validacion Afiliacion")
public class ConsultaGralContratosController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsultaGralContratosController.class);

	@Autowired
	Utils utils;
	
	@Autowired
	ContratoServices contratoServices;  


	@Operation(summary = "${api.doc.summary.cotra.contr}", description = "${api.doc.description.cotra.contr}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK",
					content = {
							@Content(mediaType = "application/json",
							schema = @Schema(implementation = ContratoResponse.class)		)					
							}),
			@ApiResponse(responseCode = "400", description = "Bad Request",
					content = {
							@Content(mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class)		)					
							}),
			@ApiResponse(responseCode = "401", description = "Unauthorized",
			content = {
					@Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)		)					
					}),
			@ApiResponse(responseCode = "409", description = "Conflict",
			content = {
					@Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)		)					
					}),
			@ApiResponse(responseCode = "500", description = "Internal Server Error",
			content = {
					@Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)		)					
					})
	})
	@PostMapping
	public ResponseEntity<?> getCosultaContratos(@RequestHeader(value = "X-Request-IP", required = true) String ip, @RequestHeader(value = "X-Request-Id", required = false) String tracerId,
			@Valid @RequestBody ContratoRequest request, HttpServletResponse response){
		
		if (tracerId == null || tracerId == ""){
			tracerId = utils.generarCodigoTracerId();
		}
			LOGGER.info("Start ConsultaAfiliacionController : getCosultaAfiliacion  RequestId :" + tracerId);
			LOGGER.info("ConsultaGralContratosController Direccion IP : " + ip);
			ResponseEntity<?> valiContratosResponse = contratoServices.getConsulta(request, tracerId);
			LOGGER.info(" End  ConsultaAfiliacionController : getCosultaAfiliacion  RequestId :" + tracerId);
			response.setHeader("X-Request-Id", tracerId);
		return valiContratosResponse;
		
	}

}