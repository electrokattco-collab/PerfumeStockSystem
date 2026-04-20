import java.io.*;
import java.util.ArrayList;

public class FileManager {
    public static final String PRODUCTS_FILE = "products.csv";
    public static final String SALES_FILE = "sales.csv";

    public static void saveProducts(ArrayList<Product> products) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PRODUCTS_FILE))) {
            writer.println("type,id,name,brand,category,costPrice,sellingPrice,quantityInStock,variantCode,sizeInMl,itemCount");
            for (Product product : products) {
                writer.println(product.toCSV());
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
            String line = reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);

                String type = data[0];
                String id = data[1];
                String name = data[2];
                String brand = data[3];
                String category = data[4];
                double costPrice = Double.parseDouble(data[5]);
                double sellingPrice = Double.parseDouble(data[6]);
                int quantityInStock = Integer.parseInt(data[7]);
                String variantCode = data[8];
                int sizeInMl = Integer.parseInt(data[9]);
                int itemCount = Integer.parseInt(data[10]);

                if (type.equals("PERFUME")) {
                    products.add(new Perfume(id, name, brand, category, costPrice, sellingPrice,
                            quantityInStock, variantCode, sizeInMl));
                } else if (type.equals("GIFTSET")) {
                    products.add(new GiftSet(id, name, brand, category, costPrice, sellingPrice,
                            quantityInStock, itemCount));
                } else {
                    products.add(new Product(id, name, brand, category, costPrice, sellingPrice,
                            quantityInStock));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading products: " + e.getMessage());
        }

        return products;
    }

    public static void saveSales(ArrayList<Sale> sales) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SALES_FILE))) {
            writer.println("saleId,productId,productName,quantitySold,unitSellingPrice,unitCostPrice,totalAmount,totalProfit,dateTime");
            for (Sale sale : sales) {
                writer.println(sale.toCSV());
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
            String line = reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);

                String saleId = data[0];
                String productId = data[1];
                String productName = data[2];
                int quantitySold = Integer.parseInt(data[3]);
                double unitSellingPrice = Double.parseDouble(data[4]);
                double unitCostPrice = Double.parseDouble(data[5]);
                String dateTime = data[8];

                sales.add(new Sale(
                        saleId,
                        productId,
                        productName,
                        quantitySold,
                        unitSellingPrice,
                        unitCostPrice,
                        dateTime
                ));
            }
        } catch (Exception e) {
            System.out.println("Error loading sales: " + e.getMessage());
        }

        return sales;
    }
}
