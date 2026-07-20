# 🚀 Smart Vendor Profit Analyzer

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java">
  <img src="https://img.shields.io/badge/MySQL-Database-blue?style=for-the-badge&logo=mysql">
  <img src="https://img.shields.io/badge/JDBC-Connectivity-success?style=for-the-badge">
  <img src="https://img.shields.io/badge/OOP-Java-red?style=for-the-badge">
  <img src="https://img.shields.io/badge/Status-Completed-brightgreen?style=for-the-badge">
</p>

---

## 📌 Project Overview

**Smart Vendor Profit Analyzer** is a desktop application developed using **Java, JDBC, MySQL, and Object-Oriented Programming (OOP)**. It is designed to help small shopkeepers and vendors efficiently manage their daily business activities.

The application allows vendors to manage products, record sales, track expenses, monitor inventory, and automatically calculate profit or loss without performing manual calculations.

This project was developed as a **Database Management System (DBMS)** academic project while focusing on solving a real-world business problem.

---

# 🎯 Problem Statement

Many small vendors still calculate their daily profit and expenses manually.

Manual calculations often lead to:

- Incorrect profit calculations
- Difficulty tracking expenses
- Poor inventory management
- No daily or monthly business reports
- Time-consuming record keeping

The **Smart Vendor Profit Analyzer** solves these problems by automating sales recording, expense tracking, inventory updates, and profit analysis.

---

# ✨ Key Features

### 📦 Product Management
- Add new products
- Store purchase price
- Manage available stock quantity

### 💰 Sales Management
- Record product sales
- Enter selling price
- Enter quantity sold
- Automatic stock deduction
- Automatic profit/loss calculation

### 💳 Expense Management
- Add daily business expenses
- Store expense description
- Maintain expense history

### 📊 Dynamic Profit Reports
Generate reports for:
- Today's Report
- Custom Date Report
- Last N Days Report

Each report includes:

- Total Sales
- Total Expenses
- Net Profit/Loss

### 📈 Product Performance Report

Displays:

- Product Name
- Quantity Sold
- Profit/Loss
- Sales Performance

### 🏆 Most Profitable Product

Automatically identifies:

- Highest earning product
- Total profit generated
- Product performance summary

### 📦 Inventory Tracking

- Available product quantity
- Automatic stock reduction after every sale
- Prevents selling unavailable stock

---

# 🛠 Technologies Used

| Technology | Purpose |
|------------|---------|
| Java | Application Development |
| Java Swing | Graphical User Interface |
| JDBC | Database Connectivity |
| MySQL | Database Management |
| SQL | Data Manipulation |
| OOP | Software Design |
| Git | Version Control |
| GitHub | Project Hosting |

---

# 🏗 Project Architecture

```
SmartVendorProfitAnalyzer
│
├── database
│      DBConnection.java
│
├── model
│      Product.java
│      Sale.java
│      Expense.java
│
├── service
│      VendorOperations.java
│      VendorService.java
│
├── gui
│      LoginFrame.java
│      Dashboard.java
│      AddProductFrame.java
│      ViewProductsFrame.java
│      RecordSaleFrame.java
│      AddExpenseFrame.java
│      ProfitReportFrame.java
│      ProductReportFrame.java
│      MostProfitableFrame.java
│
└── Main.java
```

---

# 🗄 Database Design

The project uses **three relational tables**.

## Products

| Column | Description |
|---------|-------------|
| product_id | Primary Key |
| product_name | Product Name |
| purchase_price | Purchase Price |
| quantity_available | Available Stock |

---

## Sales

| Column | Description |
|---------|-------------|
| sale_id | Primary Key |
| product_id | Foreign Key |
| selling_price | Selling Price |
| quantity | Quantity Sold |
| total_profit | Profit/Loss |
| sale_date | Sale Date |

---

## Expenses

| Column | Description |
|---------|-------------|
| expense_id | Primary Key |
| description | Expense Description |
| amount | Expense Amount |
| expense_date | Expense Date |

---

# 🔄 Workflow

```
Add Product
      │
      ▼
Product Stored in Database
      │
      ▼
Record Sale
      │
      ▼
Calculate Profit Automatically
      │
      ▼
Update Inventory
      │
      ▼
Save Sale Record
      │
      ▼
Generate Profit Report
```

---

# 📊 Business Logic

### Profit Formula

```
Profit = (Selling Price − Purchase Price) × Quantity
```

### Net Profit Formula

```
Net Profit = Total Sales Profit − Total Expenses
```

---

# 📸 Screenshots

## 🏠 Dashboard

<img width="1918" height="1006" alt="Dashboard" src="https://github.com/user-attachments/assets/94acaa07-0520-4bf3-a86e-4e1579dec2a7" />


## 📦 Add Product

<img width="1920" height="1011" alt="Add Product" src="https://github.com/user-attachments/assets/988abfe2-aa75-447f-b1b3-2dc394cfc4f7" />

## 💰 Record Sale

<img width="1920" height="1006" alt="Record Sale" src="https://github.com/user-attachments/assets/e8d3b2f3-c5f6-429f-8085-bdb0d2536cf7" />

## 💳 Add Expense

<img width="1920" height="1015" alt="Add Expense" src="https://github.com/user-attachments/assets/e71866ee-0147-4e6e-97b1-55805b8c0faf" />

## 📈 Profit Report Sheeet

<img width="1920" height="1032" alt="Profit Report Sheet" src="https://github.com/user-attachments/assets/9e08d58a-fb7a-4df8-bfbc-f1a2f1493861" />

## 🗄 Sales By Date

<img width="1920" height="992" alt="Sales By Date" src="https://github.com/user-attachments/assets/ba9fc7e0-5cd1-4cc2-8161-5936ffb2e3d9" />

## 📌 Entity Relationship Diagram (ERD)

<img width="1536" height="1024" alt="ER DIAGRAM" src="https://github.com/user-attachments/assets/9ebb11d1-c141-4d2c-8f7a-70e39999d394" />

---

# 🚀 How to Run

### 1️⃣ Clone Repository

```bash
git clone https://github.com/BharatKumar292/Smart-Vendor-Profit-Analyzer.git
```

---

### 2️⃣ Open Project

Open the project in:

- IntelliJ IDEA
- NetBeans
- Eclipse
- VS Code

---

### 3️⃣ Create Database

```sql
CREATE DATABASE vendordb;
```

---

### 4️⃣ Import SQL File

Import:

```
vendordb.sql
```

---

### 5️⃣ Update Database Credentials

Open:

```
DBConnection.java
```

Update:

```java
String url = "jdbc:mysql://localhost:3306/vendordb";
String username = "root";
String password = "Bharat@#23";
```

---

### 6️⃣ Run

Run:

```
Main.java
```

---

# 🎓 Learning Outcomes

This project helped me learn:

- Database Design
- SQL Queries
- CRUD Operations
- JDBC Connectivity
- Java Swing GUI
- Object-Oriented Programming
- Business Logic Implementation
- Inventory Management
- Report Generation
- Git & GitHub

---

# 🔮 Future Enhancements

- PDF Report Export
- Excel Report Export
- Barcode Scanner Integration
- User Authentication
- Multiple Shop Support
- Customer Management
- Supplier Management
- Sales Charts & Analytics
- Dark Mode
- Cloud Database Integration

---

# 📂 Project Structure

```
SmartVendorProfitAnalyzer/

src/
│
├── database/
├── model/
├── service/
├── gui/
│
database/
│
└── vendordb.sql

screenshots/

README.md

LICENSE
```

---

# 🤝 Contributing

Contributions, suggestions, and improvements are welcome.

Feel free to fork this repository and submit a pull request.

---

# 👨‍💻 Developer

**Bharat Kumar**

Computer Science Student

Java | JDBC | MySQL | OOP | DBMS

---

# ⭐ Support

If you found this project useful, consider giving it a ⭐ on GitHub.

It motivates me to build more useful open-source projects.

---

# 📄 License

This project is licensed under the **MIT License**.

---

<p align="center">

### ⭐ Thank you for visiting this repository! ⭐

Made with ❤️ using Java, JDBC & MySQL

</p>
