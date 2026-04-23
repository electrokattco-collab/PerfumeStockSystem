import java.util.ArrayList;
import java.util.LinkedHashSet;

public class ProductCatalog {
    public static class ProductTemplate {
        private String name;
        private String category;
        private String size;
        private double retailPrice;
        private double rewardsPrice;
        private double goldPrice;
        private double vipPrice;

        public ProductTemplate(String name, String category, String size,
                               double retailPrice, double rewardsPrice,
                               double goldPrice, double vipPrice) {
            this.name = name;
            this.category = category;
            this.size = size;
            this.retailPrice = retailPrice;
            this.rewardsPrice = rewardsPrice;
            this.goldPrice = goldPrice;
            this.vipPrice = vipPrice;
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
    }

    public static ArrayList<ProductTemplate> getSupplierProducts() {
        ArrayList<ProductTemplate> products = new ArrayList<>();

        products.add(starterProduct("50mL Superior Perfume [Eau de Parfum]", "Perfume", "50mL",
                199, 109, 98, 95));
        products.add(starterProduct("30mL Superior Perfume [Eau de Parfum]", "Perfume", "30mL",
                125, 58, 56, 56));
        products.add(starterProduct("50mL Essentials Perfume [Eau de Toilette]", "Perfume", "50mL",
                99, 65, 55, 55));
        products.add(starterProduct("400mL Body Lotion", "Body Care", "400mL",
                85, 55, 48, 48));
        products.add(starterProduct("50mL Roll On", "Roll On", "50mL",
                39, 25, 22, 21));

        return products;
    }

    public static ArrayList<String> getProductTypes() {
        LinkedHashSet<String> types = new LinkedHashSet<>();

        for (ProductTemplate template : getSupplierProducts()) {
            types.add(template.getCategory());
        }

        return new ArrayList<>(types);
    }

    public static ArrayList<ProductTemplate> getProductsByType(String type) {
        ArrayList<ProductTemplate> matches = new ArrayList<>();
        String search = type == null ? "" : type.trim().toLowerCase();

        for (ProductTemplate template : getSupplierProducts()) {
            if (template.getCategory().toLowerCase().contains(search)) {
                matches.add(template);
            }
        }

        return matches;
    }

    public static ProductTemplate findTemplateByName(String productName) {
        if (productName == null) {
            return null;
        }

        for (ProductTemplate template : getSupplierProducts()) {
            if (template.getName().equalsIgnoreCase(productName.trim())) {
                return template;
            }
        }

        return null;
    }

    public static double getSupplierUnitCost(ProductTemplate template, int quantity) {
        if (template == null || quantity <= 0) {
            return 0;
        }

        String name = template.getName().trim().toLowerCase();

        if (name.equals("50ml superior perfume [eau de parfum]")) {
            if (quantity >= 20) {
                return 99;
            }
            if (quantity >= 10) {
                return 100;
            }
            if (quantity >= 5) {
                return 109;
            }
            if (quantity == 4) {
                return 139;
            }
            if (quantity == 3) {
                return 149;
            }
            return 179;
        }

        if (name.equals("30ml superior perfume [eau de parfum]")) {
            if (quantity >= 20) {
                return 60;
            }
            if (quantity >= 10) {
                return 62;
            }
            if (quantity >= 5) {
                return 69;
            }
            if (quantity == 4) {
                return 87;
            }
            if (quantity == 3) {
                return 94;
            }
            return 113;
        }

        if (name.equals("50ml essentials perfume [eau de toilette]")) {
            if (quantity >= 20) {
                return 58;
            }
            if (quantity >= 10) {
                return 59;
            }
            if (quantity >= 5) {
                return 65;
            }
            if (quantity == 4) {
                return 69;
            }
            if (quantity == 3) {
                return 79;
            }
            return 89;
        }

        if (name.equals("400ml body lotion")) {
            if (quantity >= 10) {
                return 49;
            }
            if (quantity >= 4) {
                return 55;
            }
            if (quantity >= 2) {
                return 70;
            }
            return 77;
        }

        if (name.equals("50ml roll on")) {
            if (quantity >= 10) {
                return 23;
            }
            if (quantity >= 4) {
                return 25;
            }
            if (quantity == 3) {
                return 30;
            }
            return 35;
        }

        return template.getRetailPrice();
    }

    public static String getSupplierTierLabel(ProductTemplate template, int quantity) {
        if (template == null || quantity <= 0) {
            return "";
        }

        String name = template.getName().trim().toLowerCase();

        if (name.equals("50ml superior perfume [eau de parfum]")) {
            if (quantity >= 20) {
                return "20+ tier";
            }
            if (quantity >= 10) {
                return "10-19 tier";
            }
            if (quantity >= 5) {
                return "5-9 tier";
            }
            if (quantity == 4) {
                return "4 tier";
            }
            if (quantity == 3) {
                return "3 tier";
            }
            return "1-2 tier";
        }

        if (name.equals("30ml superior perfume [eau de parfum]")) {
            if (quantity >= 20) {
                return "20+ tier";
            }
            if (quantity >= 10) {
                return "10-19 tier";
            }
            if (quantity >= 5) {
                return "5-9 tier";
            }
            if (quantity == 4) {
                return "4 tier";
            }
            if (quantity == 3) {
                return "3 tier";
            }
            return "1-2 tier";
        }

        if (name.equals("50ml essentials perfume [eau de toilette]")) {
            if (quantity >= 20) {
                return "20+ tier";
            }
            if (quantity >= 10) {
                return "10-19 tier";
            }
            if (quantity >= 5) {
                return "5-9 tier";
            }
            if (quantity == 4) {
                return "4 tier";
            }
            if (quantity == 3) {
                return "3 tier";
            }
            return "1-2 tier";
        }

        if (name.equals("400ml body lotion")) {
            if (quantity >= 10) {
                return "10+ tier";
            }
            if (quantity >= 4) {
                return "4-9 tier";
            }
            if (quantity >= 2) {
                return "2-3 tier";
            }
            return "1 tier";
        }

        if (name.equals("50ml roll on")) {
            if (quantity >= 10) {
                return "10+ tier";
            }
            if (quantity >= 4) {
                return "4-9 tier";
            }
            if (quantity == 3) {
                return "3 tier";
            }
            return "1-2 tier";
        }

        return "default tier";
    }

    private static ProductTemplate starterProduct(String name, String category, String size,
                                                  double retailPrice, double rewardsPrice,
                                                  double goldPrice, double vipPrice) {
        // Default supplier pricing uses the stated bulk tier for each product.
        return new ProductTemplate(name, category, size, retailPrice, rewardsPrice, goldPrice, vipPrice);
    }

    public static ProductTemplate getSupplierProduct(int number) {
        ArrayList<ProductTemplate> products = getSupplierProducts();
        int index = number - 1;

        if (index < 0 || index >= products.size()) {
            return null;
        }

        return products.get(index);
    }

    public static String prefixFor(String category, String size, String name) {
        String text = ((category == null ? "" : category) + " " +
                (size == null ? "" : size) + " " +
                (name == null ? "" : name)).toLowerCase();

        if (text.contains("gift")) {
            return "PG";
        }
        if (text.contains("combo")) {
            return "PC";
        }
        if (text.contains("watch")) {
            return "PT";
        }
        if (text.contains("perfume")) {
            return text.contains("30") ? "PS" : "PP";
        }
        if (text.contains("roll")) {
            return "PR";
        }
        if (text.contains("lotion")) {
            return "PB";
        }
        if (text.contains("wash")) {
            return "PW";
        }
        if (text.contains("spray")) {
            return "PD";
        }

        return "PN";
    }

    public static String standardCategory(String name, String category) {
        String text = ((name == null ? "" : name) + " " +
                (category == null ? "" : category)).toLowerCase();

        if (text.contains("gift")) {
            return "Gift Set";
        }
        if (text.contains("combo")) {
            return "Combo";
        }
        if (text.contains("watch")) {
            return "Watch";
        }
        if (text.contains("perfume")) {
            return "Perfume";
        }
        if (text.contains("lotion") || text.contains("body care")) {
            return "Body Care";
        }
        if (text.contains("wash")) {
            return "Body Care";
        }
        if (text.contains("roll")) {
            return "Roll On";
        }
        if (text.contains("spray")) {
            return "Body Spray";
        }

        return category == null || category.isBlank() ? "General" : category;
    }

    public static String standardSize(String name, String category, String size) {
        if (size != null && !size.isBlank()) {
            return size;
        }

        String text = ((name == null ? "" : name) + " " +
                (category == null ? "" : category)).toLowerCase();

        if (text.contains("combo") || text.contains("gift") || text.contains("watch")) {
            return "";
        }

        if (text.contains("400ml") || text.contains("400 ml") ||
                text.contains("450ml") || text.contains("450 ml") ||
                text.contains("lotion") || text.contains("wash")) {
            return "400mL";
        }
        if (text.contains("50ml") || text.contains("50 ml")) {
            return "50mL";
        }
        if (text.contains("30ml") || text.contains("30 ml") || text.contains("travel")) {
            return "30mL";
        }

        return "";
    }

    public static String standardName(String name, String category, String size) {
        String text = ((name == null ? "" : name) + " " +
                (category == null ? "" : category) + " " +
                (size == null ? "" : size)).toLowerCase();

        if (text.contains("combo") || text.contains("gift") || text.contains("watch")) {
            return name == null ? "" : name;
        }

        if (text.contains("superior perfume") && text.contains("50")) {
            return "50mL Superior Perfume [Eau de Parfum]";
        }
        if ((text.contains("travel") || text.contains("superior perfume")) && text.contains("30")) {
            return "30mL Superior Perfume [Eau de Parfum]";
        }
        if (text.contains("essentials") && text.contains("perfume")) {
            return "50mL Essentials Perfume [Eau de Toilette]";
        }
        if (text.contains("body lotion")) {
            return "400mL Body Lotion";
        }
        if (text.contains("body wash")) {
            return "400mL Body Wash";
        }
        if (text.contains("roll")) {
            return "50mL Roll On";
        }
        if (text.contains("male body spray") || text.contains("body spray him")) {
            return "Male Body Spray";
        }
        if (text.contains("female body spray") || text.contains("body spray her")) {
            return "Female Body Spray";
        }

        return name == null ? "" : name;
    }
}
