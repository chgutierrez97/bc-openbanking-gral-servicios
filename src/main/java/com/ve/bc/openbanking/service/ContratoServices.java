package com.ve.bc.openbanking.service;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ve.bc.openbanking.dto.ContratoResponse;
import com.ve.bc.openbanking.dto.ResponseContratoCts;
import com.ve.bc.openbanking.dto.RespuestaConError;
import com.ve.bc.openbanking.dto.ValiContratoRequest;

import com.ve.bc.openbanking.exception.ResourceErroNoFoundServicesException;
import com.ve.bc.openbanking.repo.ContratoRepository;

@Component
public class ContratoServices {

	@Autowired
	ContratoRepository contratoRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(ContratoServices.class);
	

	public ResponseEntity<?> getConsulta(ValiContratoRequest request, String tracerId) {
		ResponseContratoCts responseContratoCts = new ResponseContratoCts();
		Map<String,String> error = new HashMap<>();
		
		responseContratoCts = contratoRepository.getConsultaCotrato(request, tracerId);

			if (responseContratoCts.getErrorConsulta().getStatus().equals(Boolean.FALSE)) {					
				return new ResponseEntity<ContratoResponse>(responseContratoCts.getDatosContrato(), HttpStatus.OK);
			} else {
	            error.put("codigoError", responseContratoCts.getErrorConsulta().getCodigoError());
	            error.put("descripcionError", responseContratoCts.getErrorConsulta().getDescripcionError());
				return new ResponseEntity<Map<String,String>>(error, HttpStatus.NOT_FOUND);
			}

	}

}
