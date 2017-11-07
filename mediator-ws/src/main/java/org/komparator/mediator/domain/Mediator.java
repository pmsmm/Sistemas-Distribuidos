package org.komparator.mediator.domain;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.ItemView;
import org.komparator.mediator.ws.ShoppingResultView;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;


/** Domain Root. */
public class Mediator {

	// Members ---------------------------------------------------------------

	/**
	 * Map of existing Carts. Uses concurrent hash table implementation
	 * supporting full concurrency of retrievals and high expected concurrency
	 * for updates.
	 */
	private Map<String, Cart> carts = new ConcurrentHashMap<>();

	/**
	 * Global shop identifier counter. Uses lock-free thread-safe single
	 * variable.
	 */
	private AtomicInteger shopIdCounter = new AtomicInteger(0);

	private Map<String, ShoppingResult> shopResults = new ConcurrentHashMap<>();

	private Map<ItemIdView,ItemView> items = new ConcurrentHashMap<>();
	//saves proff lifes dates from primary mediator
	private ArrayList<Date> timeStamp = new ArrayList<Date>();

	//saves messageId and corresponding result
	private Map<String ,Boolean> cartCheck = new ConcurrentHashMap<>();
	//saves messageId and corresponding result
	private Map<String , ShoppingResultView> shopCheck = new ConcurrentHashMap<>(); 
	//saves messageId and corresponding result
	private Map<String ,Boolean> clearCheck = new ConcurrentHashMap<>();


	// Singleton -------------------------------------------------------------

	/* Private constructor prevents instantiation from other classes */

	private Mediator() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final Mediator INSTANCE = new Mediator();
	}

	public static synchronized Mediator getInstance() {
		return SingletonHolder.INSTANCE;
	}
	// mediator ---------------------------------------------------------------

	public void reset() {
		carts.clear();
		shopResults.clear();
		shopIdCounter.set(0);
		items.clear();
		cartCheck.clear();
		shopCheck.clear();
		clearCheck.clear();
		//timeStamp.clear();
		
	}
		/*Cart------------------------------------------------------------------------ */
	public boolean cartExists(String pid) {
		return carts.containsKey(pid);
	}

	public List<String> getCartIDs() {
		List<String> cartsIds = new ArrayList<>(carts.keySet());
		return cartsIds;	
	}

	public Cart getCart(String cartId) {
		return carts.get(cartId);
	}

	public void registerCart(String cartId) {
			carts.put(cartId, new Cart(cartId));	
	}

	private boolean acceptCart(String cartId) {
		return cartId != null && !"".equals(cartId);
	}
	public Map<String, Cart> getAllCarts(){
		return carts;
	}
	public void setCart(Map<String, Cart> mapCart){
		carts=mapCart;
	}

	/*ShopResult------------------------------------*/


	public String generateShopId() {
		// relying on AtomicInteger to make sure assigned number is unique
		int shopId = shopIdCounter.incrementAndGet();
		return Integer.toString(shopId);
	}

	public void setShopId(int value){
		shopIdCounter.set(value);
	}


	public ShoppingResult getShop(String shopId) {
		return shopResults.get(shopId);
	}

	public void registerShopResult(String shopId, ShoppingResult shop) {
			shopResults.put(shopId, shop);	
	}

	public Set<String> getShopIDs() {
		return shopResults.keySet();
	}
	public Boolean shopExists(String pid) {
		return shopResults.containsKey(pid);
	}
	public Map<String, ShoppingResult> getShops(){
		return shopResults;
	}
	public void setShop(Map<String, ShoppingResult> mapShop){
		shopResults=mapShop;
	}



	/*-Items-----------------------------------*/

	public void saveItem(ItemIdView itemId, ItemView item){
    		items.put(itemId, item);
	}
	public ItemView getItemView(ItemIdView itemId) {
		return items.get(itemId);
	}
	public Boolean itemExists(ItemIdView itemId) {
		return items.containsKey(itemId);
	}
	public Map<ItemIdView, ItemView> getAllItems(){
		return items;
	}
	public List<ItemIdView> getItemsIDs() {
		List<ItemIdView> itemsIds = new ArrayList<>(items.keySet());
		return itemsIds;
	}

	/*Date------------------------------------*/
	public void saveDate(Date date){
		timeStamp.add(date);
	}
	public ArrayList<Date> getAllDates(){
		return timeStamp;
	}
	public Date getLastDate(){
		return timeStamp.get(timeStamp.size()-1);
	}
	public boolean noDates(){
		return timeStamp.isEmpty();
	}

	/*cartCheck-----------------------------------*/

	public boolean cartRequest(String id) {
		return cartCheck.containsKey(id);
	}
	public boolean getCartRes(String id){
		return cartCheck.get(id);
	}
	public void setCartRes(String id, boolean bool) {
			cartCheck.put(id, bool);	
	}



	/*shopCheck---------------------------------*/
	public boolean shopRequest(String shopId){
		return shopCheck.containsKey(shopId);
	}

	public ShoppingResultView getShopRes(String shopId) {
		return shopCheck.get(shopId);
	}
	public void setShopRes(String id, ShoppingResultView shop) {
			shopCheck.put(id,shop);	
	}

	/*clearCheck----------------------------*/
	public boolean clearRequest(String id) {
		return clearCheck.containsKey(id);
	}
	public boolean getClearRes(String id){
		return clearCheck.get(id);
	}
	public void setClearRes(String id, boolean bool) {
			clearCheck.put(id, bool);	
	}


}	
