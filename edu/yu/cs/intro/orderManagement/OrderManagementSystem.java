package edu.yu.cs.intro.orderManagement;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
/**
 * Takes orders, manages the warehouse as well as service providers
 */
public class OrderManagementSystem {
    private Warehouse warehouse;
    private Map<Service, Set<ServiceProvider>> serviceMap;
    private Set<ServiceProvider> providerSet;
    private Set<ServiceProvider> orderProviders;
    private int defaultProductStockLevel;

    /**
     * Creates a new Warehouse instance and calls the other constructor
     *
     * @param products
     * @param defaultProductStockLevel
     * @param serviceProviders
     */
    public OrderManagementSystem(Set<Product> products, int defaultProductStockLevel,
                                 Set<ServiceProvider> serviceProviders) {
        this(products, defaultProductStockLevel, serviceProviders, new Warehouse());
    }

    /**
     * 1) populate the warehouse with the products.
     * 2) retrieve set of services provided by the ServiceProviders, to save it as the set of
     services the business can provide
     * 3) create map of services to the List of service providers
     *
     * @param products - set of products to populate the warehouse with
     * @param defaultProductStockLevel - the default number of products to stock for any product
     * @param serviceProviders - set of service providers and the services they provide, to
    make up the services arm of the business
     * @param warehouse - the warehouse that we will store our products in
     */
    public OrderManagementSystem(Set<Product> products, int defaultProductStockLevel,
                                 Set<ServiceProvider> serviceProviders, Warehouse warehouse) {
        providerSet = new HashSet<>(serviceProviders);
        serviceMap = new HashMap<>();
        this.warehouse = warehouse;
        this.defaultProductStockLevel = defaultProductStockLevel;
        for (Product product : products) {
            warehouse.addNewProductToWarehouse(product, defaultProductStockLevel);
        }
        for (ServiceProvider provider : serviceProviders) {
            addServiceProvider(provider);
        }
    }

    /**
     * Accept an order:
     * 1) See if we have ServiceProviders for all Services in the order. If not, reject the order.
     * 2a) See if the we can fulfill the order for Items. If so, place the product orders with the warehouse and handle the service orders inside this class
     * 2b) We CAN fulfill a product order if either the warehouse currently has enough quantity in stock OR if the product is NOT on the "do not restock" list.
     *  In the case that the current quantity of a product is < the quantity in the order AND the product is NOT on the "do not restock" list, the order management system should
     *  first instruct the warehouse to restock the item, and then tell the warehouse to fulfill this order.
     * 4) Mark the order as completed
     * 5) Update busy status of serviced providers...
     */

    public void placeOrder(Order order) {
        Map<Service, Integer> services = new HashMap<>();
        Map<Product, Integer> products = new HashMap<>();

        for (Item item : order.getItems()) {
            if (item instanceof Service) {
                services.put((Service)item, order.getQuantity(item));
            } else {
                products.put((Product)item, order.getQuantity(item));
            }
        }

        int serviceTestResult = validateServices(services.keySet(), order);
        if (serviceTestResult == 1) {
            throw new IllegalStateException("Already assigned!");
        }
        int productTestResult = validateProducts(products.keySet(), order);
        if (!(serviceTestResult == 0 || productTestResult == 0)) {
            return;
        }

        // for (Entry<Service, Integer> entry : services.entrySet()) {
        //     Service service = entry.getKey();
        //     int quantity = entry.getValue();
        //     for (int i = 0; i < quantity; i++) {
        //         boolean gotProvider = false;
        //         for (ServiceProvider provider : providerSet) {
        //             if (provider.getServices().contains(service) && provider.isAvailable()) {
        //                 provider.assignToCustomer();
        //                 gotProvider = true;
        //                 break;
        //             }
        //         }
        //         if (!gotProvider) {
        //             throw new IllegalStateException("Already assigned!");
        //         }
        //     }
        // }

        for (ServiceProvider p : orderProviders) {
            p.assignToCustomer();
            p.setStaged(false);
        }

        for (ServiceProvider p : providerSet) {
            p.advanceProgress();
        }

        for (Entry<Product, Integer> entry : products.entrySet()) {
            warehouse.fulfill(entry.getKey().getItemNumber(), entry.getValue());
        }

        order.setCompleted(true);
    }
    /**
     * Validate that all the services being ordered can be provided. Make sure to check how many
     instances of a given service are being requested in the order, and see if we have enough providers
     for them.
     * @param services the set of services which are being ordered inside the order
     * @param order the order whose services we are validating
     * @return itemNumber of a requested service that we either do not have provider for at all, or
    for which we do not have an available provider. Return 0 if all services are valid.
     */
    protected int validateServices(Collection<Service> services, Order order) {
        // not using services parameter, instead using orderServices
        Map<Service, Integer> orderServices = new HashMap<>();
        orderProviders = new HashSet<>();

        for (Item item : order.getItems()) {
            if (item instanceof Service) {
                orderServices.put((Service)item, order.getQuantity(item));
            }
        }

        for (Entry<Service, Integer> entry : orderServices.entrySet()) {
            Service service = entry.getKey();
            int quantity = entry.getValue();
            for (int i = 0; i < quantity; i++) {
                boolean gotProvider = false;
                for (ServiceProvider provider : providerSet) {
                    if (provider.getServices().contains(service) && provider.isAvailable()) {
                        orderProviders.add(provider);
                        provider.setStaged(true);
                        gotProvider = true;
                        break;
                    }
                }
                if (!gotProvider) {
                    for (ServiceProvider p : providerSet) {
                        p.setStaged(false);
                    }
                    orderProviders.clear();
                    return service.getItemNumber();
                }
            }
        }
        return 0;
    }

    /**
     * validate that the requested quantity of products can be fulfilled
     * @param products being ordered in this order
     * @param order the order whose products we are validating
     * @return itemNumber of product which is either not in the catalog or which we have
    insufficient quantity of. Return 0 if we can fulfill.
     */
    protected int validateProducts(Collection<Product> products, Order order) {
        for (Item prods : order.getItems()){
            if (prods instanceof Product){
                if (!warehouse.canFulfill(prods.getItemNumber(),order.getQuantity(prods)) && (!warehouse.isRestockable(prods.getItemNumber()))){
                    return prods.getItemNumber();
                }     
            }
        }
        return 0;
    }

    /**
     * Adds new Products to the set of products that the warehouse can ship/fulfill
     * @param products the products to add to the warehouse
     * @return set of products that were actually added (don't include any products that were
    already in the warehouse before this was called!)
     */
    protected Set<Product> addNewProducts(Collection<Product> products) {
        Set<Product> newProds = new HashSet<Product>();
        Set<Product> PW = warehouse.getAllProductsInCatalog();
        for (Product prod : products){
            newProds.add(prod);
            PW.add(prod);
            //warehouse.addNewProductToWarehouse(prod, defaultProductStockLevel);
        }
        return newProds;
    }

    /**
     * Adds an additional ServiceProvider to the system. Update all relevant data about which
     Services are offered and which ServiceProviders provide which services
     * @param provider the provider to add
     */
    protected void addServiceProvider(ServiceProvider provider) {
        for (Service service : provider.getServices()){
            Set<ServiceProvider> providers = new HashSet<>();
            if (serviceMap.keySet().contains(service)){
                providers = serviceMap.get(service); //*Mistake
            }
            else {
                providers = new HashSet<>();
            }
            providers.add(provider);
            serviceMap.put(service, new HashSet<ServiceProvider>());//**Mistake
        }
    }
    /**
     *
     * @return get the set of all the products offered/sold by this business
     */
    public Set<Product> getProductCatalog() {
        return warehouse.getAllProductsInCatalog();
    }

    /**
     * @return get the set of all the Services offered/sold by this business
     */
    public Set<Service> getOfferedServices() {
        return new HashSet<Service>(serviceMap.keySet());
    }

    /**
     * Discontinue Item, i.e. stop selling a Service or Product.
     * Also prevent the Item from being added in the future.
     * If it's a Service - remove it from the set of provided services.
     * If it's a Product - still sell whatever instances of this Product are in stock, but do not
     restock it.
     * @param item the item to discontinue see {@link Item}
     */
    protected void discontinueItem(Item item) {
        if (item instanceof Service) {
            serviceMap.remove(item);
        }
        else if (item instanceof Product) {
            warehouse.doNotRestock(((Product)item).getItemNumber());
        }
    }

    /**
     * Set the default product stock level for the given product
     * @param prod
     * @param level
     */
    protected void setDefaultProductStockLevel(Product prod, int level) {
        warehouse.setDefaultStockLevel(prod.getItemNumber(), level);
    }
}



// package edu.yu.cs.intro.orderManagement;

// import java.util.Collection;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.Map;
// import java.util.Set;
// import java.util.Map.Entry;
// /**
// * Takes orders, manages the warehouse as well as service providers
// */
// public class OrderManagementSystem {
//     private Warehouse warehouse;
//     private Map<Service, Set<ServiceProvider>> serviceMap;
//     private Set<ServiceProvider> providerSet;
//     private Set<ServiceProvider> orderProviders; 

//     /**
//     * Creates a new Warehouse instance and calls the other constructor
//     *
//     * @param products
//     * @param defaultProductStockLevel
//     * @param serviceProviders
//     */
//     public OrderManagementSystem(Set<Product> products, int defaultProductStockLevel,
//     Set<ServiceProvider> serviceProviders) {
//         this(products, defaultProductStockLevel, serviceProviders, new Warehouse());
//     }

//     /**
//      * 1) populate the warehouse with the products.
//      * 2) retrieve set of services provided by the ServiceProviders, to save it as the set of
//     services the business can provide
//     * 3) create map of services to the List of service providers
//     *
//     * @param products - set of products to populate the warehouse with
//     * @param defaultProductStockLevel - the default number of products to stock for any product
//     * @param serviceProviders - set of service providers and the services they provide, to
//     make up the services arm of the business
//     * @param warehouse - the warehouse that we will store our products in
//     */
//     public OrderManagementSystem(Set<Product> products, int defaultProductStockLevel,
//     Set<ServiceProvider> serviceProviders, Warehouse warehouse) {
//         providerSet = new HashSet<>(serviceProviders);
//         this.warehouse = warehouse;
//         serviceMap = new HashMap<>();
//         for (Product product : products) {
//             warehouse.addNewProductToWarehouse(product, defaultProductStockLevel);
//         }
//         for (ServiceProvider provider : serviceProviders) {
//             addServiceProvider(provider);
//         }
//     }
    
//     /**
//      * Accept an order:
//      * 1) See if we have ServiceProviders for all Services in the order. If not, reject the order.
//      * 2a) See if the we can fulfill the order for Items. If so, place the product orders with the warehouse and handle the service orders inside this class
//      * 2b) We CAN fulfill a product order if either the warehouse currently has enough quantity in stock OR if the product is NOT on the "do not restock" list.
//      *  In the case that the current quantity of a product is < the quantity in the order AND the product is NOT on the "do not restock" list, the order management system should
//      *  first instruct the warehouse to restock the item, and then tell the warehouse to fulfill this order.
//     * 4) Mark the order as completed
//     * 5) Update busy status of serviced providers...
//     */
    
//     public void placeOrder(Order order) {
//         Set<Service> services = new HashSet<>();
//         Set<Product> products = new HashSet<>();
//         orderProviders = new HashSet<>();

//         for (Item item : order.getItems()) {
//             if (item instanceof Service) {
//                 services.add((Service)item);
//             } else {
//                 products.add((Product)item);
//             }
//         }

//         if (!(validateServices(services, order) == 0 || validateProducts(products, order) == 0)) {
//             return;
//         }

//         for (ServiceProvider provider : orderProviders) {
//             provider.assignToCustomer();
//         }
        
//         for (ServiceProvider provider : providerSet) {
//             provider.advanceProgress();
//         }
//         order.setCompleted(true);
//     }
//     /**
//      * Validate that all the services being ordered can be provided. Make sure to check how many
//     instances of a given service are being requested in the order, and see if we have enough providers
//     for them.
//     * @param services the set of services which are being ordered inside the order
//     * @param order the order whose services we are validating
//     * @return itemNumber of a requested service that we either do not have provider for at all, or
//     for which we do not have an available provider. Return 0 if all services are valid.
//     */
//     protected int validateServices(Collection<Service> services, Order order) {
//         // Map<Service, Integer> listOfServices = new HashMap<>();
//         // for (Item item : order.getItems()) {
//         //     if (item instanceof Service) {
//         //         listOfServices.put((Service)item, order.getQuantity(item));
//         //     }
//         // }

//         // for (Entry<Service, Integer> serviceEntry : listOfServices.entrySet()) {
//         //     Service service = serviceEntry.getKey();
//         //     int itemNumber = service.getItemNumber();
//         //     int quantity = serviceEntry.getValue();
//         //     if (!serviceMap.keySet().contains(service)) {
//         //         return itemNumber; // if we simply don't have the service, cancel the order
//         //     }
//         //     Set<ServiceProvider> providersForOrder = new HashSet<>();
//         //     for (ServiceProvider provider : serviceMap.get(service)) {//***Mistake
//         //         if (provider.isAvailable()) {
//         //             providersForOrder.add(provider);
//         //         }
//         //     }
//         //     if (quantity > providersForOrder.size()) {
//         //         return itemNumber; // if we don't have enough providers
//         //     }
//         // }
//         // return 0;
//         for (Service service : services) {
//             boolean gotProviderForService = false;
//             for (ServiceProvider provider : providerSet) {
//                 if (provider.getServices().contains(service) && provider.isAvailable()) {
//                     orderProviders.add(provider);
//                     gotProviderForService = true;
//                     break;
//                 }
//             }
//             if (!gotProviderForService) {
//                 return service.getItemNumber();
//             }
//         }
//         return 0;
//     }

//     /**
//      * validate that the requested quantity of products can be fulfilled
//      * @param products being ordered in this order
//      * @param order the order whose products we are validating
//      * @return itemNumber of product which is either not in the catalog or which we have
//     insufficient quantity of. Return 0 if we can fulfill.
//     */
//     protected int validateProducts(Collection<Product> products, Order order) {
//         int result = 0;
//         for (Item prods : order.getItems()){
//             if (prods instanceof Product){
//                 if (!warehouse.canFulfill(prods.getItemNumber(),order.getQuantity(prods))){
//                     result = 0;
//                     break;
//                 }
//                 result = prods.getItemNumber();  
//             }
//         }
//         return result;
//     }   

//     /**
//      * Adds new Products to the set of products that the warehouse can ship/fulfill
//      * @param products the products to add to the warehouse
//      * @return set of products that were actually added (don't include any products that were
//     already in the warehouse before this was called!)
//     */
//     protected Set<Product> addNewProducts(Collection<Product> products) {
//         Set<Product> newProds = new HashSet<Product>();
//         Set<Product> PW = warehouse.getAllProductsInCatalog();
//         for (Product prod : products){
//             newProds.add(prod);
//             PW.add(prod);
//         }
//         return newProds;
//     }

//     /**
//      * Adds an additional ServiceProvider to the system. Update all relevant data about which
//     Services are offered and which ServiceProviders provide which services
//     * @param provider the provider to add
//     */
//     protected void addServiceProvider(ServiceProvider provider) {
//       for (Service service : provider.getServices()){
//         Set<ServiceProvider> providers = new HashSet<>();
//            if (serviceMap.keySet().contains(service)){
//             providers = serviceMap.get(service); //*Mistake
//            }
//            else {
//             providers = new HashSet<>();
//            }
//                providers.add(provider);
//                serviceMap.put(service, new HashSet<ServiceProvider>());
//            }
//        }
//     /**
//      *
//      * @return get the set of all the products offered/sold by this business
//      */
//     public Set<Product> getProductCatalog() {
//         return warehouse.getAllProductsInCatalog();
//     }

//     /**
//      * @return get the set of all the Services offered/sold by this business
//      */
//     public Set<Service> getOfferedServices() {
//         return new HashSet<Service>(serviceMap.keySet());
//     }

//     /**
//      * Discontinue Item, i.e. stop selling a Service or Product.
//      * Also prevent the Item from being added in the future.
//      * If it's a Service - remove it from the set of provided services.
//      * If it's a Product - still sell whatever instances of this Product are in stock, but do not
//     restock it.
//     * @param item the item to discontinue see {@link Item}
//     */
//     protected void discontinueItem(Item item) {
//         if (item instanceof Service) {
//             serviceMap.remove(item);
//         } 
//         else if (item instanceof Product) {
//             warehouse.doNotRestock(((Product)item).getItemNumber());
//         }
//     }

//     /**
//      * Set the default product stock level for the given product
//      * @param prod
//      * @param level
//      */
//     protected void setDefaultProductStockLevel(Product prod, int level) {
//         warehouse.setDefaultStockLevel(prod.getItemNumber(), level);
//     }
// }