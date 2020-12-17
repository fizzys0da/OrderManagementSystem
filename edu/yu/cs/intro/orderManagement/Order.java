package edu.yu.cs.intro.orderManagement;

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

/**
* models an order placed by a customer. An item in the order can be an instance of either Product
or Service
*/
public class Order {

  private Map<Item, Integer> items;
  private boolean completed = false;

    public Order() { 
      items = new HashMap<>();
    }

    /**
     * @return all the items (products and services) in the order */
    public Item[] getItems(){
      return items.keySet().toArray(new Item[items.keySet().size()]);
    }

    /**
     * @param b
     * @return the quantity of the given item ordered in this order. Zero if the item is not in the
    order. */
    public int getQuantity(Item b){
      return items.get(b);
    }

    /**
     * Add the given quantity of the given item (product or service) to the order 
     * @param item
     * @param quantity
     */
    public void addToOrder(Item item, int quantity) {
      if (items.containsKey(item)) {
        items.put(item, items.get(item) + quantity);
      } else {
        items.put(item, quantity);
      }
    }

    /**
     * Calculate the total price of PRODUCTS in the order. Must multiply each item's price by the
     quantity.
     * @return the total price of products in this order */
    public double getProductsTotalPrice() {
      double prices = 0;
      for(Entry <Item, Integer> entry : items.entrySet()) {
        if (entry.getKey() instanceof Product) {
          prices += (entry.getKey().getPrice() * entry.getValue());
        }
      }
      return prices;
    }

    /**
     * Calculate the total price of the SERVICES in the order. Must multiply each item's price by
     the quantity.
     * @return the total price of products in this order */
    public double getServicesTotalPrice() {
      double prices = 0;
      for(Entry <Item, Integer> entry : items.entrySet()) {
        if (entry.getKey() instanceof Service) {
          prices += (entry.getKey().getPrice() * entry.getValue());
        }
      }
      return prices;
    }

    /**
     * @return has the order been completed by the order management system? */
    public boolean isCompleted() {
      return this.completed;
    }

    /**
     * Indicate if the order has been completed by the order management system
     * @param completed
     */
    public void setCompleted(boolean completed) {
      this.completed = completed;
    }
}
