package com.ve.bc.openbanking.service;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ve.bc.openbanking.dto.ContratoResponse;
import com.ve.bc.openbanking.dto.ErrorResponse;
import com.ve.bc.openbanking.dto.ResponseContratoCts;
import com.ve.bc.openbanking.dto.RespuestaConError;
import com.ve.bc.openbanking.dto.ContratoRequest;

import com.ve.bc.openbanking.exception.ResourceErroNoFoundServicesException;
import com.ve.bc.openbanking.repo.ContratoRepository;

@Component
public class ContratoServices {

	@Autowired
	ContratoRepository contratoRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(ContratoServices.class);
	

	public ResponseEntity<?> getConsulta(ContratoRequest request, String tracerId) {
		ResponseContratoCts responseContratoCts = new ResponseContratoCts();
		Map<String,String> error = new HashMap<>();
		
		responseContratoCts = contratoRepository.getConsultaCotrato(request, tracerId);

			if (responseContratoCts.getErrorConsulta().getStatus().equals(Boolean.FALSE)) {	
				HttpHeaders headers = new HttpHeaders();
		        headers.add("X-Request-Id", tracerId);
		        
		        return ResponseEntity.ok()
                .headers(headers)
                .body(responseContratoCts.getDatosContrato());				
			} else {
				ErrorResponse errorDto = new ErrorResponse();
				errorDto.setCodigoError(responseContratoCts.getErrorConsulta().getCodigoError());
				errorDto.setDescripcionError(responseContratoCts.getErrorConsulta().getDescripcionError());	           
				return new ResponseEntity<ErrorResponse>(errorDto, HttpStatus.CONFLICT);
			}

	}

}
