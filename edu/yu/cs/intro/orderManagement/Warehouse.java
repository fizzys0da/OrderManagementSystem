package edu.yu.cs.intro.orderManagement;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

public class Warehouse {

  private Map<Product, ProductTuple> inventory; // ProductTuple has both the current and default stock
  private Set<Product> noRestocks;
    
    /**
    * create a warehouse, initialize all the instance variables
    */

    protected Warehouse() {
      this.inventory = new HashMap<>();
      this.noRestocks = new HashSet<>();
    }

    /**
      * @return all unique Products stocked in the warehouse
      */
    protected Set<Product> getAllProductsInCatalog(){
      return new HashSet<Product>(inventory.keySet());
    }

    /**
      * Add a product to the warehouse, at the given stock level.
      *
      * @param product
      * @param desiredStockLevel the number to stock initially, and also to restock to when
      *                          subsequently restocked
      * @throws IllegalArgumentException if the product is in the "do not restock" set, or   if the
      *                                  product is already in the warehouse
      */
    protected void addNewProductToWarehouse(Product product, int desiredStockLevel) {
      if (inventory.containsKey(product) || noRestocks.contains(product)) {
          throw new IllegalArgumentException();
      } 
      else {
        inventory.put(product, new ProductTuple(desiredStockLevel));
      }
    }

  /**
     * If the actual stock is already >= the minimum, do nothing. Otherwise, raise it to minimum OR the default stock level, whichever is greater
     * @param productNumber
     * @param minimum
     * @throws IllegalArgumentException if the product is in the "do not restock" set, or if it is not in the catalog
     */
    protected void restock(int productNumber, int minimum) {
      if (!isInCatalog(productNumber) || noRestocks.contains(getProduct(productNumber))){
          throw new IllegalArgumentException();
      }
      Entry<Product, ProductTuple> entry = getProductEntry(productNumber); //**Can't Convert From Product to Map.Entry
      int stock = getStockLevel(productNumber);
      int defaultStock = entry.getValue().getDefaultLevel();
      if (stock >= minimum){
          return;
      }
      int restockTo = (defaultStock > minimum) ? defaultStock : minimum; // sets the amount to restock to the greater of either the default stock level or the minimum stock level
      entry.setValue(new ProductTuple(restockTo, defaultStock)); // store the new number of items in stock in the inventory entry
    }

        

    /**
      * Set the new default stock level for the given product
      *
      * @param productNumber
      * @param quantity
      * @return the old default stock level
      * @throws IllegalArgumentException if the product is in the "do not restock" set, or if it is
      *                                  not in the catalog
      */
    protected int setDefaultStockLevel(int productNumber, int quantity) {
      Entry<Product, ProductTuple> entry = getProductEntry(productNumber);
      if (entry == null || noRestocks.contains(entry.getKey())) { // if the item doesnt exist or if it's unrestockable
        throw new IllegalArgumentException();
      }
      ProductTuple tuple = entry.getValue();
      int oldDefault = tuple.getDefaultLevel();
      tuple.setDefaultLevel(quantity); // sets the old default level of that product to the new one
      entry.setValue(tuple); // write that value back to the inventory, as 'entry' is just an entry in the inventory
      return oldDefault;
    }

    /**
      * @param productNumber
      * @return how many of the given product we have in stock, or zero if it is not stocked
      */
    protected int getStockLevel(int productNumber) {
      ProductTuple tuple = getProductEntry(productNumber).getValue();
      if (tuple == null) { // if the item doesnt exist in the inventory
        return 0;
      }
      return tuple.getCurrentLevel(); // return the product's current stock amount
    }

    /**
      * @param itemNumber
      * @return true if the given item number is in the warehouse's catalog, false if not
      */
    protected boolean isInCatalog(int itemNumber) {
      return (getProduct(itemNumber) != null); // if it's not null, the item exists
    }

    /**
      * @param itemNumber
      * @return false if it's not in catalog or is in the "do not restock" set. Otherwise true.
      */
    protected boolean isRestockable(int itemNumber) {
      if (!isInCatalog(itemNumber) || noRestocks.contains(getProduct(itemNumber))){
        return false;
      }
      return true;
    }

    /**
      * add the given product to the "do not restock" set 
      * @param productNumber
      *
      * @return the current actual stock level
      */
    protected int doNotRestock(int productNumber) {
      Product product = getProduct(productNumber);
      if (product == null) {
        return 0;
      }
      noRestocks.add(product);
      return getStockLevel(productNumber);
    }

    /**
      * can the warehouse fulfill an order for the given amount of the given product? 
      * @param productNumber
      * @param quantity
      * @return false if the product is not in the catalog or there are fewer than quantity of the products in the catalog. Otherwise true.
      */
    protected boolean canFulfill(int productNumber, int quantity) {
       if (!isInCatalog(productNumber) || quantity > getStockLevel(productNumber)){
        return false;
      }
      return true;
    }

    /**
      * Fulfill an order for the given amount of the given product, i.e. lower the stock levels of
      * the product by the given amount
      *
      * @param productNumber
      * @param quantity
      * @throws IllegalArgumentException if {@link #canFulfill(int, int)} returns false
      */
    protected void fulfill(int productNumber, int quantity) {
      if (canFulfill(productNumber, quantity)) {
        ProductTuple tuple = getProductEntry(productNumber).getValue();
        tuple.setCurrentLevel(tuple.getCurrentLevel() - quantity);
        getProductEntry(productNumber).setValue(tuple);
      }
      else {
      throw new IllegalArgumentException("Can't Fullfill Order");
      }
    }

    private Entry<Product, ProductTuple> getProductEntry(int productNumber) {
      for (Entry<Product, ProductTuple> entry : inventory.entrySet()) {
        if (entry.getKey().getItemNumber() == productNumber) {
          return entry;
        }
      }
      return null;
    }

    private Product getProduct(int productNumber) {
      Entry<Product, ProductTuple> entry = getProductEntry(productNumber);
      if (entry == null) {
        return null;
      }
      return entry.getKey();
    }

    private class ProductTuple {
      private int currentStock;
      private int defaultStock;

      protected ProductTuple(int current, int def) {
        this.currentStock = current;
        this.defaultStock = def;
      }

      protected ProductTuple(int stock) {
        this.currentStock = stock;
        this.defaultStock = stock;
      }

      protected int getDefaultLevel() {
        return defaultStock;
      }

      protected int getCurrentLevel() {
        return currentStock;
      }

      protected void setDefaultLevel(int def) {
        this.defaultStock = def;
      }

      protected void setCurrentLevel(int current) { //Never Used Locally
        this.currentStock = current;
      }
    }
  }