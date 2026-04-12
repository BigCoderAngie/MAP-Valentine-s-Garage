## **Stage 3: Design**

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

### **Global Design System (Feed this to the AI first)**

- **Design Language:** Material Design 3 (MD3).
- **Theme/Vibe:** Industrial, Enterprise, Secure, High-Contrast, Professional.
- **Color Palette:**
    - **Primary:** Industrial Orange (`#FF6D00`) - Used for primary actions, FABs, and active states.
    - **Secondary:** Steel Blue/Grey (`#455A64`) - Used for secondary buttons and app bars.
    - **Background (Light Mode):** Light Ash Grey (`#F5F7F8`) to reduce glare.
    - **Background (Dark Mode):** Deep Slate (`#1E272C`).
    - **Success:** Emerald Green (`#2E7D32`) for completed tasks.
    - **Warning:** Amber (`#FFC107`) for "In Progress" states.
- **Typography:** 'Inter' or 'Roboto'. Bold/Black for headers, Medium for buttons, Regular for body text.
- **Card Style:** Elevated cards with 8dp rounded corners, subtle drop shadow, and padding of 16dp.

*Security Note:* The **Login Screen** explicitly removes the "Create Account" option. It acts as an industrial "Authorised Personnel Only" portal. It routes the user based on their Firestore `role` securely.

---

### **Screen 1: The Login Screen (Shared Entry Point)**

- **Layout:** Vertical centered alignment.
- **Top/Background:** A subtle gradient background (Light grey to white).
- **Logo/Header:** An industrial wrench/gear icon inside a soft grey rounded square. Below it, bold text: **"VALENTINE’S GARAGE"** and a subtitle in grey: *"Precision Diagnostics & Repair Portal"*.
- **Main Card (Center):** A white elevated card containing:
    - Text input field 1: Label "TECHNICIAN EMAIL", left icon (user silhouette), placeholder "[name@valentines.com](mailto:name@valentines.com)". Background slightly grey.
    - Text input field 2: Label "SECURITY KEY", left icon (padlock), placeholder "••••••••". Right-aligned above it, a small orange text link: "FORGOT PASSWORD?".
    - Button: Large, full-width, filled Primary Orange button. Text: **"INITIALIZE SESSION"** with a "login" arrow icon on the right.
- **Footer:** Small, centered grey text at the absolute bottom: *"© 2026 VALENTINE'S GARAGE. INTERNAL USE ONLY. AUTHORISED PERSONNEL PROCEED."*

---

### **Journey A: The Mechanic (The Garage Floor)**

*UI Focus: Large tap targets (for gloved hands), high visibility, and fast interactions.*

### **Screen 2: Mechanic Dashboard (Active Repairs)**

- **Top App Bar:** Steel Grey background. Left title: "Active Repairs". Right side: Mechanic’s Profile Avatar (circle).
- **Content Area:** A `LazyColumn` (vertically scrolling list) with a light grey background.
- **List Item (Vehicle Card):**
    - White elevated card.
    - **Top Row:** Bold License Plate text (e.g., "N 12345 W") on the left. On the right, a grey pill-shaped badge showing the time elapsed since arrival (e.g., "2 hrs ago").
    - **Middle Row:** Subtitle text showing Vehicle Model (e.g., "Toyota Hilux").
    - **Bottom Row:** A horizontal progress bar (Orange) showing task completion (e.g., "2/5 Tasks Done").
- **Floating Action Button (FAB):** Positioned bottom-right. Large, circular, Primary Orange background with a bold white "+" icon. (Action: Opens New Check-In).

### **Screen 3: New Check-In (The "Airplane Boarding" Form)**

- **Top App Bar:** Title "New Vehicle Intake". Left arrow to go back.
- **Content Area:** Vertically scrolling form.
    - **Section 1: Vehicle Identity:** Two outlined text fields side-by-side (License Plate, Vehicle Model).
    - **Section 2: Current Metrics:** Numeric input field labeled "Odometer (Kilometers)" with a speedometer icon.
    - **Section 3: Initial Condition:** A large, multi-line text area labeled "Intake Condition Report". Placeholder: "Describe any existing scratches, dents, or leaks..."
    - **Section 4: Required Tasks (Dynamic List):**
        - An input field labeled "Add Repair Task" with an orange "ADD" button next to it.
        - Below it, a list of added tasks displayed as dismissible chips or small rows with an "X" icon to remove them.
- **Bottom Sticky Bar:** A thick white container anchored to the bottom. Contains a full-width, Primary Orange button: **"AUTHORISE CHECK-IN"**.

### **Screen 4: Collaborative Service Board (Scrum/Kanban)**

- **Top App Bar:** Shows License Plate and Model.
- **Sub-Header Card:** A collapsed card at the top. Shows Odometer and Intake Condition. (Expandable if tapped).
- **Navigation/Tabs:** A sticky `TabRow` just below the header with 3 equally spaced tabs: **"TO DO"**, **"IN PROGRESS"**, **"DONE"**. The active tab has an orange underline.
- **Tab View 1 (TO DO):**
    - List of task cards.
    - Card shows Task Description in bold.
    - Bottom right of the card: An outlined orange button **"START WORK"**. (Tapping moves it to In Progress).
- **Tab View 2 (IN PROGRESS):**
    - Card shows Task Description.
    - Below description: An Amber/Yellow badge with a person icon reading: *"In Progress by [Mechanic Name]"*.
    - If the logged-in mechanic owns the task: A filled orange button **"FINISH & ADD NOTES"** appears. If owned by someone else, button is hidden.
- **Bottom Sheet Modal (Triggered by "Finish & Add Notes"):**
    - Slides up from the bottom. Title: "Complete Task".
    - Text input field: "Repair Notes / Parts Used".
    - Full-width Green button: **"MARK AS DONE"**.
- **Tab View 3 (DONE):**
    - Card shows Task Description with a strikethrough.
    - Below description: A Green badge reading: *"Completed by [Mechanic Name]"*.
    - A grey text block below showing the exact notes they typed.

---

### **Journey B: Admin / Valentine (The HQ)**

*UI Focus: Data density, analytics, read-only audit trails, and filtering.*

### **Screen 5: Admin Dashboard (Overview)**

- **Navigation:** BottomNavigationBar (Tabs: **OVERVIEW** [Active], **PROFILE** [Inactive]).
- **Top App Bar:** Steel Grey background. Title "Valentine's Overview".
- **Analytics Header (Top):** A horizontal scrolling row (`LazyRow`) of square summary cards:
    - Card 1: "Vehicles Today" (Big number: 14)
    - Card 2: "Active Repairs" (Big number: 6)
    - Card 3: "Completed Today" (Big number: 8)
- **Search Bar:** Below the metrics. A full-width search input with a magnifying glass icon. Placeholder: "Search by License Plate or Mechanic".
- **Content Area (History List):**
    - List of historical Check-In cards.
    - Card Layout: Left side shows Date & Time. Middle shows License Plate & Model. Right side shows a status badge (Green "COMPLETED" or Orange "IN PROGRESS").

### **Screen 6: Accountability Report (The Audit Trail)**

- **Top App Bar:** Title "Audit Report: [License Plate]". Left arrow to go back. Right side: A "Print/Export" icon.
- **Top Section (Intake Snapshot):**
    - A solid grey card containing static data: Arrival Time, Checked-in By (Name), Intake Kilometers, and the exact Initial Condition text.
- **Middle Section (The Timeline):**
    - A vertical timeline UI (a line running down the left side with dots for each task).
    - Next to each dot is a Task Card.
    - **Task Card Design:**
        - Task Name (e.g., "Brake Pad Replacement").
        - **Accountability Tag:** A distinct UI row inside the card with a tiny avatar, showing: *"Actioned by: John Doe at 14:35"*.
        - **Notes Block:** A light yellow or grey box with quote marks containing the mechanic's exact notes (e.g., *"Replaced front pads. Rotors looked fine."*).
- **Bottom Section (Sign-off):**
    - If the vehicle is completed, a large green footer block: "VEHICLE CLEARED".

---

### **Shared Screens**

**7. User Profile**

- **Purpose:** Session management and settings.
- **Navigation:** BottomNavigationBar (Tabs: **GARAGE/OVERVIEW** [Inactive], **PROFILE** [Active]).
- **Top App Bar:** Title "My Profile".
- **Content:**
    - Large centered User Avatar (Circle).
    - Text: "John Doe" (Large, Bold).
    - Text: "Role: Senior Mechanic" (Grey, Medium).
    - Spacer.
    - An Outlined Button: "Change Password".
    - A large filled Red Button at the bottom: **"SECURE LOGOUT"**.