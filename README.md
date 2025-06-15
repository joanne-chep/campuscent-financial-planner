CampusCent: A Financial Management System for Students

Overview
CampusCent is a Java-based financial planning tool built to address the financial literacy gap among university students, particularly within the Ghanaian context. The system empowers students to develop disciplined financial habits by providing structured tools to track income, expenses, and savings goals while also introducing them to basic investment strategies.

Key Features
     User Management: Secure user registration and login system.

    Savings Goals: Set financial goals with target amounts and deadlines, and monitor progress.

    Spending Limits: Define daily expenditure ceilings and receive updates on spending behavior.

    Transaction Logging: Categorize and track income and expenses (e.g., food, education, emergency).

    Financial Insights: Visualize spending trends and savings progress to support decision-making.

    Investment Learning: Introduces users to treasury bills and basic financial instruments.

    Currency Integration: All transactions are managed in Ghanaian Cedi (GH₵).

    Data Persistence: Utilizes a lightweight SQLite database to store user data and transaction records locally.

Technologies Used

    Java (OOP, exception handling, lambda expressions, File I/O)

    SQLite (embedded relational database)

    Apache POI (optional future integration for Excel support)

    Git (version control)

System Architecture
The application follows a modular architecture with clear separation of concerns:

    User account logic

    Goal management

    Transaction handling

    Database access layer

These components are designed using standard Java practices, making use of abstraction, polymorphism, and encapsulation.

Setup and Usage

    Ensure JDK 8 or higher is installed.

    Include the SQLite JDBC driver in the project classpath.

    Compile the .java source files.

    Run the application using the Java runtime:
    java App

Future Enhancements

    Graphical User Interface (GUI) using JavaFX or Swing

    Android application integration

    Real-time investment data integration via APIs

    Encryption for sensitive user data

License
This project is intended for academic and educational use. Licensing terms may be defined in future versions.
