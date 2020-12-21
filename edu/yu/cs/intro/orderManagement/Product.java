package edu.yu.cs.intro.orderManagement;
/**
 * Is a "physical" item that is "stocked" in the warehouse.
 */
public class Product implements Item {
    private String name;
    private double price;
    private int productID;
    
    public Product(String name, double price, int productID){
        this.name = name;
        this.price = price;
        this.productID = productID;
    }
        @Override
        public int getItemNumber(){
            return productID;
        }
        @Override
        public String getDescription(){
            return name;
        }
        @Override
        public double getPrice(){
            return price;
        }
        @Override
        public boolean equals(Object o){
          if (o instanceof Product) {
            return o.hashCode() == this.hashCode();
          }
          return false;
        } 
        @Override
        public int hashCode(){
            return productID;
        }
}