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
    private int jobTimeLeft = 0;
    private Set<Service> services;
    private boolean isStaged = false;

    /**
      *
      * @param name
      * @param id unique id of the ServiceProvider
      * @param services set of services this provider can provide */
    public ServiceProvider(String name, int id, Set<Service> services){

        this.name = name;
        this.id = id;
        this.services = new HashSet<>(services);
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
        if (busy) {
            throw new IllegalStateException("Already assigned!");
        } 
        else {
            busy = true;
            jobTimeLeft = 3;
        }
    }
        
    /**
      * Free this provider up - is not longer assigned to a customer
      * @throws IllegalStateException if the provider is NOT currently assigned to a job */
    protected void endCustomerEngagement() {
        if (busy == true){
            busy = false;
        } else {
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

    protected void advanceProgress() {
        if (!busy && jobTimeLeft > 0) {
            jobTimeLeft--;
        }
        if (jobTimeLeft == 0 && busy) {
            this.endCustomerEngagement();
        }
    }

    protected boolean isAvailable() {
        return !busy && !isStaged && jobTimeLeft == 0;
    }

    protected void setStaged(boolean isStaged) {
        this.isStaged = isStaged;
    }

    @Override
    public boolean equals(Object o){
      if (o instanceof ServiceProvider) {
        return o.hashCode() == this.hashCode();
      }
      return false;
    } 

    @Override
    public int hashCode() {
        return this.id;
    }
}
