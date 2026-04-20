public class Sale {
    private String saleId;
    private String productId;
    private String productName;
    private int quantitySold;
    private double unitSellingPrice;
    private double unitCostPrice;
    private double totalAmount;
    private double totalProfit;
    private String dateTime;

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Sale(String saleId, String productId, String productName, int quantitySold,
                double unitSellingPrice, double unitCostPrice, String dateTime) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.unitSellingPrice = unitSellingPrice;
        this.unitCostPrice = unitCostPrice;
        this.totalAmount = quantitySold * unitSellingPrice;
        this.totalProfit = quantitySold * (unitSellingPrice - unitCostPrice);
        this.dateTime = dateTime;
    }

    public String getSaleId() {
        return saleId;
    }

    public String toCSV() {
        return saleId + "," +
                productId + "," +
                productName.replace(",", " ") + "," +
                quantitySold + "," +
                unitSellingPrice + "," +
                unitCostPrice + "," +
                totalAmount + "," +
                totalProfit + "," +
                dateTime.replace(",", " ");
    }

    public void printSummary() {
        System.out.println("=== SALE SUMMARY ===");
        System.out.println("Sale ID: " + saleId);
        System.out.println("Product ID: " + productId);
        System.out.println("Product Name: " + productName);
        System.out.println("Quantity Sold: " + quantitySold);
        System.out.println("Total Amount: R" + totalAmount);
        System.out.println("Total Profit: R" + totalProfit);
        System.out.println("Date/Time: " + dateTime);
    }
}