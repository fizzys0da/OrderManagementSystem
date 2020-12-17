package edu.yu.cs.intro.orderManagement;
import java.security.Provider;
import java.util.HashSet;
import java.util.Set;

public class ServiceProvider {
/**
  * 1) has a Set of services that he can provide
  * 2) can only work on one order at a time - once assigned to a customer, can not take another
  assignment until 3 other orders have been placed with the order management system
  * 3) is uniquely identified by its ID
  */

    private String name;
    private int id;
    private boolean busy = false;
    private int breakLeft = 0;
    private Set<Service> services = new HashSet<>();

    /**
      *
      * @param name
      * @param id unique id of the ServiceProvider
      * @param services set of services this provider can provide */
    public ServiceProvider(String name, int id, Set<Service> services){

        this.name = name;
        this.id = id;
        this.services = services;
    }

    public String getName(){
        return this.name;
    }

    public int getId(){
        return this.id;
    }

    /**
      * Assign this provider to a customer. Record the fact that he is busy.
      * @throws IllegalStateException if the provider is currently assigned to a job */
    protected void assignToCustomer() {
        if (busy == false) {
            busy = true;
            breakLeft = 3;
        } 
        else {
            throw new IllegalStateException("Already assigned!");
        }
    }
        
    /**
      * Free this provider up - is not longer assigned to a customer
      * @throws IllegalStateException if the provider is NOT currently assigned to a job */
    protected void endCustomerEngagement(){
        if(busy == true){
            busy = false;
        }
        else{
            throw new IllegalStateException("Not assigned!");
        }
    }

    /**
      * @param s add the given service to the set of services this provider can provide
      *          * @return true if it was added, false if not
      */
    protected boolean addService(Service s){
        // if(equals(s)){ //don't think this is needed
        return this.services.add(s);
    }
            // return true;
    //     }
    //     else{
    //         return false;
    //     }
    // }

    /**
      * @param s remove the given service to the set of services this provider can provide
      *          * @return true if it was removed, false if not
      */
    protected boolean removeService(Service s){
        // if(equals(s)){//don't think this is needed
        return this.services.remove(s);
    }
    //         return true;
    //     }
    //     else{
    //         return false;
    //     }
    // }

    /** *
      * @return a COPY of the set of services. MUST NOT return the Set instance itself, since that would allow a caller to then add/remove services
      */
    public Set<Service> getServices(){
        Set<Service> copy = new HashSet<>();
        copy.addAll(this.services);
        return copy;
    }

    protected void advanceBreak() {
      if (!busy && breakLeft > 0) {
        breakLeft--;
      }
    }

    protected boolean isBusy() {
      return busy;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this){
            return true;
        }
        if(o == null || o.getClass()!= this.getClass()){
            return false;
        }
        ServiceProvider provider = (ServiceProvider) o;

        return (provider.name.equals(this.name)  && provider.id == this.id);
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}