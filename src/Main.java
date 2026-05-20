import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final InventoryManager inventoryManager = new InventoryManager();

    public static void main(String[] args) {
        inventoryManager.loadData();

        int choice;
        do {
            printMenu();
            choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> recordInventory();
                case 2 -> recordSale();
                case 3 -> viewInventory();
                case 4 -> viewSales();
                case 5 -> viewProfitSummary();
                case 6 -> editInventoryEntry();
                case 7 -> deleteInventoryEntry();
                case 8 -> editSaleEntry();
                case 9 -> deleteSaleEntry();
                case 10 -> viewReportsMenu();
                case 11 -> clearInventoryData();
                case 12 -> searchAndFilterMenu();
                case 13 -> {
                    inventoryManager.saveData();
                    System.out.println("Exiting system...");
                }
                default -> System.out.println("Invalid option.");
            }
        } while (choice != 12);
    }

    private static void printMenu() {
        System.out.println("\n=== PERFUME SHOP SYSTEM ===");
        System.out.println("1. Record Inventory");
        System.out.println("2. Record Sale");
        System.out.println("3. View Inventory");
        System.out.println("4. View Sales");
        System.out.println("5. View Profit / Summary");
        System.out.println("6. Edit Inventory Entry");
        System.out.println("7. Delete Inventory Entry");
        System.out.println("8. Edit Sale Entry");
        System.out.println("9. Delete Sale Entry");
        System.out.println("10. View Reports");
        System.out.println("11. Clear Inventory Data");
        System.out.println("12. Search & Filter");
        System.out.println("13. Exit");
    }

    private static void recordInventory() {
        String type = selectProductType();
        if (type == null) {
            return;
        }

        ProductCatalog.ProductTemplate template = selectCatalogProduct(type);
        if (template == null) {
            return;
        }

        int quantity = readInt("Quantity stocked: ");
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        Purchase purchase = inventoryManager.recordInventory(template, quantity);
        if (purchase == null) {
            System.out.println("Inventory record failed.");
            return;
        }

        String tier = ProductCatalog.getSupplierTierLabel(template, quantity);
        System.out.println("Inventory recorded successfully.");
        System.out.println("Entry ID: " + purchase.getPurchaseId());
        System.out.println("Product: " + purchase.getProductName());
        System.out.println("Quantity: " + purchase.getQuantity());
        System.out.println("Supplier tier: " + tier);
        System.out.println("Supplier unit cost: " + formatMoney(purchase.getUnitCost()));
        System.out.println("Total purchase cost: " + formatMoney(purchase.getTotalCost()));

        inventoryManager.saveData();
    }

    private static void recordSale() {
        ArrayList<Product> stockedProducts = inventoryManager.getStockedProducts();

        if (stockedProducts.isEmpty()) {
            System.out.println("No stocked products available for sale.");
            return;
        }

        Product product = selectStockedProduct(stockedProducts);
        if (product == null) {
            return;
        }

        int quantity = readInt("Quantity sold: ");
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        double unitPrice = readDouble("Selling price per item: ");
        Sale sale = inventoryManager.recordSale(product.getProductId(), quantity, unitPrice);

        if (sale == null) {
            System.out.println("Sale could not be recorded. Check stock quantity.");
            return;
        }

        System.out.println("Sale recorded successfully.");
        System.out.println("Sale ID: " + sale.getSaleId());
        System.out.println("Product: " + sale.getProductName());
        System.out.println("Quantity: " + sale.getQuantity());
        System.out.println("Total revenue: " + formatMoney(sale.getTotalAmount()));
        System.out.println("Cost of goods sold: " + formatMoney(sale.getCostOfGoodsSold()));
        System.out.println("Actual profit: " + formatMoney(sale.getProfit()));

        inventoryManager.saveData();
    }

    private static void viewInventory() {
        ArrayList<Purchase> entries = inventoryManager.getPurchases();

        if (entries.isEmpty()) {
            System.out.println("No inventory recorded yet.");
            return;
        }

        System.out.println("\n=== INVENTORY ===");
        for (Product product : inventoryManager.getProducts()) {
            System.out.println("\n----------------------------");
            System.out.println("Product: " + product.getName());
            System.out.println("Type: " + product.getCategory());
            System.out.println("Quantity In Stock: " + product.getStockQuantity());
            System.out.println("Supplier Cost History: " + inventoryManager.getPurchaseBatchSummary(product));
        }
    }

    private static void viewSales() {
        ArrayList<Sale> sales = inventoryManager.getSales();

        if (sales.isEmpty()) {
            System.out.println("No sales recorded.");
            return;
        }

        System.out.println("\n=== SALES ===");
        for (Sale sale : sales) {
            System.out.println("\n----------------------------");
            System.out.println("Sale ID: " + sale.getSaleId());
            System.out.println("Product: " + sale.getProductName());
            System.out.println("Type: " + sale.getCategory());
            System.out.println("Quantity Sold: " + sale.getQuantity());
            System.out.println("Selling Price: " + formatMoney(sale.getUnitPrice()));
            System.out.println("Revenue: " + formatMoney(sale.getTotalAmount()));
            System.out.println("Cost of Goods Sold: " + formatMoney(sale.getCostOfGoodsSold()));
            System.out.println("Profit: " + formatMoney(sale.getProfit()));
            System.out.println("Date: " + sale.getDate());
        }
    }

    private static void viewProfitSummary() {
        System.out.println("\n=== PROFIT / SUMMARY ===");
        System.out.println("Money Spent on Inventory: " + formatMoney(inventoryManager.getMoneySpentOnInventory()));
        System.out.println("Sales Revenue: " + formatMoney(inventoryManager.getSalesRevenue()));
        System.out.println("Remaining Stock Value: " + formatMoney(inventoryManager.getRemainingStockValue()));
        System.out.println("Expected Profit on Remaining Stock: " + formatMoney(inventoryManager.getExpectedRemainingProfit()));
        System.out.println("Actual Profit from Completed Sales: " + formatMoney(inventoryManager.getActualProfitFromSales()));
        System.out.println("Total Stock Units: " + inventoryManager.getTotalStockUnits());
    }

    private static void editInventoryEntry() {
        ArrayList<Purchase> entries = inventoryManager.getPurchases();

        if (entries.isEmpty()) {
            System.out.println("No inventory entries available.");
            return;
        }

        printInventoryEntries(entries);
        String entryId = readText("Enter inventory entry ID to edit: ");
        Purchase entry = inventoryManager.findPurchaseById(entryId);

        if (entry == null) {
            System.out.println("Inventory entry not found.");
            return;
        }

        int newQuantity = readOptionalInt("New quantity", entry.getQuantity());
        double newCost = readOptionalDouble("New supplier unit cost", entry.getUnitCost());

        String result = inventoryManager.editInventoryEntry(entry.getPurchaseId(), newQuantity, newCost);
        if (result != null) {
            System.out.println(result);
            return;
        }

        System.out.println("Inventory entry updated successfully.");
        inventoryManager.saveData();
    }

    private static void deleteInventoryEntry() {
        ArrayList<Purchase> entries = inventoryManager.getPurchases();

        if (entries.isEmpty()) {
            System.out.println("No inventory entries available.");
            return;
        }

        printInventoryEntries(entries);
        String entryId = readText("Enter inventory entry ID to delete: ");
        Purchase entry = inventoryManager.findPurchaseById(entryId);

        if (entry == null) {
            System.out.println("Inventory entry not found.");
            return;
        }

        if (!confirmYesNo("Are you sure you want to delete entry " + entry.getPurchaseId() + "? (yes/no): ")) {
            System.out.println("Delete cancelled.");
            return;
        }

        String result = inventoryManager.deleteInventoryEntry(entry.getPurchaseId());
        if (result != null) {
            System.out.println(result);
            return;
        }

        System.out.println("Inventory entry deleted successfully.");
        inventoryManager.saveData();
    }

    private static void editSaleEntry() {
        ArrayList<Sale> sales = inventoryManager.getSales();

        if (sales.isEmpty()) {
            System.out.println("No sales entries available.");
            return;
        }

        printSalesEntries(sales);
        String saleId = readText("Enter sale ID to edit: ");
        Sale sale = inventoryManager.findSaleById(saleId);

        if (sale == null) {
            System.out.println("Sale entry not found.");
            return;
        }

        int newQuantity = readOptionalInt("New quantity sold", sale.getQuantity());
        double newUnitPrice = readOptionalDouble("New selling price per item", sale.getUnitPrice());

        String result = inventoryManager.editSaleEntry(sale.getSaleId(), newQuantity, newUnitPrice);
        if (result != null) {
            System.out.println(result);
            return;
        }

        System.out.println("Sale entry updated successfully.");
        inventoryManager.saveData();
    }

    private static void deleteSaleEntry() {
        ArrayList<Sale> sales = inventoryManager.getSales();

        if (sales.isEmpty()) {
            System.out.println("No sales entries available.");
            return;
        }

        printSalesEntries(sales);
        String saleId = readText("Enter sale ID to delete: ");
        Sale sale = inventoryManager.findSaleById(saleId);

        if (sale == null) {
            System.out.println("Sale entry not found.");
            return;
        }

        if (!confirmYesNo("Are you sure you want to delete sale " + sale.getSaleId() + "? (yes/no): ")) {
            System.out.println("Delete cancelled.");
            return;
        }

        String result = inventoryManager.deleteSaleEntry(sale.getSaleId());
        if (result != null) {
            System.out.println(result);
            return;
        }

        System.out.println("Sale entry deleted successfully.");
        inventoryManager.saveData();
    }

    private static void viewReportsMenu() {
        while (true) {
            System.out.println("\n=== REPORTS ===");
            System.out.println("1. Low Stock Report");
            System.out.println("2. Profit by Product");
            System.out.println("3. Current Stock Valuation");
            System.out.println("4. Full Transaction History");
            System.out.println("5. Back");

            int choice = readInt("Choose a report: ");

            switch (choice) {
                case 1 -> {
                    int threshold = readInt("Enter low stock threshold: ");
                    if (threshold < 0) {
                        threshold = 0;
                    }
                    System.out.println(inventoryManager.buildLowStockReport(threshold));
                }
                case 2 -> System.out.println(inventoryManager.buildProfitByProductReport());
                case 3 -> System.out.println(inventoryManager.buildCurrentStockValuationReport());
                case 4 -> System.out.println(inventoryManager.buildFullTransactionHistoryReport());
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid report option.");
            }
        }
    }

    private static void clearInventoryData() {
        if (!confirmYesNo("Are you sure you want to clear all recorded inventory and sales data? (yes/no): ")) {
            System.out.println("Clear action cancelled.");
            return;
        }

        inventoryManager.clearRecordedData();
        System.out.println("All recorded inventory and sales data have been cleared.");
    }

    private static void searchAndFilterMenu() {
        while (true) {
            System.out.println("\n=== SEARCH & FILTER ===");
            System.out.println("1. Search Products by Name");
            System.out.println("2. Search Products by Category");
            System.out.println("3. Filter Products by Stock Range");
            System.out.println("4. View Out of Stock Products");
            System.out.println("5. Search Sales by Product Name");
            System.out.println("6. Filter Sales by Date Range");
            System.out.println("7. Filter Sales by Minimum Profit");
            System.out.println("8. Back to Main Menu");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> searchProductsByName();
                case 2 -> searchProductsByCategory();
                case 3 -> filterProductsByStockRange();
                case 4 -> viewOutOfStockProducts();
                case 5 -> searchSalesByProductName();
                case 6 -> filterSalesByDateRange();
                case 7 -> filterSalesByMinProfit();
                case 8 -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void searchProductsByName() {
        String query = readText("Enter product name to search: ");
        ArrayList<Product> results = inventoryManager.searchProductsByName(query);
        
        if (results.isEmpty()) {
            System.out.println("No products found matching '" + query + "'.");
            return;
        }
        
        results = sortProductsMenu(results);
        System.out.println(inventoryManager.buildProductSearchReport(results, "Name contains: '" + query + "'"));
        
        if (confirmYesNo("Export results to CSV? (yes/no): ")) {
            String filename = readText("Enter filename (e.g., search_results.csv): ");
            String csv = inventoryManager.exportProductsToCsv(results);
            if (inventoryManager.saveToFile(csv, filename)) {
                System.out.println("Results exported to " + filename);
            } else {
                System.out.println("Failed to export results.");
            }
        }
    }

    private static void searchProductsByCategory() {
        String category = readText("Enter category to search: ");
        ArrayList<Product> results = inventoryManager.searchProductsByCategory(category);
        
        if (results.isEmpty()) {
            System.out.println("No products found in category '" + category + "'.");
            return;
        }
        
        results = sortProductsMenu(results);
        System.out.println(inventoryManager.buildProductSearchReport(results, "Category contains: '" + category + "'"));
        
        offerExport(results, true);
    }

    private static void filterProductsByStockRange() {
        int minStock = readInt("Enter minimum stock quantity: ");
        int maxStock = readInt("Enter maximum stock quantity: ");
        
        if (minStock > maxStock) {
            System.out.println("Minimum stock cannot be greater than maximum stock.");
            return;
        }
        
        ArrayList<Product> results = inventoryManager.filterProductsByStockRange(minStock, maxStock);
        
        if (results.isEmpty()) {
            System.out.println("No products found with stock between " + minStock + " and " + maxStock + ".");
            return;
        }
        
        results = sortProductsMenu(results);
        System.out.println(inventoryManager.buildProductSearchReport(results, 
            "Stock range: " + minStock + " to " + maxStock));
        
        offerExport(results, true);
    }

    private static void viewOutOfStockProducts() {
        ArrayList<Product> results = inventoryManager.getOutOfStockProducts();
        
        if (results.isEmpty()) {
            System.out.println("No out of stock products found.");
            return;
        }
        
        results = sortProductsMenu(results);
        System.out.println(inventoryManager.buildProductSearchReport(results, "Out of Stock"));
        offerExport(results, true);
    }

    private static void searchSalesByProductName() {
        String query = readText("Enter product name to search: ");
        ArrayList<Sale> results = inventoryManager.searchSalesByProductName(query);
        
        if (results.isEmpty()) {
            System.out.println("No sales found for product '" + query + "'.");
            return;
        }
        
        results = sortSalesMenu(results);
        System.out.println(inventoryManager.buildSalesSearchReport(results, 
            "Product name contains: '" + query + "'"));
        
        offerExport(results, false);
    }

    private static void filterSalesByDateRange() {
        System.out.println("Enter dates in format YYYY-MM-DD (leave blank for no limit)");
        String startDate = readText("Start date: ");
        String endDate = readText("End date: ");
        
        ArrayList<Sale> results = inventoryManager.filterSalesByDateRange(startDate, endDate);
        
        if (results.isEmpty()) {
            System.out.println("No sales found in the specified date range.");
            return;
        }
        
        results = sortSalesMenu(results);
        String criteria = "Date range: " + 
            (startDate.isEmpty() ? "Any" : startDate) + " to " + 
            (endDate.isEmpty() ? "Any" : endDate);
        System.out.println(inventoryManager.buildSalesSearchReport(results, criteria));
        
        offerExport(results, false);
    }

    private static void filterSalesByMinProfit() {
        double minProfit = readDouble("Enter minimum profit amount: R");
        
        ArrayList<Sale> results = inventoryManager.filterSalesByMinProfit(minProfit);
        
        if (results.isEmpty()) {
            System.out.println("No sales found with profit >= R" + String.format("%.2f", minProfit));
            return;
        }
        
        results = sortSalesMenu(results);
        System.out.println(inventoryManager.buildSalesSearchReport(results, 
            "Minimum profit: R" + String.format("%.2f", minProfit)));
        
        offerExport(results, false);
    }

    private static ArrayList<Product> sortProductsMenu(ArrayList<Product> products) {
        if (products.size() <= 1) {
            return products;
        }
        
        System.out.println("\nSort by:");
        System.out.println("1. Name (A-Z)");
        System.out.println("2. Name (Z-A)");
        System.out.println("3. Stock (Low-High)");
        System.out.println("4. Stock (High-Low)");
        System.out.println("5. Price (Low-High)");
        System.out.println("6. Price (High-Low)");
        System.out.println("7. No sorting");
        
        int choice = readInt("Choose: ");
        
        return switch (choice) {
            case 1 -> inventoryManager.sortProductsByName(products, true);
            case 2 -> inventoryManager.sortProductsByName(products, false);
            case 3 -> inventoryManager.sortProductsByStock(products, true);
            case 4 -> inventoryManager.sortProductsByStock(products, false);
            case 5 -> inventoryManager.sortProductsByPrice(products, true);
            case 6 -> inventoryManager.sortProductsByPrice(products, false);
            default -> products;
        };
    }

    private static ArrayList<Sale> sortSalesMenu(ArrayList<Sale> sales) {
        if (sales.size() <= 1) {
            return sales;
        }
        
        System.out.println("\nSort by:");
        System.out.println("1. Date (Oldest first)");
        System.out.println("2. Date (Newest first)");
        System.out.println("3. Profit (Low-High)");
        System.out.println("4. Profit (High-Low)");
        System.out.println("5. No sorting");
        
        int choice = readInt("Choose: ");
        
        return switch (choice) {
            case 1 -> inventoryManager.sortSalesByDate(sales, true);
            case 2 -> inventoryManager.sortSalesByDate(sales, false);
            case 3 -> inventoryManager.sortSalesByProfit(sales, true);
            case 4 -> inventoryManager.sortSalesByProfit(sales, false);
            default -> sales;
        };
    }

    private static void offerExport(ArrayList<?> results, boolean isProducts) {
        if (confirmYesNo("Export results to CSV? (yes/no): ")) {
            String filename = readText("Enter filename (e.g., search_results.csv): ");
            String csv;
            if (isProducts) {
                @SuppressWarnings("unchecked")
                ArrayList<Product> products = (ArrayList<Product>) results;
                csv = inventoryManager.exportProductsToCsv(products);
            } else {
                @SuppressWarnings("unchecked")
                ArrayList<Sale> sales = (ArrayList<Sale>) results;
                csv = inventoryManager.exportSalesToCsv(sales);
            }
            if (inventoryManager.saveToFile(csv, filename)) {
                System.out.println("Results exported to " + filename);
            } else {
                System.out.println("Failed to export results.");
            }
        }
    }

    private static void printInventoryEntries(ArrayList<Purchase> entries) {
        System.out.println("\n=== INVENTORY ENTRIES ===");
        for (Purchase purchase : entries) {
            System.out.println("\n----------------------------");
            System.out.println("Entry ID: " + purchase.getPurchaseId());
            System.out.println("Date: " + purchase.getDate());
            System.out.println("Product: " + purchase.getProductName());
            System.out.println("Category: " + purchase.getCategory());
            System.out.println("Quantity: " + purchase.getQuantity());
            System.out.println("Supplier Unit Cost: " + formatMoney(purchase.getUnitCost()));
            System.out.println("Batch Total Cost: " + formatMoney(purchase.getTotalCost()));
            System.out.println("Remaining: " + purchase.getRemainingQuantity());
        }
    }

    private static void printSalesEntries(ArrayList<Sale> sales) {
        System.out.println("\n=== SALES ENTRIES ===");
        for (Sale sale : sales) {
            System.out.println("\n----------------------------");
            System.out.println("Sale ID: " + sale.getSaleId());
            System.out.println("Date: " + sale.getDate());
            System.out.println("Product: " + sale.getProductName());
            System.out.println("Category: " + sale.getCategory());
            System.out.println("Quantity Sold: " + sale.getQuantity());
            System.out.println("Selling Price: " + formatMoney(sale.getUnitPrice()));
            System.out.println("Revenue: " + formatMoney(sale.getTotalAmount()));
            System.out.println("Cost of Goods Sold: " + formatMoney(sale.getCostOfGoodsSold()));
            System.out.println("Profit: " + formatMoney(sale.getProfit()));
        }
    }

    private static String selectProductType() {
        ArrayList<String> types = ProductCatalog.getProductTypes();

        if (types.isEmpty()) {
            System.out.println("No supplier products available.");
            return null;
        }

        System.out.println("\n=== PRODUCT TYPES ===");
        for (int i = 0; i < types.size(); i++) {
            System.out.println((i + 1) + ". " + types.get(i));
        }

        int choice = readInt("Choose product type: ");
        if (choice < 1 || choice > types.size()) {
            System.out.println("Invalid type.");
            return null;
        }

        return types.get(choice - 1);
    }

    private static ProductCatalog.ProductTemplate selectCatalogProduct(String type) {
        ArrayList<ProductCatalog.ProductTemplate> products = ProductCatalog.getProductsByType(type);

        if (products.isEmpty()) {
            System.out.println("No products found for that type.");
            return null;
        }

        System.out.println("\n=== PRODUCTS ===");
        for (int i = 0; i < products.size(); i++) {
            ProductCatalog.ProductTemplate template = products.get(i);
            System.out.println((i + 1) + ". " + template.getName());
        }

        int choice = readInt("Choose product: ");
        if (choice < 1 || choice > products.size()) {
            System.out.println("Invalid product.");
            return null;
        }

        return products.get(choice - 1);
    }

    private static Product selectStockedProduct(ArrayList<Product> stockedProducts) {
        System.out.println("\n=== STOCKED PRODUCTS ===");
        for (int i = 0; i < stockedProducts.size(); i++) {
            Product product = stockedProducts.get(i);
            System.out.println((i + 1) + ". " + product.getName() + " | Stock: " + product.getStockQuantity());
        }

        int choice = readInt("Choose product: ");
        if (choice < 1 || choice > stockedProducts.size()) {
            System.out.println("Invalid product.");
            return null;
        }

        return stockedProducts.get(choice - 1);
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private static int readOptionalInt(String prompt, int currentValue) {
        while (true) {
            System.out.print(prompt + " [current " + currentValue + "]: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return currentValue;
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    private static double readOptionalDouble(String prompt, double currentValue) {
        while (true) {
            System.out.print(prompt + " [current " + formatMoney(currentValue) + "]: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return currentValue;
            }

            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static boolean confirmYesNo(String prompt) {
        String answer = readText(prompt).toLowerCase();
        return answer.equals("yes") || answer.equals("y");
    }

    private static String formatMoney(double value) {
        return String.format("R%.2f", value);
    }
}
