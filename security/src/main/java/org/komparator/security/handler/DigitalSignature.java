
package org.komparator.security.handler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.security.cert.Certificate;

import java.io.*;
import java.nio.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;
import java.security.spec.*;
import org.w3c.dom.*;


import org.komparator.security.CryptoUtil;
import org.komparator.security.*;
import java.lang.RuntimeException;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import org.komparator.security.domain.Security;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext.Scope;

// This SOAPHandler add a header containing the digital signaature of the sender
//in the inbound message the signature is verified with the corresponding public key
//this allows the servers and the clients to send messages with authenticy and integrity
/* * A header is created in an outbound message and is read on an inbound message.
 *
 * The value that is read from the header is placed in a SOAP message context
 * property that can be accessed by other handlers or by the application.*/

public class DigitalSignature implements SOAPHandler<SOAPMessageContext> {

	
	// Handler interface implementation
  
    private static final String SIGNATURE_ALGO = "SHA256withRSA";
    final static String CA_CERTIFICATE = "ca.cer";


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
        //System.out.println("AddHeaderHandler: Handling message in DigitalSignature handler");

        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (outboundElement.booleanValue()) {
                System.out.println("Making digital signature...in Outbound message");
                
                Security sec = Security.getInstance();
                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPBody sb = se.getBody();

                // add header
                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();

                //add header element containg the wsName of the application
                //thats is sending the message
                Name name2 = se.createName("AppEntity", "s1", "http:demo2");
                SOAPHeaderElement element2 = sh.addHeaderElement(name2);
                element2.addTextNode(sec.getMineCert());
                msg.saveChanges();

                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                msg.writeTo(byteOut);

                //gets private key with help of the Security class (manager)
                PrivateKey privateKey = CryptoUtil.getPrivateKeyFromKeyStoreFile(sec.getKeyStorePath(), 
                    sec.getKSPass().toCharArray(), sec.getKeyAlias(), sec.getKeyPass().toCharArray()); 
                byte[] sign = CryptoUtil.makeDigitalSignature(SIGNATURE_ALGO, privateKey,byteOut.toByteArray());
                
                String encoded = Base64.getEncoder().encodeToString(sign);
               
               // add header element containing the signed message
                Name name = se.createName("SignedMsg", "d1", "http://demo");
                SOAPHeaderElement element = sh.addHeaderElement(name);
                element.addTextNode(encoded);

                

            } else {
                System.out.println("Verifying digital signature...in Inbound message");

                // get SOAP envelope header
                SOAPMessage msg = smc.getMessage();

                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPBody sb = se.getBody();

                SOAPHeader sh = se.getHeader();

                // check header
                if (sh == null) {
                    return true;
                }

                // gets header element that contains the signed message
                Name name = se.createName("SignedMsg", "d1", "http://demo");
                Iterator it = sh.getChildElements(name);
                // check header element
                if (!it.hasNext()) {
                    return true;
                }
                SOAPElement element = (SOAPElement) it.next();
                String signedMsg = element.getValue();
                element.getParentNode().removeChild(element); 
                msg.saveChanges();
                byte[] encoded = Base64.getDecoder().decode(signedMsg.getBytes());
        
                
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                msg.writeTo(byteOut);

                //gets header element that contains the wsName of the application
                //wsName used to get public certificate
                Name name2=se.createName("AppEntity", "s1", "http:demo2");
                Iterator it2 = sh.getChildElements(name2);
                // check header element
                if (!it2.hasNext()) {
                    return true;
                }
                SOAPElement element2=(SOAPElement) it2.next();
                String sender = element2.getValue();
        
                //contacts CA to obtain public certficate of the application that sent the message
                //uses the publicKey obtain from the certificate to verify the assignature
                Security sec = Security.getInstance();
                CAClient ca = new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca");
                String cert = ca.getCertificate(sender);
                Certificate certificate = CryptoUtil.getX509CertificateFromPEMString(cert);
                Certificate caCertificate = CryptoUtil.getX509CertificateFromResource(CA_CERTIFICATE);
                boolean result = CryptoUtil.verifySignedCertificate(certificate, caCertificate);
                if(!result){
                    throw new RuntimeException("public certificate was not assigned by the CA ... rejecting message");
                }
                boolean verify = CryptoUtil.verifyDigitalSignature(SIGNATURE_ALGO, 
                    certificate, byteOut.toByteArray(),encoded);

                if(!verify){
                    throw new RuntimeException("failed veryfing assignature... rejecting message");
                }
                if(verify){
                    System.out.println("digital signature verified with sucess");
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