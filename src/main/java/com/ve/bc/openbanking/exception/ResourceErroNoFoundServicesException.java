package com.ve.bc.openbanking.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ResourceErroNoFoundServicesException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String nombreDelRecurso;

	public ResourceErroNoFoundServicesException(String nombreDelRecurso) {
		super(String.format("'%s'", nombreDelRecurso));
		this.nombreDelRecurso = nombreDelRecurso;	
	}

	public String getNombreDelRecurso() {
		return nombreDelRecurso;
	}

	public void setNombreDelRecurso(String nombreDelRecurso) {
		this.nombreDelRecurso = nombreDelRecurso;
	}


	

}
