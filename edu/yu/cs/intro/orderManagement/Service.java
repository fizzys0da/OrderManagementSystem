package edu.yu.cs.intro.orderManagement;

/**
 * An implementation of item which represents a Service provided by the business.
 * Has a price per billable hour as well a number of hours this service takes.
 * The price returned by getPrice must be the per hour price multiplied by the number of hours the
service takes
 */ 
public class Service implements Item {
    private double pricePerHour;
    private int numberOfHours;
    private int serviceID;
    private String description;

    public Service(double pricePerHour, int numberOfHours, int serviceID, String description){
        this.pricePerHour = pricePerHour;
        this.numberOfHours = numberOfHours;
        this.serviceID = serviceID;
        this.description = description;
    }
    /**
* @return the number of hours this service takes */
    public int getNumberOfHours(){
        return numberOfHours;
    } 
    @Override
    public int getItemNumber(){
        return serviceID;
    } 
    @Override
    public String getDescription(){
        return description;
    } 
    @Override
    public double getPrice(){
        return pricePerHour * numberOfHours;
    } 
    @Override
    public boolean equals(Object o){
      if (o instanceof Service) {
        return o.hashCode() == this.hashCode();
      }
      return false;
    } 
    @Override
    public int hashCode(){
        return serviceID;
    }
}
