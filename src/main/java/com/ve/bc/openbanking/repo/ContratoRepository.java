package com.ve.bc.openbanking.repo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ve.bc.openbanking.dto.ContratoResponse;
import com.ve.bc.openbanking.dto.ResponseContratoCts;
import com.ve.bc.openbanking.dto.RespuestaConError;
import com.ve.bc.openbanking.dto.ContratoRequest;


import com.ve.bc.openbanking.exception.ResourceErroServicesException;


@Repository
public class ContratoRepository {

	@Value("${url.servi.consulta}")
    String UrlCccte;
	@Value("${api.contrato.canal}")
	String Canal;
	
	@Value("${api.ssl.status}")
    Boolean statusMetodo;
	@Value("${api.ssl.certif.name}")
    String certifName;
	
	@Autowired
	RestTemplate template;
	
				
			
	private static final Logger LOGGER = LoggerFactory.getLogger(ContratoRepository.class);

	public ResponseContratoCts getConsultaCotrato(ContratoRequest valiAfiliacionRequest, String tracerId) {
		ResponseContratoCts responseContratoCts = new ResponseContratoCts();
		LOGGER.info("Start contratoRepository  : getConsultaCotrato  RequestId :" + tracerId);
		if(statusMetodo) {
			responseContratoCts = getConsultaContratosCtsSsl(valiAfiliacionRequest, tracerId);
		}else {
			responseContratoCts = getConsultaContratosCts(valiAfiliacionRequest, tracerId);
		}		 
		LOGGER.info("End  contratoRepository  : getConsultaCotrato  RequestId :" + tracerId);
		return responseContratoCts;
	}

	public ResponseContratoCts getConsultaContratosCts(ContratoRequest requestContratos, String tracerId) {

		LOGGER.info("Start contratoRepository  : getConsultaContratosCts  RequestId :" + tracerId);
		ContratoResponse contratoResponse = new ContratoResponse();
		ResponseContratoCts valiContratosResponse = new ResponseContratoCts();
		RespuestaConError errorConsulta = new RespuestaConError();
		URL url = null;
		URLConnection connection = null;
		HttpURLConnection httpConn = null;
		String responseString = null;
		String outputString = "";
		OutputStream out = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		String operacion = "ser:ValidarContrato";
	

		String xmlInput =  "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://service.afiliacion.general.openbanking.ecobis.cobiscorp\" xmlns:dto2=\"http://dto2.sdf.cts.cobis.cobiscorp.com\" xmlns:dto21=\"http://dto2.commons.ecobis.cobiscorp\" xmlns:dto=\"http://dto.payload.afiliacion.general.openbanking.ecobis.cobiscorp\">\r\n"
				+ ""
				+ " <soapenv:Header/>" + "<soapenv:Body>" + "<"+ operacion +">" + "<ser:inContratoRequest>"
				+ "<dto:cedruc>" + requestContratos.getClienteRIF() + "</dto:cedruc>" + "<dto:hash>"
				+ requestContratos.getClienteHash() + "</dto:hash>" + "<dto:canal>"
				+ Canal + "</dto:canal>" + "</ser:inContratoRequest>" + "</"+operacion
				+">" + "</soapenv:Body>" + "</soapenv:Envelope>";
		
		
		 
		try {
			url = new URL(UrlCccte);
			connection = url.openConnection();

			httpConn = (HttpURLConnection) connection;

			byte[] buffer = new byte[xmlInput.length()];
			buffer = xmlInput.getBytes();
			
			String SOAPAction = "";
			// Set the appropriate HTTP parameters.
			httpConn.setRequestProperty("Content-Length", String.valueOf(buffer.length));
			httpConn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
			httpConn.setRequestProperty("SOAPAction", SOAPAction);
			// httpConn.setRequestProperty ("Authorization", "Basic " + Llave);
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			out = httpConn.getOutputStream();
			out.write(buffer);
			out.close();

			// Read the response and write it to standard out.
			isr = new InputStreamReader(httpConn.getInputStream());
			in = new BufferedReader(isr);

			while ((responseString = in.readLine()) != null) {
				outputString = outputString + responseString;
			}
			
			// Get the response from the web service call
			Document document = parseXmlFile(outputString);

			document.getDocumentElement().normalize();

			NodeList nodeLst = document.getElementsByTagName("ns3:success");
			String Status = nodeLst.item(0).getTextContent();

			if (Boolean.valueOf(Status)) {
				
			
				NodeList nodeLstEnte = document.getElementsByTagName("ns2:ente");
				String Ente = nodeLstEnte.item(0).getTextContent();
				//contratoResponse.setEnte(Integer.valueOf(Ente));
				NodeList nodeLstEstado = document.getElementsByTagName("ns2:estado");
				String Estado = nodeLstEstado.item(0).getTextContent();
				contratoResponse.setEstado(Estado);
				
				errorConsulta.setStatus(Boolean.FALSE);
				valiContratosResponse.setDatosContrato(contratoResponse);
				valiContratosResponse.setErrorConsulta(errorConsulta);
				
				LOGGER.info("End  contratoRepository : getConsultaContratosCts  RequestId :" + tracerId);
				return valiContratosResponse;
			} else {
				
				NodeList nodeCod = document.getElementsByTagName("ns0:code");
				String Cod = nodeCod.item(0).getTextContent();
				NodeList nodeMsn = document.getElementsByTagName("ns0:message");
				String Mensaje = nodeMsn.item(0).getTextContent();

				errorConsulta.setCodigoError(Cod);
				errorConsulta.setDescripcionError(Mensaje);
				errorConsulta.setStatus(Boolean.TRUE);
				
				valiContratosResponse.setErrorConsulta(errorConsulta);
				
				LOGGER.info("End  contratoRepository : getConsultaContratosCts  RequestId :" + tracerId);
				return valiContratosResponse;
			}
		} catch (IOException e) {
			System.out.println(e.toString());

			LOGGER.info("End  contratoRepository : getConsultaContratosCts  RequestId :" + tracerId+" >>>>>>> "+e.toString());;
			throw new ResourceErroServicesException("contratoRepository", "getConsultaContratosCts");
		} catch (Exception e) {		
			LOGGER.info("End  contratoRepository : getConsultaContratosCts  RequestId :" + tracerId+" >>>>>>> "+e.toString());
			throw new ResourceErroServicesException("contratoRepository", "getConsultaContratosCts");
		}
	}

	//*********************************************************** - CON SERTIFICADO TSL - ***********************************************************************

	public ResponseContratoCts getConsultaContratosCtsSsl(ContratoRequest requestContratos, String tracerId) {

		LOGGER.info("Start contratoRepository  : getConsultaContratosCts  RequestId :" + tracerId);
		ContratoResponse contratoResponse = new ContratoResponse();
		ResponseContratoCts valiContratosResponse = new ResponseContratoCts();
		RespuestaConError errorConsulta = new RespuestaConError();
		URL url = null;
		HttpsURLConnection connection = null;
		HttpsURLConnection httpConn = null;
		String responseString = null;
		String outputString = "";
		OutputStream out = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		String operacion = "ser:ValidarContrato";
		Certificate ca;

		String xmlInput =  "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://service.afiliacion.general.openbanking.ecobis.cobiscorp\" xmlns:dto2=\"http://dto2.sdf.cts.cobis.cobiscorp.com\" xmlns:dto21=\"http://dto2.commons.ecobis.cobiscorp\" xmlns:dto=\"http://dto.payload.afiliacion.general.openbanking.ecobis.cobiscorp\">\r\n"
				+ ""
				+ " <soapenv:Header/>" + "<soapenv:Body>" + "<"+ operacion +">" + "<ser:inContratoRequest>"
				+ "<dto:cedruc>" + requestContratos.getClienteRIF() + "</dto:cedruc>" + "<dto:hash>"
				+ requestContratos.getClienteHash() + "</dto:hash>" + "<dto:canal>"
				+ Canal + "</dto:canal>" + "</ser:inContratoRequest>" + "</"+operacion
				+">" + "</soapenv:Body>" + "</soapenv:Envelope>";

		try {

			//LOGGER.info("Paso 2 "+xmlInput);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			LOGGER.info("Paso 3  " +certifName);
			InputStream caInput = new BufferedInputStream(new FileInputStream(certifName));
			LOGGER.info("Paso 4");
			ca = cf.generateCertificate(caInput);
			LOGGER.info("Paso 5");
			String keyStoreType = KeyStore.getDefaultType();
			LOGGER.info("Paso 6 " +keyStoreType);
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			LOGGER.info("Paso 7");
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);
			LOGGER.info("Paso 8");
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			//LOGGER.info("Paso 9 "+tmfAlgorithm);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			LOGGER.info("Paso 10");
			tmf.init(keyStore);
			SSLContext context = SSLContext.getInstance("TLS");
			LOGGER.info("Paso 11");
			context.init(null, tmf.getTrustManagers(), null);
			LOGGER.info("Paso 12");
			url = new URL(UrlCccte);
			LOGGER.info("Paso 13");
			connection = (HttpsURLConnection) url.openConnection();
			LOGGER.info("Paso 14");
			connection.setSSLSocketFactory(context.getSocketFactory());
			LOGGER.info("Paso 15");
			httpConn = (HttpsURLConnection) connection;
			LOGGER.info("Paso 16");
			byte[] buffer = new byte[xmlInput.length()];
			LOGGER.info("Paso 17");
			buffer = xmlInput.getBytes();
			LOGGER.info("Paso 18");

			String SOAPAction = "";

			httpConn.setRequestProperty("Content-Length", String.valueOf(buffer.length));
			httpConn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
			httpConn.setRequestProperty("SOAPAction", SOAPAction);
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			LOGGER.info("Paso 19");
			out = httpConn.getOutputStream();
			LOGGER.info("Paso 20");
			out.write(buffer);
			LOGGER.info("Paso 21");
			out.close();
			LOGGER.info("Paso 21");
			// Read the response and write it to standard out.
			isr = new InputStreamReader(httpConn.getInputStream());
			LOGGER.info("Paso 21");
			in = new BufferedReader(isr);
			LOGGER.info("Paso 22");
			while ((responseString = in.readLine()) != null) {
				outputString = outputString + responseString;
			}
			
			// Get the response from the web service call
			Document document = parseXmlFile(outputString);

			document.getDocumentElement().normalize();

			NodeList nodeLst = document.getElementsByTagName("ns3:success");
			String Status = nodeLst.item(0).getTextContent();
			LOGGER.info("Paso 23 "+Status);
			if (Boolean.valueOf(Status)) {
				
				LOGGER.info("Paso 24");
				
				
				NodeList nodeLstEnte = document.getElementsByTagName("ns2:ente");
				String Ente = nodeLstEnte.item(0).getTextContent();
				//contratoResponse.setEnte(Integer.valueOf(Ente));
				NodeList nodeLstEstado = document.getElementsByTagName("ns2:estado");
				String Estado = nodeLstEstado.item(0).getTextContent();
				contratoResponse.setEstado(Estado);
				
				errorConsulta.setStatus(Boolean.FALSE);
				valiContratosResponse.setDatosContrato(contratoResponse);
				valiContratosResponse.setErrorConsulta(errorConsulta);
				
				LOGGER.info("End  contratoRepository : getConsultaContratosCts  RequestId :" + tracerId);
				return valiContratosResponse;
			} else {
				LOGGER.info("Paso 25");
				NodeList nodeCod = document.getElementsByTagName("ns0:code");
				String Cod = nodeCod.item(0).getTextContent();
				NodeList nodeMsn = document.getElementsByTagName("ns0:message");
				String Mensaje = nodeMsn.item(0).getTextContent();

				errorConsulta.setCodigoError(Cod);
				errorConsulta.setDescripcionError(Mensaje);
				errorConsulta.setStatus(Boolean.TRUE);
				
				valiContratosResponse.setErrorConsulta(errorConsulta);
				
				LOGGER.info("End  contratoRepository : getConsultaContratosCts  RequestId :" + tracerId);
				return valiContratosResponse;
			}
		} catch (IOException e) {
			System.out.println(e.toString());
			LOGGER.info("End  contratoRepository : getConsultaContratosCts  RequestId :" + tracerId+" >>>>>>> "+e.toString());;
			throw new ResourceErroServicesException("contratoRepository", "getConsultaContratosCts");
		} catch (Exception e) {	
			LOGGER.info("Paso 27");
			LOGGER.info("End  contratoRepository : getConsultaContratosCts  RequestId :" + tracerId +" >>>>>>> "+e.toString());
			throw new ResourceErroServicesException("contratoRepository", "getConsultaContratosCts");
		}
	}

	private Document parseXmlFile(String in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
