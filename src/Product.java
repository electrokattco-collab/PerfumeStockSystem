public class Product {
    private String productId;
    private String name;
    private String category;
    private String size;
    private double retailPrice;
    private double rewardsPrice;
    private double goldPrice;
    private double vipPrice;
    private int stockQuantity;

    public Product(String productId, String name, String category, String size,
                   double retailPrice, double rewardsPrice, double goldPrice,
                   double vipPrice, int stockQuantity) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.size = size;
        this.retailPrice = Math.max(0, retailPrice);
        this.rewardsPrice = Math.max(0, rewardsPrice);
        this.goldPrice = Math.max(0, goldPrice);
        this.vipPrice = Math.max(0, vipPrice);
        this.stockQuantity = Math.max(0, stockQuantity);
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSize() {
        return size;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public double getRewardsPrice() {
        return rewardsPrice;
    }

    public double getGoldPrice() {
        return goldPrice;
    }

    public double getVipPrice() {
        return vipPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setRetailPrice(double retailPrice) {
        if (retailPrice >= 0) {
            this.retailPrice = retailPrice;
        }
    }

    public void setRewardsPrice(double rewardsPrice) {
        if (rewardsPrice >= 0) {
            this.rewardsPrice = rewardsPrice;
        }
    }

    public void setGoldPrice(double goldPrice) {
        if (goldPrice >= 0) {
            this.goldPrice = goldPrice;
        }
    }

    public void setVipPrice(double vipPrice) {
        if (vipPrice >= 0) {
            this.vipPrice = vipPrice;
        }
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity >= 0) {
            this.stockQuantity = stockQuantity;
        }
    }

    public void addStock(int quantity) {
        if (quantity > 0) {
            stockQuantity += quantity;
        }
    }

    public boolean reduceStock(int quantity) {
        if (quantity > 0 && quantity <= stockQuantity) {
            stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    public boolean isLowStock(int lowStockLevel) {
        return stockQuantity <= lowStockLevel;
    }
}
