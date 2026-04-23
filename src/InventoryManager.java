import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class InventoryManager {
    private static final int LOW_STOCK_LEVEL = 3;

    private ArrayList<Product> products = new ArrayList<>();
    private ArrayList<Purchase> purchases = new ArrayList<>();
    private ArrayList<Sale> sales = new ArrayList<>();
    private HashMap<String, Integer> productCounters = new HashMap<>();
    private int purchaseCounter = 1;
    private int saleCounter = 1;

    public void loadData() {
        products = FileManager.loadProducts();
        purchases = FileManager.loadPurchases();
        sales = FileManager.loadSales();
        rebuildInventoryFromBatches();
        initializeCounters();
    }

    public void saveData() {
        FileManager.saveProducts(products);
        FileManager.savePurchases(purchases);
        FileManager.saveSales(sales);
    }

    public void clearRecordedData() {
        products = new ArrayList<>();
        purchases = new ArrayList<>();
        sales = new ArrayList<>();
        productCounters = new HashMap<>();
        purchaseCounter = 1;
        saleCounter = 1;
        saveData();
    }

    public Purchase recordInventory(ProductCatalog.ProductTemplate template, int quantity) {
        if (template == null || quantity <= 0) {
            return null;
        }

        Product product = ensureActiveProduct(template, null);
        double unitCost = ProductCatalog.getSupplierUnitCost(template, quantity);
        Purchase purchase = new Purchase(
                nextPurchaseId(),
                product.getProductId(),
                template.getName(),
                template.getCategory(),
                quantity,
                unitCost,
                quantity,
                LocalDateTime.now().toString()
        );

        purchases.add(purchase);
        product.addStock(quantity);
        return purchase;
    }

    public Sale recordSale(String productId, int quantity, double unitPrice) {
        Product product = findProductById(productId);

        if (product == null || quantity <= 0 || unitPrice < 0) {
            return null;
        }

        if (getAvailableStock(productId) < quantity) {
            return null;
        }

        double costOfGoodsSold = consumeBatches(productId, quantity);
        product.setStockQuantity(product.getStockQuantity() - quantity);

        Sale sale = new Sale(
                nextSaleId(),
                product.getProductId(),
                product.getName(),
                product.getCategory(),
                quantity,
                unitPrice,
                costOfGoodsSold,
                LocalDateTime.now().toString()
        );
        sales.add(sale);
        return sale;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public ArrayList<Purchase> getPurchases() {
        return purchases;
    }

    public ArrayList<Sale> getSales() {
        return sales;
    }

    public Purchase findPurchaseById(String purchaseId) {
        if (purchaseId == null) {
            return null;
        }

        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseId().equalsIgnoreCase(purchaseId.trim())) {
                return purchase;
            }
        }

        return null;
    }

    public Sale findSaleById(String saleId) {
        if (saleId == null) {
            return null;
        }

        for (Sale sale : sales) {
            if (sale.getSaleId().equalsIgnoreCase(saleId.trim())) {
                return sale;
            }
        }

        return null;
    }

    public ArrayList<Product> getCatalogProductsByType(String type) {
        ArrayList<Product> matches = new ArrayList<>();
        String search = type == null ? "" : type.trim().toLowerCase();

        for (Product product : products) {
            if (product.getCategory().toLowerCase().contains(search)) {
                matches.add(product);
            }
        }

        return matches;
    }

    public ArrayList<Product> getStockedProducts() {
        ArrayList<Product> stocked = new ArrayList<>();

        for (Product product : products) {
            if (product.getStockQuantity() > 0) {
                stocked.add(product);
            }
        }

        return stocked;
    }

    public Product findProductById(String productId) {
        if (productId == null) {
            return null;
        }

        for (Product product : products) {
            if (product.getProductId().equalsIgnoreCase(productId.trim())) {
                return product;
            }
        }

        return null;
    }

    public String getInventorySummary(Product product) {
        if (product == null) {
            return "";
        }

        StringBuilder summary = new StringBuilder();
        boolean found = false;

        for (Purchase purchase : purchases) {
            if (!purchase.getProductId().equalsIgnoreCase(product.getProductId())) {
                continue;
            }

            found = true;

            if (summary.length() > 0) {
                summary.append(", ");
            }

            summary.append(purchase.getRemainingQuantity())
                    .append(" @ ")
                    .append(formatMoney(purchase.getUnitCost()));
        }

        if (!found) {
            return "No supplier cost history";
        }

        return summary.toString();
    }

    public double getMoneySpentOnInventory() {
        double total = 0;

        for (Purchase purchase : purchases) {
            total += purchase.getTotalCost();
        }

        return total;
    }

    public double getSalesRevenue() {
        double total = 0;

        for (Sale sale : sales) {
            total += sale.getTotalAmount();
        }

        return total;
    }

    public double getActualProfitFromSales() {
        double total = 0;

        for (Sale sale : sales) {
            total += sale.getProfit();
        }

        return total;
    }

    public double getRemainingStockValue() {
        double total = 0;

        for (Purchase purchase : purchases) {
            total += purchase.getRemainingQuantity() * purchase.getUnitCost();
        }

        return total;
    }

    public double getExpectedRemainingProfit() {
        double total = 0;

        for (Product product : products) {
            if (product.getStockQuantity() <= 0) {
                continue;
            }

            double remainingValue = 0;
            double retailValue = product.getStockQuantity() * product.getRetailPrice();

            for (Purchase purchase : purchases) {
                if (!purchase.getProductId().equalsIgnoreCase(product.getProductId())) {
                    continue;
                }

                remainingValue += purchase.getRemainingQuantity() * purchase.getUnitCost();
            }

            total += Math.max(0, retailValue - remainingValue);
        }

        return total;
    }

    public int getTotalStockUnits() {
        int total = 0;

        for (Product product : products) {
            total += product.getStockQuantity();
        }

        return total;
    }

    public ArrayList<Product> getLowStockProducts(int threshold) {
        ArrayList<Product> lowStockProducts = new ArrayList<>();

        for (Product product : products) {
            if (product.getStockQuantity() >= 0 && product.getStockQuantity() <= threshold) {
                lowStockProducts.add(product);
            }
        }

        return lowStockProducts;
    }

    public String getPurchaseBatchSummary(Product product) {
        if (product == null) {
            return "";
        }

        StringBuilder summary = new StringBuilder();
        boolean found = false;

        for (Purchase purchase : purchases) {
            if (!purchase.getProductId().equalsIgnoreCase(product.getProductId())) {
                continue;
            }

            found = true;

            if (summary.length() > 0) {
                summary.append(" | ");
            }

            summary.append(purchase.getQuantity())
                    .append(" bought @ ")
                    .append(formatMoney(purchase.getUnitCost()))
                    .append(" [remaining ")
                    .append(purchase.getRemainingQuantity())
                    .append("]");
        }

        if (!found) {
            return "No purchase history";
        }

        return summary.toString();
    }

    public int getAvailableStock(String productId) {
        Product product = findProductById(productId);

        if (product == null) {
            return 0;
        }

        return product.getStockQuantity();
    }

    public String editInventoryEntry(String entryId, int newQuantity, double newUnitCost) {
        Purchase purchase = findPurchaseById(entryId);

        if (purchase == null) {
            return "Inventory entry not found.";
        }

        if (newQuantity <= 0 || newUnitCost < 0) {
            return "Quantity and supplier unit cost must be valid.";
        }

        int totalSold = getTotalSoldQuantity(purchase.getProductId());
        int projectedPurchased = getTotalPurchasedQuantity(purchase.getProductId()) - purchase.getQuantity() + newQuantity;

        if (projectedPurchased < totalSold) {
            return "Cannot reduce this inventory entry below the quantity already sold.";
        }

        purchase.setQuantity(newQuantity);
        purchase.setUnitCost(newUnitCost);
        recalculateTransactionState();
        return null;
    }

    public String deleteInventoryEntry(String entryId) {
        Purchase purchase = findPurchaseById(entryId);

        if (purchase == null) {
            return "Inventory entry not found.";
        }

        int totalSold = getTotalSoldQuantity(purchase.getProductId());
        int projectedPurchased = getTotalPurchasedQuantity(purchase.getProductId()) - purchase.getQuantity();

        if (projectedPurchased < totalSold) {
            return "Cannot delete this inventory entry because sales depend on it.";
        }

        purchases.remove(purchase);
        recalculateTransactionState();
        return null;
    }

    public String editSaleEntry(String saleId, int newQuantity, double newUnitPrice) {
        Sale sale = findSaleById(saleId);

        if (sale == null) {
            return "Sale entry not found.";
        }

        if (newQuantity <= 0 || newUnitPrice < 0) {
            return "Quantity and selling price must be valid.";
        }

        int totalPurchased = getTotalPurchasedQuantity(sale.getProductId());
        int projectedSold = getTotalSoldQuantity(sale.getProductId()) - sale.getQuantity() + newQuantity;

        if (projectedSold > totalPurchased) {
            return "Not enough recorded inventory to support that sale quantity.";
        }

        sale.setQuantity(newQuantity);
        sale.setUnitPrice(newUnitPrice);
        recalculateTransactionState();
        return null;
    }

    public String deleteSaleEntry(String saleId) {
        Sale sale = findSaleById(saleId);

        if (sale == null) {
            return "Sale entry not found.";
        }

        sales.remove(sale);
        recalculateTransactionState();
        return null;
    }

    public String buildLowStockReport(int threshold) {
        StringBuilder report = new StringBuilder();
        report.append("\n=== LOW STOCK REPORT ===\n");

        ArrayList<Product> lowStockProducts = getLowStockProducts(threshold);

        if (lowStockProducts.isEmpty()) {
            report.append("No products are at or below the threshold of ")
                    .append(threshold)
                    .append(".\n");
            return report.toString();
        }

        for (Product product : lowStockProducts) {
            report.append("\n----------------------------\n");
            report.append("Product: ").append(product.getName()).append("\n");
            report.append("Product ID: ").append(product.getProductId()).append("\n");
            report.append("Category: ").append(product.getCategory()).append("\n");
            report.append("Stock: ").append(product.getStockQuantity()).append("\n");
        }

        return report.toString();
    }

    public String buildProfitByProductReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n=== PROFIT BY PRODUCT ===\n");

        if (products.isEmpty()) {
            report.append("No products available.\n");
            return report.toString();
        }

        double grandRevenue = 0;
        double grandCost = 0;
        double grandProfit = 0;

        for (Product product : products) {
            double revenue = 0;
            double cost = 0;
            int soldQuantity = 0;

            for (Sale sale : sales) {
                if (sale.getProductId().equalsIgnoreCase(product.getProductId())) {
                    revenue += sale.getTotalAmount();
                    cost += sale.getCostOfGoodsSold();
                    soldQuantity += sale.getQuantity();
                }
            }

            if (soldQuantity == 0 && product.getStockQuantity() == 0) {
                continue;
            }

            double profit = revenue - cost;
            grandRevenue += revenue;
            grandCost += cost;
            grandProfit += profit;

            report.append("\n----------------------------\n");
            report.append("Product: ").append(product.getName()).append("\n");
            report.append("Product ID: ").append(product.getProductId()).append("\n");
            report.append("Sold Qty: ").append(soldQuantity).append("\n");
            report.append("Revenue: ").append(formatMoney(revenue)).append("\n");
            report.append("Cost: ").append(formatMoney(cost)).append("\n");
            report.append("Profit: ").append(formatMoney(profit)).append("\n");
        }

        report.append("\n----------------------------\n");
        report.append("Total Revenue: ").append(formatMoney(grandRevenue)).append("\n");
        report.append("Total Cost: ").append(formatMoney(grandCost)).append("\n");
        report.append("Total Profit: ").append(formatMoney(grandProfit)).append("\n");
        return report.toString();
    }

    public String buildCurrentStockValuationReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n=== CURRENT STOCK VALUATION ===\n");

        if (products.isEmpty()) {
            report.append("No products recorded.\n");
            return report.toString();
        }

        double totalValue = 0;
        boolean found = false;

        for (Product product : products) {
            int stock = product.getStockQuantity();
            if (stock <= 0) {
                continue;
            }

            double value = 0;
            for (Purchase purchase : purchases) {
                if (purchase.getProductId().equalsIgnoreCase(product.getProductId())) {
                    value += purchase.getRemainingQuantity() * purchase.getUnitCost();
                }
            }

            totalValue += value;
            found = true;

            report.append("\n----------------------------\n");
            report.append("Product: ").append(product.getName()).append("\n");
            report.append("Product ID: ").append(product.getProductId()).append("\n");
            report.append("Quantity: ").append(stock).append("\n");
            report.append("Stock Value: ").append(formatMoney(value)).append("\n");
        }

        if (!found) {
            report.append("No stock on hand.\n");
            return report.toString();
        }

        report.append("\n----------------------------\n");
        report.append("Total Stock Value: ").append(formatMoney(totalValue)).append("\n");
        return report.toString();
    }

    public String buildFullTransactionHistoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n=== FULL TRANSACTION HISTORY ===\n");

        report.append("\n--- Inventory Entries ---\n");
        if (purchases.isEmpty()) {
            report.append("No inventory entries recorded.\n");
        } else {
            for (Purchase purchase : purchases) {
                report.append("\n----------------------------\n");
                report.append("Entry ID: ").append(purchase.getPurchaseId()).append("\n");
                report.append("Date: ").append(purchase.getDate()).append("\n");
                report.append("Product: ").append(purchase.getProductName()).append("\n");
                report.append("Category: ").append(purchase.getCategory()).append("\n");
                report.append("Quantity: ").append(purchase.getQuantity()).append("\n");
                report.append("Supplier Unit Cost: ").append(formatMoney(purchase.getUnitCost())).append("\n");
                report.append("Batch Total Cost: ").append(formatMoney(purchase.getTotalCost())).append("\n");
                report.append("Remaining: ").append(purchase.getRemainingQuantity()).append("\n");
            }
        }

        report.append("\n--- Sales Entries ---\n");
        if (sales.isEmpty()) {
            report.append("No sales recorded.\n");
        } else {
            for (Sale sale : sales) {
                report.append("\n----------------------------\n");
                report.append("Sale ID: ").append(sale.getSaleId()).append("\n");
                report.append("Date: ").append(sale.getDate()).append("\n");
                report.append("Product: ").append(sale.getProductName()).append("\n");
                report.append("Category: ").append(sale.getCategory()).append("\n");
                report.append("Quantity Sold: ").append(sale.getQuantity()).append("\n");
                report.append("Selling Price: ").append(formatMoney(sale.getUnitPrice())).append("\n");
                report.append("Revenue: ").append(formatMoney(sale.getTotalAmount())).append("\n");
                report.append("Cost of Goods Sold: ").append(formatMoney(sale.getCostOfGoodsSold())).append("\n");
                report.append("Profit: ").append(formatMoney(sale.getProfit())).append("\n");
            }
        }

        return report.toString();
    }

    private void rebuildInventoryFromBatches() {
        HashMap<String, Product> productIndex = new HashMap<>();

        for (Product product : products) {
            product.setStockQuantity(0);
            productIndex.put(product.getProductId().toLowerCase(), product);
        }

        for (Purchase purchase : purchases) {
            Product product = productIndex.get(purchase.getProductId().toLowerCase());

            if (product == null) {
                Product created = createProductFromPurchase(purchase);
                products.add(created);
                productIndex.put(created.getProductId().toLowerCase(), created);
                product = created;
            }

            product.addStock(purchase.getRemainingQuantity());
        }
    }

    private Product createProductFromPurchase(Purchase purchase) {
        ProductCatalog.ProductTemplate template = ProductCatalog.findTemplateByName(purchase.getProductName());

        if (template != null) {
            return new Product(
                    purchase.getProductId(),
                    template.getName(),
                    template.getCategory(),
                    template.getSize(),
                    template.getRetailPrice(),
                    template.getRewardsPrice(),
                    template.getGoldPrice(),
                    template.getVipPrice(),
                    0
            );
        }

        return new Product(
                purchase.getProductId(),
                purchase.getProductName(),
                purchase.getCategory(),
                "",
                purchase.getUnitCost(),
                purchase.getUnitCost(),
                purchase.getUnitCost(),
                purchase.getUnitCost(),
                0
        );
    }

    private Product createProductFromSale(Sale sale) {
        ProductCatalog.ProductTemplate template = ProductCatalog.findTemplateByName(sale.getProductName());

        if (template != null) {
            return new Product(
                    sale.getProductId(),
                    template.getName(),
                    template.getCategory(),
                    template.getSize(),
                    template.getRetailPrice(),
                    template.getRewardsPrice(),
                    template.getGoldPrice(),
                    template.getVipPrice(),
                    0
            );
        }

        return new Product(
                sale.getProductId(),
                sale.getProductName(),
                sale.getCategory(),
                "",
                sale.getUnitPrice(),
                sale.getUnitPrice(),
                sale.getUnitPrice(),
                sale.getUnitPrice(),
                0
        );
    }

    private Product ensureActiveProduct(ProductCatalog.ProductTemplate template, String forcedProductId) {
        Product product = findProductByName(template.getName());

        if (product != null) {
            return product;
        }

        String productId = forcedProductId != null ? forcedProductId : nextProductId(template);
        product = new Product(
                productId,
                template.getName(),
                template.getCategory(),
                template.getSize(),
                template.getRetailPrice(),
                template.getRewardsPrice(),
                template.getGoldPrice(),
                template.getVipPrice(),
                0
        );
        products.add(product);
        return product;
    }

    private Product findProductByName(String productName) {
        if (productName == null) {
            return null;
        }

        String search = productName.trim().toLowerCase();

        for (Product product : products) {
            if (product.getName().toLowerCase().equals(search)) {
                return product;
            }
        }

        return null;
    }

    private double consumeBatches(String productId, int quantity) {
        double cost = 0;
        int remaining = quantity;

        for (Purchase purchase : purchases) {
            if (!purchase.getProductId().equalsIgnoreCase(productId)) {
                continue;
            }

            if (purchase.getRemainingQuantity() <= 0) {
                continue;
            }

            int consumed = Math.min(remaining, purchase.getRemainingQuantity());
            purchase.consume(consumed);
            cost += consumed * purchase.getUnitCost();
            remaining -= consumed;

            if (remaining == 0) {
                break;
            }
        }

        return remaining == 0 ? cost : -1;
    }

    private void recalculateTransactionState() {
        HashMap<String, Product> productIndex = new HashMap<>();

        for (Product product : products) {
            product.setStockQuantity(0);
            productIndex.put(product.getProductId().toLowerCase(), product);
        }

        for (Purchase purchase : purchases) {
            purchase.setRemainingQuantity(purchase.getQuantity());
            Product product = productIndex.get(purchase.getProductId().toLowerCase());

            if (product == null) {
                product = createProductFromPurchase(purchase);
                products.add(product);
                productIndex.put(product.getProductId().toLowerCase(), product);
            }

            product.addStock(purchase.getQuantity());
        }

        for (Sale sale : sales) {
            Product product = productIndex.get(sale.getProductId().toLowerCase());

            if (product == null) {
                product = createProductFromSale(sale);
                products.add(product);
                productIndex.put(product.getProductId().toLowerCase(), product);
            }

            double cost = consumeBatches(sale.getProductId(), sale.getQuantity());
            if (cost < 0) {
                cost = 0;
            }

            sale.setCostOfGoodsSold(cost);
            sale.recalculate();
            product.reduceStock(sale.getQuantity());
        }
    }

    private int getTotalPurchasedQuantity(String productId) {
        int total = 0;

        for (Purchase purchase : purchases) {
            if (purchase.getProductId().equalsIgnoreCase(productId)) {
                total += purchase.getQuantity();
            }
        }

        return total;
    }

    private int getTotalSoldQuantity(String productId) {
        int total = 0;

        for (Sale sale : sales) {
            if (sale.getProductId().equalsIgnoreCase(productId)) {
                total += sale.getQuantity();
            }
        }

        return total;
    }

    private String nextProductId(ProductCatalog.ProductTemplate template) {
        String prefix = ProductCatalog.prefixFor(template.getCategory(), template.getSize(), template.getName());
        int nextNumber = productCounters.getOrDefault(prefix, 1);
        productCounters.put(prefix, nextNumber + 1);
        return String.format("%s%03d", prefix, nextNumber);
    }

    private String nextPurchaseId() {
        String purchaseId = String.format("INV%03d", purchaseCounter);
        purchaseCounter++;
        return purchaseId;
    }

    private String nextSaleId() {
        String saleId = String.format("SAL%03d", saleCounter);
        saleCounter++;
        return saleId;
    }

    private void initializeCounters() {
        productCounters.clear();
        purchaseCounter = 1;
        saleCounter = 1;

        for (Product product : products) {
            registerProductId(product.getProductId());
        }

        for (Purchase purchase : purchases) {
            int sequence = Math.max(readSequence(purchase.getPurchaseId(), "INV"), readSequence(purchase.getPurchaseId(), "PUR"));
            purchaseCounter = Math.max(purchaseCounter, sequence + 1);
        }

        for (Sale sale : sales) {
            saleCounter = Math.max(saleCounter, readSequence(sale.getSaleId(), "SAL") + 1);
        }
    }

    private void registerProductId(String productId) {
        if (productId == null || productId.length() < 3) {
            return;
        }

        String prefix = productId.substring(0, 2);
        int number = readSequence(productId, prefix);
        productCounters.put(prefix, Math.max(productCounters.getOrDefault(prefix, 1), number + 1));
    }

    private int readSequence(String id, String prefix) {
        if (id == null || !id.startsWith(prefix)) {
            return 0;
        }

        try {
            return Integer.parseInt(id.substring(prefix.length()));
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatMoney(double value) {
        return String.format("R%.2f", value);
    }
}
