public class GiftSet extends Product {
    private int itemCount;

    public GiftSet(String productId, String name, String brand, String category,
                   double costPrice, double sellingPrice, int quantityInStock,
                   int itemCount) {
        super(productId, name, brand, category, costPrice, sellingPrice, quantityInStock);
        this.itemCount = itemCount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        if (itemCount > 0) {
            this.itemCount = itemCount;
        }
    }

    @Override
    public String getType() {
        return "GIFTSET";
    }

    @Override
    public String toCSV() {
        return getType() + "," +
                clean(getProductId()) + "," +
                clean(getName()) + "," +
                clean(getBrand()) + "," +
                clean(getCategory()) + "," +
                getCostPrice() + "," +
                getSellingPrice() + "," +
                getQuantityInStock() + ",,0," +
                itemCount;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Items In Set: " + itemCount);
    }
}