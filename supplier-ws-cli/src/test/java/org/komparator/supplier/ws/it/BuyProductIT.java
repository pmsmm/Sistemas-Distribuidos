package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;


import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;

import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.PurchaseView;

import java.util.ArrayList;
import java.util.List;



/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() {
		// clear remote service state before all tests
		client.clear();

	}


	@AfterClass
	public static void oneTimeTearDown() {
		// clear remote service state after all tests
		client.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {

			client.clear();

		// fill-in test products
		{
			ProductView product = new ProductView();
			product.setId("BbBb123");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(50);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("A");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
	
		{
			ProductView product = new ProductView();
			product.setId("1C2");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("olaola");
			product.setDesc("Tennis;;ball");
			product.setPrice(173);
			product.setQuantity(8);
			client.createProduct(product);
		}
		
	
	}
	@After
	public void tearDown() {
	}

	// tests
	// assertEquals(expected, actual);

	// public String buyProduct(String productId, int quantity)
	// throws BadProductId_Exception, BadQuantity_Exception,
	// InsufficientQuantity_Exception {

	// bad input tests
	

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNullTest() 
		throws BadProductId_Exception, BadQuantity_Exception,InsufficientQuantity_Exception {	
		client.buyProduct(null, 2);
	}


	@Test(expected = BadProductId_Exception.class)
	public void buyProductEmptyTest() 
		throws BadProductId_Exception,BadQuantity_Exception,InsufficientQuantity_Exception  {
		client.buyProduct("", 4);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductWhitespaceTest() 
		throws BadProductId_Exception,BadQuantity_Exception,InsufficientQuantity_Exception  {
		client.buyProduct(" ",2);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductTabTest() 
		throws BadProductId_Exception,BadQuantity_Exception,InsufficientQuantity_Exception  {
		client.buyProduct("\t",6);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNewlineTest() 
		throws BadProductId_Exception,BadQuantity_Exception,InsufficientQuantity_Exception  {
		client.buyProduct("\n",8);
	}

	@Test(expected = BadQuantity_Exception.class)
	public void buyPoductNegativeValueTest() 
		throws BadProductId_Exception, BadQuantity_Exception,InsufficientQuantity_Exception {
		client.buyProduct("A" , -4);
	}

	@Test(expected = BadQuantity_Exception.class)
	public void buyPoductZeroValueTest() 
		throws BadProductId_Exception, BadQuantity_Exception,InsufficientQuantity_Exception {	
		client.buyProduct("A" , 0);
		
	}

	@Test(expected= BadProductId_Exception.class)
	public void buyProductNotExistsTest() 
		throws BadProductId_Exception,BadQuantity_Exception,InsufficientQuantity_Exception {
		client.buyProduct("A0",1);
	}

	@Test(expected= BadProductId_Exception.class)
	public void buyProductNotExistTest() 
		throws BadProductId_Exception,BadQuantity_Exception,InsufficientQuantity_Exception {
		client.buyProduct("A      0 ",1);
	}

	@Test(expected= InsufficientQuantity_Exception.class)
	public void tooMuchQuantityTest() 
		throws BadProductId_Exception ,BadQuantity_Exception,InsufficientQuantity_Exception {
		//not enough quantity of the specific product is avaiable
		client.buyProduct("1C2",66);
	}

	@Test(expected= BadProductId_Exception.class)
	public void buyProductLowercaseNotExistsTest() 
		throws BadProductId_Exception ,BadQuantity_Exception,InsufficientQuantity_Exception {
		// product identifiers are case sensitive,
		// so "1c2" is not the same as "1C2"
		String t2 = client.buyProduct("1c2",2);
	}
	@Test(expected=BadProductId_Exception.class)
	public void twoBadInputsTest() 
		throws BadProductId_Exception ,BadQuantity_Exception,InsufficientQuantity_Exception {
		//the exception thrown should be related to the first argument
		client.buyProduct("olaol",9);
	}
	@Test(expected=BadProductId_Exception.class)
	public void NonAlphanumericIdTest() 
		throws BadProductId_Exception ,BadQuantity_Exception,InsufficientQuantity_Exception {
		client.buyProduct(";+;",3);
	}


	// main tests

	@Test
	public void buyProductTwiceExistsTest() 
		throws BadProductId_Exception ,BadQuantity_Exception,InsufficientQuantity_Exception  {
		//test function twice in a row with the same product
		String b1=client.buyProduct("BbBb123",1);
		assertEquals(49, client.getProduct("BbBb123").getQuantity());
		//verify if number of the specific product decreases
		List<PurchaseView> pvs1 = client.listPurchases();
		for(PurchaseView a : pvs1){
			if(b1.equals(a.getId())){
				assertEquals("BbBb123", a.getProductId());
				assertEquals(20, a.getUnitPrice());
				assertEquals(1, a.getQuantity());
			}
		}	

		String b2 =client.buyProduct("BbBb123",2);
		assertEquals(47, client.getProduct("BbBb123").getQuantity());
		//verify if number of the specific product decreases
		List<PurchaseView> pvs2 = client.listPurchases();
		for(PurchaseView a : pvs2){
			if(b2.equals(a.getId())){
				assertEquals("BbBb123", a.getProductId());
				assertEquals(20, a.getUnitPrice());
				assertEquals(2, a.getQuantity());
			}
		}	
	}


	@Test
	public void buyAnotherProductExistsTest() 
		throws BadProductId_Exception ,BadQuantity_Exception,InsufficientQuantity_Exception  {
		
		String b=client.buyProduct("A",10);
		assertEquals(0, client.getProduct("A").getQuantity());
		//all the products avaiable were bought,so quantity must be zero
		List<PurchaseView> pvs = client.listPurchases();
		for(PurchaseView a : pvs){
			if(b.equals(a.getId())){
				assertEquals("A", a.getProductId());
				assertEquals(10, a.getUnitPrice());
				assertEquals(10, a.getQuantity());

			}
		}
	}
	@Test
	public void buyProductSpecialDescriptionTest() 
		throws BadProductId_Exception ,BadQuantity_Exception,InsufficientQuantity_Exception  {
		
		String b=client.buyProduct("olaola",4);
		assertEquals(4, client.getProduct("olaola").getQuantity());
		
		//purchaseId bought must appear just once on the purchaseList
		int count=0;
		List<PurchaseView> pvs = client.listPurchases();
		for(PurchaseView a : pvs){
			if(b.equals(a.getId())){
				count++;
				assertEquals("olaola", a.getProductId());
				assertEquals(173, a.getUnitPrice());
				assertEquals(4, a.getQuantity());

			}
		}
	}

	@Test(expected= InsufficientQuantity_Exception.class)
	public void buyProductZeroQuantityTest() 
		throws BadProductId_Exception ,BadQuantity_Exception,InsufficientQuantity_Exception  {
		//buy all quantity of a specific product
		String b1=client.buyProduct("A",10);
		//verify if number of the specific product decreases
		assertEquals(0, client.getProduct("A").getQuantity());

		List<PurchaseView> pvs1 = client.listPurchases();
		for(PurchaseView a : pvs1){
			if(b1.equals(a.getId())){
				assertEquals("A", a.getProductId());
				assertEquals(10, a.getUnitPrice());
				assertEquals(10, a.getQuantity());
			}
		}	
		//try to buy product with zero quantity
		String b2 =client.buyProduct("A",1);
	}

	public void buyAllProductInARowTest() 
		throws BadProductId_Exception ,BadQuantity_Exception,InsufficientQuantity_Exception  {
		//buy all the different products in a row
		String b1=client.buyProduct("BbBb123",46);
		//verify if number of the specific product decreases
		assertEquals(4,client.getProduct("A").getQuantity());
		assertNotNull(b1);

		String b2=client.buyProduct("A",10);
		assertEquals(0,client.getProduct("A").getQuantity());
		assertNotNull(b2);
		
		String b3=client.buyProduct("1C2",23);
		assertEquals(7,client.getProduct("1C2").getQuantity());
		assertNotNull(b3);

		String b4=client.buyProduct("olaola",1);
		assertEquals(172,client.getProduct("olaola").getQuantity());
		assertNotNull(b4);
	
	}



}