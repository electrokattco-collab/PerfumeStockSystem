import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static ArrayList<Product> products = new ArrayList<>();
    static ArrayList<Sale> sales = new ArrayList<>();
    static int perfume50Counter = 1;
    static int perfume30Counter = 1;
    static int rollOnCounter = 1;
    static int bodyLotionCounter = 1;
    static int bodyWashCounter = 1;
    static int giftSetCounter = 1;
    static int comboCounter = 1;
    static int watchCounter = 1;
    static int otherCounter = 1;
    static int saleCounter = 1;
    static final int LOW_STOCK_LEVEL = 5;

    public static String generateProductId(String prefix) {
        return switch (prefix) {
            case "PP" -> String.format("PP%03d", perfume50Counter++);
            case "PS" -> String.format("PS%03d", perfume30Counter++);
            case "PR" -> String.format("PR%03d", rollOnCounter++);
            case "PB" -> String.format("PB%03d", bodyLotionCounter++);
            case "PW" -> String.format("PW%03d", bodyWashCounter++);
            case "PG" -> String.format("PG%03d", giftSetCounter++);
            case "PC" -> String.format("PC%03d", comboCounter++);
            case "PT" -> String.format("PT%03d", watchCounter++);
            default -> String.format("PN%03d", otherCounter++);
        };
    }

    public static void main(String[] args) {
        products = FileManager.loadProducts();
        sales = FileManager.loadSales();
        initializeProductCounters();
        saleCounter = getNextSaleNumber();

        int choice;

        do {
            System.out.println("\n=== PERFUME STOCK SYSTEM ===");
            System.out.println("1. Add Perfume");
            System.out.println("2. Add Gift Set");
            System.out.println("3. Add General Product");
            System.out.println("4. View All Products");
            System.out.println("5. Search Product");
            System.out.println("6. Update Product");
            System.out.println("7. Delete Product");
            System.out.println("8. Record Sale");
            System.out.println("9. View Low Stock");
            System.out.println("10. Save Data");
            System.out.println("11. Load Starter Inventory");
            System.out.println("12. View All Sales");
            System.out.println("13. Sales Summary Report");
            System.out.println("14. Restock Product");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            choice = readInt();

            switch (choice) {
                case 1 -> addPerfume();
                case 2 -> addGiftSet();
                case 3 -> addGeneralProduct();
                case 4 -> viewAllProducts();
                case 5 -> searchProduct();
                case 6 -> updateProduct();
                case 7 -> deleteProduct();
                case 8 -> recordSale();
                case 9 -> viewLowStock();
                case 10 -> saveData();
                case 11 -> loadStarterInventory();
                case 12 -> viewAllSales();
                case 13 -> salesSummaryReport();
                case 14 -> restockProduct();
                case 0 -> {
                    saveData();
                    System.out.println("Exiting system...");
                }
                default -> System.out.println("Invalid option.");
            }
        } while (choice != 0);
    }

    public static String generateSaleId() {
        String id = String.format("SAL%03d", saleCounter);
        saleCounter++;
        return id;
    }

    public static void initializeProductCounters() {
        for (Product product : products) {
            String id = product.getProductId();

            try {
                String prefix = id.substring(0, 2);
                int number = Integer.parseInt(id.substring(2));

                switch (prefix) {
                    case "PP" -> perfume50Counter = Math.max(perfume50Counter, number + 1);
                    case "PS" -> perfume30Counter = Math.max(perfume30Counter, number + 1);
                    case "PR" -> rollOnCounter = Math.max(rollOnCounter, number + 1);
                    case "PB" -> bodyLotionCounter = Math.max(bodyLotionCounter, number + 1);
                    case "PW" -> bodyWashCounter = Math.max(bodyWashCounter, number + 1);
                    case "PG" -> giftSetCounter = Math.max(giftSetCounter, number + 1);
                    case "PC" -> comboCounter = Math.max(comboCounter, number + 1);
                    case "PT" -> watchCounter = Math.max(watchCounter, number + 1);
                    case "PN" -> otherCounter = Math.max(otherCounter, number + 1);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static void addPerfume() {
        System.out.print("Enter perfume name: ");
        String name = scanner.nextLine();

        System.out.print("Enter brand: ");
        String brand = scanner.nextLine();

        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        System.out.print("Enter variant code: ");
        String variantCode = scanner.nextLine();

        int sizeInMl;
        do {
            System.out.print("Enter bottle size (30 or 50): ");
            sizeInMl = readInt();
            if (sizeInMl != 30 && sizeInMl != 50) {
                System.out.println("Only 30ml or 50ml allowed.");
            }
        } while (sizeInMl != 30 && sizeInMl != 50);

        System.out.print("Enter cost price: ");
        double costPrice = readDouble();

        System.out.print("Enter selling price: ");
        double sellingPrice = readDouble();

        System.out.print("Enter quantity in stock: ");
        int quantity = readInt();

        String prefix = (sizeInMl == 50) ? "PP" : "PS";
        Product perfume = new Perfume(generateProductId(prefix), name, brand, category, costPrice, sellingPrice, quantity, variantCode, sizeInMl);
        products.add(perfume);
        System.out.println("Perfume added successfully.");
    }

    public static int getNextSaleNumber() {
        int max = 0;
        for (Sale sale : sales) {
            String id = sale.getSaleId().replace("SAL", "");
            try {
                int number = Integer.parseInt(id);
                if (number > max) {
                    max = number;
                }
            } catch (Exception ignored) {
            }
        }
        return max + 1;
    }
    public static void viewAllSales() {
        if (sales.isEmpty()) {
            System.out.println("No sales found.");
            return;
        }

        for (Sale sale : sales) {
            System.out.println("\n============================");
            System.out.println("Sale ID: " + sale.getSaleId());
            System.out.println("Product ID: " + sale.getProductId());
            System.out.println("Product Name: " + sale.getProductName());
            System.out.println("Quantity Sold: " + sale.getQuantitySold());
            System.out.println("Total Amount: R" + sale.getTotalAmount());
            System.out.println("Total Profit: R" + sale.getTotalProfit());
            System.out.println("Date/Time: " + sale.getDateTime());
        }
        System.out.println("\n============================");
    }
    public static void salesSummaryReport() {
        if (sales.isEmpty()) {
            System.out.println("No sales available for report.");
            return;
        }

        int totalUnitsSold = 0;
        double totalRevenue = 0;
        double totalProfit = 0;

        for (Sale sale : sales) {
            totalUnitsSold += sale.getQuantitySold();
            totalRevenue += sale.getTotalAmount();
            totalProfit += sale.getTotalProfit();
        }

        System.out.println("\n=== SALES SUMMARY REPORT ===");
        System.out.println("Number of Sales: " + sales.size());
        System.out.println("Total Units Sold: " + totalUnitsSold);
        System.out.println("Total Revenue: R" + totalRevenue);
        System.out.println("Total Profit: R" + totalProfit);
    }
    public static void addGiftSet() {
        System.out.print("Enter gift set name: ");
        String name = scanner.nextLine();

        System.out.print("Enter brand: ");
        String brand = scanner.nextLine();

        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        System.out.print("Enter cost price: ");
        double costPrice = readDouble();

        System.out.print("Enter selling price: ");
        double sellingPrice = readDouble();

        System.out.print("Enter quantity in stock: ");
        int quantity = readInt();

        System.out.print("Enter number of items in set: ");
        int itemCount = readInt();

        Product giftSet = new GiftSet(generateProductId("PG"), name, brand, category, costPrice, sellingPrice, quantity, itemCount);        products.add(giftSet);
        System.out.println("Gift set added successfully.");
    }

    public static void addGeneralProduct() {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();

        System.out.print("Enter brand: ");
        String brand = scanner.nextLine();

        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        System.out.print("Enter cost price: ");
        double costPrice = readDouble();

        System.out.print("Enter selling price: ");
        double sellingPrice = readDouble();

        System.out.print("Enter quantity in stock: ");
        int quantity = readInt();

        String prefix;
        String categoryLower = category.toLowerCase();

        if (categoryLower.contains("roll")) {
            prefix = "PR";
        } else if (categoryLower.contains("lotion")) {
            prefix = "PB";
        } else if (categoryLower.contains("wash")) {
            prefix = "PW";
        } else if (categoryLower.contains("combo")) {
            prefix = "PC";
        } else if (categoryLower.contains("watch")) {
            prefix = "PT";
        } else {
            prefix = "PN";
        }

        Product product = new Product(generateProductId(prefix), name, brand, category, costPrice, sellingPrice, quantity);
        products.add(product);
        System.out.println("General product added successfully.");
    }

    public static void viewAllProducts() {
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        for (Product product : products) {
            System.out.println("\n----------------------------");
            product.displayDetails();
        }
        System.out.println("\n----------------------------");
    }

    public static void searchProduct() {
        System.out.print("Enter product ID, name, or variant code to search: ");
        String search = scanner.nextLine().trim().toLowerCase();

        boolean found = false;

        for (Product product : products) {
            boolean matchesId = product.getProductId().toLowerCase().contains(search);
            boolean matchesName = product.getName().toLowerCase().contains(search);
            boolean matchesVariant = false;

            if (product instanceof Perfume perfume) {
                matchesVariant = perfume.getVariantCode().toLowerCase().contains(search);
            }

            if (matchesId || matchesName || matchesVariant) {
                System.out.println("\n----------------------------");
                product.displayDetails();
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching product found.");
        }
    }
    public static void updateProduct() {
        System.out.print("Enter product ID to update: ");
        String id = scanner.nextLine();

        Product product = findProductById(id);

        if (product == null) {
            System.out.println("Product not found.");
            return;
        }

        System.out.println("\nUpdating: " + product.getName());
        System.out.println("1. Update Name");
        System.out.println("2. Update Brand");
        System.out.println("3. Update Category");
        System.out.println("4. Update Cost Price");
        System.out.println("5. Update Selling Price");
        System.out.println("6. Update Quantity In Stock");

        if (product instanceof Perfume) {
            System.out.println("7. Update Variant Code");
            System.out.println("8. Update Bottle Size");
        }

        if (product instanceof GiftSet) {
            System.out.println("9. Update Item Count");
        }

        System.out.print("Choose field to update: ");
        int choice = readInt();

        switch (choice) {
            case 1 -> {
                System.out.print("Enter new name: ");
                product.setName(scanner.nextLine());
                System.out.println("Name updated.");
            }
            case 2 -> {
                System.out.print("Enter new brand: ");
                product.setBrand(scanner.nextLine());
                System.out.println("Brand updated.");
            }
            case 3 -> {
                System.out.print("Enter new category: ");
                product.setCategory(scanner.nextLine());
                System.out.println("Category updated.");
            }
            case 4 -> {
                System.out.print("Enter new cost price: ");
                product.setCostPrice(readDouble());
                System.out.println("Cost price updated.");
            }
            case 5 -> {
                System.out.print("Enter new selling price: ");
                product.setSellingPrice(readDouble());
                System.out.println("Selling price updated.");
            }
            case 6 -> {
                System.out.print("Enter new quantity in stock: ");
                product.setQuantityInStock(readInt());
                System.out.println("Stock quantity updated.");
            }
            case 7 -> {
                if (product instanceof Perfume perfume) {
                    System.out.print("Enter new variant code: ");
                    perfume.setVariantCode(scanner.nextLine());
                    System.out.println("Variant code updated.");
                } else {
                    System.out.println("Invalid choice for this product type.");
                }
            }
            case 8 -> {
                if (product instanceof Perfume perfume) {
                    int newSize;
                    do {
                        System.out.print("Enter new bottle size (30 or 50): ");
                        newSize = readInt();
                        if (newSize != 30 && newSize != 50) {
                            System.out.println("Only 30ml or 50ml allowed.");
                        }
                    } while (newSize != 30 && newSize != 50);

                    perfume.setSizeInMl(newSize);
                    System.out.println("Bottle size updated.");
                } else {
                    System.out.println("Invalid choice for this product type.");
                }
            }
            case 9 -> {
                if (product instanceof GiftSet giftSet) {
                    System.out.print("Enter new item count: ");
                    int newItemCount = readInt();
                    giftSet.setItemCount(newItemCount);
                    System.out.println("Item count updated.");
                } else {
                    System.out.println("Invalid choice for this product type.");
                }
            }
            default -> System.out.println("Invalid choice.");
        }
    }
    public static void deleteProduct() {
        System.out.print("Enter product ID to delete: ");
        String id = scanner.nextLine();

        Product product = findProductById(id);

        if (product == null) {
            System.out.println("Product not found.");
            return;
        }

        products.remove(product);
        System.out.println("Product deleted successfully.");
    }

    public static void recordSale() {
        System.out.print("Enter product ID, name, or variant code to sell: ");
        String searchText = scanner.nextLine();

        Product product = findProduct(searchText);

        if (product == null) {
            System.out.println("Product not found.");
            return;
        }

        System.out.println("\nProduct found:");
        product.displayDetails();

        System.out.print("Enter quantity sold: ");
        int quantitySold = readInt();

        if (product.reduceStock(quantitySold)) {
            Sale sale = new Sale(
                    generateSaleId(),
                    product.getProductId(),
                    product.getName(),
                    quantitySold,
                    product.getSellingPrice(),
                    product.getCostPrice(),
                    LocalDateTime.now().toString()
            );

            sales.add(sale);
            sale.printSummary();
            System.out.println("Stock updated successfully.");
        } else {
            System.out.println("Not enough stock available.");
        }
    }

    public static void viewLowStock() {
        boolean found = false;

        for (Product product : products) {
            if (product.getQuantityInStock() <= LOW_STOCK_LEVEL) {
                System.out.println("\nLOW STOCK ALERT");
                product.displayDetails();
                found = true;
            }
        }

        if (!found) {
            System.out.println("No low-stock products found.");
        }
    }

    public static void saveData() {
        FileManager.saveProducts(products);
        FileManager.saveSales(sales);
    }
    public static Product findProductById(String id) {
        for (Product product : products) {
            if (product.getProductId().equalsIgnoreCase(id)) {
                return product;
            }
        }
        return null;
    }
    public static Product findProduct(String searchText) {
        String search = searchText.trim().toLowerCase();

        for (Product product : products) {
            if (product.getProductId().toLowerCase().equals(search)) {
                return product;
            }

            if (product.getName().toLowerCase().contains(search)) {
                return product;
            }

            if (product instanceof Perfume perfume) {
                if (perfume.getVariantCode().toLowerCase().equals(search)) {
                    return perfume;
                }
            }
        }

        return null;
    }
    public static int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Enter a valid whole number: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    public static void loadStarterInventory() {
        if (!products.isEmpty()) {
            System.out.println("Starter inventory was not loaded because products already exist.");
            System.out.println("Use it only on a fresh system, or delete existing items first.");
            return;
        }

        products.add(new Perfume(generateProductId("PP"), "Superior Perfume", "Arthur Ford", "For Him", 125, 199, 10, "#SUP50", 50));
        products.add(new Perfume(generateProductId("PP"), "Gold & Rose Gold Perfume", "Arthur Ford", "For Her", 125, 199, 10, "#GRG50", 50));
        products.add(new Perfume(generateProductId("PS"), "Travel Size Perfume", "Arthur Ford", "Travel", 80, 125, 10, "#TRV30", 30));

        products.add(new Product(generateProductId("PW"), "Body Wash 450ml", "Arthur Ford", "Body Wash", 0, 85, 10));
        products.add(new Product(generateProductId("PB"), "Body Lotion 400ml", "Arthur Ford", "Body Lotion", 60, 85, 10));
        products.add(new Product(generateProductId("PR"), "Roll-on Deodorant 50ml", "Arthur Ford", "Roll-on", 22, 39, 10));

        products.add(new GiftSet(generateProductId("PG"), "PureLite Glow Gift Set", "Arthur Ford", "Gift Set", 0, 240, 10, 4));

        products.add(new Product(generateProductId("PT"), "Arthur Ford Watch", "Arthur Ford", "Watch", 0, 350, 10));

        products.add(new Product(generateProductId("PC"), "Combo 1 Perfume + Roll-on", "Arthur Ford", "Combo", 147, 219, 10));
        products.add(new Product(generateProductId("PC"), "Combo 2 Perfume + Roll-on + Lotion", "Arthur Ford", "Combo", 207, 299, 10));
        products.add(new Product(generateProductId("PC"), "Combo 3 Perfume + Body Spray Him", "Arthur Ford", "Combo", 125, 249, 10));
        products.add(new Product(generateProductId("PC"), "Combo 4 Perfume + Body Spray Her", "Arthur Ford", "Combo", 125, 235, 10));

        System.out.println("Starter inventory loaded successfully.");
    }

    public static double readDouble() {
        while (!scanner.hasNextDouble()) {
            System.out.print("Enter a valid number: ");
            scanner.next();
        }
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }
}