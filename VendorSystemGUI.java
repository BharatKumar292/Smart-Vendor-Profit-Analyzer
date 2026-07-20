import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/* =======================================================================
   SMART VENDOR PROFIT ANALYZER - PROFESSIONAL GUI EDITION
   All original database logic, SQL queries and business rules are kept
   IDENTICAL to the console version. Only the presentation layer (GUI)
   and small return-value refactors (so results can be shown on screen
   instead of printed to console) have been added.
   ======================================================================= */

/* ================= DATABASE CONNECTION ================= */
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

/* ================= MODELS ================= */
class Product {
    private int id;
    private String name;
    private double purchasePrice;
    private int availableQuantity;

    public Product(String name, double purchasePrice, int availableQuantity) {
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.availableQuantity = availableQuantity;
    }

    public Product(int id, String name, double purchasePrice, int availableQuantity) {
        this.id = id;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.availableQuantity = availableQuantity;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPurchasePrice() { return purchasePrice; }
    public int getAvailableQuantity() { return availableQuantity; }

    @Override
    public String toString() {
        return id + " - " + name + "  (Stock: " + availableQuantity + ")";
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

    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getSellingPrice() { return sellingPrice; }
}

class Expense {
    private String description;
    private double amount;

    public Expense(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() { return description; }
    public double getAmount() { return amount; }
}

/* ================= SERVICE INTERFACE ================= */
interface VendorOperations {
    void addProduct(Product p) throws Exception;
    double recordSale(Sale s) throws Exception;
    void addExpense(Expense e) throws Exception;
    double viewProfit() throws Exception;
    double viewTotalSales() throws Exception;
    double viewTotalExpenses() throws Exception;
}

/* ================= SERVICE IMPLEMENTATION ================= */
class VendorService implements VendorOperations {

    Connection conn = DBConnection.getConnection();

    private void requireConnection() throws SQLException {
        if (conn == null) {
            throw new SQLException("No database connection. Please check DBConnection settings.");
        }
    }

    // ADD PRODUCT
    public void addProduct(Product p) throws Exception {
        requireConnection();
        String sql = "INSERT INTO Products (product_name, purchase_price, quantity_available) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, p.getName());
        ps.setDouble(2, p.getPurchasePrice());
        ps.setInt(3, p.getAvailableQuantity());

        ps.executeUpdate();
    }

    // RECORD SALE (CORE LOGIC preserved, extended with stock availability check)
    public double recordSale(Sale s) throws Exception {
        requireConnection();

        // 1. get purchase price and available quantity
        String q = "SELECT purchase_price, quantity_available FROM Products WHERE product_id=?";
        PreparedStatement ps1 = conn.prepareStatement(q);
        ps1.setInt(1, s.getProductId());

        ResultSet rs = ps1.executeQuery();

        double purchasePrice = 0;
        int availableQuantity = 0;
        boolean productFound = false;
        if (rs.next()) {
            purchasePrice = rs.getDouble(1);
            availableQuantity = rs.getInt(2);
            productFound = true;
        }

        if (!productFound) {
            throw new Exception("Product not found.");
        }

        // 1b. stock availability check
        if (s.getQuantity() > availableQuantity) {
            throw new Exception("Insufficient stock. Only " + availableQuantity + " unit(s) available.");
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

        // 4. reduce available stock
        String updateStockSql = "UPDATE Products SET quantity_available = quantity_available - ? WHERE product_id=?";
        PreparedStatement ps3 = conn.prepareStatement(updateStockSql);
        ps3.setInt(1, s.getQuantity());
        ps3.setInt(2, s.getProductId());
        ps3.executeUpdate();

        return profit;
    }

    // ADD EXPENSE
    public void addExpense(Expense e) throws Exception {
        requireConnection();
        String sql = "INSERT INTO Expenses (description, amount) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, e.getDescription());
        ps.setDouble(2, e.getAmount());

        ps.executeUpdate();
    }

    // TOTAL SALES
    public double viewTotalSales() throws Exception {
        requireConnection();
        String sql = "SELECT SUM(selling_price * quantity) FROM Sales";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        double total = 0;
        if (rs.next()) total = rs.getDouble(1);
        return total;
    }

    // TOTAL EXPENSES
    public double viewTotalExpenses() throws Exception {
        requireConnection();
        String sql = "SELECT SUM(amount) FROM Expenses";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        double total = 0;
        if (rs.next()) total = rs.getDouble(1);
        return total;
    }

    // OVERALL PROFIT
    public double viewProfit() throws Exception {
        requireConnection();
        double sales = 0;
        double expenses = 0;

        ResultSet rs1 = conn.createStatement()
            .executeQuery("SELECT SUM(total_profit) FROM Sales");
        if (rs1.next()) sales = rs1.getDouble(1);

        ResultSet rs2 = conn.createStatement()
            .executeQuery("SELECT SUM(amount) FROM Expenses");
        if (rs2.next()) expenses = rs2.getDouble(1);

        return sales - expenses;
    }

    public double todayProfit() throws Exception {
        requireConnection();
        double sales = 0, expense = 0;

        ResultSet rs1 = conn.createStatement()
            .executeQuery("SELECT SUM(total_profit) FROM Sales WHERE DATE(sale_date)=CURDATE()");
        if (rs1.next()) sales = rs1.getDouble(1);

        ResultSet rs2 = conn.createStatement()
            .executeQuery("SELECT SUM(amount) FROM Expenses WHERE DATE(expense_date)=CURDATE()");
        if (rs2.next()) expense = rs2.getDouble(1);

        return sales - expense;
    }

    public double yesterdayProfit() throws Exception {
        requireConnection();
        double sales = 0, expense = 0;

        ResultSet rs1 = conn.createStatement()
            .executeQuery("SELECT SUM(total_profit) FROM Sales WHERE DATE(sale_date)=CURDATE()-INTERVAL 1 DAY");
        if (rs1.next()) sales = rs1.getDouble(1);

        ResultSet rs2 = conn.createStatement()
            .executeQuery("SELECT SUM(amount) FROM Expenses WHERE DATE(expense_date)=CURDATE()-INTERVAL 1 DAY");
        if (rs2.next()) expense = rs2.getDouble(1);

        return sales - expense;
    }

    public double weeklyProfit() throws Exception {
        requireConnection();
        double sales = 0, expense = 0;

        ResultSet rs1 = conn.createStatement()
            .executeQuery("SELECT SUM(total_profit) FROM Sales WHERE sale_date >= CURDATE()-INTERVAL 7 DAY");
        if (rs1.next()) sales = rs1.getDouble(1);

        ResultSet rs2 = conn.createStatement()
            .executeQuery("SELECT SUM(amount) FROM Expenses WHERE expense_date >= CURDATE()-INTERVAL 7 DAY");
        if (rs2.next()) expense = rs2.getDouble(1);

        return sales - expense;
    }

    public double monthlyProfit() throws Exception {
        requireConnection();
        double sales = 0, expense = 0;

        ResultSet rs1 = conn.createStatement()
            .executeQuery("SELECT SUM(total_profit) FROM Sales WHERE MONTH(sale_date)=MONTH(CURDATE()) AND YEAR(sale_date)=YEAR(CURDATE())");
        if (rs1.next()) sales = rs1.getDouble(1);

        ResultSet rs2 = conn.createStatement()
            .executeQuery("SELECT SUM(amount) FROM Expenses WHERE MONTH(expense_date)=MONTH(CURDATE()) AND YEAR(expense_date)=YEAR(CURDATE())");
        if (rs2.next()) expense = rs2.getDouble(1);

        return sales - expense;
    }

    // PROFIT REPORT SHEET -> returns rows instead of printing
    public List<Object[]> profitReportSheet() throws Exception {
        requireConnection();

        String sql =
            "SELECT d.date, IFNULL(sa.sales,0) AS sales, IFNULL(ea.expense,0) AS expense, " +
            "IFNULL(sa.sales,0) - IFNULL(ea.expense,0) AS profit " +
            "FROM (SELECT DATE(sale_date) AS date FROM Sales " +
            "UNION SELECT DATE(expense_date) AS date FROM Expenses) d " +
            "LEFT JOIN (SELECT DATE(sale_date) AS date, SUM(total_profit) AS sales FROM Sales GROUP BY DATE(sale_date)) sa " +
            "ON d.date = sa.date " +
            "LEFT JOIN (SELECT DATE(expense_date) AS date, SUM(amount) AS expense FROM Expenses GROUP BY DATE(expense_date)) ea " +
            "ON d.date = ea.date " +
            "ORDER BY d.date";

        ResultSet rs = conn.createStatement().executeQuery(sql);

        List<Object[]> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(new Object[] {
                rs.getDate(1),
                rs.getDouble(2),
                rs.getDouble(3),
                rs.getDouble(4)
            });
        }
        return rows;
    }

    // PRODUCT SALES BY DATE -> returns rows instead of printing
    public List<Object[]> productSalesByDate(String date) throws Exception {
        requireConnection();

        String sql =
            "SELECT p.product_name, s.quantity, s.total_profit " +
            "FROM Sales s JOIN Products p ON s.product_id=p.product_id " +
            "WHERE DATE(s.sale_date)=?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, date);

        ResultSet rs = ps.executeQuery();

        List<Object[]> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(new Object[] {
                rs.getString(1),
                rs.getInt(2),
                rs.getDouble(3)
            });
        }
        return rows;
    }

    // Helper for GUI - populate product dropdowns/tables (new, non-destructive addition)
    public List<Product> getAllProducts() throws Exception {
        requireConnection();
        String sql = "SELECT product_id, product_name, purchase_price, quantity_available FROM Products ORDER BY product_id";
        ResultSet rs = conn.createStatement().executeQuery(sql);

        List<Product> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Product(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getInt(4)));
        }
        return list;
    }
}

/* =======================================================================
   REUSABLE UI COMPONENTS
   ======================================================================= */

class RoundedButton extends JButton {
    private final Color bgColor;
    private final Color hoverColor;
    private boolean hovering = false;

    public RoundedButton(String text, Color bgColor, Color hoverColor) {
        super(text);
        this.bgColor = bgColor;
        this.hoverColor = hoverColor;
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isEnabled() ? (hovering ? hoverColor : bgColor) : new Color(180, 186, 196));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }
}

class SidebarButton extends JButton {
    private boolean active = false;
    private boolean hovering = false;
    private static final Color ACTIVE_BG = new Color(37, 99, 235);
    private static final Color HOVER_BG = new Color(30, 41, 59);

    public SidebarButton(String text) {
        super(text);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.PLAIN, 15));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 12));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 15));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (active) {
            g2.setColor(ACTIVE_BG);
            g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 10, 10);
        } else if (hovering) {
            g2.setColor(HOVER_BG);
            g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 10, 10);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

class StatCard extends JPanel {
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final Color accent;

    public StatCard(String title, String iconChar, Color accent) {
        this.accent = accent;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 20));

        JPanel iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(accent);
                g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(iconChar)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(iconChar, tx, ty);
                g2.dispose();
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(38, 38));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(100, 116, 139));

        JPanel headerRow = new JPanel(new BorderLayout(10, 0));
        headerRow.setOpaque(false);
        headerRow.add(iconBadge, BorderLayout.WEST);
        headerRow.add(titleLabel, BorderLayout.CENTER);

        valueLabel = new JLabel("--");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(15, 23, 42));
        valueLabel.setBorder(BorderFactory.createEmptyBorder(14, 0, 2, 0));

        subtitleLabel = new JLabel(" ");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(148, 163, 184));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(valueLabel);
        bottom.add(subtitleLabel);

        add(headerRow, BorderLayout.NORTH);
        add(bottom, BorderLayout.SOUTH);
    }

    public void setValue(String text, Color color) {
        valueLabel.setText(text);
        valueLabel.setForeground(color);
    }

    public void setSubtitle(String text) {
        subtitleLabel.setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 4;
        int h = getHeight() - 6;

        // soft drop shadow
        g2.setColor(new Color(15, 23, 42, 22));
        g2.fillRoundRect(2, 4, w, h, 16, 16);

        // card body
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w, h, 16, 16);

        // subtle border
        g2.setColor(new Color(226, 232, 240));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

        // left accent bar (decorative, inset, no corner clash)
        g2.setColor(accent);
        g2.fillRoundRect(0, 14, 4, h - 28, 4, 4);

        g2.dispose();
        super.paintComponent(g);
    }
}

/* =======================================================================
   MAIN APPLICATION WINDOW
   ======================================================================= */
public class VendorSystemGUI extends JFrame {

    private static final Color BG = new Color(241, 245, 249);
    private static final Color SIDEBAR_BG = new Color(15, 23, 42);
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color ACCENT_VIOLET = new Color(124, 58, 237);
    private static final Color ACCENT_TEAL = new Color(13, 148, 136);
    private static final Color ACCENT_INDIGO = new Color(79, 70, 229);
    private static final Color ACCENT_SKY = new Color(2, 132, 199);
    private static final DecimalFormat MONEY = new DecimalFormat("\u20B9 #,##0.00");
    private static final String ARROW_UP = "\u25B2";
    private static final String ARROW_DOWN = "\u25BC";

    private final VendorService service = new VendorService();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final List<SidebarButton> navButtons = new ArrayList<>();
    private JLabel connectionStatusLabel;

    // Dashboard stat cards
    private StatCard cardToday, cardYesterday, cardWeekly, cardMonthly;
    private StatCard cardTotalSales, cardTotalExpenses, cardOverallProfit;

    // Record sale combo
    private JComboBox<Product> productCombo;

    // Tables
    private DefaultTableModel productTableModel;
    private DefaultTableModel reportTableModel;
    private DefaultTableModel salesByDateTableModel;

    public VendorSystemGUI() {
        setTitle("Smart Vendor Profit Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildTopBar(), BorderLayout.NORTH);

        contentPanel.setBackground(BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        contentPanel.add(buildDashboardPanel(), "DASHBOARD");
        contentPanel.add(buildAddProductPanel(), "ADD_PRODUCT");
        contentPanel.add(buildRecordSalePanel(), "RECORD_SALE");
        contentPanel.add(buildAddExpensePanel(), "ADD_EXPENSE");
        contentPanel.add(buildReportSheetPanel(), "REPORT_SHEET");
        contentPanel.add(buildSalesByDatePanel(), "SALES_BY_DATE");

        add(contentPanel, BorderLayout.CENTER);

        showCard("DASHBOARD");
        refreshDashboard();
        updateConnectionStatus();
    }

    /* ---------------- SIDEBAR ---------------- */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        JLabel brand = new JLabel("  SMART VENDOR");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel("  Profit Analyzer");
        brandSub.setForeground(new Color(148, 163, 184));
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(brand);
        sidebar.add(brandSub);
        sidebar.add(Box.createVerticalStrut(30));

        String[][] safeItems = {
            {"DASHBOARD", "Dashboard"},
            {"ADD_PRODUCT", "Add Product"},
            {"RECORD_SALE", "Record Sale"},
            {"ADD_EXPENSE", "Add Expense"},
            {"REPORT_SHEET", "Profit Report Sheet"},
            {"SALES_BY_DATE", "Sales by Date"},
        };

        for (String[] item : safeItems) {
            SidebarButton btn = new SidebarButton("   " + item[1]);
            btn.addActionListener(e -> showCard(item[0]));
            btn.putClientProperty("cardName", item[0]);
            navButtons.add(btn);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        SidebarButton exitBtn = new SidebarButton("   Exit Application");
        exitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?", "Confirm Exit",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });
        sidebar.add(exitBtn);
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }

    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
        for (SidebarButton b : navButtons) {
            b.setActive(name.equals(b.getClientProperty("cardName")));
        }
        if (name.equals("DASHBOARD")) refreshDashboard();
        if (name.equals("RECORD_SALE")) refreshProductCombo();
        if (name.equals("ADD_PRODUCT")) refreshProductTable();
    }

    /* ---------------- TOP BAR ---------------- */
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));

        JLabel pageTitle = new JLabel("Overview");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitle.setForeground(TEXT_DARK);

        connectionStatusLabel = new JLabel("Checking connection...");
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        top.add(pageTitle, BorderLayout.WEST);
        top.add(connectionStatusLabel, BorderLayout.EAST);
        return top;
    }

    private void updateConnectionStatus() {
        boolean connected = service.conn != null;
        connectionStatusLabel.setText(connected ? "\u25CF Database Connected" : "\u25CF Database Disconnected");
        connectionStatusLabel.setForeground(connected ? SUCCESS : DANGER);
    }

    /* ---------------- DASHBOARD ---------------- */
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(2, 4, 18, 18));
        grid.setOpaque(false);

        cardToday = new StatCard("Today's Profit", "\u25CF", PRIMARY);
        cardYesterday = new StatCard("Yesterday's Profit", "\u25C6", ACCENT_VIOLET);
        cardWeekly = new StatCard("Weekly Profit", "\u25B2", ACCENT_TEAL);
        cardMonthly = new StatCard("Monthly Profit", "\u2605", ACCENT_INDIGO);
        cardTotalSales = new StatCard("Total Sales", "\u2191", ACCENT_SKY);
        cardTotalExpenses = new StatCard("Total Expenses", "\u2193", DANGER);
        cardOverallProfit = new StatCard("Overall Profit", "\u2714", SUCCESS);

        for (StatCard c : new StatCard[]{cardToday, cardYesterday, cardWeekly, cardMonthly,
                cardTotalSales, cardTotalExpenses, cardOverallProfit}) {
            grid.add(c);
        }

        JLabel subheading = new JLabel("A live snapshot of your business performance");
        subheading.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subheading.setForeground(TEXT_MUTED);

        RoundedButton refreshBtn = new RoundedButton("Refresh Dashboard", PRIMARY, PRIMARY_HOVER);
        refreshBtn.addActionListener(e -> refreshDashboard());

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerRow.add(subheading, BorderLayout.WEST);
        JPanel refreshWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        refreshWrap.setOpaque(false);
        refreshWrap.add(refreshBtn);
        headerRow.add(refreshWrap, BorderLayout.EAST);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private void refreshDashboard() {
        updateConnectionStatus();
        setStatCardSafely(cardToday, () -> service.todayProfit());
        setStatCardSafely(cardYesterday, () -> service.yesterdayProfit());
        setStatCardSafely(cardWeekly, () -> service.weeklyProfit());
        setStatCardSafely(cardMonthly, () -> service.monthlyProfit());
        setStatCardSafely(cardTotalSales, () -> service.viewTotalSales());
        setStatCardSafely(cardTotalExpenses, () -> service.viewTotalExpenses());
        setStatCardSafely(cardOverallProfit, () -> service.viewProfit());
    }

    private interface DoubleSupplierEx {
        double get() throws Exception;
    }

    private void setStatCardSafely(StatCard card, DoubleSupplierEx supplier) {
        try {
            double value = supplier.get();
            boolean positive = value >= 0;
            String arrow = positive ? ARROW_UP : ARROW_DOWN;
            card.setValue(arrow + " " + MONEY.format(Math.abs(value)), positive ? SUCCESS : DANGER);
            card.setSubtitle(positive ? "In the green" : "Running at a loss");
        } catch (Exception ex) {
            card.setValue("N/A", TEXT_MUTED);
            card.setSubtitle("Could not load data");
        }
    }

    /* ---------------- ADD PRODUCT ---------------- */
    private JPanel buildAddProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        JPanel formCard = card();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        JLabel heading = sectionHeading("Add New Product");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(heading, gbc);
        gbc.gridwidth = 1;

        JTextField nameField = styledField();
        JTextField priceField = styledField();
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gbc.gridy = 1; gbc.gridx = 0; formCard.add(formLabel("Product Name"), gbc);
        gbc.gridx = 1; formCard.add(nameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; formCard.add(formLabel("Purchase Price"), gbc);
        gbc.gridx = 1; formCard.add(priceField, gbc);

        JTextField quantityField = styledField();
        gbc.gridy = 3; gbc.gridx = 0; formCard.add(formLabel("Available Quantity"), gbc);
        gbc.gridx = 1; formCard.add(quantityField, gbc);

        RoundedButton addBtn = new RoundedButton("Add Product", PRIMARY, PRIMARY_HOVER);
        gbc.gridy = 4; gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formCard.add(addBtn, gbc);

        gbc.gridy = 5; gbc.gridx = 1;
        formCard.add(statusLabel, gbc);

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String quantityText = quantityField.getText().trim();
            if (name.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
                statusLabel.setForeground(DANGER);
                statusLabel.setText("Please fill in all fields.");
                return;
            }
            try {
                double price = Double.parseDouble(priceText);
                int quantity = Integer.parseInt(quantityText);
                if (quantity < 0) {
                    statusLabel.setForeground(DANGER);
                    statusLabel.setText("Available quantity cannot be negative.");
                    return;
                }
                service.addProduct(new Product(name, price, quantity));
                statusLabel.setForeground(SUCCESS);
                statusLabel.setText("Product \"" + name + "\" added successfully.");
                nameField.setText("");
                priceField.setText("");
                quantityField.setText("");
                refreshProductTable();
            } catch (NumberFormatException nfe) {
                statusLabel.setForeground(DANGER);
                statusLabel.setText("Purchase price and quantity must be valid numbers.");
            } catch (Exception ex) {
                statusLabel.setForeground(DANGER);
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(sectionHeading("Existing Products"), BorderLayout.NORTH);

        productTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Purchase Price", "Available Qty"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable productTable = styledTable(productTableModel);
        tableCard.add(new JScrollPane(productTable), BorderLayout.CENTER);

        panel.add(formCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private void refreshProductTable() {
        try {
            productTableModel.setRowCount(0);
            for (Product p : service.getAllProducts()) {
                productTableModel.addRow(new Object[]{p.getId(), p.getName(), MONEY.format(p.getPurchasePrice()), p.getAvailableQuantity()});
            }
        } catch (Exception ignored) { }
    }

    /* ---------------- RECORD SALE ---------------- */
    private JPanel buildRecordSalePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel formCard = card();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(sectionHeading("Record a Sale"), gbc);
        gbc.gridwidth = 1;

        productCombo = new JComboBox<>();
        productCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel stockLabel = new JLabel(" ");
        stockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stockLabel.setForeground(TEXT_MUTED);

        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
        quantitySpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTextField priceField = styledField();
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        gbc.gridy = 1; gbc.gridx = 0; formCard.add(formLabel("Product"), gbc);
        gbc.gridx = 1; formCard.add(productCombo, gbc);

        gbc.gridy = 2; gbc.gridx = 1;
        formCard.add(stockLabel, gbc);

        gbc.gridy = 3; gbc.gridx = 0; formCard.add(formLabel("Quantity"), gbc);
        gbc.gridx = 1; formCard.add(quantitySpinner, gbc);

        gbc.gridy = 4; gbc.gridx = 0; formCard.add(formLabel("Selling Price (per unit)"), gbc);
        gbc.gridx = 1; formCard.add(priceField, gbc);

        RoundedButton recordBtn = new RoundedButton("Record Sale", PRIMARY, PRIMARY_HOVER);
        RoundedButton refreshProductsBtn = new RoundedButton("Refresh Products", new Color(100, 116, 139), new Color(71, 85, 105));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(recordBtn);
        btnRow.add(refreshProductsBtn);

        gbc.gridy = 5; gbc.gridx = 1;
        formCard.add(btnRow, gbc);

        gbc.gridy = 6; gbc.gridx = 1;
        formCard.add(resultLabel, gbc);

        productCombo.addActionListener(e -> updateStockLabel(stockLabel));
        refreshProductsBtn.addActionListener(e -> {
            refreshProductCombo();
            updateStockLabel(stockLabel);
        });

        recordBtn.addActionListener(e -> {
            Product selected = (Product) productCombo.getSelectedItem();
            String priceText = priceField.getText().trim();
            if (selected == null) {
                resultLabel.setForeground(DANGER);
                resultLabel.setText("No product selected. Click Refresh Products first.");
                return;
            }
            if (priceText.isEmpty()) {
                resultLabel.setForeground(DANGER);
                resultLabel.setText("Please enter a selling price.");
                return;
            }
            int qty = (Integer) quantitySpinner.getValue();
            if (qty > selected.getAvailableQuantity()) {
                resultLabel.setForeground(DANGER);
                resultLabel.setText("Insufficient stock. Only " + selected.getAvailableQuantity() + " unit(s) available.");
                return;
            }
            try {
                double price = Double.parseDouble(priceText);
                double profit = service.recordSale(new Sale(selected.getId(), qty, price));
                resultLabel.setForeground(profit >= 0 ? SUCCESS : DANGER);
                resultLabel.setText((profit >= 0 ? "Sale recorded. Profit: " : "Sale recorded. Loss: ")
                    + MONEY.format(Math.abs(profit)));
                priceField.setText("");
                refreshProductCombo();
                updateStockLabel(stockLabel);
                refreshDashboard();
            } catch (NumberFormatException nfe) {
                resultLabel.setForeground(DANGER);
                resultLabel.setText("Selling price must be a valid number.");
            } catch (Exception ex) {
                resultLabel.setForeground(DANGER);
                resultLabel.setText("Error: " + ex.getMessage());
            }
        });

        panel.add(formCard, BorderLayout.NORTH);
        return panel;
    }

    private void updateStockLabel(JLabel stockLabel) {
        Product selected = (Product) productCombo.getSelectedItem();
        if (selected == null) {
            stockLabel.setText(" ");
        } else {
            stockLabel.setText("Available in stock: " + selected.getAvailableQuantity() + " unit(s)");
            stockLabel.setForeground(selected.getAvailableQuantity() > 0 ? TEXT_MUTED : DANGER);
        }
    }

    private void refreshProductCombo() {
        try {
            Product selectedBefore = (Product) productCombo.getSelectedItem();
            int selectedIdBefore = selectedBefore != null ? selectedBefore.getId() : -1;
            productCombo.removeAllItems();
            for (Product p : service.getAllProducts()) {
                productCombo.addItem(p);
                if (p.getId() == selectedIdBefore) {
                    productCombo.setSelectedItem(p);
                }
            }
        } catch (Exception ignored) { }
    }

    /* ---------------- ADD EXPENSE ---------------- */
    private JPanel buildAddExpensePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel formCard = card();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(sectionHeading("Add New Expense"), gbc);
        gbc.gridwidth = 1;

        JTextField descField = styledField();
        JTextField amountField = styledField();
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gbc.gridy = 1; gbc.gridx = 0; formCard.add(formLabel("Description"), gbc);
        gbc.gridx = 1; formCard.add(descField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; formCard.add(formLabel("Amount"), gbc);
        gbc.gridx = 1; formCard.add(amountField, gbc);

        RoundedButton addBtn = new RoundedButton("Add Expense", PRIMARY, PRIMARY_HOVER);
        gbc.gridy = 3; gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formCard.add(addBtn, gbc);

        gbc.gridy = 4; gbc.gridx = 1;
        formCard.add(statusLabel, gbc);

        addBtn.addActionListener(e -> {
            String desc = descField.getText().trim();
            String amountText = amountField.getText().trim();
            if (desc.isEmpty() || amountText.isEmpty()) {
                statusLabel.setForeground(DANGER);
                statusLabel.setText("Please fill in all fields.");
                return;
            }
            try {
                double amount = Double.parseDouble(amountText);
                service.addExpense(new Expense(desc, amount));
                statusLabel.setForeground(SUCCESS);
                statusLabel.setText("Expense added successfully.");
                descField.setText("");
                amountField.setText("");
                refreshDashboard();
            } catch (NumberFormatException nfe) {
                statusLabel.setForeground(DANGER);
                statusLabel.setText("Amount must be a valid number.");
            } catch (Exception ex) {
                statusLabel.setForeground(DANGER);
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        panel.add(formCard, BorderLayout.NORTH);
        return panel;
    }

    /* ---------------- PROFIT REPORT SHEET ---------------- */
    private JPanel buildReportSheetPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(sectionHeading("Profit Report Sheet (Day by Day)"), BorderLayout.WEST);

        RoundedButton loadBtn = new RoundedButton("Load Report", PRIMARY, PRIMARY_HOVER);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(loadBtn);
        headerRow.add(btnWrap, BorderLayout.EAST);

        reportTableModel = new DefaultTableModel(new Object[]{"Date", "Sales", "Expense", "Profit"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(reportTableModel);

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        loadBtn.addActionListener(e -> {
            try {
                reportTableModel.setRowCount(0);
                List<Object[]> rows = service.profitReportSheet();
                for (Object[] r : rows) {
                    reportTableModel.addRow(new Object[]{
                        r[0], MONEY.format((double) r[1]), MONEY.format((double) r[2]), MONEY.format((double) r[3])
                    });
                }
                if (rows.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No sales records found.", "Profit Report Sheet", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    /* ---------------- PRODUCT SALES BY DATE ---------------- */
    private JPanel buildSalesByDatePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel headerCard = card();
        headerCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        headerCard.add(sectionHeading("Product Sales by Date"), gbc);
        gbc.gridwidth = 1;

        JTextField dateField = styledField();
        dateField.setText(java.time.LocalDate.now().toString());

        RoundedButton todayBtn = new RoundedButton("Today", new Color(100, 116, 139), new Color(71, 85, 105));
        RoundedButton loadBtn = new RoundedButton("Load", PRIMARY, PRIMARY_HOVER);

        gbc.gridy = 1; gbc.gridx = 0; headerCard.add(formLabel("Date (YYYY-MM-DD)"), gbc);
        gbc.gridx = 1; headerCard.add(dateField, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(todayBtn);
        btnRow.add(loadBtn);
        gbc.gridx = 2; headerCard.add(btnRow, gbc);

        todayBtn.addActionListener(e -> dateField.setText(java.time.LocalDate.now().toString()));

        salesByDateTableModel = new DefaultTableModel(new Object[]{"Product", "Quantity", "Profit"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(salesByDateTableModel);

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        loadBtn.addActionListener(e -> {
            String date = dateField.getText().trim();
            if (date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a date.", "Missing Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                salesByDateTableModel.setRowCount(0);
                List<Object[]> rows = service.productSalesByDate(date);
                for (Object[] r : rows) {
                    salesByDateTableModel.addRow(new Object[]{r[0], r[1], MONEY.format((double) r[2])});
                }
                if (rows.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No sales found for " + date + ".", "Product Sales by Date", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(headerCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    /* ---------------- SHARED UI HELPERS ---------------- */
    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 22, 20, 22)
        ));
        return p;
    }

    private JLabel sectionHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(TEXT_DARK);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        return label;
    }

    private JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 14));
        return label;
    }

    private JTextField styledField() {
        JTextField field = new JTextField(22);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                label.setOpaque(true);
                label.setBackground(PRIMARY);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 0, column == t.getColumnCount() - 1 ? 0 : 1, new Color(255, 255, 255, 40)),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)
                ));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                return label;
            }
        });

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                String text = value == null ? "" : value.toString();
                String colName = t.getColumnName(column);
                boolean isProfitColumn = colName.equalsIgnoreCase("Profit");
                if (isProfitColumn && text.contains("\u20B9")) {
                    c.setForeground(text.trim().startsWith("-") ? DANGER : SUCCESS);
                    ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 14));
                } else {
                    c.setForeground(TEXT_DARK);
                    ((JLabel) c).setFont(new Font("Segoe UI", Font.PLAIN, 14));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                return c;
            }
        });
        return table;
    }

    /* ---------------- MAIN ---------------- */
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("control", BG);
            UIManager.put("nimbusBase", PRIMARY);
            UIManager.put("nimbusBlueGrey", new Color(226, 232, 240));
            UIManager.put("nimbusLightBackground", Color.WHITE);
            UIManager.put("text", TEXT_DARK);
            UIManager.put("nimbusSelectionBackground", PRIMARY);
            UIManager.put("info", Color.WHITE);
            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) { }
        }

        SwingUtilities.invokeLater(() -> {
            VendorSystemGUI frame = new VendorSystemGUI();
            frame.setVisible(true);
        });
    }
}
