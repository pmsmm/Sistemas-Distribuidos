package org.komparator.security.handler;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;
import java.security.spec.*;
import org.w3c.dom.*;

import org.komparator.security.CryptoUtil;
import java.lang.RuntimeException;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPBody;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.security.cert.Certificate;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import org.komparator.security.domain.Security;
import javax.xml.bind.DatatypeConverter;


 /**
 * This SOAPHandler adds a header with the ciphered creditcard number if inbound message and 
 *decipher the creditcard number in inbound message with private key
 *
 * A header is created in an outbound message and is read on an inbound message.
 *
 * The value that is read from the header is placed in a SOAP message context
 * property that can be accessed by other handlers or by the application.
 */

public class CipherHandler implements SOAPHandler<SOAPMessageContext> {

	

	public static final String OPERATION_NAME_TO_CIPHER = "buyCart"; 
	public static final String NAME_OF_SECRET_ARGUMENT = "creditCardNr"; 
	final static String CA_CERTIFICATE = "ca.cer";
	// Handler interface implementation
	//

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		//System.out.println("CipherCCNumberHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try{
			if (outboundElement.booleanValue()) { /** Outbound message*/
				System.out.println("Ciphering CC number in outbound SOAP message...");
				
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();

				SOAPBody sb = se.getBody();

				//Obtain name of the service and operation
				QName svcn = (QName) smc.get(MessageContext.WSDL_SERVICE);
				QName opn = (QName) smc.get(MessageContext.WSDL_OPERATION);

				//If soap message doesnt have the specific argument there 
				//is nothing to cipher so continues to the next handler
				if (!opn.getLocalPart().equals(OPERATION_NAME_TO_CIPHER)) {return true;}

				//Obtain nodes of the messages
				NodeList children = sb.getFirstChild().getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node argument = children.item(i);
					//for each node verify if it corresponds to the argument to cipher
					if (argument.getNodeName().equals(NAME_OF_SECRET_ARGUMENT)) {
						String secretArgument = argument.getTextContent();
						byte[] noTencoded = Base64.getDecoder().decode(secretArgument.getBytes());

						//contac CA entity to get public certificate
						//verify of certificate was assigned by the CA
						CAClient ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca");
                		String cert = ca.getCertificate("T24_mediator");
                		Certificate certificate = CryptoUtil.getX509CertificateFromPEMString(cert);
                		Certificate caCertificate = CryptoUtil.getX509CertificateFromResource(CA_CERTIFICATE);
                		boolean result = CryptoUtil.verifySignedCertificate(certificate, caCertificate);
                		if(!result){
                			throw new RuntimeException("public certificate was not assigned by the CA ... rejecting message");
    					}
    					//get public key from certificate
    					PublicKey publicKey = certificate.getPublicKey();	
						// cipher message with assymmetric key
						byte[] cipheredArgument = CryptoUtil.asymCipher(noTencoded, publicKey); 
						String encoded = Base64.getEncoder().encodeToString(cipheredArgument);
					
						//save changes to message soap
						argument.setTextContent(encoded);
						msg.saveChanges();

						return true;
					}
	
				}

			} else { /**Inbound message*/
				System.out.println("Deciphering CC number in inbound SOAP message...");

				Security sec = Security.getInstance();
				System.out.println(sec.getName());
				
				

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = se.getBody();

				//Obtains name of the service and operation
				QName svcn = (QName) smc.get(MessageContext.WSDL_SERVICE);
				QName opn = (QName) smc.get(MessageContext.WSDL_OPERATION);

				//If soap message doesnt have the specific argument there is nothing 
				//to decipher so continues to the next handler
				if (!opn.getLocalPart().equals(OPERATION_NAME_TO_CIPHER)) {return true;}

				//Obtain nodes of the message
				NodeList children = sb.getFirstChild().getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node argument = children.item(i);
					//for each node verify if it corresponds to the argument to decipher
					if (argument.getNodeName().equals(NAME_OF_SECRET_ARGUMENT)) {
						String secretArgument = argument.getTextContent();
						
						byte[] encoded = Base64.getDecoder().decode(secretArgument.getBytes());
						
						//gets privateKey of the application
						PrivateKey privateKey = CryptoUtil.getPrivateKeyFromKeyStoreFile(sec.getKeyStorePath(), 
							sec.getKSPass().toCharArray(), sec.getKeyAlias(), sec.getKeyPass().toCharArray());
						// decipher message with assymmetric key
						
						byte[] decipheredArgument = CryptoUtil.asymDecipher(encoded, privateKey);
						String decoded = Base64.getEncoder().encodeToString(decipheredArgument);
					

						//save changes to message soap
						argument.setTextContent(decoded);
						msg.saveChanges();

						return true;
					}
					
				}
			}
		} catch (Exception e) {
			System.out.println("Continue normal processing..." + e);
		}

		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}
}
