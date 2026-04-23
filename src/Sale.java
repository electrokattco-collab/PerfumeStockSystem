public class Sale {
    private String saleId;
    private String productId;
    private String productName;
    private String category;
    private int quantity;
    private double unitPrice;
    private double totalAmount;
    private double costOfGoodsSold;
    private double profit;
    private String date;

    public Sale(String saleId, String productId, String productName, String category,
                int quantity, double unitPrice, double costOfGoodsSold, String date) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = quantity * unitPrice;
        this.costOfGoodsSold = costOfGoodsSold;
        this.profit = this.totalAmount - costOfGoodsSold;
        this.date = date;
    }

    public String getSaleId() {
        return saleId;
    }

    public String getEntryId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
            recalculate();
        }
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice >= 0) {
            this.unitPrice = unitPrice;
            recalculate();
        }
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        if (totalAmount >= 0) {
            this.totalAmount = totalAmount;
        }
    }

    public double getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    public void setCostOfGoodsSold(double costOfGoodsSold) {
        if (costOfGoodsSold >= 0) {
            this.costOfGoodsSold = costOfGoodsSold;
            recalculate();
        }
    }

    public double getProfit() {
        return profit;
    }

    public void recalculate() {
        this.totalAmount = quantity * unitPrice;
        this.profit = this.totalAmount - costOfGoodsSold;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
