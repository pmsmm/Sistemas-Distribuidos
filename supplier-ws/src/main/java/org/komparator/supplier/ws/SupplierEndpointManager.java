package org.komparator.supplier.ws;

import java.io.IOException;
import java.io.*;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import org.komparator.security.domain.Security;


/** End point manager */
public class SupplierEndpointManager {

	/** Web Service location to publish */
	private String wsURL = null;

	private UDDINaming uddiNaming = null;
	private String uddiURL = null;
	private String name = null;


	/** Port implementation */
	private SupplierPortImpl portImpl = new SupplierPortImpl(this);

// TODO
//	/** Obtain Port implementation */
	public SupplierPortType getPort() {
		return portImpl;
	}

	/** Web Service end point */
	private Endpoint endpoint = null;

	/** output option **/
	private boolean verbose = true;


	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL **/
	public SupplierEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
	}

	public SupplierEndpointManager(String wsURL, String uddiURL, String name){
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;

		if(uddiURL == null)
			throw new NullPointerException("uddi URL cannot be null");
		this.uddiURL = uddiURL;

		if(name == null)
			throw new NullPointerException("name cannot be null");
		this.name = name;
		buildSecurity(name);


	}

	/* end point management */

	public void start() throws Exception {
		try {
			// publish end point
			endpoint = Endpoint.create(this.portImpl);
			if (verbose) {
				System.out.printf("Starting %s%n", wsURL);
			}
			endpoint.publish(wsURL);
			
			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(name, wsURL);

		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}

	public void stop() throws Exception {
		try {
			if (endpoint != null) {
				// stop end point
				endpoint.stop();
				if (verbose) {
					System.out.printf("Stopped %s%n", wsURL);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}

		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(name);
				System.out.printf("Deleted '%s' from UDDI%n", name);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when deleting: %s%n", e);
		}

		this.portImpl = null;
	}

	public void buildSecurity(String name){
		Security sec = Security.getInstance();
		sec.setName(name);
		sec.setCaCert("src/main/resources/ca.cer");
		sec.setMineCertPath("src/main/resources/"+name+".cer");
		sec.setKeyStorePath("src/main/resources/"+name+".jks");
		sec.setKSPass("U4yAXsQ4");
		sec.setKeyAlias(name.toLowerCase());
		sec.setKeyPass("U4yAXsQ4");
		sec.setMineCert(name);

		System.out.println("ready to go!! supplier");

	}

}
