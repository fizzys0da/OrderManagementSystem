package edu.yu.cs.intro.orderManagement;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Assignment8Tests {

    class IncorrectBehaviorException extends RuntimeException{
        public IncorrectBehaviorException(String message){
            super(message);
        }
    };

    private TestHelper helper;
    private String output;
    private int pointsEarned;
    private int possiblePoints;

    private Set<Product> products;
    private Set<ServiceProvider> providers;
    private Set<Service> allServices;
    private Map<Integer, Product> idToProduct;
    private Map<Integer, Service> idToService;

    protected static final String PASSED = "+++++TEST PASSED+++++\n\n";
    protected static final String FAILED = "+++++TEST FAILED+++++\n\n";

    public static void main(String[] args) throws IOException {
        Assignment8Tests a8t = new Assignment8Tests();
        a8t.runTests();
        writeResultsToFile(a8t.pointsEarned,a8t.possiblePoints,a8t.output);
    }

    private void initHelperAndOutput(){
        this.helper = new TestHelper();
        this.helper.setCharDifLimit(12);
        this.output = "";
        this.pointsEarned = 0;
        this.possiblePoints = 0;
    }

    private void initTestData(){
        this.products = new HashSet<>();
        this.idToProduct = new HashMap<>();
        this.idToService = new HashMap<>();
        this.allServices = new HashSet<>();
        this.providers = new HashSet<>();
        this.createDemoProducts();
        this.createDemoServiceProviders();
    }

    public Assignment8Tests() {
        this.initHelperAndOutput();

    }
    private void runTest(String name, String description) {
        this.runTest(12,name,description);
    }
    
    private void runTest(int points, String name, String description){
        try {
            this.output += "TEST NAME: " + name + "\n";
            this.output += "TEST DESCRIPTION: " + description + "\n";
            this.output += "TEST POINT VALUE: " + points + "\n";
            this.possiblePoints += points;
            this.helper.runMethod(Assignment8Tests.class,this,name);
            this.output += PASSED;
            this.pointsEarned += points;
        }
        catch (Throwable e) {
            this.output += this.helper.getExceptionOutput(e.getCause());
            this.output += FAILED;
        }
    }

    public void runTests() {
        //tests on OrderManagementSystem:
        this.runTest("testAddGetCatalog","OrderManagementSystem: Check that adding products to the catalog, and retrieving them, all work properly");
        this.runTest("testAddGetSevices", "OrderManagementSystem: Check that adding services, and retrieving them, all work properly");
        this.runTest("placeSuccessfulServiceOrder", "OrderManagementSystem: Place an order for services that the system should be able to fulfill. Make sure Providers are marked busy.");
        this.runTest("placeUnsuccessfulServiceOrder", "OrderManagementSystem: Place an order for services that the system should be unable to fulfill. Should throw an exception.");
        this.runTest("placeSuccessfulProductOrder", "OrderManagementSystem: Place an order for products that the system should be able to fulfill.");
        this.runTest("placeProductOrderNoSuchProduct", "OrderManagementSystem: Place an order for products that the system should NOT be able to fulfill - no such product.");
        this.runTest("placeProductOrderDoNotRestock", "OrderManagementSystem: Place an order for products that the system should NOT be able to fulfill - ordered more than in stock, its on the do not restock list, and we ordered more than was in stock.");
        this.runTest("successfulValidateServices", "OrderManagementSystem: call validateServices in a situation in which it should return 0.");
        this.runTest("unsuccessfulValidateServicesNotEnoughProviders", "OrderManagementSystem: call validateServices in a situation in which it should find we don't have enough providers for a given service");
        this.runTest("unsuccessfulValidateServicesProvidersBusy", "OrderManagementSystem: call validateServices in a situation in which providers for a given service are busy");
        this.runTest("successfulValidateProducts", "OrderManagementSystem: call validateProducts in a situation in which it should return 0.");
        this.runTest("unsuccessfulValidateProductsItemDiscontinued", "OrderManagementSystem: call validateProducts in a situation in which it should fail because item is discontinued and we ordered more than are in stock.");
        //test Warehouse
        this.runTest("simpleAddProductToWarehouse", "Warehouse: add product to warehouse, see that it is there and at the specified stock level.");
        this.runTest("simpleAddProductsToWarehouse", "Warehouse: add multiple products to warehouse, see that they are there and at the specified stock level.");
        this.runTest("addProductAlreadyInWarehouse", "Warehouse: add same product to warehouse multiple times, should throw an IllegalArgumentException.");
        this.runTest("addProductWhichIsOnDoNotRestockList", "Warehouse: add product to warehouse which is on doNotRestock list, should throw an IllegalArgumentException.");
        this.runTest("restockAndGetStockLevel", "Warehouse: make sure calling warehouse.setDefaultStockLevel with level > current level increases the current level");
        this.runTest("restockToLowerAndGetStockLevel", "Warehouse: make sure calling warehouse.setDefaultStockLevel with level < current level doesn't decrease the level without orders being placed");
        this.runTest("setStockLevelRunDownViaOrdersReachNewLevel", "Warehouse: placing orders that lower stock level to zero should cause stock level to rise to whatever the default stock level has been set to for the given item");
        this.runTest("runDownStockViaOrdersRestoreToDefaultLevel", "Warehouse: placing orders that lower stock level to zero should cause stock level to rise to whatever the default stock level is for the given item");
        this.runTest("testIsInCatalog", "Warehouse: adding a product to the catalog should result in a call to warehouse.isInCatalog with its product number returns true");
        this.runTest("testIsRestockable", "Warehouse: calling warehouse.isRestockable on a product in the catalog should return true, unless warehouse.doNotRestock has been called, in which case it should return false");
        this.runTest("testCanFulfill", "Warehouse: calling warehouse.canFullfill should return false if the product is not in the catalog or there are fewer than quantity of the products in the catalog. Otherwise it should return true.");
        this.runTest("testFulfill", "Warehouse: calling warehouse.fullfill should work when there are enough of the product, lower the stock by the right amount, and throw an IllegalArgumentException if there is not enough in stock");
        //test Order
        this.runTest("testGetItems", "Order: check if order.getItems returns the right number, and right set, of products and services");
        //"You MUST implement equals and hashCode for both the Product class and the Service class. It is only their itemNumber (a.k.a. serviceID or productID)  that uniquely identifies them, and you must take that into account when you write the equals and hashCode methods"
        this.runTest("testProductEqualsHashcode", "Product: equals and hashCode should both be determined exclusively by productID");
        this.runTest("testServiceEqualsHashcode", "Service: equals and hashCode should both be determined exclusively by serviceID");
        this.runTest("testAddRemoveGetServices", "ServiceProvider: adding, removing, and getting services");
        this.runTest("testAssignEndCustomerEngagement", "ServiceProvider: assign / end customer engagement");
        this.runTest("testEqualsHashcodeServiceProvider", "ServiceProvider: equals and hashCode should both be determined exclusively by ID");
    }

    //equals, hashCode - uniquely identified by ID
    public void testEqualsHashcodeServiceProvider(){
        Service s1 = new Service(1,1,1,"srvc1");
        Service s2 = new Service(2,2,1,"srvc2");
        if(!(s1.equals(s2) && s2.equals(s1))){
            throw new IncorrectBehaviorException("Two ServiceProviders must be equal if they have the same serviceID, regardless of description, price, and number of hours");
        }
        if(s1.hashCode() != s2.hashCode()){
            throw new IncorrectBehaviorException("Two ServiceProviders must have the same hashCode if they have the same serviceID, regardless of description, price, and number of hours");
        }
    }

    //assign / end customer engagement
    public void testAssignEndCustomerEngagement(){
        Service s1 = new Service(1,1,1,"srvc1");
        HashSet<Service> services = new HashSet<>();
        services.add(s1);
        ServiceProvider provider = new ServiceProvider("test provider",1,services);
        provider.assignToCustomer();
        provider.endCustomerEngagement();
        provider.assignToCustomer();
        try{
            provider.assignToCustomer();
            throw new IncorrectBehaviorException("assigning a ServiceProvider to a customer after it has already been assigned should've thrown an IllegalStateException");
        }catch(IllegalStateException e){}
        provider.endCustomerEngagement();
        try{
            provider.endCustomerEngagement();
            throw new IncorrectBehaviorException("ServiceProvider.endCustomerEngagement when it is not assigned should've thrown an IllegalStateException");
        }catch(IllegalStateException e){}
    }

    //add, remove, and get services
    public void testAddRemoveGetServices(){
        Service s1 = new Service(1,1,1,"srvc1");
        Service s2 = new Service(2,1,2,"srvc2");
        Service s3 = new Service(3,1,3,"srvc3");
        HashSet<Service> services = new HashSet<>();
        services.add(s1);
        services.add(s2);
        ServiceProvider provider = new ServiceProvider("test provider",1,services);
        //has both in it?
        Set<Service> svcs = provider.getServices();
        int size = svcs.size();
        if(svcs.size() != 2){
            throw new IncorrectBehaviorException("ServiceProvider should've had 2 services in it, but size of set returned was " + size);
        }
        if(!svcs.contains(s1)){
            throw new IncorrectBehaviorException("ServiceProvider should've contained " + s1.getDescription());
        }
        if(!svcs.contains(s2)){
            throw new IncorrectBehaviorException("ServiceProvider should've contained " + s2.getDescription());
        }
        //remove one of them, see if only one, the correct one, left
        provider.removeService(s1);
        svcs = provider.getServices();
        size = svcs.size();
        if(svcs.size() != 1){
            throw new IncorrectBehaviorException("ServiceProvider should've had 1 service in it after one was removed, but size of set returned was " + size);
        }
        if(!svcs.contains(s2)){
            throw new IncorrectBehaviorException("ServiceProvider should've contained " + s2.getDescription());
        }
        //add two more, see that all three are there
        provider.addService(s1);
        provider.addService(s3);
        svcs = provider.getServices();
        size = svcs.size();
        if(svcs.size() != 3){
            throw new IncorrectBehaviorException("ServiceProvider should've had 3 services in it after one was removed, but size of set returned was " + size);
        }
        if(!svcs.contains(s1)){
            throw new IncorrectBehaviorException("ServiceProvider should've contained " + s1.getDescription());
        }
        if(!svcs.contains(s2)){
            throw new IncorrectBehaviorException("ServiceProvider should've contained " + s2.getDescription());
        }
        if(!svcs.contains(s3)) {
            throw new IncorrectBehaviorException("ServiceProvider should've contained " + s3.getDescription());
        }
    }

    public void testServiceEqualsHashcode(){
        Service s2 = new Service(2,1,2,"srvc2");
        Service s4 = new Service(4,1,2,"srvc4");
        if( !(s2.equals(s4) && s4.equals(s2)) ){
            throw new IncorrectBehaviorException("two services with the same serviceID would be equal, regardless of name or price");
        }
        if(s2.hashCode() != s4.hashCode()){
            throw new IncorrectBehaviorException("two services with the same serviceID should have the same hashCode, regardless of name or price");
        }
    }

    public void testProductEqualsHashcode(){
        Product p1 = new Product("prod1",1,1);
        Product p2 = new Product("prod2",2,1);
        if( !(p1.equals(p2) && p2.equals(p1)) ){
            throw new IncorrectBehaviorException("two products with the same productID would be equal, regardless of name or price");
        }
        if(p1.hashCode() != p2.hashCode()){
            throw new IncorrectBehaviorException("two products with the same productID should have the same hashCode, regardless of name or price");
        }
    }

    public void getTotalPrices(){
        Product p1 = new Product("prod1",1,1);
        Product p2 = new Product("prod2",2,2);
        Service s2 = new Service(2,1,2,"srvc2");
        Service s4 = new Service(4,1,4,"srvc4");
        //Services and Products
        Order order = new Order();
        order.addToOrder(p1,1);
        order.addToOrder(p2,2);
        order.addToOrder(s2,2);
        order.addToOrder(s4,2);
        double price = order.getProductsTotalPrice();
        if(price != 5){
            throw new IncorrectBehaviorException("order.getProductsTotalPrice should've returned 5, but returned " + price);
        }
        price = order.getServicesTotalPrice();
        if(price != 16){
            throw new IncorrectBehaviorException("order.getServicesTotalPrice should've returned 16, but returned " + price);
        }
    }

    public void testGetItems(){
        Product p1 = new Product("prod1",1,1);
        Product p2 = new Product("prod2",2,2);
        Product p3 = new Product("prod3",3,3);
        Order order = new Order();
        //Product only
        order.addToOrder(p1,1);
        order.addToOrder(p2,2);
        order.addToOrder(p3,3);
        Item[] items = order.getItems();
        if(items.length != 3){
            throw new IncorrectBehaviorException("order.getItems should've returned an array of length 3, but returned and array of length " + items.length);
        }
        List<Item> itemList = Arrays.asList(items);
        if(!itemList.contains(p1) || !itemList.contains(p2) || !itemList.contains(p3)){
            throw new IncorrectBehaviorException("product missing from array returned by order.getItems");
        }
        //Services only
        Service s2 = new Service(2,1,2,"srvc2");
        Service s4 = new Service(4,1,4,"srvc4");
        order = new Order();
        order.addToOrder(s2,2);
        order.addToOrder(s4,2);
        items = order.getItems();
        if(items.length != 2){
            throw new IncorrectBehaviorException("order.getItems should've returned an array of length 2, but returned and array of length " + items.length);
        }
        itemList = Arrays.asList(items);
        if(!itemList.contains(s4) || !itemList.contains(s2)){
            throw new IncorrectBehaviorException("service missing from array returned by order.getItems");
        }
        //Services and Products
        order = new Order();
        order.addToOrder(p1,1);
        order.addToOrder(p2,2);
        order.addToOrder(s2,2);
        order.addToOrder(s4,2);
        items = order.getItems();
        if(items.length != 4){
            throw new IncorrectBehaviorException("order.getItems should've returned an array of length 4, but returned and array of length " + items.length);
        }
        itemList = Arrays.asList(items);
        if(!itemList.contains(s4) || !itemList.contains(s2) || !itemList.contains(p1) || !itemList.contains(p2)){
            throw new IncorrectBehaviorException("item missing from array returned by order.getItems");
        }
    }

    public void testFulfill() {
        //does fulfilling an order reduce change the stock by the right amount?
        Warehouse warehouse = new Warehouse();
        Product prod2 = new Product("prod2",2,2);
        warehouse.addNewProductToWarehouse(prod2,10);
        warehouse.fulfill(2,5);
        int level = warehouse.getStockLevel(2);
        if(level!=5){
            throw new IncorrectBehaviorException("There should be 5 of the given product in stock, but warehouse.getStockLevel returned " + level);
        }
        //does it throw when appropriate?
        try {
            warehouse.fulfill(2, 6);
            throw new IncorrectBehaviorException("There should be 5 of the given product in stock, so calling warehouse.fulfill with quantity=6 should throw an IllegalArgumentException, but it did not");
        }catch(IllegalArgumentException e){}
    }

        public void testCanFulfill(){
        Warehouse warehouse = new Warehouse();
        Product prod2 = new Product("prod2",2,2);
        warehouse.addNewProductToWarehouse(prod2, 5);
        if(!warehouse.canFulfill(2,4)){
            throw new IncorrectBehaviorException("There should be 5 of the given product in stock, but warehouse.canFulfill returned false for quantity == 4");
        }
        if(warehouse.canFulfill(2,40)){
            throw new IncorrectBehaviorException("There should be 5 of the given product in stock, but warehouse.canFulfill returned true for quantity == 40");
        }
        if(warehouse.canFulfill(20,1)){
            throw new IncorrectBehaviorException("The given product was never added to the warehouse, but warehouse.canFulfill returned true");
        }
    }

    public void testIsRestockable(){
        Warehouse warehouse = new Warehouse();
        Product prod2 = new Product("prod2",2,2);
        warehouse.addNewProductToWarehouse(prod2, 5);
        if(!warehouse.isRestockable(2)){
            throw new IncorrectBehaviorException("Product was added to the warehouse but then warehouse.isRestockable returned false");
        }
        warehouse.doNotRestock(2);
        if(warehouse.isRestockable(2)){
            throw new IncorrectBehaviorException("Product was added to the warehouse but then warehouse.doNotRestock was called on it, but then warehouse.isRestockable returned true");
        }
    }

    public void testIsInCatalog(){
        Warehouse warehouse = new Warehouse();
        Product prod2 = new Product("prod2",2,2);
        warehouse.addNewProductToWarehouse(prod2, 5);
        if(!warehouse.isInCatalog(2)){
            throw new IncorrectBehaviorException("Product was added to the warehouse but then warehouse.isInCatalog returned false");
        }
    }

    public void runDownStockViaOrdersRestoreToDefaultLevel(){
        Warehouse warehouse = new Warehouse();
        Product prod2 = new Product("prod2",2,2);
        warehouse.addNewProductToWarehouse(prod2, 5);
        warehouse.fulfill(2,4);
        warehouse.fulfill(2,1);
        warehouse.restock(2,1);

        //should be 5
        int level = warehouse.getStockLevel(2);
        if(warehouse.getStockLevel(2)!= 5){
            throw new IncorrectBehaviorException("Stock level of product 2 was initially 5, 4 were ordered and then 1 was ordered, warehouse.getStockLevel should now return 5, but it returned " + level);
        }
    }

    public void setStockLevelRunDownViaOrdersReachNewLevel(){
        Warehouse warehouse = new Warehouse();
        Product prod2 = new Product("prod2",2,2);
        warehouse.addNewProductToWarehouse(new Product("prod1",1,1), 5);
        warehouse.addNewProductToWarehouse(prod2, 5);
        warehouse.addNewProductToWarehouse(new Product("prod3",3,3), 5);
        warehouse.fulfill(2,4);
        warehouse.setDefaultStockLevel(2,10);
        warehouse.restock(2,3);
        warehouse.fulfill(2,3);

        //should be 8
        int level = warehouse.getStockLevel(2);
        if(warehouse.getStockLevel(2)!= 7){
            throw new IncorrectBehaviorException("Stock level of product 2 was initially 5, 4 were ordered, defaultStockLevel was raised to 10, 3 more ordered, warehouse.getStockLevel should now return 7, but it returned " + level);
        }
    }

    public void restockAndGetStockLevel(){
        Warehouse warehouse = new Warehouse();
        warehouse.addNewProductToWarehouse(new Product("prod1",1,1), 5);
        warehouse.addNewProductToWarehouse(new Product("prod2",2,2), 5);
        warehouse.addNewProductToWarehouse(new Product("prod3",3,3), 5);
        warehouse.setDefaultStockLevel(1,10);
        warehouse.restock(1,7);
        int level = warehouse.getStockLevel(1);
        if(level!= 10){
            throw new IncorrectBehaviorException("Stock level of product 1 was initially 5, then set to 10 using warehouse.setDefaultStockLevel and it was then restocked, but warehouse.getStockLevel returned " + level);
        }
    }

    public void restockToLowerAndGetStockLevel(){
        Warehouse warehouse = new Warehouse();
        warehouse.addNewProductToWarehouse(new Product("prod1",1,1), 5);
        warehouse.addNewProductToWarehouse(new Product("prod2",2,2), 5);
        warehouse.addNewProductToWarehouse(new Product("prod3",3,3), 5);
        warehouse.setDefaultStockLevel(1,1);
        int level = warehouse.getStockLevel(1);
        if(warehouse.getStockLevel(1)!= 5){
            throw new IncorrectBehaviorException("Stock level of product 1 was initially 5, then set to 1 using warehouse.setDefaultStockLevel, but warehouse.getStockLevel returned " + level);
        }
    }

    public void addProductWhichIsOnDoNotRestockList(){
        Warehouse warehouse = new Warehouse();
        Product p1 = new Product("prod1",1,1);
        warehouse.addNewProductToWarehouse(p1,1);
        warehouse.doNotRestock(1);
        try {
            warehouse.addNewProductToWarehouse(p1, 1);
            throw new IncorrectBehaviorException("Added product which was added to doNotRestock - should've thrown an IllegalArgumentException");
        }catch (IllegalArgumentException e){}
    }

    public void addProductAlreadyInWarehouse(){
        Warehouse warehouse = new Warehouse();
        Product p1 = new Product("prod1",1,1);
        warehouse.addNewProductToWarehouse(p1,1);
        try {
            warehouse.addNewProductToWarehouse(p1, 1);
            throw new IncorrectBehaviorException("Added same product twice to the warehouse - should've thrown an IllegalArgumentException");
        }catch (IllegalArgumentException e){}

    }

    private void confirmPresence(Warehouse warehouse, Product prod){
        if(!warehouse.getAllProductsInCatalog().contains(prod)){
            throw new IncorrectBehaviorException(prod.getDescription() + " was added to warehouse but was not present in set returned by Warehouse.getAllProductsInCatalog");
        }
    }

    private void confirmLevel(Warehouse warehouse, Product prod, int expectedLevel){
        int level = warehouse.getStockLevel(prod.getItemNumber());
        if( level != expectedLevel){
            throw new IncorrectBehaviorException("expected " + expectedLevel + " of " + prod.getDescription() + " to be in warehouse, but instead of Warehouse.getStockLevel returning " + expectedLevel +  ", it returned " + level);
        }
    }

    private void addAndConfirm(Warehouse warehouse, Product prod, int level){
        warehouse.addNewProductToWarehouse(prod,level);
        this.confirmPresence(warehouse,prod);
        this.confirmLevel(warehouse,prod,level);
    }

    public void simpleAddProductToWarehouse(){
        Warehouse warehouse = new Warehouse();
        Product p1 = new Product("prod1",1,1);
        //add products to warehouse - should work fine
        this.addAndConfirm(warehouse,p1,10);
    }

    public void simpleAddProductsToWarehouse(){
        Warehouse warehouse = new Warehouse();
        Product p1 = new Product("prod1",1,1);
        Product p2 = new Product("prod2",2,2);
        Product p3 = new Product("prod3",3,3);
        //add products to warehouse - should work fine
        this.addAndConfirm(warehouse,p1,10);
        this.addAndConfirm(warehouse,p2,15);
        this.addAndConfirm(warehouse,p3,1);
        int size = warehouse.getAllProductsInCatalog().size();
        if( size != 3){
            throw new IncorrectBehaviorException("added 3 items to warehouse, but catalog contains " + size + " products");
        }
    }

    public void successfulValidateProducts(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Order order = new Order();
        Product p1 = this.idToProduct.get(1);
        Product p2 = this.idToProduct.get(2);
        order.addToOrder(p1,15);
        order.addToOrder(p2,4);
        ArrayList<Product> products = new ArrayList<>();
        products.add(p1);
        products.add(p2);
        int ret = system.validateProducts(products,order);
        if(ret != 0){
            throw new IncorrectBehaviorException("validateProducts should've returned 0, but returned " + ret);
        }
    }
    public void unsuccessfulValidateProductsItemDiscontinued(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Order order = new Order();
        Product p1 = this.idToProduct.get(1);
        Product p2 = this.idToProduct.get(2);
        order.addToOrder(p1,15);
        order.addToOrder(p2,4);
        ArrayList<Product> products = new ArrayList<>();
        products.add(p1);
        products.add(p2);
        system.discontinueItem(p1);
        int ret = system.validateProducts(products,order);
        if(ret != 1){
            throw new IncorrectBehaviorException("validateServices should've returned 1, but returned " + ret);
        }
    }

    public void successfulValidateServices(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Order order = new Order();
        Service srvc1 = this.idToService.get(1);
        Service srvc2 = this.idToService.get(2);
        order.addToOrder(srvc1,1);
        order.addToOrder(srvc2,1);
        ArrayList<Service> srvcs = new ArrayList<>();
        srvcs.add(srvc1);
        srvcs.add(srvc1);
        int ret = system.validateServices(srvcs,order);
        if(ret != 0){
            throw new IncorrectBehaviorException("validateServices should've returned 0, but returned " + ret);
        }
    }

    public void unsuccessfulValidateServicesNotEnoughProviders(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Order order = new Order();
        Service srvc1 = this.idToService.get(1);
        Service srvc2 = this.idToService.get(2);
        Service srvc6 = this.idToService.get(6);
        order.addToOrder(srvc1,2);
        order.addToOrder(srvc2,2);
        order.addToOrder(srvc6,1);
        ArrayList<Service> srvcs = new ArrayList<>();
        srvcs.add(srvc1);
        srvcs.add(srvc2);
        srvcs.add(srvc6);
        int ret = system.validateServices(srvcs,order);
        if(ret != 6 && ret != 2){
            throw new IncorrectBehaviorException("validateServices should've returned 6 or 2, but returned " + ret);
        }
    }
    public void unsuccessfulValidateServicesProvidersBusy(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Order order = new Order();
        Service srvc1 = this.idToService.get(1);
        order.addToOrder(srvc1,3);
        system.placeOrder(order);
        //now validate again, now that the service providers are busy
        order = new Order();
        order.addToOrder(srvc1,1);
        ArrayList<Service> srvcs = new ArrayList<>();
        srvcs.add(srvc1);
        int ret = system.validateServices(srvcs,order);
        if(ret != 1){
            throw new IncorrectBehaviorException("validateServices should've returned 1, but returned " + ret);
        }
    }

    public void placeSuccessfulProductOrder(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Order order = new Order();
        order.addToOrder(new Product("prod1",1,1),1);
        order.addToOrder(new Product("prod2",2,2),1);
        order.addToOrder(new Product("prod3",3,3),1);
        system.placeOrder(order);
        if(!order.isCompleted()){
            throw new IncorrectBehaviorException("Successful order should've been marked as completed");
        }
    }

    public void placeProductOrderNoSuchProduct(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Order order = new Order();
        order.addToOrder(new Product("prod1",1,1),1);
        order.addToOrder(new Product("prod88",88,88),1); //no such product
        try {
            system.placeOrder(order);
            throw new IncorrectBehaviorException("Should NOT have been able to fulfill the order due to there being no such product, and thus should've thrown an IllegalArgumentException");
        }catch(IllegalArgumentException e){}
    }
    public void placeProductOrderDoNotRestock(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Product prod1 = new Product("prod1",1,1);
        system.discontinueItem(prod1);
        Order order = new Order();
        order.addToOrder(prod1,15);
        try {
            system.placeOrder(order);
            throw new IncorrectBehaviorException("Should NOT have been able to fulfill the order due to the product being discontinued and not having enough leftover stock to fulfill, and thus should've thrown an IllegalArgumentException");
        }catch(IllegalArgumentException e){}
    }

    public void placeUnsuccessfulServiceOrder(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Order order = new Order();
        Service svc = new Service(1,1,1,"srvc1");
        order.addToOrder(svc,5);
        try {
            system.placeOrder(order);
            throw new IncorrectBehaviorException("Should NOT have been able to fulfill the order due to insufficient ServiceProviders, and thus should've thrown an IllegalStateException");
        }catch(IllegalStateException e){}
    }

    public void placeSuccessfulServiceOrder(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        Order order = new Order();
        Service svc = new Service(1,1,1,"srvc1");
        order.addToOrder(svc,3);
        system.placeOrder(order);
        for(ServiceProvider sp : this.providers){
            if(sp.getServices().contains(svc)){
                try{
                    sp.assignToCustomer();
                    throw new IncorrectBehaviorException("ServiceProvider " + sp.getId() + " should've been assigned to fulfilling service " + svc.getItemNumber() + " but did not throw IllegalStateException when attempted to reassign");
                }catch(IllegalStateException e){
                }
            }
        }
        if(!order.isCompleted()){
            throw new IncorrectBehaviorException("Successful order should've been marked as completed");
        }
    }

    public void testAddGetCatalog(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        this.catalogMatchesExpectedSet(system);
        //make sure that adding duplicates doesn't result in more products in the catalog
        system.addNewProducts(this.products);
        this.catalogMatchesExpectedSet(system);
    }
    private void catalogMatchesExpectedSet(OrderManagementSystem system){
        //populate our system with products and services
        system.addNewProducts(this.products);
        //make sure all the products added are in the catalog
        Set<Product> catalog = system.getProductCatalog();
        if(this.products.size() != catalog.size() || !catalog.containsAll(this.products)){
            throw new IncorrectBehaviorException("Incorrect Set of Products returned from OrderManagementSystem.getProductCatalog after products were added via OrderManagementSystem.addNewProducts");
        }
    }

    public void testAddGetSevices(){
        this.initTestData();
        OrderManagementSystem system = new OrderManagementSystem(this.products,5,this.providers);
        this.servicesMatchExpectedSet(system);
        //make sure that adding duplicates doesn't result in service providers being duplicated
        for(ServiceProvider p : this.providers){
            system.addServiceProvider(p);
        }
        this.servicesMatchExpectedSet(system);
    }
    private void servicesMatchExpectedSet(OrderManagementSystem system) {
        //make sure all the services are in the services offered
        Set<Service> services = system.getOfferedServices();
        if(this.allServices.size() != services.size() || !services.containsAll(this.allServices)){
            throw new IncorrectBehaviorException("Incorrect Set of Services returned from OrderManagementSystem.getOfferedServices after services were added via OrderManagementSystem.addServiceProvider");
        }
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
        srvcSetThree.add(s1);
        srvcSetThree.add(s2);
        srvcSetThree.add(s3);
        this.providers.add(new ServiceProvider("p2",2,srvcSetThree));

        Set<Service> singleService = new HashSet<>();
        singleService.add(s1);
        this.providers.add(new ServiceProvider("p2",3,singleService));
    }
    protected static void writeResultsToFile(int pointsEarned, int possiblePoints, String output) throws IOException{
        //write points file
        File pointsFile = new File("grade.txt");
        pointsFile.createNewFile();
        PrintWriter writer = new PrintWriter(pointsFile);
        writer.println(pointsEarned);
        writer.flush();
        writer.close();

        //write details file
        File details = new File("results.txt");
        details.createNewFile();
        writer = new PrintWriter(details);
        writer.println("YOUR SCORE: " + pointsEarned + " out of " + possiblePoints + "\n");
        writer.println(output);
        writer.flush();
        writer.close();
    }
}
