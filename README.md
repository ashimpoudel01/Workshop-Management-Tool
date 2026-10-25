# Motorcycle Workshop Management System

An offline desktop application for managing motorcycle and scooter workshop operations. It provides sales and job cards, customer records, service history, spare-parts inventory, supplier purchases, expenses, pricing, reports, invoices, backups, and role-based access.

## Main Features

### Login and User Roles

The application opens with a login screen.

| Role | Username | Password | Access |
| --- | --- | --- | --- |
| Administrator | `admin` | `admin123` | Full workshop management and financial information |
| Employee | `employee` | `employee123` | Sales, job cards, customers, and read-only inventory |

The current credentials are defined in the application code. Change this authentication approach before using the application in a production or multi-user environment.

### Administrator Dashboard

Administrators can view:

- Today's sales revenue
- Gross profit
- Today's expenses
- Today's net profit
- Today's purchases
- Inventory valuation
- Low-stock alerts
- Completed bills
- Monthly sales, expenses, purchases, net profit, and average daily sales
- Top-selling parts
- Top-performing services

Administrators also have access to all navigation sections:

- Sales and Job Cards
- Parts Purchases
- Parts and Inventory
- Workshop Expenses
- Owner Expenses
- Pricing and Labor
- Customer Directory
- Financial Reports
- System Settings

### Employee Dashboard

Employees see a separate restricted dashboard containing only:

- Today's revenue
- Completed bills
- Job-card and sales actions
- Inventory availability information

Employees do not see purchase prices, inventory valuation, cost of goods sold, gross profit, net profit, supplier purchases, expenses, pricing administration, financial reports, or system settings.

### Sales and Job Cards

Create invoices and job cards for motorcycle and scooter work.

- Generate invoice numbers automatically.
- Record customer and vehicle details.
- Select an existing customer or create a customer profile.
- Record vehicle type, brand, model, registration number, and notes.
- Add multiple workshop services and spare parts.
- Use configured service prices and part selling/workshop prices.
- Edit service and part prices on the invoice when required.
- Apply discounts.
- Record paid amount and calculate due amount.
- Select payment method: Cash, Bank, eSewa, Khalti, or Other.
- Calculate parts total, service fees, subtotal, total payable, COGS, and gross profit.
- Deduct sold parts from inventory automatically.
- Record inventory transactions for parts consumed by a job card.
- Save the invoice or save and open a printable invoice preview.
- Search invoices by invoice number, customer, phone, vehicle plate, or model.
- Filter sales by date and payment method.
- Export the sales table to CSV.
- View and print invoice details, including every part and service line item.

Employee inventory changes happen through saved job cards. Employees cannot manually adjust stock.

### Customer Directory

Manage customer profiles and motorcycle information.

- Add, edit, search, and deactivate customers.
- Store name, phone, address, vehicle plate, brand, model, and notes.
- Record a manual customer credit amount.
- View calculated invoice credit due.
- View exact customer service history using the selected customer's ID.
- Open a customer's specific invoice from service history.
- Review the parts and services used on each invoice.
- Export customer records to CSV.

The service history is filtered by exact customer ID, preventing another customer's invoices from appearing in the selected customer's history.

### Parts and Inventory

Administrators can:

- Add new spare parts.
- Edit part details.
- Deactivate parts.
- Adjust stock quantities.
- View stock movement history and audit records.
- Search by part name, part number, brand, or supplier.
- Filter by category, brand, low stock, and out-of-stock status.
- Configure purchase, retail, and workshop prices.
- Set minimum stock levels and units of measure.
- Export inventory to CSV.

Inventory records include:

- Part name and part number
- Category and brand
- Supplier
- Purchase price
- Selling price
- Workshop price
- Current quantity
- Minimum stock level
- Unit
- Stock status

Employees can view operational inventory information but cannot add, edit, delete, or manually adjust stock. Purchase price and inventory valuation are hidden from employees.

### Parts Purchases and Suppliers

Administrators can record supplier purchases that increase inventory stock.

- Create purchase invoices.
- Select or create suppliers.
- Add multiple purchased parts.
- Record quantity and purchase rate.
- Track payment status: PAID, UNPAID, or PARTIAL.
- Update product purchase price and stock quantity.
- Record inventory transactions for incoming stock.
- Record price history when purchase prices change.
- Search and filter purchases.
- View purchase details.
- Export purchase data to CSV.

Supplier records include name, phone, email, address, and notes.

### Pricing and Labor

Administrators can maintain the workshop price list.

#### Services

- Add, edit, and deactivate services.
- Set default service charges.
- Set estimated duration.
- Store service descriptions.
- Use active services when creating job cards.

#### Parts Pricing

- View product pricing.
- Update retail and workshop prices.
- Record the reason for a price change.
- View product price history.

### Workshop Expenses

Administrators can record and manage operating expenses.

- Add and edit expenses.
- Store date, category, description, amount, payment method, and notes.
- Use categories such as utilities, rent, salaries, supplies, repairs, transport, and other expenses.
- Search and filter expenses by date and category.
- Export expense records to CSV.

### Owner Expenses

Administrators have a separate **Owner Expenses** menu for tracking personal or owner spending without mixing it into workshop operating expenses.

- Add, edit, and delete owner expense records.
- Store date, category, description, amount, payment method, and notes.
- Filter by description, category, date range, today, or all time.
- Track categories such as Personal, Family, Education, Health, Travel, Loan, Investment, and Other.
- View the total for the filtered owner-expense period.
- Export owner expenses to CSV.

Owner expenses are stored in their own `owner_expenses` table and are not included in workshop operating-expense totals or profit reports.

### Financial and Operational Reports

Administrators can generate reports for:

- Today
- Yesterday
- This week
- This month
- All time
- Custom date ranges

Reports include:

- Total sales revenue
- Parts revenue
- Service labor revenue
- Discounts
- Cost of goods sold
- Gross profit
- Operating expenses
- Net workshop profit
- Stock purchases
- Invoice count
- Sales invoice list
- Purchase list
- Expense list
- Top-selling parts
- Top-performing services
- Inventory list

Report tabs can be exported to CSV.

### Invoices

Invoice previews contain:

- Workshop profile and contact details
- Invoice number and date
- Customer and vehicle information
- Each part and service used
- Item type, quantity, unit rate, and amount
- Parts total
- Service charges
- Discount
- Subtotal
- Total payable
- Paid amount
- Due amount
- Payment method

Invoices can be printed from the preview dialog.

### Database Backup and Restore

Administrators can back up and restore the SQLite database from System Settings.

- Create a `.db` backup file.
- Restore a selected database backup.
- Create an automatic safety backup before restoring.
- Reinitialize the database schema after a restore.
- Use SQLite WAL mode and foreign-key enforcement.

## Date Handling

The application stores dates internally in Gregorian `yyyy-MM-dd` format and displays user-facing dates in Bikram Sambat format.

- New invoices and expenses accept Bikram Sambat dates.
- Date filters support Bikram Sambat input.
- The default month filter begins on day `01` of the current Bikram Sambat month.
- Reports convert the entered date range to the internal storage format.

## Data Storage

The application is local and does not require an internet connection or server.

Default database location:

```text
~/.motorworkshop/workshop.db
```

The database contains tables for:

- Settings
- Categories
- Suppliers
- Products
- Services
- Customers
- Purchases and purchase items
- Sales and sale items
- Expenses
- Price history
- Inventory transactions

The application creates and updates the schema during startup and seeds default services, categories, suppliers, products, settings, and sample customers when the relevant tables are empty.

## Requirements

- Java 25
- Maven 3.9 or newer recommended
- A graphical desktop session for the Swing interface

The project is configured with:

- Java Swing
- FlatLaf
- SQLite JDBC
- Maven Shade Plugin
- SLF4J Simple

## Build

From the project root:

```bash
mvn clean package
```

The executable shaded JAR is created at:

```text
target/MotorcycleWorkshop-1.0.0.jar
```

If the system default Java is older than Java 25, set `JAVA_HOME` to a Java 25 installation before building:

```bash
export JAVA_HOME=/path/to/jdk-25
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean package
```

## Run

Run the shaded JAR with Java 25:

```bash
java --enable-native-access=ALL-UNNAMED -jar target/MotorcycleWorkshop-1.0.0.jar
```

Or run the main class from the IDE using:

```text
com.motorworkshop.Main
```

The application initializes the database, displays the login dialog, and then opens the dashboard for the authenticated role.

## Project Structure

```text
src/main/java/com/motorworkshop/
├── Main.java                  Application entry point
├── dao/                       SQLite data access objects
├── database/                  Schema initialization and default data
├── model/                     Domain models and report objects
├── service/                   Business logic services
├── ui/                        Main window and application panels
│   └── dialogs/               Invoice, customer, sales, purchase, and form dialogs
└── util/                      Dates, formatting, invoices, exports, and UI helpers
```

## Important Operational Rules

- A saved job card is the normal employee inventory deduction event.
- Manual stock adjustment is restricted to administrators.
- Employee-facing screens do not expose purchase price or profit information.
- Customer service history is restricted to the selected customer's exact records.
- Invoice service fees are calculated from saved service line items.
- Existing invoice summaries are repaired from their stored line items during database initialization.
- Back up `~/.motorworkshop/workshop.db` regularly.

## Troubleshooting

### Java release error

If Maven reports that release 25 is unsupported, Maven is using an older JDK. Configure `JAVA_HOME` to point to Java 25 and run the build again.

### Database startup error

Close any running copy of the application and verify that the user has write permission for `~/.motorworkshop`. For a fresh installation, rename the existing database and start the application again:

```bash
mv ~/.motorworkshop/workshop.db ~/.motorworkshop/workshop.db.backup
```

Only do this after creating a backup if the existing data is important.

### Login

Use one of the built-in accounts listed in the Login and User Roles section. These credentials are intended for the current local application implementation and should be replaced with secure, stored user accounts before production use.
