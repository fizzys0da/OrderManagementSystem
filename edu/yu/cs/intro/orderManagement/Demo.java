package edu.yu.cs.intro.orderManagement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Demo {
    private Set<Product> products;
    private Set<ServiceProvider> providers;
    private Set<Service> allServices;
    private Map<Integer, Product> idToProduct;
    private Map<Integer, Service> idToService;
    private Warehouse warehouse;
    
    public static void main(String[] args) {
        Demo dd = new Demo();
        dd.runDemo();
    }
    public Demo(){
        this.warehouse = new Warehouse();
        this.products = new HashSet<>();
        this.idToProduct = new HashMap<>();
        this.idToService = new HashMap<>();
        this.allServices = new HashSet<>();
        this.providers = new HashSet<>();
    }
    void runDemo(){
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers,this.warehouse);
        //populate our system with products and services
        this.createDemoProducts();
        system.addNewProducts(this.products);
        this.createDemoServiceProviders();
        for(ServiceProvider p : this.providers){
            system.addServiceProvider(p);
        }
        //make sure all the products added are in the catalog
        Set<Product> catalog = system.getProductCatalog();
        assert this.products.size() == catalog.size();
        assert catalog.containsAll(this.products);
        //make sure all the services are in the services offered
        Set<Service> services = system.getOfferedServices();
        assert this.allServices.size() == services.size();
        assert services.containsAll(this.allServices);
        //create an order
        Order order = new Order();
        order.addToOrder(this.idToProduct.get(1),3); //will use out of 5 of product #1
        order.addToOrder(this.idToService.get(6),1); //will use the only service provider for #6
        system.placeOrder(order);
        assert this.warehouse.getStockLevel(1) == 2;
        assert order.isCompleted();
        //place another order, should throw IllegalStateException
        order = new Order();
        order.addToOrder(this.idToService.get(6),1); //provider for #6 not available - should throw
        boolean caught = false;
        try{
            system.placeOrder(order);
        }catch (IllegalStateException e){
            caught = true;
        }
        assert caught;
        assert !order.isCompleted();
        //force it to throw exception for ordering more than available of a discontinued item
        system.discontinueItem(this.idToProduct.get(1));
        order = new Order();
        order.addToOrder(this.idToProduct.get(1),3); //only 2 left of product #1
        caught = false;
        try{
            system.placeOrder(order);
        }catch (IllegalArgumentException e){
            caught = true;
        }
        assert caught;
        assert !order.isCompleted();
        //order more than available of a current item, make sure it ups the stock level and fulfills it
        assert this.warehouse.getStockLevel(2) == 5;
        order = new Order();
        order.addToOrder(this.idToProduct.get(2),10);
        system.placeOrder(order);
        assert order.isCompleted();
        assert this.warehouse.getStockLevel(2) == 0;
        this.warehouse.restock(2,10);
        assert this.warehouse.getStockLevel(2) == 10;
        //place 2 more order2 to make 3 orders since service provider for 6 was all busy. Should then be able to place order for service #6
        order = new Order();
        order.addToOrder(this.idToProduct.get(3),1);
        system.placeOrder(order);
        assert order.isCompleted();
        order = new Order();
        order.addToOrder(this.idToProduct.get(4),1);
        system.placeOrder(order);
        assert order.isCompleted();
        order = new Order();
        order.addToOrder(this.idToService.get(6),1);
        system.placeOrder(order);
        assert order.isCompleted();
    }

    private void createDemoProducts(){
        this.products.add(new Product("prod1",1,1));
        this.products.add(new Product("prod2",2,2));
        this.products.add(new Product("prod3",3,3));
        this.products.add(new Product("prod4",4,4));
        this.products.add(new Product("prod5",5,5));
        this.products.add(new Product("prod6",6,6));
        this.products.add(new Product("prod7",7,7));
        for(Product p : this.products){
            this.idToProduct.put(p.getItemNumber(),p);
        }
    }

    private void createDemoServiceProviders(){
        Service s1 = new Service(1,1,1,"srvc1");
        Service s2 = new Service(2,1,2,"srvc2");
        Service s3 = new Service(3,1,3,"srvc3");
        Service s4 = new Service(4,1,4,"srvc4");
        Service s5 = new Service(5,1,5,"srvc5");
        Service s6 = new Service(6,1,6,"srvc6");
        Set<Service> srvcSetAll = new HashSet<>();
        srvcSetAll.add(s1);
        srvcSetAll.add(s2);
        srvcSetAll.add(s3);
        srvcSetAll.add(s4);
        srvcSetAll.add(s5);
        srvcSetAll.add(s6);
        this.allServices.addAll(srvcSetAll);
        for(Service srvc : this.allServices){
            this.idToService.put(srvc.getItemNumber(),srvc);
        }
        this.providers.add(new ServiceProvider("p1",1,srvcSetAll));
        Set<Service> srvcSetThree = new HashSet<>();
        srvcSetAll.add(s1);
        srvcSetAll.add(s2);
        srvcSetAll.add(s3);
        this.providers.add(new ServiceProvider("p2",2,srvcSetThree));
        Set<Service> singleService = new HashSet<>();
        srvcSetAll.add(s1);
        this.providers.add(new ServiceProvider("p2",3,singleService));
    }
}