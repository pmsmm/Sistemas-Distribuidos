package org.komparator.mediator.ws;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;
import org.komparator.supplier.ws.cli.SupplierClient;

import org.komparator.mediator.ws.*;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.MediatorEndpointManager;
import org.komparator.mediator.domain.Cart;
import org.komparator.mediator.domain.Mediator;
import org.komparator.mediator.domain.ShoppingResult;

import org.komparator.mediator.ws.ShoppingResultView;
import org.komparator.mediator.ws.Result;
import org.komparator.mediator.ws.MediatorEndpointManager;
import org.komparator.mediator.ws.cli.MediatorClientException;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;

import org.komparator.security.handler.MessageIDHandler;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.*;
import java.util.Date;


@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
		wsdlLocation = "mediator.1_0.wsdl", 
		name = "MediatorWebService", 
		portName = "MediatorPort", 
		targetNamespace = "http://ws.mediator.komparator.org/", 
		serviceName = "MediatorService"
)

@HandlerChain(file = "/mediator-ws_handler-chain.xml")
// TODO annotate to bind with WSDL
// TODO implement port type interface
public class MediatorPortImpl implements MediatorPortType {
    //mediatorClient
    private MediatorClient mClient = null;

    private static final String MEDIATOR_2 ="http://localhost:8072/mediator-ws/endpoint";
    // end point manager
    private MediatorEndpointManager endpointManager;

    @Resource
    private WebServiceContext webServiceContext;


	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
        try{
            this.endpointManager = endpointManager;
            this.mClient = new MediatorClient(MEDIATOR_2);
        }catch(MediatorClientException e){
            System.out.println(e);
        }
	}	
   
	// Main operations -------------------------------------------------------

    @Override
    public List<ItemView> getItems(String productId) throws InvalidItemId_Exception{

            //check productId
            if (productId == null) 
                throwInvalidItemId("Item ID cannot be null");
            productId = productId.trim();
            if (productId.length() == 0) 
                throwInvalidItemId("Item ID cannot be empty or whitespace");
          
            //instantiate lists
            ArrayList<SupplierClient> temp = searchSuppliers();
            ArrayList<ItemView> res = new ArrayList<ItemView>();
            //Mediator mediator = Mediator.getInstance();


            try{
                //iterate over the list of suppliers
                //if the product exists, createItemView aand add to list to be returned
                for (SupplierClient p : temp) {
                    if(p.getProduct(productId)!=null){
                        ProductView t = p.getProduct(productId);      
                        ItemView a = newItemView(t,p);
                        res.add(a);     
                    }
                }

            }catch (BadProductId_Exception e){
                System.out.println("Error while getting the product");
            }
            //sort list of itemviews by quantity (crescent order)
            Collections.sort(res, new Comparator<ItemView>() {
                    public int compare(ItemView result1, ItemView result2) {
                        return Integer.compare(result1.getPrice(),result2.getPrice());
                    }
            });
            return res;
    }

   
	
	@Override 
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {

	   //check descText argument
		if (descText == null) 
			throwInvalidText("Description cannot be null!");
		descText=descText.trim();
		if(descText.length() == 0)
			throwInvalidText("Description cannot be empt or whitespace!");
		
        //initatlize lists
        ArrayList<SupplierClient> temp = searchSuppliers();
        ArrayList<ItemView> res = new ArrayList<ItemView>();
        List<ProductView> products = new ArrayList<ProductView>();
        ArrayList<ItemView> prov = new ArrayList<ItemView>();
        ArrayList<ItemView> fin = new ArrayList<ItemView>();
        Mediator mediator = Mediator.getInstance();

        try{
            //iterate over the suplliers lists
            //receive productView List from each supplier
            for (SupplierClient sc : temp) {
                products = sc.searchProducts(descText); 
                if((products.isEmpty()) ==false){
                //create newItem view for each product received
                    for(ProductView p : products){
                        ItemView a = newItemView(p,sc);
                        res.add(a);
                    }
                }      
            }

        }catch (BadText_Exception e){
            System.out.println("Error while getting the product");
        }

        if((res.isEmpty()) == true)
            return res;
        
        //sort ItemViewList 
        //firs srot alphabeticaly, than per price
        Collections.sort(res, new Comparator<ItemView>() {
            public int compare(ItemView result1, ItemView result2) {
                return result1.getItemId().getProductId().compareTo(result2.getItemId().getProductId());
            }
        });

        String a = res.get(0).getItemId().getProductId();
        for(ItemView i: res){
            if (i.getItemId().getProductId().equals(a)){
                prov.add(i);    
            }
            else{
                Collections.sort(prov, new Comparator<ItemView>() {
                    public int compare(ItemView result1, ItemView result2) {
                        return Integer.compare(result1.getPrice(),result2.getPrice());
                    }
                });
                fin.addAll(prov);
                prov.clear();
                a= i.getItemId().getProductId(); 
                prov.add(i);
            }
        }
        //sort last elements that could have been added but not yet sorted
        Collections.sort(prov, new Comparator<ItemView>() {
            public int compare(ItemView result1, ItemView result2) {
                return Integer.compare(result1.getPrice(),result2.getPrice());
            }
        });
        //list fin has now all elements sorted
        fin.addAll(prov);
        return fin;
	}




	@Override
	public ShoppingResultView buyCart(String cartId,String creditCardNr) throws 
		EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {

            Mediator mediator = Mediator.getInstance();
            String current = getCurrentMessageId();
            //verify if is a repeated messageID
            //if true returns result corresponding to the messageId
            if(verifyId_Shop(current)){ 
                System.out.println("received messageid on buyCart: " + current+ " is repeated...dont execute");
                return mediator.getShopRes(current);
            }
            System.out.println("received new messageid on buyCart: " + current + " execute...");

            //check cartId
            if (cartId == null)
                throwInvalidCartId("Cart identifier cannot be null!");
            cartId = cartId.trim();
            if (cartId.length() == 0)
                throwInvalidCartId("Cart identifier cannot be empty or whitespace!");
            //check creditcardId
            if (creditCardNr == null) 
                throwInvalidCreditCard("Credit Card ID cannot be null");
            creditCardNr=creditCardNr.trim();
            if (creditCardNr.length() == 0) 
                throwInvalidCreditCard("Credit Card ID cannot be empty or whitespace");
            
            //checks if the cart exists
    
            if(mediator.cartExists(cartId) == false)
                throwInvalidCartId("Car does not exist");
            
            
            try{
                //create creditCrad client to validate creditCard
                CreditCardClient cc = new CreditCardClient("http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc");
                if(cc.validateNumber(creditCardNr) == true){
                    Cart car = mediator.getCart(cartId);
         
                    //initialize list with dropped and purchased items, and suplliers list
                    ArrayList<CartItemView> drop = new ArrayList<CartItemView>();
                    ArrayList<CartItemView> got = new ArrayList<CartItemView>();
                    ArrayList<SupplierClient> temp = searchSuppliers();
                    List<CartItemView> list = car.getList();

                    int total=0;
                    //iterate over suplliers list
                    for (SupplierClient i : temp){
                        //for each element in the CartItemView list check if that element is from the specific supplier
                        for(CartItemView c : list){
                            ItemIdView item = c.getItem().getItemId();
                            if((item.getSupplierId()).equals(i.getName())){
                   
                                if((c.getQuantity()) <= (i.getProduct(item.getProductId()).getQuantity())){
                                    i.buyProduct(item.getProductId(), c.getQuantity());
                                    got.add(c);
                                    total+=(c.getItem().getPrice())*(c.getQuantity());
                                }
                                else{drop.add(c); }
                                                 
                            }
                        }
                    }

                    //create the elements neeeded in the the shoppingResult
                    String id= mediator.generateShopId();
                    System.out.println(id);
                    Result result=null; 
                    if(total == car.getTotalPrices())
                        result = Result.valueOf("COMPLETE");
                    else if(total ==0)
                        result = Result.valueOf("EMPTY");
                    else{ result = Result.valueOf("PARTIAL"); }
                
         
                    ShoppingResult shopRes = new ShoppingResult(id,drop,got,total,result);
                    mediator.registerShopResult(id, shopRes);
                    ShoppingResultView shop = newShoppingResultView(shopRes);

                    //saves the result of corresponding messageId
                    //if primary mediator, calls update fucntion on secundary mediator
                    mediator.setShopRes(current,shop);
                    if(endpointManager.isPrimary()){
                    mClient.updateShopHistory(shop, current);
                    }
                    return shop;

                }throwInvalidCreditCard("inavlid Credit Card ID");


            }catch(BadProductId_Exception|BadQuantity_Exception|InsufficientQuantity_Exception|CreditCardClientException e){
                System.out.println("Error while getting product or validating creditCard");
            }
        
        return null;

	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty)
        throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception{


            //if id is repeated doesnt exceute
            if(verifyId_Cart(getCurrentMessageId())){
                System.out.println("received messageid on addTCart: " + getCurrentMessageId()+ " is repeated...dont execute");
                return;
            }
            System.out.println("received new messageid on clear: " + getCurrentMessageId() +" execute...");

            //check cartId
            if (cartId == null)
                throwInvalidCartId("Cart identifier cannot be null!");
            cartId = cartId.trim();
            if (cartId.length() == 0)
                throwInvalidCartId("Cart identifier cannot be empty or whitespace!");
            //check ItemdId
            if (itemId == null)
                throwInvalidItemId("Item identifier cannot be null!");
            if((itemId.getSupplierId() == null) || (itemId.getProductId()) == null || itemId.getSupplierId().trim().length() == 0 
                    || itemId.getProductId().trim().length()==0)
                throwInvalidItemId("Item identifier has null fields");
            //check if item quantity is postive
            if (itemQty <= 0)
                throwInvalidQuantity("Please enter a valid quantity");


        try{    

            Mediator mediator = Mediator.getInstance();
            //check if car exists
            if(mediator.cartExists(cartId) == false)
                mediator.registerCart(cartId);
    
            Cart car = mediator.getCart(cartId);
            //list with suppliers
            //check if product exists
            ArrayList<SupplierClient> temp = searchSuppliers();
            for (SupplierClient i : temp){
                if (i.getName().equals(itemId.getSupplierId())){

                    if (i.getProduct(itemId.getProductId()) != null){
                        //check if quantity wanted is avaiable
                        ProductView ttt = i.getProduct(itemId.getProductId());
                        if((ttt.getQuantity()) < (itemQty)){
                            throwNotEnoughItems("Not enough quantity");
                        }
                        //check if item is already in cart
                        else if(car.existItem(itemId)==true){
                                System.out.println(ttt.getQuantity());
                                System.out.println(car.getItemIdQuantity(itemId));
                            if((ttt.getQuantity())<= (itemQty + car.getItemIdQuantity(itemId))){
                                throwNotEnoughItems("Not enough quantity");
                            }
                            else{
                                car.upgradeQuantity(itemId, itemQty);
                            }
                               
                        }

                        //add new Item to Cart         
                        else{ 
                            car.addNewProduct(itemId, itemQty, ttt);
                        }  

                    }else{throwInvalidItemId("Item identifier has null fields");}
                }
            
            }
           //if is the primary Mediator calls the uodate function to the secundary mediator
            if(endpointManager.isPrimary()){
                mClient.updateCart(cartId, mediator.getCart(cartId).getList(),getCurrentMessageId());
            }

        }catch(BadProductId_Exception e){System.out.println("error while getting the productttt");}

        
    }


    
	// Auxiliary operations --------------------------------------------------	
	
    // TODO*/
    @Override
    public String ping(String arg0){


    	ArrayList<SupplierClient> temp = searchSuppliers();
    	StringBuilder builder = new StringBuilder();
        builder.append("Mediator OK\n");
        int b=1;
    	for (SupplierClient p : temp){
    		String a = p.ping(arg0);
    		builder.append("Found supplier: " + b + ":").append(a +"\n");
            b++;
    	}
    	return builder.toString();
    } 
    


    
    @Override
    public void clear(){

        //if repeated messageID doesnt execute
        String current =getCurrentMessageId();
        if(verifyId_Clear(current)){
            System.out.println("received messageid on clear: " + getCurrentMessageId()+ " is repeated...dont execute");
            return;
        }
        System.out.println("received new messageid on clear: " + getCurrentMessageId() +" execute...");

    	ArrayList<SupplierClient> temp = searchSuppliers();
    	for (SupplierClient p : temp){
    		p.clear();
		}
        Mediator.getInstance().reset();    

        //if is the primary mediator calls update function on secundary mediator
        if(endpointManager.isPrimary()){
            mClient.updateClear(current);
        }
    }



    @Override
    public List<CartView> listCarts(){
        Mediator mediator = Mediator.getInstance();
        List<CartView> pvs = new ArrayList<CartView>();
        for (String pid : mediator.getCartIDs()) {
            Cart p = mediator.getCart(pid);
            CartView pv = newCartView(p);
            pvs.add(pv);
        }
        return pvs;
    }

    @Override
    public List<ShoppingResultView> shopHistory(){
        Mediator mediator = Mediator.getInstance();
        List<ShoppingResultView> pvs = new ArrayList<ShoppingResultView>();
        for (String pid : mediator.getShopIDs()) {
            ShoppingResult p = mediator.getShop(pid);
            ShoppingResultView pv = newShoppingResultView(p);
            pvs.add(pv);
        }
        return pvs;
    	
    }

    @Override
    public void imAlive(){
        if(endpointManager.isPrimary()) return;
        //saves life proof from the primary mediator
        Mediator mediator = Mediator.getInstance();
        Date date = new Date();
        mediator.saveDate(date);
        System.out.println("date saved");
    }


    @Override
    public void updateShopHistory(ShoppingResultView shop, String id){

        if(endpointManager.isPrimary()) return;
        //if is a repeated request doesnt execute
        if(verifyId_Shop(id)){ 
            System.out.println("dont actualize shopHistory, message id: " + id+" is repeated");
            return;
        }
        //actualizes the state in secundary mediator
        Mediator mediator = Mediator.getInstance();
        ShoppingResult shopRes = new ShoppingResult(shop.getId(),(ArrayList<CartItemView>)shop.getDroppedItems(),
            (ArrayList<CartItemView>)shop.getPurchasedItems(),shop.getTotalPrice(),shop.getResult());
        mediator.registerShopResult(shop.getId(), shopRes);
        mediator.setShopId(Integer.parseInt(shop.getId()));
        //saves update result
        mediator.setShopRes(id,shop);
        System.out.println("shop history updated with sucess on secundary mediator with new messageId of :" + id);

    }


    @Override
    public void updateCart(String cartId, List<CartItemView> list, String id){
 
        if(endpointManager.isPrimary()) return;
        //if is a repeated request doesnt execute
        if(verifyId_Cart(id)){
            System.out.println("dont actualize cart , message id: " + id+" is repeated");
            return;
        }

        //actualizes the state in secundary mediator
        Mediator mediator = Mediator.getInstance();
        //check if car exists
        if(mediator.cartExists(cartId) == false)
            mediator.registerCart(cartId);

        Cart cart = mediator.getCart(cartId);
        cart.setList(list);
        System.out.println("cart updated with sucess on secundary mediator with messageId of :" + id);
    }


    @Override
    public void updateClear(String id){

        if(endpointManager.isPrimary()) return;
         //if is a repeated request doesnt execute
        if(verifyId_Clear(id)){
            System.out.println("dont actualize clear , message id: " + id+" is repeated");
            return;
        };
        //actualizes the state in secundary mediator
        Mediator.getInstance().reset();  
        System.out.println("clear executed with sucess on secundary mediator with messageId of :" + id);  
    }


    //method that create list of suplliers
    private ArrayList<SupplierClient> searchSuppliers(){
        try {
            UDDINaming uddi = endpointManager.getUddiNaming();
            Collection<UDDIRecord> supplierRecords = uddi.listRecords("T24_Supplier%");
            ArrayList<SupplierClient> fornecedores = new ArrayList<SupplierClient>();

            for (UDDIRecord sup : supplierRecords){
                String url = sup.getUrl();
                SupplierClient client = new SupplierClient(url);
                client.setName(sup.getOrgName());
                fornecedores.add(client);
            }
            return fornecedores;

        }catch (Exception e){
            System.out.printf("Caught exception when binding to UDDI: %s%n", e);
            return null;
        }
    }

    //verifies if messageId is repeated
    //if is reapeted doenst execute. If is not repeated saves messageid and state
    public boolean verifyId_Cart(String current){
           
        Mediator med = Mediator.getInstance();
        if(med.cartRequest(current)){
            return med.getCartRes(current);
        }
        else{
            med.setCartRes(current, true);
            return false;
        }
    }

     //verifies if messageId is repeated
    //if is reapeted doenst execute. If is not repeated saves messageid and state
    public boolean verifyId_Shop(String current){
        
        Mediator med = Mediator.getInstance();
        if(med.shopRequest(current)){
            return true;
        }
        else{
            med.setShopRes(current, new ShoppingResultView());
            return false;
        }
    }

    //verifies if messageId is repeated
    //if is reapeted doenst execute. If is not repeated saves messageid and state
    public boolean verifyId_Clear(String current){

        Mediator med = Mediator.getInstance();
        if(med.clearRequest(current)){
            return true;
        }else{
            med.setClearRes(current, true);
            return false;
        }
    }

    //reads messageId from messageContext
    public String getCurrentMessageId(){

        MessageContext messageContext = webServiceContext.getMessageContext();
        // get token from message context
        String propertyValue = (String) messageContext.get(MessageIDHandler.REQUEST_PROPERTY);
        return propertyValue;
    }



    

	
	// View helpers -----------------------------------------------------
	
    // TODO
    private CartView newCartView(Cart cart) {
        CartView view = new CartView();
        view.setCartId(cart.getCartId());
        view.getItems().addAll(cart.getList());
        return view;
    }

    private ItemView newItemView(ProductView product , SupplierClient sp){
    	ItemView view = new ItemView();
        ItemIdView item = newItemIdView(product,sp);
    	view.setItemId(item);
    	view.setDesc(product.getDesc());
    	view.setPrice(product.getPrice());
    	return view;
    }

    private ItemIdView newItemIdView(ProductView product , SupplierClient sp){
    	ItemIdView view = new ItemIdView();
    	view.setProductId(product.getId());
        System.out.println(sp.getName());
    	view.setSupplierId(sp.getName());
    	return view;

    }

    private CartItemView newCartItemView(ItemView item, int quantity){
        CartItemView view = new CartItemView();
        view.setItem(item);
        view.setQuantity(quantity);
        return view;

    }

  
    private ShoppingResultView newShoppingResultView(ShoppingResult p){
        ShoppingResultView view = new ShoppingResultView();
        view.setId(p.getShopId());
        view.setResult(p.getResult());
        view.getPurchasedItems().addAll(p.getPurchasedItems());
        view.getDroppedItems().addAll(p.getDroppedItems());
        view.setTotalPrice(p.getTotalPrice());
        return view;
    }

    
	// Exception helpers -----------------------------------------------------

    /** Helper method to throw new InvalidText exception */
    private void throwInvalidText(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.message = message;
		throw new InvalidText_Exception(message, faultInfo);
	}

	/** Helper method to throw new InvalidCartId exception */
	private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.message = message;
		throw new InvalidCartId_Exception(message, faultInfo);
	}

	/** Helper method to throw new InavlidItemId exception */
	private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.message = message;
		throw new InvalidItemId_Exception(message, faultInfo);
	}

	/** Helper method to throw new InvalidCreditCard exception */
	private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.message = message;
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}

	/** Helper method to throw new InvalidQuantity exception */
	private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.message = message;
		throw new InvalidQuantity_Exception(message, faultInfo);
	}

	/** Helper method to throw new NotEnoguhItems exception */
	private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.message = message;
		throw new NotEnoughItems_Exception(message, faultInfo);
	}

	/** Helper method to throw new EmptyCart exception */
	private void throwEmptyCart(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.message = message;
		throw new EmptyCart_Exception(message, faultInfo);
	}

   
}
	
	




	
