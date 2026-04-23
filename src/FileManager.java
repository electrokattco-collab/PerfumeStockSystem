import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileManager {
    public static final String PRODUCTS_FILE = "products.csv";
    public static final String PURCHASES_FILE = "purchases.csv";
    public static final String SALES_FILE = "sales.csv";

    public static void saveProducts(ArrayList<Product> products) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PRODUCTS_FILE))) {
            writer.println("productId,name,category,size,retailPrice,rewardsPrice,goldPrice,vipPrice,stockQuantity");
            for (Product product : products) {
                writer.println(clean(product.getProductId()) + "," +
                        clean(product.getName()) + "," +
                        clean(product.getCategory()) + "," +
                        clean(product.getSize()) + "," +
                        product.getRetailPrice() + "," +
                        product.getRewardsPrice() + "," +
                        product.getGoldPrice() + "," +
                        product.getVipPrice() + "," +
                        product.getStockQuantity());
            }
            System.out.println("Products saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving products: " + e.getMessage());
        }
    }

    public static ArrayList<Product> loadProducts() {
        ArrayList<Product> products = new ArrayList<>();
        File file = new File(PRODUCTS_FILE);

        if (!file.exists()) {
            return products;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);

                if (header != null && header.startsWith("type,")) {
                    products.add(readOldProduct(data));
                } else {
                    products.add(readProduct(data));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading products: " + e.getMessage());
        }

        return products;
    }

    public static void savePurchases(ArrayList<Purchase> purchases) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PURCHASES_FILE))) {
            writer.println("entryId,date,productId,productName,category,quantity,supplierUnitCost,batchTotalCost,remainingQuantity");
            for (Purchase purchase : purchases) {
                writer.println(clean(purchase.getPurchaseId()) + "," +
                        clean(purchase.getDate()) + "," +
                        clean(purchase.getProductId()) + "," +
                        clean(purchase.getProductName()) + "," +
                        clean(purchase.getCategory()) + "," +
                        purchase.getQuantity() + "," +
                        purchase.getUnitCost() + "," +
                        purchase.getTotalCost() + "," +
                        purchase.getRemainingQuantity());
            }
            System.out.println("Purchases saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving purchases: " + e.getMessage());
        }
    }

    public static ArrayList<Purchase> loadPurchases() {
        ArrayList<Purchase> purchases = new ArrayList<>();
        File file = new File(PURCHASES_FILE);

        if (!file.exists()) {
            return purchases;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);

                if (header != null && header.startsWith("entryId,")) {
                    purchases.add(new Purchase(
                            value(data, 0),
                            value(data, 2),
                            value(data, 3),
                            value(data, 4),
                            parseInt(value(data, 5)),
                            parseDouble(value(data, 6)),
                            parseInt(value(data, 8)),
                            value(data, 1)
                    ));
                } else {
                    purchases.add(new Purchase(
                            value(data, 0),
                            value(data, 1),
                            value(data, 2),
                            value(data, 3),
                            parseInt(value(data, 4)),
                            parseDouble(value(data, 5)),
                            header != null && header.contains("remainingQuantity") ? parseInt(value(data, 7)) : parseInt(value(data, 4)),
                            header != null && header.contains("remainingQuantity") ? value(data, 8) : value(data, 7)
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading purchases: " + e.getMessage());
        }

        return purchases;
    }

    public static void saveSales(ArrayList<Sale> sales) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SALES_FILE))) {
            writer.println("saleId,date,productId,productName,category,quantitySold,sellingPrice,revenue,costOfGoodsSold,profit");
            for (Sale sale : sales) {
                writer.println(clean(sale.getSaleId()) + "," +
                        clean(sale.getDate()) + "," +
                        clean(sale.getProductId()) + "," +
                        clean(sale.getProductName()) + "," +
                        clean(sale.getCategory()) + "," +
                        sale.getQuantity() + "," +
                        sale.getUnitPrice() + "," +
                        sale.getTotalAmount() + "," +
                        sale.getCostOfGoodsSold() + "," +
                        sale.getProfit());
            }
            System.out.println("Sales saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving sales: " + e.getMessage());
        }
    }

    public static ArrayList<Sale> loadSales() {
        ArrayList<Sale> sales = new ArrayList<>();
        File file = new File(SALES_FILE);

        if (!file.exists()) {
            return sales;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);

                if (header != null && header.startsWith("saleId,date,")) {
                    sales.add(new Sale(
                            value(data, 0),
                            value(data, 2),
                            value(data, 3),
                            value(data, 4),
                            parseInt(value(data, 5)),
                            parseDouble(value(data, 6)),
                            parseDouble(value(data, 8)),
                            value(data, 1)
                    ));
                } else {
                    sales.add(readLegacySale(data));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading sales: " + e.getMessage());
        }

        return sales;
    }

    private static Product readProduct(String[] data) {
        return new Product(
                value(data, 0),
                value(data, 1),
                value(data, 2),
                value(data, 3),
                parseDouble(value(data, 4)),
                parseDouble(value(data, 5)),
                parseDouble(value(data, 6)),
                parseDouble(value(data, 7)),
                parseInt(value(data, 8))
        );
    }

    private static Product readOldProduct(String[] data) {
        String id = value(data, 1);
        String oldName = value(data, 2);
        String oldCategory = value(data, 4);
        double sellingPrice = parseDouble(value(data, 6));
        int stockQuantity = parseInt(value(data, 7));
        int sizeInMl = parseInt(value(data, 9));

        String size = sizeInMl > 0 ? sizeInMl + "mL" : ProductCatalog.standardSize(oldName, oldCategory, "");
        String category = ProductCatalog.standardCategory(oldName, oldCategory);
        String name = ProductCatalog.standardName(oldName, category, size);

        return new Product(id, name, category, size,
                sellingPrice, sellingPrice, sellingPrice, sellingPrice, stockQuantity);
    }

    private static Sale readLegacySale(String[] data) {
        return new Sale(
                value(data, 0),
                value(data, 1),
                value(data, 2),
                value(data, 3),
                parseInt(value(data, 4)),
                parseDouble(value(data, 5)),
                parseDouble(value(data, 7)),
                value(data, 9)
        );
    }

    private static String value(String[] data, int index) {
        if (index >= data.length) {
            return "";
        }
        return data[index];
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace(",", " ");
    }
}
