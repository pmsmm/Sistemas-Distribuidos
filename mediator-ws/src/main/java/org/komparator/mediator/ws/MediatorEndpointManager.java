package org.komparator.mediator.ws;

import java.io.IOException;

import javax.xml.ws.Endpoint;
import java.util.Timer;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import org.komparator.security.domain.Security;

/** End point manager */
public class MediatorEndpointManager {

	private Timer timer=null;

	private boolean primary=false;

	/** UDDI naming server location */
	private String uddiURL = null;
	/** Web Service name */
	private String wsName = null;

	/** Get Web Service UDDI publication name */
	public String getWsName() {
		return wsName;
	}

	/** Web Service location to publish */
	private String wsURL = null;

	/** Port implementation */
// TODO uncomment after port implementation is done
	private MediatorPortImpl portImpl = new MediatorPortImpl(this);

	/** Obtain Port implementation */
// TODO uncomment after port implementation is done
	public MediatorPortImpl getPort() {
	        return portImpl;
	}
	public void setTimer(Timer ti){
		this.timer= ti;
	}


	/** Web Service endpoint */
	private Endpoint endpoint = null;
	/** UDDI Naming instance for contacting UDDI server */
	private UDDINaming uddiNaming = null;

	/** Get UDDI Naming instance for contacting UDDI server */
	UDDINaming getUddiNaming() {
		return uddiNaming;
	}

	/** output option **/
	private boolean verbose = false;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public Endpoint getEndpoint(){
		return endpoint;
	}

	public String getWsURL(){
		return wsURL;
	}

	public boolean isPrimary(){
		return primary;
	}
	public void setPrimary(boolean bool){
		this.primary=bool;
	}
	/** constructor with provided UDDI location, WS name, and WS URL */
	public MediatorEndpointManager(String uddiURL, String wsName, String wsURL) {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
		buildSecurity();	
	}

	/** constructor with provided web service URL */
	public MediatorEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
		buildSecurity();
		
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
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
		publishToUDDI();
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
		this.portImpl = null;
		unpublishFromUDDI();
		timer.cancel();
        timer.purge();
	}

	/* UDDI */

	public void publishToUDDI() throws Exception {
		try {
			// publish to UDDI
			if (uddiURL != null) {
				if (verbose) {
					System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
			
					uddiNaming = new UDDINaming(uddiURL);
					uddiNaming.rebind(wsName, wsURL);
				}
			}
		} catch (Exception e) {
			uddiNaming = null;
			if (verbose) {
				System.out.printf("Caught exception when binding to UDDI: %s%n", e);
			}
			throw e;
		}
	}

	public void unpublishFromUDDI() {
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(wsName);
				if (verbose) {
					System.out.printf("Unpublished '%s' from UDDI%n", wsName);
				}
				uddiNaming = null;
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when unbinding: %s%n", e);
			}
		}
	}

	public void buildSecurity(){
		Security sec = Security.getInstance();
		sec.setName("T24_Mediator");
		sec.setCaCert("src/main/resources/ca.cer");
		sec.setMineCertPath("src/main/resources/T24_Mediator.cer");
		sec.setKeyStorePath("src/main/resources/T24_Mediator.jks");
		sec.setKSPass("U4yAXsQ4");
		sec.setKeyAlias("t24_mediator");
		sec.setKeyPass("U4yAXsQ4");
		sec.setMineCert("T24_Mediator");
	}
}
