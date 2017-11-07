package org.komparator.mediator.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.supplier.ws.cli.SupplierClient;



public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	private static final int NR_SUPPLIERS = 2;
	protected static MediatorClient mediatorClient;
	protected static SupplierClient client1;
	protected static SupplierClient client2;
	protected static SupplierClient[] supplierClients = new SupplierClient[NR_SUPPLIERS];
	protected static String[] supplierNames = new String[NR_SUPPLIERS];




	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		testProps = new Properties();
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Loaded test properties:");
			System.out.println(testProps);
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		String uddiEnabled = testProps.getProperty("uddi.enabled");
		String uddiURL = testProps.getProperty("uddi.url");
		String wsName = testProps.getProperty("ws.name");
		String wsURL = testProps.getProperty("ws.url");

		if ("true".equalsIgnoreCase(uddiEnabled)) {
			mediatorClient = new MediatorClient(uddiURL, wsName);
		} else {
			mediatorClient = new MediatorClient(wsURL);
		}
		client1 = new SupplierClient("http://localhost:8081/supplier-ws/endpoint");
		client2 = new SupplierClient("http://localhost:8082/supplier-ws/endpoint");
		supplierClients[0]=client1;
		supplierClients[1]=client2;
		supplierNames[0]="T24_Supplier1";
		supplierNames[1]="T24_Supplier2";
	}

	@AfterClass
	public static void cleanup() {
	}

}