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
import com.ve.bc.openbanking.dto.ValiContratoRequest;
import com.ve.bc.openbanking.service.ContratoServices;
import com.ve.bc.openbanking.utils.Utils;

import io.swagger.v3.oas.annotations.Operation;
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
	@PostMapping//("/validarContrato")
	public ResponseEntity<?> getCosultaContratos(@RequestHeader(value = "X-Request-IP", required = true) String ip, @RequestHeader(value = "X-Request-Id", required = false) String tracerId,
			@Valid @RequestBody ValiContratoRequest request, HttpServletResponse response){
		
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