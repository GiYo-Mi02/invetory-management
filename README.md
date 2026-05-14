# CCIS SC Inventory Management System

JavaFX inventory system (Eclipse Java Project, no Maven) following MVC.

## Requirements
- JDK 25
- MySQL 8+
- JavaFX jars (already in lib)
- JDBC + HikariCP + BCrypt + SLF4J jars (already in lib)

## Project Setup (Eclipse)
1. Import the project as an existing Java project.
2. Add all jars inside lib to the Java Build Path.
3. Mark resources as a source folder so FXML/CSS/config load correctly.
4. Ensure src is on the build path.

## Database Setup
1. Create the database and tables using:
   - resources/com/ccissc/inventory/db/schema.sql
2. Update credentials in:
   - resources/com/ccissc/inventory/config.properties

Default credentials:
- Username: admin
- Password: admin123

## Run
- Run Main.java.
- Login screen will appear.

## Notes
- Update the logo at resources/com/ccissc/inventory/images/ccis-sc-logo.png when available.
- Make sure the JavaFX jars are included on the classpath and in VM arguments if needed.
