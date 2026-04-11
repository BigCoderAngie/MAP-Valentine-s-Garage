### **Stage 3: Design**

### **1. High-Level Design (HLD) - The Architecture**

To get the 20/20 for "App Architecture (Layered architecture, modularisation, and resources files)," we will use a modern multi-module MVVM (Model-View-ViewModel) architecture.

**The Tech Stack:**

- **Backend/Database:** Firebase Authentication (for logging in) & Firebase Cloud Firestore (NoSQL real-time database).
- **Dependency Injection:** Dagger Hilt.
- **Asynchronous Programming:** Kotlin Coroutines & `StateFlow`.
- **UI:** Jetpack Compose.

**The Multi-Module Structure:**
Instead of putting everything in one folder, we will structure the Android Studio project like this:

- `app` *(The main shell that ties everything together)*
- `core`
    - `:core:model` *(Contains the Kotlin Data Classes)*
    - `:core:data` *(Contains Firebase Repositories and Data Sources)*
    - `:core:ui` *(Shared UI components like custom buttons, themes, and colors)*
- `feature`
    - `:feature:auth` *(Login Screen)*
    - `:feature:checkin` *(The "Airplane Boarding" flow: logging kilometers and condition)*
    - `:feature:mechanic` *(The collaborative checklist screen)*
    - `:feature:admin` *(Valentine's report dashboard)*

*Architectural Flow (UDF):* Firebase (Data Layer) ➔ `Repository` ➔ `ViewModel` ➔ `StateFlow` ➔ Jetpack Compose UI.

---

### **2. Low-Level Design (LLD) - Firebase Schema & Kotlin Data Models**

Since Firestore is a NoSQL database, we need to design our "Collections" and "Documents."

We will put these exact `data class` definitions inside the `:core:model` module. It has been designed to perfectly match the lecturer's "Airplane Boarding" concept and the accountability requirement.

**Collection 1: `users`***(Tracks who is who—crucial for accountability)*

```kotlin
data class User(
    val id: String = "",           // Firebase Auth UID
    val name: String = "",         // e.g., "John Doe"
    val role: Role = Role.MECHANIC // Enum: MECHANIC or ADMIN (Valentine)
)

enum class Role { MECHANIC, ADMIN }
```

**Collection 2: `vehicles`***(Just the static details of the truck)*

```kotlin
data class Vehicle(
    val id: String = "",           // Auto-generated Firestore ID
    val licensePlate: String = "", // e.g., "N 12345 W"
    val model: String = ""         // e.g., "Toyota Hilux"
)
```

**Collection 3: `check_ins`***(The core event! As the lecturer said, if a truck comes twice, it's two separate events).*

```kotlin
data class CheckIn(
    val id: String = "",             // Auto-generated Firestore ID
    val vehicleId: String = "",      // Links back to the Vehicle
    val timestamp: Long = 0L,        // When it arrived
    val kilometersDriven: Int = 0,   // "captures the number of kilometers driven"
    val initialCondition: String = "",// "condition of the vehicles... to prevent misuse"
    val checkedInBy: String = "",    // User ID of who checked it in
    val isCompleted: Boolean = false // True when repairs are fully done
)
```

**Collection 4: `tasks`***(The collaborative checklist. Each task links to a specific `check_in`)*

```kotlin
data class Task(
    val id: String = "",             // Auto-generated Firestore ID
    val checkInId: String = "",      // Which check-in event this belongs to
    val description: String = "",    // e.g., "Change oil", "Fix brakes"
    val isDone: Boolean = false,     // Ticked off or not
    val mechanicId: String? = null,  // Accountability: WHO ticked it off?
    val mechanicName: String? = null,// Easier display on the UI
    val notes: String = ""           // "mechanics should... write notes on what they worked on"
)
```