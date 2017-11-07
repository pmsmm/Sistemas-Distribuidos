package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp(){
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
	public void setUp() throws BadProductId_Exception, BadProduct_Exception{

		client.clear();

		// fill-in test products
		{
			ProductView product = new ProductView();
			product.setId("b");
			product.setDesc("bicicleta");
			product.setPrice(100);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("c");
			product.setDesc("carro");
			product.setPrice(200);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("s3");
			product.setDesc("skate");
			product.setPrice(300);
			product.setQuantity(30);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("bi");
			product.setDesc("bici");
			product.setPrice(400);
			product.setQuantity(40);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("4K");
			product.setDesc("4KHD++");
			product.setPrice(500);
			product.setQuantity(50);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("m8");
			product.setDesc("Tennis  ball");
			product.setPrice(173);
			product.setQuantity(8);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("You11rs");
			product.setDesc("null");
			product.setPrice(80);
			product.setQuantity(100);
			client.createProduct(product);
		}
	
	}

	@After
	public void tearDown() {
	}

	// tests
	// assertEquals(expected, actual);

	// public List<ProductView> searchProducts(String descText) throws
	// BadText_Exception

	// bad input tests
	@Test(expected = BadText_Exception.class)
	public void searchProductsNullTest() throws BadText_Exception {
		client.searchProducts(null);
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsEmptyTest() throws BadText_Exception {
		client.searchProducts("");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsWhitespaceTest() throws BadText_Exception {
		client.searchProducts(" ");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsTabTest() throws BadText_Exception {
		client.searchProducts("\t");
	}

	@Test(expected = BadText_Exception.class)
	public void getProductsNewlineTest() throws BadText_Exception {
		client.searchProducts("\n");
	}
	

	// main tests

	@Test
	public void searchProductsExistsTest() throws BadText_Exception {
		//search an existant product
		List<ProductView> products = client.searchProducts("te");
		for(ProductView p : products){
			assertEquals("s3", p.getId());
			assertEquals(300, p.getPrice());
			assertEquals(30, p.getQuantity());
			assertEquals("skate", p.getDesc());
		}	
	}
	@Test
	public void searchProductsSpecialCharacteresExistsTest() throws BadText_Exception {
		//search for product which description contains special characters
		List<ProductView> products = client.searchProducts("++");
		for(ProductView p : products){
			assertEquals("4K", p.getId());
			assertEquals(500, p.getPrice());
			assertEquals(50, p.getQuantity());
			assertEquals("4KHD++", p.getDesc());
		}	
	}


	@Test
	public void searchDoubleProductsExistsTest() throws BadText_Exception {
		//"bic" is in the description of two products so the list recieved must contain two elements
		List<ProductView> products = client.searchProducts("bic");
		int a=0;
		for(ProductView p : products){
		 	a++;
		 	assertNotNull(p);
		}
		assertEquals(a,2);
	}


	@Test
	public void searchProductsOneLetterExistsTest() throws BadText_Exception {
		//Three products description contain the letter "c"
		List<ProductView> products = client.searchProducts("c");
		int a=0;
		for(ProductView p : products){
			a++;
			assertNotNull(p);
		}
		assertEquals(a,3);
	}

	@Test
	public void searchProductsLetterPlusSpacesTest() throws BadText_Exception {
		//Two products must be found
		List<ProductView> products = client.searchProducts(" ll  ");
		int a=0;
		for(ProductView p : products){
			 a++;
			 assertNotNull(p);
		}
		assertEquals(a,2);
	}

	@Test
	public void searchProductsFullDescriptionExistsTest() throws BadText_Exception {
		//search text is equal to the product description
		List<ProductView> products = client.searchProducts("bicicleta");
		for(ProductView p : products){
			assertEquals("b", p.getId());
			assertEquals(100, p.getPrice());
			assertEquals(10, p.getQuantity());
			assertEquals("bicicleta", p.getDesc());
		}	
	}

	@Test
	public void searchProductSpacedDescExistsTest() throws BadText_Exception {
		//Two sapces between words in desc are consider
		List<ProductView> products = client.searchProducts("Tennis  ball");
		for(ProductView p : products){
			assertEquals("m8", p.getId());
			assertEquals(173, p.getPrice());
			assertEquals(8, p.getQuantity());
			assertEquals("Tennis  ball", p.getDesc());
		}	

	}
	// main tests

	@Test
	public void searchProductsSpaceDescriptionTest() throws BadText_Exception, BadProductId_Exception ,
	   BadQuantity_Exception, InsufficientQuantity_Exception {
		//Product with zero quantity must appear in the results
		client.buyProduct("s3",30);
		//Product has now zero quantity
		List<ProductView> products = client.searchProducts("sk");
		for(ProductView p : products){
			assertEquals("s3", p.getId());
			assertEquals(300, p.getPrice());
			assertEquals(0, p.getQuantity());
			assertEquals("skate", p.getDesc());
		}	
	}


	@Test
	public void searchProductsNotExistsTest() throws BadText_Exception {
		//if product doesnt exist list should be empty, not null
		List<ProductView> products = client.searchProducts("oceano");
		assertEquals(true, products.isEmpty());
		assertNotNull(products);

	}

	@Test
	public void searchProductAlmostDescExistsTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("skte");
		assertEquals(true, products.isEmpty());
		assertNotNull(products);
	}

	@Test
	public void searchProductsLowercaseNotExistsTest() throws BadText_Exception {
		//method is case sensitive
		//if product doesnt exist list should be empty, not null
		List<ProductView> products = client.searchProducts("Carro");
		assertEquals(true, products.isEmpty());
		assertNotNull(products);	
	}
	@Test
	public void searchProductWrongDescTest() throws BadText_Exception {
		//wrong description (1 less space)
		List<ProductView> products = client.searchProducts("Tennis ball");
		assertEquals(true, products.isEmpty());
		assertNotNull(products);	
	}




}
