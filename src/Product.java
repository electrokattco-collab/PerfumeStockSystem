public class Product {
    private String productId;
    private String name;
    private String brand;
    private String category;
    private double costPrice;
    private double sellingPrice;
    private int quantityInStock;

    public Product(String productId, String name, String brand, String category,
                   double costPrice, double sellingPrice, int quantityInStock) {
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.quantityInStock = quantityInStock;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCostPrice(double costPrice) {
        if (costPrice >= 0) {
            this.costPrice = costPrice;
        }
    }

    public void setSellingPrice(double sellingPrice) {
        if (sellingPrice >= 0) {
            this.sellingPrice = sellingPrice;
        }
    }

    public void setQuantityInStock(int quantityInStock) {
        if (quantityInStock >= 0) {
            this.quantityInStock = quantityInStock;
        }
    }

    public double getProfitPerUnit() {
        return sellingPrice - costPrice;
    }

    public boolean reduceStock(int quantitySold) {
        if (quantitySold > 0 && quantitySold <= quantityInStock) {
            quantityInStock -= quantitySold;
            return true;
        }
        return false;
    }

    public String getType() {
        return "GENERAL";
    }

    public String toCSV() {
        return getType() + "," +
                clean(productId) + "," +
                clean(name) + "," +
                clean(brand) + "," +
                clean(category) + "," +
                costPrice + "," +
                sellingPrice + "," +
                quantityInStock + ",,0,0";
    }

    protected String clean(String value) {
        return value == null ? "" : value.replace(",", " ");
    }

    public void displayDetails() {
        System.out.println("ID: " + productId);
        System.out.println("Type: " + getType());
        System.out.println("Name: " + name);
        System.out.println("Brand: " + brand);
        System.out.println("Category: " + category);
        System.out.println("Cost Price: R" + costPrice);
        System.out.println("Selling Price: R" + sellingPrice);
        System.out.println("Profit Per Unit: R" + getProfitPerUnit());
        System.out.println("Stock Quantity: " + quantityInStock);
    }
}