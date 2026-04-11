### **Stage 3: Design**

### **1. High-Level Design (HLD) - The Architecture**

To get the 20/20 for "App Architecture (Layered architecture, modularisation, and resource files)," we will use a modern multi-module MVVM (Model-View-ViewModel) architecture, inspired by Google's official *Now in Android* app.

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
    - `:feature:checkin` *(The "Airplane Boarding" flow: logging kilometers, condition, and required tasks)*
    - `:feature:mechanic` *(The collaborative Service Board screen)*
    - `:feature:admin` *(Valentine's report dashboard)*

*Architectural Flow (UDF):* Firebase (Data Layer) ➔ `Repository` ➔ `ViewModel` ➔ `StateFlow` ➔ Jetpack Compose UI.

---

### **2. Low-Level Design (LLD) - Firebase Schema & Kotlin Data Models**

Since Firestore is a NoSQL database, we need to design our "Collections" and "Documents." We will put these exact `data class` definitions inside the `:core:model` module.

**Collection 1: `users`***(Tracks who is who, crucial for accountability. Note: Registration is disabled for security. Admin provisions accounts via Firebase Console, and mechanics use "Forgot Password" to set their secure key).*

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

**Collection 3: `check_ins`***(The core event! If a truck comes twice, it's two separate events).*

```kotlin
data class CheckIn(
    val id: String = "",             // Auto-generated Firestore ID
    val vehicleId: String = "",      // Links back to the Vehicle
    val timestamp: Long = 0L,        // When it arrived
    val kilometersDriven: Int = 0,   // Captures the number of kilometers driven
    val initialCondition: String = "",// Condition of the vehicles to prevent misuse
    val checkedInBy: String = "",    // User ID of who checked it in
    val isCompleted: Boolean = false // True when all linked tasks are DONE
)
```

**Collection 4: `tasks`***(The collaborative checklist. Updated to support a Scrum/Kanban workflow for real-time collaboration).*

```kotlin
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}

data class Task(
    val id: String = "",             // Auto-generated Firestore ID
    val checkInId: String = "",      // Which check-in event this belongs to
    val description: String = "",    // What needs to be done (e.g., "Fix brakes")
    val status: TaskStatus = TaskStatus.TODO, // The Scrum Status
    val mechanicId: String? = null,  // Accountability: WHO is currently working on it / finished it?
    val mechanicName: String? = null,// Easier display on the UI
    val notes: String = ""           // Notes added when moving from IN_PROGRESS to DONE
)
```

---

### **3. UI/UX Design - Screens**

Since the app has two distinct roles (MECHANIC and ADMIN), we will design two separate user journeys.

*Security Note:* The **Login Screen** explicitly removes the "Create Account" option. It acts as an industrial "Authorized Personnel Only" portal. It routes the user based on their Firestore `role` securely.

### **Journey A: The Mechanic (The Garage Floor)**

*This journey focuses on efficiency, large tap targets, and collaborative workspaces.*

**1. Mechanic Dashboard (The Active Garage)**

- **Purpose:** The home screen for mechanics to see which vehicles are currently being worked on.
- **UI Elements:**
    - **Top App Bar:** Shows "Active Repairs" and the logged-in mechanic's name.
    - **Main Content (LazyColumn):** A scrollable list of Material Design Cards. Each card represents an *Active Check-In Event* showing the Truck Model, License Plate, and Time Arrived.
    - **Floating Action Button (FAB):** A large button in the bottom right with a "+" icon to initiate a New Check-In.

**2. New Check-In Screen (The "Airplane Boarding" Concept)**

- **Purpose:** Registering a specific instance of a truck arriving and defining its required scope of work.
- **UI Elements:**
    - **Vehicle Identification:** Text fields for License Plate and Truck Model.
    - **Current State Logging:** A numeric input for **Current Kilometers** and a multi-line text area for **Initial Condition** (e.g., "Scratched left bumper, leaking oil").
    - **Required Repairs / Services Input:** A dynamic list where the mechanic types in the specific tasks requested by the customer (e.g., "Replace brake pads", "Change oil").
    - **Action Button:** A prominent "Authorise Check-In" button. Pressing this creates the `CheckIn` document AND automatically generates the specific `Task` documents defined in the dynamic list for this event.

**3. Collaborative Service Board (The Workshop)**

- **Purpose:** Where mechanics claim tasks, tick them off, and write notes. This screen supports real-time Firebase updates using a Scrum/Kanban board layout (HorizontalPager or categorised headers).
- **UI Elements:**
    - **Header:** Displays the Truck info and Initial Condition as a reminder.
    - **Tab 1: To Do (Pending):** A list of all tasks created during check-in. Any mechanic can tap a "Start Work" button on a task.
    - **Tab 2: In Progress:** When a mechanic taps "Start Work", the task moves here. It displays a badge: **"In Progress by [Mechanic's Name]"**. Other mechanics can see it but cannot complete it. Only the assigned mechanic can tap the "Mark Done & Add Notes" button.
    - **Tab 3: Done:** Once completed, the task moves here. It shows a green badge: **"Completed by [Mechanic's Name]"** along with their repair notes (fulfilling the Accountability requirement).
    - **Action Button:** "Mark Repair as Complete" (sets the Check-In to complete and removes the truck from the Active Garage dashboard).

---

### **Journey B: Valentine / Admin (The HQ)**

*This journey focuses on data visualisation, accountability, and reporting.*

**4. Admin Dashboard (The Overseer)**

- **Purpose:** Valentine’s home screen to get a bird's-eye view of garage operations.
- **UI Elements:**
    - **Analytics Header:** Quick stats (e.g., "Trucks Serviced This Week: 14").
    - **Search/Filter Bar:** To search for a specific license plate.
    - **History List (LazyColumn):** A historical log of all Check-In events, sorted by date. Each card shows the Truck ID, Date, and a status badge ("Completed" or "In Progress"). Tapping a card opens the Report Details.

**5. Accountability Report Screen (The Audit)**

- **Purpose:** A read-only screen for Valentine to verify *who* did *what*, preventing the "I thought another colleague did it" excuse.
- **UI Elements:**
    - **Intake Summary Card:** Shows the exact kilometers and the condition the truck was in when it arrived.
    - **Audit Trail List:** A timeline or list showing every required task for that check-in.
    - **Accountability Tags:** Next to every completed task, it explicitly lists the **Mechanic's Name**, the **Timestamp** of when they checked it off, and the **Notes** they left.