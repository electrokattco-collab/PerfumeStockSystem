public class Perfume extends Product {
    private String variantCode;
    private int sizeInMl;

    public Perfume(String productId, String name, String brand, String category,
                   double costPrice, double sellingPrice, int quantityInStock,
                   String variantCode, int sizeInMl) {
        super(productId, name, brand, category, costPrice, sellingPrice, quantityInStock);
        this.variantCode = variantCode;
        this.sizeInMl = sizeInMl;
    }

    public String getVariantCode() {
        return variantCode;
    }

    public int getSizeInMl() {
        return sizeInMl;
    }

    public void setVariantCode(String variantCode) {
        this.variantCode = variantCode;
    }

    public void setSizeInMl(int sizeInMl) {
        if (sizeInMl == 30 || sizeInMl == 50) {
            this.sizeInMl = sizeInMl;
        }
    }

    @Override
    public String getType() {
        return "PERFUME";
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
                getQuantityInStock() + "," +
                clean(variantCode) + "," +
                sizeInMl + ",0";
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Variant Code: " + variantCode);
        System.out.println("Bottle Size: " + sizeInMl + "ml");
    }
}