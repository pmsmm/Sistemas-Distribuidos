package org.komparator.mediator.domain;

import org.komparator.security.domain.Security;
import java.util.TimerTask;
import java.util.Timer;
import javax.xml.ws.WebServiceException;
import java.util.Date;
import org.komparator.mediator.domain.Mediator;
import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;
import org.komparator.mediator.ws.MediatorEndpointManager;

//class that exetends TimerTask and repetes task imAlive() with configurable time duration 
public class LifeProof extends TimerTask {
	//wsURL of client
	private static final String MEDIATOR_2 ="http://localhost:8072/mediator-ws/endpoint";
	//** Web Service endpoint */
	private MediatorEndpointManager medManager= null;
	private Timer timeer=null;
	private MediatorClient mClient=null;


	public LifeProof(MediatorEndpointManager mEm, Timer time){
		try{
			this.medManager=mEm;
			this.timeer = time;
			this.mClient = new MediatorClient(MEDIATOR_2);
		}catch(MediatorClientException e){
			System.out.println(e);
		}
	}	

	public void run() {

		try{
			//if primary mediator , send life proof to secundary mediator
			if(medManager.isPrimary()){
				mClient.imAlive();
				System.out.println("LifeProof sent");
	
			}else{
				//if secundary mediator , compares last date of life proof 
				//of primary mediator with current date
				Mediator mediator = Mediator.getInstance();
				Date date = new Date();
				if(!mediator.noDates()){
        			Date lastDate = mediator.getLastDate();
        			float dif = (date.getTime()-lastDate.getTime());

        			//if time difference is bigger than 5 seconds means that primary mediator is dead
        			//secundary mediator publish on uddi and replace primary mediator
        			if (dif> 5500){
        				timeer.cancel();
        				timeer.purge();
        				medManager.setVerbose(true);
        				medManager.publishToUDDI();
        				medManager.setPrimary(true);
        				System.out.println("Published on uddi with sucess");
        			}
				}
			}
		}catch(WebServiceException e) {
			System.out.println("Secundary mediator isnt UP yet...");
			System.out.println("Trying again...");
		}catch(Exception e){
			System.out.println(e);
		}    
    }
}