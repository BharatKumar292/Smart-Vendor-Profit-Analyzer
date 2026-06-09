import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;
import java.sql.*;

class DBConnection {

    public static Connection getConnection() {
        Connection conn = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/vendordb",
                "root",
                "Bharat@#23");
         } catch (Exception e) {
            System.out.println(e);
        }

        return conn;
    }
}

class Product {
    private int id;
    private String name;
    private double purchasePrice;

    public Product(String name, double purchasePrice) {
        this.name = name;
        this.purchasePrice = purchasePrice;
    }

    public String getName() {
        return name;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }
}

class Sale {
    private int productId;
    private int quantity;
    private double sellingPrice;

    public Sale(int productId, int quantity, double sellingPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSellingPrice() {
        return sellingPrice;

    }
}

class Expense {
    private String description;
    private double amount;

    public Expense(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }
}

interface VendorOperations {

    void addProduct(Product p);

    void recordSale(Sale s);

    void addExpense(Expense e);

    void viewProfit();

    void viewTotalSales();

    void viewTotalExpenses();
}


class VendorService implements VendorOperations {

    Connection conn = DBConnection.getConnection();

    // ADD PRODUCT
    public void addProduct(Product p) {
        try {
            String sql = "INSERT INTO Products (product_name, purchase_price) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPurchasePrice());

            ps.executeUpdate();
            System.out.println("Product Added");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // RECORD SALE (CORE LOGIC)
    public void recordSale(Sale s) {
        try {
            // 1. get purchase price
            String q = "SELECT purchase_price FROM Products WHERE product_id=?";
            PreparedStatement ps1 = conn.prepareStatement(q);
            ps1.setInt(1, s.getProductId());

            ResultSet rs = ps1.executeQuery();

            double purchasePrice = 0;
            if (rs.next()) {
                purchasePrice = rs.getDouble(1);
            }

            // 2. calculate profit
            double profit = (s.getSellingPrice() - purchasePrice) * s.getQuantity();

            // 3. insert sale
            String sql = "INSERT INTO Sales (product_id, selling_price, quantity, total_profit) VALUES (?, ?, ?, ?)";
            PreparedStatement ps2 = conn.prepareStatement(sql);

            ps2.setInt(1, s.getProductId());
            ps2.setDouble(2, s.getSellingPrice());
            ps2.setInt(3, s.getQuantity());
            ps2.setDouble(4, profit);

            ps2.executeUpdate();

            System.out.println("Sale Recorded");
            System.out.println("Profit/Loss: " + profit);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // ADD EXPENSE
    public void addExpense(Expense e) {
        try {
            String sql = "INSERT INTO Expenses (description, amount) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, e.getDescription());
            ps.setDouble(2, e.getAmount());

            ps.executeUpdate();

            System.out.println("Expense Added");

        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    // TOTAL SALES
    public void viewTotalSales() {
        try {
            String sql = "SELECT SUM(selling_price * quantity) FROM Sales";
            Statement st = conn.createStatement();

            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                System.out.println("Total Sales: " + rs.getDouble(1));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // TOTAL EXPENSES
    public void viewTotalExpenses() {
        try {
            String sql = "SELECT SUM(amount) FROM Expenses";
            Statement st = conn.createStatement();

            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                System.out.println("Total Expenses: " + rs.getDouble(1));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // PROFIT
    public void viewProfit() {
        try {
            double sales = 0;
            double expenses = 0;

            ResultSet rs1 = conn.createStatement()
                .executeQuery("SELECT SUM(total_profit) FROM Sales");

            if (rs1.next()) {
                sales = rs1.getDouble(1);
            }

            ResultSet rs2 = conn.createStatement()
                .executeQuery("SELECT SUM(amount) FROM Expenses");

            if (rs2.next()) {
                expenses = rs2.getDouble(1);
            }

            double profit = sales - expenses;

            System.out.println("Profit: " + profit);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void todayProfit() {
    try {
        double sales = 0, expense = 0;

        ResultSet rs1 = conn.createStatement()
            .executeQuery("SELECT SUM(total_profit) FROM Sales WHERE DATE(sale_date)=CURDATE()");

        if (rs1.next()) sales = rs1.getDouble(1);

        ResultSet rs2 = conn.createStatement()
            .executeQuery("SELECT SUM(amount) FROM Expenses WHERE DATE(expense_date)=CURDATE()");

        if (rs2.next()) expense = rs2.getDouble(1);

        System.out.println("Today's Profit: " + (sales - expense));

    } catch (Exception e) {
        System.out.println(e);
    }
}


    public void yesterdayProfit() {
    try {
        double sales = 0, expense = 0;

        ResultSet rs1 = conn.createStatement()
            .executeQuery("SELECT SUM(total_profit) FROM Sales WHERE DATE(sale_date)=CURDATE()-INTERVAL 1 DAY");

        if (rs1.next()) sales = rs1.getDouble(1);

        ResultSet rs2 = conn.createStatement()
            .executeQuery("SELECT SUM(amount) FROM Expenses WHERE DATE(expense_date)=CURDATE()-INTERVAL 1 DAY");

        if (rs2.next()) expense = rs2.getDouble(1);

        System.out.println("Yesterday Profit: " + (sales - expense));

    } catch (Exception e) {
        System.out.println(e);
    }
}

public void weeklyProfit() {
    try {
        double sales = 0, expense = 0;

        ResultSet rs1 = conn.createStatement()
            .executeQuery("SELECT SUM(total_profit) FROM Sales WHERE sale_date >= CURDATE()-INTERVAL 7 DAY");

        if (rs1.next()) sales = rs1.getDouble(1);

        ResultSet rs2 = conn.createStatement()
            .executeQuery("SELECT SUM(amount) FROM Expenses WHERE expense_date >= CURDATE()-INTERVAL 7 DAY");

        if (rs2.next()) expense = rs2.getDouble(1);

        System.out.println("Weekly Profit: " + (sales - expense));

    } catch (Exception e) {
        System.out.println(e);
    }
}

public void monthlyProfit() {
    try {
        double sales = 0, expense = 0;

        ResultSet rs1 = conn.createStatement()
            .executeQuery("SELECT SUM(total_profit) FROM Sales WHERE MONTH(sale_date)=MONTH(CURDATE()) AND YEAR(sale_date)=YEAR(CURDATE())");

        if (rs1.next()) sales = rs1.getDouble(1);

        ResultSet rs2 = conn.createStatement()
            .executeQuery("SELECT SUM(amount) FROM Expenses WHERE MONTH(expense_date)=MONTH(CURDATE()) AND YEAR(expense_date)=YEAR(CURDATE())");

        if (rs2.next()) expense = rs2.getDouble(1);

        System.out.println("Monthly Profit: " + (sales - expense));

    } catch (Exception e) {
        System.out.println(e);
    }
}

public void profitReportSheet() {
    try {

        String sql =
        "SELECT DATE(sale_date) AS date, " +
        "SUM(total_profit) AS sales, " +
        "(SELECT IFNULL(SUM(amount),0) FROM Expenses e WHERE DATE(e.expense_date)=DATE(s.sale_date)) AS expense, " +
        "SUM(total_profit) - " +
        "(SELECT IFNULL(SUM(amount),0) FROM Expenses e WHERE DATE(e.expense_date)=DATE(s.sale_date)) AS profit " +
        "FROM Sales s GROUP BY DATE(sale_date)";

        ResultSet rs = conn.createStatement().executeQuery(sql);

        System.out.println("\nDATE | SALES | EXPENSE | PROFIT");
        System.out.println("--------------------------------------");

        while (rs.next()) {
            System.out.println(
                rs.getDate(1) + " | " +
                rs.getDouble(2) + " | " +
                rs.getDouble(3) + " | " +
                rs.getDouble(4)
            );
        }

    } catch (Exception e) {
        System.out.println(e);
    }
}

public void productSalesByDate(String date) {
    try {

        String sql =
        "SELECT p.product_name, s.quantity, s.total_profit " +
        "FROM Sales s JOIN Products p ON s.product_id=p.product_id " +
        "WHERE DATE(s.sale_date)=?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, date);

        ResultSet rs = ps.executeQuery();

        System.out.println("\nPRODUCT | QTY | PROFIT");
        System.out.println("--------------------------");

        while (rs.next()) {
            System.out.println(
                rs.getString(1) + " | " +
                rs.getInt(2) + " | " +
                rs.getDouble(3)
            );
        }

    } catch (Exception e) {
        System.out.println(e);
    }
}
}


public class Vendor_System {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        VendorService service = new VendorService();

        while (true) {

            System.out.println("\n===== SMART VENDOR SYSTEM =====");
            System.out.println("1. Add Product");
            System.out.println("2. Record Sale");
            System.out.println("3. Add Expense");

            System.out.println("4. View Today's Profit");
            System.out.println("5. View Yesterday's Profit");
            System.out.println("6. View Weekly Profit");
            System.out.println("7. View Monthly Profit");

            System.out.println("8. View Profit Report Sheet");
            System.out.println("9. View Product Sales by Date");

            System.out.println("10. Exit");
            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    System.out.print("Name: ");
                    String name = sc.next();

                    System.out.print("Purchase Price: ");
                    double pp = sc.nextDouble();

                    service.addProduct(new Product(name, pp));
                    break;

                case 2:
                    System.out.print("Product ID: ");
                    int pid = sc.nextInt();

                    System.out.print("Quantity: ");
                    int q = sc.nextInt();

                    System.out.print("Selling Price: ");
                    double sp = sc.nextDouble();

                    service.recordSale(new Sale(pid, q, sp));
                    break;

                case 3:
                    System.out.print("Expense: ");
                    String desc = sc.next();

                    System.out.print("Amount: ");
                    double amt = sc.nextDouble();

                    service.addExpense(new Expense(desc, amt));
                    break;

                case 4:
            service.todayProfit();
            break;

        case 5:
            service.yesterdayProfit();
            break;

        case 6:
            service.weeklyProfit();
            break;

        case 7:
            service.monthlyProfit();
            break;

        case 8:
            service.profitReportSheet();
            break;

        case 9:
            System.out.print("Enter date (YYYY-MM-DD): ");
            String date = sc.next();
            service.productSalesByDate(date);
            break;

        case 10:
            System.exit(0);
            }
        }
    }
}

