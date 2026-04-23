public class Purchase {
    private String purchaseId;
    private String productId;
    private String productName;
    private String category;
    private int quantity;
    private double unitCost;
    private double totalCost;
    private int remainingQuantity;
    private String date;

    public Purchase(String purchaseId, String productId, String productName, String category,
                    int quantity, double unitCost, int remainingQuantity, String date) {
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = quantity * unitCost;
        this.remainingQuantity = Math.max(0, remainingQuantity);
        this.date = date;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public String getEntryId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
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
            this.totalCost = this.quantity * this.unitCost;
            this.remainingQuantity = Math.min(this.remainingQuantity, this.quantity);
        }
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        if (unitCost >= 0) {
            this.unitCost = unitCost;
            this.totalCost = this.quantity * this.unitCost;
        }
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        if (totalCost >= 0) {
            this.totalCost = totalCost;
        }
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = Math.max(0, Math.min(remainingQuantity, quantity));
    }

    public boolean hasRemainingQuantity() {
        return remainingQuantity > 0;
    }

    public void consume(int amount) {
        if (amount > 0) {
            remainingQuantity = Math.max(0, remainingQuantity - amount);
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
