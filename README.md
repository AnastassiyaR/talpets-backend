# TalPets Backend

## Description
This is the backend part of the TalPets project — an online store for pet merchandise created by and for the TalTech community.  
It provides RESTful APIs for managing users, products, and orders, and connects to a PostgreSQL database.

## Table of Contents
- [Technologies](#technologies)
- [Installation](#installation)
- [How to Run](#how-to-run)
- [Build](#build)

## Technologies
**Backend:**
* Java 21
* Spring Boot 3
* Gradle
* PostgreSQL
* MapStruct
* Lombok
* Liquibase

## Installation
**Steps:**

1. **Clone the repository from Git.**  
   Open your terminal or command prompt.  
   If you are using an IDE, open the integrated terminal.

2. **Navigate to the directory** where you want to clone the project.

3. **Clone the repository** using the HTTPS link:
   git clone https://github.com/your-username/iti0302-2025-backend.git

4. **Navigate to backend directory** - _cd iti0302-2025-backend_.

5. **Set up the database** - Make sure you have PostgreSQL installed and running.
   Create a new database and update your credentials in application.properties or application.yml:
   ```bash
   spring.application.name=backend
   spring.datasource.url=jdbc:postgresql://localhost:5433/postgres
   spring.datasource.username=talpets
   spring.datasource.password=talpets
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.liquibase.change-log=classpath:/db/changelog/changelog-master.xml

6. **Build the project using Gradle** - _./gradlew build_.

7. **To start the application, run:** _./gradlew bootRun_. The backend will start at: _http://localhost:8080_.

