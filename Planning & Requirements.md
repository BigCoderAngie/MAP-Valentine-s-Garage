### **Stage 1: Planning & Requirement Analysis**

**1. Define Project Scope:**

The scope is to build the **Valentine’s Garage App**. It must handle isolated "Check-In" events (the "airplane boarding" concept the lecturer mentioned), collaborative mechanic checklists, and accountability reporting.

**2. Set Objectives and Goals:**

- **Primary Goal:** Prevent task mismanagement and hold mechanics accountable.
- **Academic Goal:** Score 100% (20/20 in Architecture, Code, Functionality, UI/Navigation, and Presentation).

**3. Resource Planning (Team Roles):**

Since we have a group of 4, we must divide the work fairly to satisfy the "Clear separation of tasks done" requirement in the rubric. Here is the recommended breakdown:

- **Member 1 (Lead Architect):** Handles Stage 3 (Design), sets up the Multi-Module Architecture, Dependency Injection (Hilt), and Database (Room/Firebase).
- **Member 2 (Frontend/UI Lead):** Handles Jetpack Compose UIs, Material 3 theming, Navigation, and making sure the app is aesthetic and easy to use.
- **Member 3 (Business Logic Lead):** Connects the UI to the Database using ViewModels, StateFlow, and Coroutines (Unidirectional Data Flow).
- **Member 4 (QA & Presentation Lead):** Writes the Unit Tests (required for the Code 20/20 mark), does manual/automated testing (Stage 5), and builds the final presentation deck.

---

### **Stage 2: Defining Requirements**

**1. Functional Requirements (What the app *must* do):**

Based strictly on the assignment and explanations from the lecturer:

- **Authentication:** Users (Mechanics and Admin/Valentine) must be able to log in.
- **Create Check-In Event:** A user must be able to register a truck's arrival. This must capture: Truck ID/License, Current Kilometers, and Current Condition. *(Crucial: As the lecturer noted, if the same truck comes back 3 weeks later, it is a brand new check-in event, completely separate from the first).*
- **Collaborative Task List:** Mechanics must see a list of tasks (e.g., change oil, change wheels, check water). They must be able to tick them off and add notes.
- **Accountability Tracking:** The system must record *who* ticked off the task so no one can say "I thought he did it."
- **Admin Reports:** Valentine must be able to view a summary of a check-in event, seeing the initial condition and the completed tasks with the mechanic's names.

**2. Technical Requirements (How it will be built):**

- **Language:** 100% Kotlin.
- **UI Framework:** Jetpack Compose (Declarative UI).
- **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles (UI Layer, Domain Layer, Data Layer).
- **State Management:** StateFlow and Coroutines.
- **Local Storage/Database:** Firebase (cloud sync).