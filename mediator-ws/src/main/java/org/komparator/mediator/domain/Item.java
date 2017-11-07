package org.komparator.mediator.domain;

import java.util.TreeMap;
import java.util.ArrayList;
import org.komparator.mediator.ws.ItemIdView;

/**
 * Cart entity.
 */

public class Item {
	/** Cart Name. */
	

    private ItemIdView itemId;
    private String desc;
    private int price;

    public Item(ItemIdView item, String descri, int pricee){
    	this.itemId = item;
    	this.desc=descri;
    	this.price= pricee;
    }

    public ItemIdView getItemId() {
        return itemId;
    }

   
    public void setItemId(ItemIdView value) {
        this.itemId = value;
    }

    public String getDesc() {
        return desc;
    }

  
    public void setDesc(String value) {
        this.desc = value;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int value) {
        this.price = value;
    }


}
