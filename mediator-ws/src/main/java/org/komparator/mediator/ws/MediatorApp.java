package org.komparator.mediator.ws;
import java.util.Timer;
import org.komparator.mediator.domain.LifeProof;


public class MediatorApp {

	public static final String MEDIATOR_1 ="http://localhost:8071/mediator-ws/endpoint";

	public static void main(String[] args) throws Exception {

		
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;

		// Create server implementation object, according to options
		MediatorEndpointManager endpoint = null;
		if (args.length == 1 ){
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL);

		
		} else if (args.length >= 3) {
			uddiURL = args[1];
			wsName = args[2];
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL);
			if (args[0].equals(MEDIATOR_1)){
				//only the primary mediator publish on uddi when initializing
            	endpoint.setVerbose(true);
            	endpoint.setPrimary(true);
            	System.out.println("Im the primary Mediator");
			}
			else{
				endpoint.setPrimary(false);
				System.out.println("Im the secundary Mediator");
			}
		}
		


		try {
			endpoint.start();
			//activity that executes in an independent way
			//life proofs from primary mediator to secundary mediator
			Timer timer = new Timer(/*isDaemon*/ true);
			timer.schedule(new LifeProof(endpoint,timer), 10000,5000);
			endpoint.setTimer(timer);

			endpoint.awaitConnections();
			
		} finally {
			endpoint.stop();
		}

	}

}
