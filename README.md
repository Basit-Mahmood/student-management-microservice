🎓 School Microservices System

  A robust microservices architecture built with Spring Boot 4 and JDK 25, demonstrating service orchestration, automated fee payments, and email notifications.

📋 Prerequisites

  Ensure you have the following installed before starting:

  - JDK 25 (Latest version)
  - Spring Boot 4
  - Docker (Optional but highly recommended for containerized testing)
  - Gmail Account & App Password:
    - [!IMPORTANT] To send payment receipts, you must generate a Google App Password (not your standard login password) from your Google Account security settings.

📂 Project Structure

  The repository consists of four core microservices and a deployment utility:

  - student-service: Manages student records.
  - payment-service: Orchestrates fee transactions.
  - notification-service: Handles email dispatch.
  - gateway-service: The entry point (API Gateway) for all services.
  - app-deployment/: Contains the docker-compose.yaml for environment setup.

📥 Getting Started
  - Download & Import
    - Download or clone the repository to your local machine.
    - Open Eclipse IDE (Latest Version).
    - Go to File > Import > Maven > Existing Maven Projects.
    - Browse to the root directory where the four services are located.
    - Select all pom.xml files and click Finish.

  - Configure Email Settings
    - In Eclipse, expand the notification-service.
    - Navigate to src/main/resources/mail/app-local-mail.yml.
    - Update the following fields with your credentials:
      - username: your-email@gmail.com
      - password: your-google-app-password

🐳 Testing with Docker (Recommended)
  - Ensure Docker Desktop is running.
  - Open a terminal and navigate to:
    - cd app-deployment/docker
  - Build and start the system:
    - docker-compose up --build
  - Access the API Gateway:
    - Open http://localhost:9000/swagger-ui/index.html.
    - Select Student Service from the top-right dropdown.
    - Add a Student: Use POST /api/students. Provide a valid email to receive the receipt. Copy the studentId.
  - Verify Database:
    - Open the H2 Console: http://localhost:8082.
    - JDBC URL: jdbc:h2:mem:studentdb
    - User: sa | Password: password
    - Run SELECT * FROM STUDENT; to verify the record.
  - Process Payment:
    - In the Gateway Swagger, switch to Payment Service.
    - Use POST /api/payments with the studentId you copied earlier.
    - Check your email for the "Fees Payment Successful" receipt.

🚀 Testing with API Gateway (Local IDE)
  - Import projects into Eclipse as described above.
  - Start the Gateway: Right-click GatewayServiceApplication.java > Run As > Java Application.
  - Follow step 4 and onwards from the Docker section above.

