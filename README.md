# 🏥 Smart Healthcare Appointment System

<div align="center">

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?style=for-the-badge&logo=mongodb)
![JWT](https://img.shields.io/badge/JWT-Auth-red?style=for-the-badge&logo=jsonwebtokens)

A full-featured **Spring Boot 4** healthcare system for managing patients, doctors, appointments, prescriptions, and medical records — with **JWT authentication**, **role-based authorization**, **dual database architecture** (MySQL + MongoDB), **AOP logging**, **Hibernate caching**, and comprehensive **unit testing**.

</div>

---

## 📑 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Tech Stack](#-tech-stack)
- [Design Patterns & Strategies](#-design-patterns--strategies)
- [Spring Concepts Used](#-spring-concepts-used)
- [Project Structure](#-project-structure)
- [Database Design](#-database-design)
- [API Endpoints](#-api-endpoints)
- [Authorization Matrix](#-authorization-matrix)
- [Security Architecture](#-security-architecture)
- [Caching Strategy](#-caching-strategy)
- [AOP Logging](#-aop-logging)
- [Testing Strategy](#-testing-strategy)
- [Setup & Installation](#-setup--installation)
- [Running the Application](#-running-the-application)
- [Postman Collection](#-postman-collection)
- [Screenshots](#-screenshots)

---

## 🏗 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT (Postman)                      │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP Requests (JSON)
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐  │
│  │ JWT Filter   │→ │ SecurityConfig│→ │ Role-Based Access │  │
│  │ (extracts    │  │ (URL rules)  │  │ ADMIN / DOCTOR /  │  │
│  │  token)      │  │              │  │ PATIENT           │  │
│  └──────────────┘  └──────────────┘  └───────────────────┘  │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                     AOP LAYER (Logging)                      │
│  @LogAppointment → logs booking/cancellation/completion      │
│  @LogPrescription → logs prescription create/update          │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER (REST)                     │
│  AuthController │ DoctorController │ AppointmentController   │
│  AdminController│ PatientController│ PrescriptionController   │
│                 │ AvailabilityCtrl │ MedicalRecordController  │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER (Business Logic)             │
│  AuthService    │ DoctorService    │ AppointmentService       │
│  AdminService   │ PatientService   │ PrescriptionService      │
│                 │ AvailabilityServ │ MedicalRecordService      │
└────────┬────────────────────────────────────┬───────────────┘
         │                                    │
         ▼                                    ▼
┌────────────────────────┐    ┌───────────────────────────────┐
│     MySQL (JPA)        │    │       MongoDB (NoSQL)          │
│  ┌───────��──────────┐  │    │  ┌─────────────────────────┐  │
│  │ User             │  │    │  │ Prescription (document) │  │
│  │ Doctor           │  │    │  │ MedicalRecord (document)│  │
│  │ Patient          │  │    │  └─────────────────────────┘  │
│  │ Appointment      │  │    └───────────────────────────────┘
│  │ DoctorAvailability│ │
│  └──────────────────┘  │
│                        │
│  ┌──────────────────┐  │
│  │ Ehcache (L2)     │  │
│  │ Doctor queries   │  │
│  └──────────────────┘  │
└────────────────────────┘
```

---

## 🛠 Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Language** | Java 25 | Latest JDK with modern features |
| **Framework** | Spring Boot 4.0.2 | Application framework |
| **Web** | Spring Web (Tomcat) | REST API + embedded server |
| **Security** | Spring Security + JWT | Authentication & authorization |
| **ORM** | Spring Data JPA + Hibernate | MySQL entity management |
| **NoSQL** | Spring Data MongoDB | Prescription & medical records |
| **Validation** | Jakarta Bean Validation | DTO input validation |
| **AOP** | Spring AOP + AspectJ | Cross-cutting logging concerns |
| **Caching** | Ehcache + Hibernate L2 + Spring Cache | Performance optimization |
| **Testing** | JUnit 5 + Mockito | Unit testing with mocks |
| **API Testing** | Postman | Integration/E2E testing |
| **Build** | Maven | Dependency management |
| **Utilities** | Lombok | Boilerplate code generation |

---

## 🧠 Design Patterns & Strategies

### 1. **Layered Architecture (N-Tier)**
```
Controller → Service → Repository → Database
```
Each layer has a single responsibility. Controllers handle HTTP, services handle business logic, repositories handle data access. No layer skips another.

### 2. **Repository Pattern**
```java
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialtyContainingIgnoreCase(String specialty);
}
```
Abstracts database access behind interfaces. Spring Data JPA auto-generates implementations at runtime.

### 3. **DTO Pattern (Data Transfer Object)**
```
Client ←→ Controller ←→ DTO ←→ Service ←→ Entity ←→ Database
```
- **Request DTOs**: validate and accept client input (`DoctorRequest`)
- **Response DTOs**: shape what the client sees (`DoctorResponse`)
- **Entities are never exposed** to the client directly

### 4. **Mapper Pattern**
```java
public class DoctorMapper {
    public DoctorResponse toResponse(Doctor doctor) { ... }
}
```
Dedicated classes convert between entities and DTOs. Keeps conversion logic out of services.

### 5. **Builder Pattern (via Lombok)**
```java
Doctor.builder()
    .name("Dr. Smith")
    .specialty("Cardiology")
    .build();
```
Lombok's `@Builder` generates fluent builder classes for clean object construction.

### 6. **Singleton Pattern (Spring Beans)**
All Spring beans (`@Service`, `@Repository`, `@Controller`) are singletons by default — one instance shared across the entire application.

### 7. **Proxy Pattern (AOP)**
```java
@LogAppointment
public AppointmentResponse bookAppointment(...) { }
```
Spring AOP creates proxy objects around annotated methods to inject logging behavior without modifying business logic.

### 8. **Filter Chain Pattern (Security)**
```
Request → JwtFilter → SecurityFilterChain → Controller
```
Each filter processes the request and passes it to the next. JWT extraction happens before authorization checks.

### 9. **Strategy Pattern (Cache Providers)**
Hibernate's caching uses JCache (JSR-107) as an abstraction. Ehcache is the concrete strategy — swappable without code changes.

### 10. **Template Method Pattern (Spring Data)**
`JpaRepository` and `MongoRepository` provide template methods (`save`, `findById`, `deleteAll`) — you override by defining query method signatures.

---

## 🌱 Spring Concepts Used

| Concept | Where Used | Explanation |
|---------|-----------|-------------|
| **IoC (Inversion of Control)** | Entire application | Spring manages object creation — you never use `new Service()` |
| **DI (Dependency Injection)** | `@RequiredArgsConstructor` | Spring injects dependencies via constructor automatically |
| **Bean Lifecycle** | `DataSeeder` | `@PostConstruct` / `CommandLineRunner` seeds admin on startup |
| **Bean Scopes** | All `@Service`, `@Repository` | Default singleton scope — one instance per bean |
| **AOP** | `LoggingAspect.java` | Cross-cutting logging via `@Around` advice |
| **Spring Security** | `SecurityConfig` | URL-based role authorization + JWT filter |
| **Spring Data JPA** | SQL repositories | Auto-generated CRUD for MySQL entities |
| **Spring Data MongoDB** | NoSQL repositories | Auto-generated CRUD for MongoDB documents |
| **Spring Cache** | `@Cacheable`, `@CacheEvict` | Method-level caching on DoctorService |
| **Hibernate L2 Cache** | `ehcache.xml` | Entity-level caching for Doctor queries |
| **Validation** | `@Valid` on controllers | Automatic DTO validation before processing |
| **Exception Handling** | `@RestControllerAdvice` | Global error handling with consistent JSON responses |
| **Profiles** | `application.yml` | Environment-specific configuration |

---

## 📁 Project Structure

```
Smart-Healthcare-Appointment-System/
│
├── 📄 pom.xml
├── 📄 README.md
├── 📄 Smart-Healthcare-Postman-Collection.json
│
├── 📂 src/main/java/org/example/healthcare/
│   │
│   ├── 📄 SmartHealthcareApplication.java
│   │
│   ├── 📂 config/
│   │   ├── 📄 SecurityConfig.java
│   │   ├── 📄 CacheConfig.java
│   │   └── 📄 DataSeeder.java
│   │
│   ├── 📂 security/
│   │   ├── 📄 JwtAuthenticationFilter.java
│   │   ├── 📄 JwtService.java
│   │   └── 📄 CustomUserDetailsService.java
│   │
│   ├── 📂 controller/
│   │   ├── 📄 AuthController.java
│   │   ├── 📄 AdminController.java
│   │   ├── 📄 DoctorController.java
│   │   ├── 📄 PatientController.java
│   │   ├── 📄 AppointmentController.java
│   │   ├── 📄 DoctorAvailabilityController.java
│   │   ├── 📄 PrescriptionController.java
│   │   └── 📄 MedicalRecordController.java
│   │
│   ├── 📂 service/
│   │   ├── 📄 AuthService.java
│   │   ├── 📄 AdminService.java
│   │   ├── 📄 DoctorService.java
│   │   ├── 📄 PatientService.java
│   │   ├── 📄 AppointmentService.java
│   │   ├── 📄 DoctorAvailabilityService.java
│   │   ├── 📄 PrescriptionService.java
│   │   └── 📄 MedicalRecordService.java
│   │
│   ├── 📂 repository/
│   │   ├── 📂 sql/
│   │   │   ├── 📄 UserRepository.java
│   │   │   ├── 📄 DoctorRepository.java
│   │   │   ├── 📄 PatientRepository.java
│   │   │   ├── 📄 AppointmentRepository.java
│   │   │   └── 📄 DoctorAvailabilityRepository.java
│   │   └── 📂 nosql/
│   │       ├── 📄 PrescriptionRepository.java
│   │       └── 📄 MedicalRecordRepository.java
│   │
│   ├── 📂 models/
│   │   ├── 📂 sql/
│   │   │   ├── 📄 User.java
│   │   │   ├── 📄 Doctor.java
│   │   │   ├── 📄 Patient.java
│   │   │   ├── 📄 Appointment.java
│   │   │   └── 📄 DoctorAvailability.java
│   │   ├── 📂 nosql/
│   │   │   ├── 📄 Prescription.java
│   │   │   └── 📄 MedicalRecord.java
│   │   └── 📂 enums/
│   │       ├── 📄 Role.java
│   │       └── 📄 AppointmentStatus.java
│   │
│   ├── 📂 dto/
│   │   ├── 📂 request/
│   │   │   ├── 📄 LoginRequest.java
│   │   │   ├── 📄 RegisterRequest.java
│   │   │   ├── 📄 DoctorRequest.java
│   │   │   ├── 📄 PatientRequest.java
│   │   │   ├── 📄 AppointmentRequest.java
│   │   │   ├── 📄 PrescriptionRequest.java
│   │   │   └── 📄 MedicalRecordRequest.java
│   │   └── 📂 response/
│   │       ├── 📄 AuthResponse.java
│   │       ├── 📄 DoctorResponse.java
│   │       ├── 📄 PatientResponse.java
│   │       ├── 📄 AppointmentResponse.java
│   │       ├── 📄 AvailableSlotResponse.java
│   │       ├── 📄 PrescriptionResponse.java
│   │       └── 📄 MedicalRecordResponse.java
│   │
│   ├── 📂 mapper/
│   │   ├── 📄 DoctorMapper.java
│   │   ├── 📄 PatientMapper.java
│   │   └── 📄 AppointmentMapper.java
│   │
│   ├── 📂 aop/
│   │   ├── 📄 LoggingAspect.java
│   │   └── 📂 annotation/
│   │       ├── 📄 LogAppointment.java
│   │       └── 📄 LogPrescription.java
│   │
│   └── 📂 exception/
│       ├── 📄 GlobalExceptionHandler.java
│       ├── 📄 ResourceNotFoundException.java
│       └── 📄 DoubleBookingException.java
│
├── 📂 src/main/resources/
│   ├── 📄 application.yml
│   └── 📄 ehcache.xml
│
└── 📂 src/test/java/org/example/healthcare/
    ├── 📄 SmartHealthcareAppointmentSystemApplicationTests.java
    ├── 📂 helpers/
    │   ├── 📄 TestDataHelper.java
    │   ├── 📄 DoctorServiceTestHelper.java
    │   ├── 📄 PatientServiceTestHelper.java
    │   └── 📄 AppointmentServiceTestHelper.java
    └── 📂 service/
        ├── 📄 DoctorServiceTest.java
        ├── 📄 PatientServiceTest.java
        └── 📄 AppointmentServiceTest.java
```

---

## 🗄 Database Design

### MySQL (Relational — JPA/Hibernate)

```
┌─────���────────────┐       ┌──────────────────┐
│      User        │       │     Doctor       │
├──────────────────┤       ├──────────────────┤
│ id (PK)          │──┐    │ id (PK)          │
│ username (unique) │  │    │ name             │
│ email (unique)   │  ├───→│ specialty        │
│ password (hash)  │  │    │ user_id (FK→User)│
│ role (enum)      │  │    │ created_at       │
│ enabled          │  │    │ updated_at       │
└──────────────────┘  │    └──────┬───────────┘
                      │           │
                      │    ┌──────┴───────────────┐
                      │    │ DoctorAvailability    │
                      │    ├──────────────────────-┤
                      │    │ id (PK)               │
                      │    │ doctor_id (FK→Doctor)  │
                      │    │ day_of_week (enum)     │
                      │    │ start_time             │
                      │    │ end_time               │
                      │    │ slot_duration_minutes   │
                      │    └────────────────────────┘
                      │
┌──────────────────┐  │    ┌──────────────────────┐
│     Patient      │  │    │    Appointment        │
├──────────────────┤  │    ├──────────────────────-┤
│ id (PK)          │←─┤    │ id (PK)               │
│ name             │  │    │ patient_id (FK→Patient)│
│ date_of_birth    │  ├───→│ doctor_id (FK→Doctor)  │
│ phone            │       │ appointment_date       │
│ address          │       │ start_time             │
│ user_id (FK→User)│       │ end_time               │
│ created_at       │       │ status (enum)          │
│ updated_at       │       │ reason                 │
└──────────────────┘       │ notes                  │
                           └────────────────────────┘
```

### MongoDB (NoSQL — Documents)

```json
// Prescription Document
{
    "_id": "ObjectId",
    "appointmentId": 1,
    "patientId": 1,
    "patientName": "John Doe",
    "doctorId": 1,
    "doctorName": "Dr. Smith",
    "medicines": ["Aspirin 100mg", "Vitamin D"],
    "diagnosis": "Mild chest pain",
    "instructions": "Take after meals for 2 weeks",
    "createdAt": "2026-02-18T10:30:00",
    "updatedAt": "2026-02-18T10:30:00"
}

// MedicalRecord Document
{
    "_id": "ObjectId",
    "patientId": 1,
    "patientName": "John Doe",
    "recordDate": "2026-02-18",
    "title": "Cardiology Consultation",
    "description": "Patient presented with chest pain. ECG normal.",
    "prescriptionIds": ["ObjectId"],
    "labReports": ["ECG Report - Normal", "Blood Test - Normal"],
    "createdAt": "2026-02-18T11:00:00"
}
```

### Why Dual Database?

| Data | Database | Reason |
|------|----------|--------|
| Users, Doctors, Patients, Appointments | **MySQL** | Structured, relational, FK constraints, ACID transactions |
| Prescriptions, Medical Records | **MongoDB** | Flexible schema, nested arrays, varying fields per record |

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/auth/login` | Login with username/password, returns JWT | Public |
| `POST` | `/api/auth/register/admin` | Register new admin | Admin |
| `POST` | `/api/auth/register/doctor` | Register new doctor | Admin |
| `POST` | `/api/auth/register/patient` | Register new patient | Admin |

### Admin

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `DELETE` | `/api/admin/reset` | Reset database (keeps admin) | Admin |

### Doctors

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/doctors` | List all doctors | All authenticated |
| `GET` | `/api/doctors/{id}` | Get doctor by ID | All authenticated |
| `GET` | `/api/doctors/specialty/{specialty}` | Search by specialty | All authenticated |
| `PUT` | `/api/doctors/{id}` | Update doctor | Admin |
| `DELETE` | `/api/doctors/{id}` | Delete doctor | Admin |

### Patients

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/patients` | List all patients | Admin, Doctor |
| `GET` | `/api/patients/{id}` | Get patient by ID | Admin, Doctor |
| `PUT` | `/api/patients/{id}` | Update patient | Admin, Patient |
| `DELETE` | `/api/patients/{id}` | Delete patient | Admin |

### Doctor Availability

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/availability/doctor/{doctorId}` | Set weekly availability | Doctor |
| `GET` | `/api/availability/doctor/{doctorId}` | Get doctor's schedule | All authenticated |
| `DELETE` | `/api/availability/{id}` | Remove availability slot | Doctor |

### Appointments

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/appointments/patient/{patientId}` | Book appointment | Patient |
| `GET` | `/api/appointments/patient/{patientId}` | Get patient appointments | All authenticated |
| `GET` | `/api/appointments/doctor/{doctorId}` | Get doctor appointments | All authenticated |
| `GET` | `/api/appointments/available-slots` | Get available time slots | All authenticated |
| `PATCH` | `/api/appointments/{id}/complete` | Mark as completed | Doctor |
| `PATCH` | `/api/appointments/{id}/cancel` | Cancel appointment | Patient |

### Prescriptions (MongoDB)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/prescriptions` | Create prescription | Doctor |
| `GET` | `/api/prescriptions/{id}` | Get by ID | Doctor, Patient |
| `GET` | `/api/prescriptions/patient/{patientId}` | Get patient's prescriptions | Doctor, Patient |
| `PUT` | `/api/prescriptions/{id}` | Update prescription | Doctor |

### Medical Records (MongoDB)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/medical-records` | Create record | Doctor |
| `GET` | `/api/medical-records/{id}` | Get by ID | Doctor, Patient |
| `GET` | `/api/medical-records/patient/{patientId}` | Get patient's records | Doctor, Patient |
| `PUT` | `/api/medical-records/{id}` | Update record | Doctor |
| `DELETE` | `/api/medical-records/{id}` | Delete record | Doctor |

---

## 🔒 Authorization Matrix

| Endpoint | Method | 🔴 ADMIN | 🔵 DOCTOR | 🟢 PATIENT | 🔓 No Token |
|----------|--------|----------|-----------|------------|-------------|
| `/api/auth/login` | POST | ✅ | ✅ | ✅ | ✅ |
| `/api/auth/register/**` | POST | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/admin/reset` | DELETE | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/doctors` | GET | ✅ | ✅ | ✅ | ❌ 401 |
| `/api/doctors/{id}` | PUT | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/doctors/{id}` | DELETE | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/patients` | GET | ✅ | ✅ | ❌ 403 | ❌ 401 |
| `/api/patients/{id}` | PUT | ✅ | ❌ 403 | ✅ | ❌ 401 |
| `/api/patients/{id}` | DELETE | ✅ | ❌ 403 | ❌ 403 | ❌ 401 |
| `/api/availability/**` | POST | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |
| `/api/availability/**` | GET | ✅ | ✅ | ✅ | ❌ 401 |
| `/api/appointments/**/book` | POST | ❌ 403 | ❌ 403 | ✅ | ❌ 401 |
| `/api/appointments/**/complete` | PATCH | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |
| `/api/appointments/**/cancel` | PATCH | ❌ 403 | ❌ 403 | ✅ | ❌ 401 |
| `/api/appointments/**` | GET | ✅ | ✅ | ✅ | ❌ 401 |
| `/api/prescriptions` | POST | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |
| `/api/prescriptions/**` | GET | ❌ 403 | ✅ | ✅ | ❌ 401 |
| `/api/medical-records` | POST | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |
| `/api/medical-records/**` | GET | ❌ 403 | ✅ | ✅ | ❌ 401 |
| `/api/medical-records/{id}` | DELETE | ❌ 403 | ✅ | ❌ 403 | ❌ 401 |

---

## 🔐 Security Architecture

### Authentication Flow

```
1. Client sends POST /api/auth/login { username, password }
                    │
2. AuthService validates credentials against MySQL
                    │
3. JwtService generates JWT token (HS256, 24h expiry)
                    │
4. Client receives { token: "eyJhb...", role: "PATIENT" }
                    │
5. Client includes token in all subsequent requests:
   Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
                    │
6. JwtAuthenticationFilter extracts & validates token
                    │
7. SecurityConfig checks role against endpoint rules
                    │
8. Request proceeds or is rejected (401/403)
```

### Security Implementation

```
┌─────────────────────────────────────────────────────┐
│                  SecurityConfig                      │
│                                                     │
│  No Token?                                          │
│  └→ authenticationEntryPoint → 401 + JSON body      │
│                                                     │
│  Valid Token, Wrong Role?                           │
│  └→ accessDeniedHandler → 403 + JSON body           │
│                                                     │
│  Valid Token, Correct Role?                         │
│  └→ Request proceeds to controller                  │
└─────────────────────────────────────────────────────┘
```

### Password Security

- Passwords are hashed using **BCrypt** (never stored in plain text)
- BCrypt includes a salt automatically — same password produces different hashes
- `PasswordEncoder` bean handles encoding and verification

---

## ⚡ Caching Strategy

### Three Levels of Caching

```
┌────────────────────────────────────────────────────────┐
│  Level 1: Hibernate First-Level Cache (Session)        │
│  ├── Automatic, per-transaction                        │
│  ├── Same entity queried twice in one request → 1 SQL  │
│  └── No configuration needed                           │
├────────────────────────────────────────────────────────┤
│  Level 2: Hibernate Second-Level Cache (Ehcache)       │
│  ├── @Cache on Doctor entity                           │
│  ├── Shared across all sessions/transactions           │
│  ├── Configured in ehcache.xml (TTL, heap size)        │
│  └── Same doctor queried by different users → 0 SQL    │
├────────────────────────────────────────────────────────┤
│  Level 3: Spring Cache (@Cacheable)                    │
│  ├── Method-level caching on DoctorService             │
│  ├── getAllDoctors() → cached, skips service logic      │
│  ├── @CacheEvict on update/delete → refreshes cache    │
│  └── Uses Ehcache as provider                          │
└────────────────────────────────────────────────────────┘
```

### Cache Annotations Used

```java
@Cacheable("allDoctors")
public List<DoctorResponse> getAllDoctors() { ... }

@CacheEvict(value = "allDoctors")
public DoctorResponse updateDoctor(Long id, ...) { ... }
```

### Ehcache Configuration

| Cache Region | TTL | Heap Size | Purpose |
|-------------|-----|-----------|---------|
| `allDoctors` | 15 min | 1 entry | List of all doctors |
| `doctorById` | 30 min | 200 entries | Individual doctor lookups |
| `doctorsBySpecialty` | 30 min | 50 entries | Specialty search results |
| `default-update-timestamps-region` | ∞ | 1000 entries | Hibernate query cache timestamps |
| `default-query-results-region` | 30 min | 100 entries | Hibernate query results |

---

## 📋 AOP Logging

### How It Works

```
Normal code:                    With AOP:

bookAppointment()               bookAppointment()
  └→ save to DB                   ├→ @Around BEFORE: "Booking attempt..."
                                  ├→ save to DB
                                  └→ @Around AFTER:  "Booking successful"
```

### Custom Annotations

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAppointment { }

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogPrescription { }
```

### What Gets Logged

| Event | Log Level | Example Message |
|-------|-----------|-----------------|
| Appointment booked | INFO | `[APPOINTMENT] Booked: Patient John Doe with Dr. Smith on 2026-02-18 at 09:00` |
| Appointment cancelled | WARN | `[APPOINTMENT] Cancelled: Appointment #5` |
| Appointment completed | INFO | `[APPOINTMENT] Completed: Appointment #5 by Dr. Smith` |
| Double booking prevented | WARN | `[APPOINTMENT] Double booking attempt prevented for Dr. Smith at 09:00` |
| Prescription created | INFO | `[PRESCRIPTION] Created for Patient #1 by Doctor #1` |
| Prescription updated | INFO | `[PRESCRIPTION] Updated: Prescription #abc123` |

---

## 🧪 Testing Strategy

### Unit Tests (JUnit 5 + Mockito)

| Test Class | Tests | What's Tested |
|-----------|-------|---------------|
| `DoctorServiceTest` | 8 | CRUD operations, not-found exceptions, specialty search |
| `PatientServiceTest` | 7 | CRUD operations, not-found exceptions |
| `AppointmentServiceTest` | 10 | Booking, double-booking prevention, cancellation, completion |
| **Total** | **25** | |

### Test Architecture

```
Test Class (thin)                    Helper (logic)
┌──────────────────────┐            ┌────────────────────────────┐
│ @Test                │            │                            │
│ @DisplayName("...")  │───calls───→│ Sets up mocks (when/then)  │
│ void testName() {   │            │ Calls service method        │
│   helper.scenario() │            │ Asserts results             │
│ }                    │            │ Verifies interactions       │
└──────────────────────┘            └────────────────────────────┘
                                              │
                                    ┌─────────┴─────────┐
                                    │  TestDataHelper    │
                                    │  (shared factory)  │
                                    │  createDoctor()    │
                                    │  createPatient()   │
                                    │  createAppointment()│
                                    └───────────────────┘
```

### Key Test: Double Booking Prevention

```java
when(appointmentRepository.countOverlappingAppointments(...)).thenReturn(1L);

assertThrows(DoubleBookingException.class,
    () -> appointmentService.bookAppointment(patientId, request));

verify(appointmentRepository, never()).save(any());
```

### API Tests (Postman — 35 requests)

| Section | Requests | What's Tested |
|---------|----------|---------------|
| 0. Reset | 1 | Database cleanup for repeatable runs |
| 1. Authentication | 5 | Login (3 roles), register doctor/patient |
| 2. Doctor Management | 4 | CRUD + specialty search |
| 3. Patient Management | 3 | Get all, get by ID, update |
| 4. Availability | 2 | Set schedule, view slots |
| 5. Appointments | 7 | Book, double-book (409), view, complete, cancel |
| 6. Prescriptions | 4 | Create, view, update (MongoDB) |
| 7. Medical Records | 3 | Create, view by ID, view by patient (MongoDB) |
| 8. Security | 6 | 401 unauthorized, 403 forbidden (5 scenarios) |
| **Total** | **35** | |

### Running Tests

```bash
# Unit tests (no DB required)
mvn test

# API tests (requires running app + MySQL + MongoDB)
# Import Smart-Healthcare-Postman-Collection.json into Postman
# Click "Run Collection" → runs all 35 requests in order
```

---

## ⚙ Setup & Installation

### Prerequisites

| Software | Version | Download |
|----------|---------|----------|
| Java JDK | 25+ | [OpenJDK](https://jdk.java.net/25/) |
| Maven | 3.9+ | [Apache Maven](https://maven.apache.org/) |
| MySQL | 8.0+ | [MySQL](https://dev.mysql.com/downloads/) |
| MongoDB | 7.0+ | [MongoDB](https://www.mongodb.com/try/download/community) |
| Postman | Latest | [Postman](https://www.postman.com/downloads/) |

### 1. Clone the Repository

```bash
git clone https://github.com/khdour17/Smart-Healthcare-Appointment-System.git
cd Smart-Healthcare-Appointment-System
```

### 2. Create MySQL Database

```sql
CREATE DATABASE healthcare_db;
```

### 3. Configure Application

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/healthcare_db
    username: your_mysql_username
    password: your_mysql_password

  data:
    mongodb:
      uri: mongodb://localhost:27017/healthcare_mongo

application:
  security:
    jwt:
      secret: your_base64_encoded_secret_key
      expiration: 86400000
```

### 4. Build the Project

```bash
mvn clean install
```

---

## 🚀 Running the Application

### Start Required Services

```bash
# Start MySQL
# Windows: services.msc → MySQL → Start
# Mac: brew services start mysql
# Linux: sudo systemctl start mysql

# Start MongoDB
# Windows: services.msc → MongoDB → Start
# Mac: brew services start mongodb-community
# Linux: sudo systemctl start mongod
```

### Run the Application

```bash
mvn spring-boot:run
```

### Default Admin Credentials

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin123` |
| Role | `ADMIN` |

---

## 📬 Postman Collection

The repository includes a ready-to-use Postman collection: `Smart-Healthcare-Postman-Collection.json`

### Import & Run

1. Open Postman
2. **File → Import** → select `Smart-Healthcare-Postman-Collection.json`
3. Click the collection name → **Run** → **Run Smart Healthcare...**
4. All 35 requests execute in order with automated assertions

### Features

- **Auto-saves tokens**: Login responses automatically save JWT tokens as variables
- **Auto-saves IDs**: Created resources save their IDs for subsequent requests
- **Database reset**: First request clears all data for repeatable runs
- **Automated assertions**: Every request validates status codes and response body

---

## 📸 Screenshots

### 1. Application Startup
![App Startup](screenshots/startup.png)

### 2. Postman - Full Collection Run
![Postman Run](screenshots/postman-run.png)

### 3. Postman - Appointment Booking
![Book Appointment](screenshots/book-appointment.png)

### 4. Postman - Double Booking Rejected
![Double Booking](screenshots/double-booking.png)

### 5. Postman - Security (401 & 403)
![Security 401](screenshots/security-401.png)
![Security 403](screenshots/security-403.png)

### 6. Unit Tests Passing
![Unit Tests](screenshots/unit-tests.png)

### 7. MySQL Database
![MySQL](screenshots/mysql-tables.png)

### 8. MongoDB Collections
![MongoDB](screenshots/mongodb-collections.png)

### 9. AOP Logging
![AOP Logs](screenshots/aop-logging.png)

### 10. Cache Behavior
![Cache](screenshots/cache-logs.png)

---

<div align="center">

**Built with ❤️ using Spring Boot**

</div>