package org.komparator.mediator.domain;

import java.util.ArrayList;
import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.Result;

import java.util.Date;

/**
 * ShoopingResult entity. Immutable i.e. once an object is created it cannot be
 * changed.
 */
public class ShoppingResult {
	/** Hopping identifier. */
	private String shopId;
	/** Shop product identifier. */
	private ArrayList<CartItemView> droppedItems = new ArrayList<CartItemView>();
	//list with dropped items
	private ArrayList<CartItemView> purchasedItems = new ArrayList<CartItemView>();
	//list with purchased items
	private int totalPrice;
	/** Date of purchase. */
	private Result result;
	//final result of purchase
	private Date timestamp = new Date();

	/** Create a new ShoppingResult */
	public ShoppingResult (String pid, ArrayList<CartItemView> dropped, ArrayList<CartItemView> purchased, int total, Result res) {
		this.shopId = pid;
		this.droppedItems = dropped;
		this.purchasedItems = purchased;
		this.totalPrice = total;
		this.result=res;
	}

	public String getShopId() {
		return shopId;
	}

	public ArrayList<CartItemView> getDroppedItems() {
		return droppedItems;
	}
	public ArrayList<CartItemView> getPurchasedItems() {
		return purchasedItems;
	}

	public int getTotalPrice() {
		return totalPrice;
	}
	public Result getResult(){
		return result;
	}

	public Date getTimestamp() {
		return timestamp;
	}
}