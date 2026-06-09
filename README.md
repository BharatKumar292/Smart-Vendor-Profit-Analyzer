# Smart Vendor Profit Analyzer

A business analytics system developed using Java, JDBC, and MySQL that helps small vendors manage products, record sales, track expenses, and automatically calculate profit or loss.

---

# 📖 Project Overview

Smart Vendor Profit Analyzer is a Database Management System (DBMS) project designed for small vendors and shopkeepers. The system automates sales recording, expense management, and profit analysis, helping business owners make informed decisions based on real-time data.

---

# 🎯 Objectives

* Automate sales and expense management
* Reduce manual calculation errors
* Track product profitability
* Generate business reports dynamically
* Improve business decision-making
* Apply OOP concepts with JDBC and MySQL

---

# ✨ Features

## Product Management

* Add new products
* Store purchase prices
* Maintain product records

## Sales Management

* Record product sales
* Store selling price and quantity
* Automatically calculate profit or loss

## Expense Management

* Record daily business expenses
* Store expense descriptions and amounts

## Dynamic Reporting

* Today's Report
* Custom Date Report
* Last N Days Report
* Product Sales Report

## Profit Analysis

* Automatic profit calculation
* Net profit calculation after expenses
* Business performance monitoring

---

# 🛠 Technologies Used

* Java
* JDBC
* MySQL
* SQL
* MySQL Workbench
* Object-Oriented Programming (OOP)

---

# 🗄 Database Tables

## Products Table

| Column         | Description    |
| -------------- | -------------- |
| product_id     | Primary Key    |
| product_name   | Product Name   |
| purchase_price | Purchase Price |

## Sales Table

| Column        | Description            |
| ------------- | ---------------------- |
| sale_id       | Primary Key            |
| product_id    | Foreign Key            |
| selling_price | Selling Price          |
| quantity      | Quantity Sold          |
| total_profit  | Calculated Profit/Loss |
| sale_date     | Sale Date              |

## Expenses Table

| Column       | Description         |
| ------------ | ------------------- |
| expense_id   | Primary Key         |
| description  | Expense Description |
| amount       | Expense Amount      |
| expense_date | Expense Date        |

---

# 📊 Profit Calculation

```text
Profit = (Selling Price - Purchase Price) × Quantity
```

### Example

```text
Purchase Price = 200
Selling Price = 250
Quantity = 2

Profit = (250 - 200) × 2
Profit = 100
```

---

# 📋 Application Menu

```text
1. Add Product
2. Record Sale
3. Add Expense
4. View Profit Report
5. View Product Sales Report
6. Exit
```

---

# 🏗 OOP Concepts Used

## Classes

* DBConnection
* Product
* Sale
* Expense
* VendorService

## Interface

* VendorOperations

## Encapsulation

Private attributes with getters.

## Abstraction

Business operations are defined in an interface and implemented in VendorService.

---

# 📁 Project Structure

```text
Smart-Vendor-Profit-Analyzer
│
├── src
│   ├── DBConnection.java
│   ├── Product.java
│   ├── Sale.java
│   ├── Expense.java
│   ├── VendorOperations.java
│   ├── VendorService.java
│   └── Final_Vendor.java
│
├── database
│   └── vendordb.sql
│
├── diagrams
│   ├── ERD.png
│   └── SystemDiagram.png
│
├── presentation
│   └── Smart_Vendor_Profit_Analyzer_Presentation.pptx
│
└── README.md
```

---

# 🚀 How to Run

## Step 1

Create the database:

```sql
CREATE DATABASE vendordb;
```

## Step 2

Run the SQL script to create all tables.

## Step 3

Update database credentials in:

```java
DBConnection.java
```

## Step 4

Add MySQL JDBC Driver.

## Step 5

Run:

```java
Final_Vendor.java
```

---

# 📈 Future Enhancements

* GUI Interface
* User Authentication
* PDF Reports
* Dashboard Analytics
* Excel Export
* Inventory Tracking

---

# 🎓 Learning Outcomes

* Database Design
* SQL Queries
* CRUD Operations
* JDBC Connectivity
* OOP Principles
* Report Generation
* Business Analytics

---

# 👨‍💻 Author

**Bharat Kumar**

Database Management System Project

Smart Vendor Profit Analyzer

---

# 📄 License

This project is created for educational and academic purposes only.
