package org.komparator.mediator.domain;

import java.util.TreeMap;
import java.util.ArrayList;
import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.ProductView;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.MediatorPortImpl;
import org.komparator.mediator.ws.CartItemView;
import java.util.*;

/**
 * Cart entity.
 */

public class Cart {
	/** Cart Name. */
	private String cartId;
	/** Products in Cart. */

	List<CartItemView> templista = new ArrayList<CartItemView>();
	List<CartItemView> lista = Collections.synchronizedList(templista);
	
	/** Quantity of products in cart. */
	
	/** Create a new cart */
	public Cart(String id) {
		this.cartId = id;

	}

	/** Add Product to cart. */
	public void addNewProduct(ItemIdView elem, int quant, ProductView pp){
		Mediator mediator = Mediator.getInstance();
		ItemView b = new ItemView();
		b.setItemId(elem);
		b.setDesc(pp.getDesc());
		b.setPrice(pp.getPrice());

		CartItemView c = new CartItemView();
		c.setItem(b);
		c.setQuantity(quant);

		lista.add(c);
		
	}

	//get CartId
	public String getCartId(){
		return cartId;

	}
	
	//verify if ItemIdview is contain in the car
	public boolean existItem(ItemIdView item){
		for(CartItemView c : lista){
			if((c.getItem().getItemId().getProductId().equals(item.getProductId())) && (c.getItem().getItemId().getSupplierId().equals(item.getSupplierId()))){
				return true;
			}
		}return false;
	}
	
	//item already in the cart- upgrade quantity
	public void upgradeQuantity(ItemIdView item, int quant){
		for(CartItemView c : lista){
			if((c.getItem().getItemId().getProductId().equals(item.getProductId())) && (c.getItem().getItemId().getSupplierId().equals(item.getSupplierId()))){
				int a=c.getQuantity();
				c.setQuantity(quant + a );
			}
		}
	}

	public int getItemIdQuantity(ItemIdView item){
		for(CartItemView c : lista){
			if((c.getItem().getItemId().getProductId().equals(item.getProductId())) && (c.getItem().getItemId().getSupplierId().equals(item.getSupplierId()))){
				return c.getQuantity();
			}
		}
		return 0;
	}
	//returns list of the cartItemViews contains in the cart
	public List<CartItemView> getList(){
		return lista;
	}
	public void setList(List<CartItemView> list){
		lista = list;
	}

	//sum the price of the products
	//if product is in quantity>1, some the prices of all products
	public int getTotalPrices() {
		int total=0;
		for(CartItemView c : lista){
			int a =c.getItem().getPrice();
			int b =c.getQuantity();
			total +=a*b;
		}
		return total;	
	}
}
