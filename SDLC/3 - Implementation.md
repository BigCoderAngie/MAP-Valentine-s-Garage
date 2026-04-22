# **Stage 4: Implementation Strategy & Scrum Roadmap**

## **Sprint 0: Foundation & Environment Setup**

**Goal:** Have a compiling Android Studio project with the Multi-Module Architecture and Firebase connected.
**Assignee:** Lead Architect

### **Task 0.1: Project Initialisation**
### **Task 0.2: Firebase Console Setup**
### **Task 0.3: Applying Dependencies (Version Catalogs)**
### **Task 0.4: Multi-Module Architecture Generation**
### **Task 0.5: Application Setup & Offline Persistence**

---

## **Sprint 1: The Design System & Data Models**

**Goal:** Define the visual language ("Digital Foreman") and the static data structures.
**Assignees:** UI/Frontend Lead & Lead Architect

### **Task 1.1: Core Models (`:core:model`)**
### **Task 1.2: The Design System (`:core:ui`)**
### **Task 1.3: Shared UI Components (`:core:ui`)**

---

## **Sprint 2: Firebase Repository & Business Logic**

**Goal:** Have functional data mappers, offline persistence, and domain interactions ready for the UI layer.
**Assignee:** Business Logic Lead

### **Task 2.1: Domain Interfaces (`:core:domain`)**
### **Task 2.2: DTOs and Mappers (`:core:data`)**
### **Task 2.3: Repository Implementations (`:core:data`)**
### **Task 2.4: Dependency Injection Modules (`:core:data`)**

---

## **Sprint 3: Authorization & Routing Navigation**

**Goal:** A user can launch the app, log in, and be routed securely to their specific screen.
**Assignee:** UI Lead & Business Logic Lead

### **Task 3.1: Login ViewModel (`:feature:auth`)**
### **Task 3.2: Login UI (`:feature:auth`)**
### **Task 3.3: App Navigation (`app` module)**

---

## **Sprint 4: The Mechanic Flow (Core Features)**

**Goal:** Mechanics can log vehicles and tick off service tasks.
**Assignees:** Entire Team

### **Task 4.1: New Intake Form (`:feature:checkin`)**
### **Task 4.2: Mechanic Dashboard (`:feature:mechanic`)**
### **Task 4.3: Scrum Service Board (`:feature:mechanic`)**

---

## **Sprint 5: Admin Flow & Functional Analytics**

**Goal:** Admin dashboard correctly reflects math, timelines, and audit features.
**Assignees:** UI Lead & Business Logic Lead

### **Task 5.1: Valentine's Dashboard (`:feature:admin`)**
### **Task 5.2: Audit Trail View (`:feature:admin`)**
### **Task 5.3: User Profile Screen (Shared Route)**

---

## **Sprint 6: Testing, Polish, & Grading Requirements**

**Goal:** Prove the code works to satisfy the 20/20 Code Rubric mark. Ensure no crashes.
**Assignee:** QA

### **Task 6.1: JVM Unit Tests (`src/test/`)**
### **Task 6.2: Jetpack Compose UI Tests (`src/androidTest/`)**
### **Task 6.3: Lints & Compilation**