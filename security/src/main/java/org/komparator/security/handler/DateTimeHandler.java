package org.komparator.security.handler;

import java.util.Iterator;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.lang.RuntimeException;


import org.komparator.security.CryptoUtil;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This SOAPHandler add a header with a a date and time so that we can verify the freshness of the messages
 *
 * A header is created in an outbound message and is read on an inbound message.
 *
 * The value that is read from the header is placed in a SOAP message context
 * property that can be accessed by other handlers or by the application.
 */
public class DateTimeHandler implements SOAPHandler<SOAPMessageContext> {

	
	//
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
		//System.out.println("AddHeaderHandler: Handling message in DateTime handler");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) {
				System.out.println("Writing header (date and time) in outbound SOAP message...");

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();

				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				// add header element (name, namespace prefix, namespace)
				Name name = se.createName("DateHeader", "d", "http://demo");
				SOAPHeaderElement element = sh.addHeaderElement(name);

				// add header element value
    			Calendar calendar = Calendar.getInstance();
    			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
   				String valueString = dateFormat.format(calendar.getTime());
				element.addTextNode(valueString);


			} else {
				System.out.println("Reading header(date and time) in inbound SOAP message...");

				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					return true;
				}

				// get first header element
				Name name = se.createName("DateHeader", "d", "http://demo");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();

				// get header element value
				String valueString = element.getValue();
		
				SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
       			Date date = parser.parse(valueString);
       			Date now = new Date();
       			long seconds = (now.getTime()-date.getTime())/1000;
       			if(seconds >3){
       				throw new RuntimeException("time difference was > 3 seconds..rejecting message");
       			}
       			System.out.println("time difference was <= 3 seconds...continue processing");


			}
		
		}catch (Exception e) {
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
