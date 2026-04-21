# **Stage 3: Design**

## **1. High-Level Design (HLD) - The Architecture**

To achieve 20/20 for "App Architecture (Layered architecture, modularisation, and resource files)," we are utilizing a modern multi-module MVVM architecture, strictly adhering to Unidirectional Data Flow (UDF), High Cohesion, Low Coupling, and the Dependency Inversion principle.

**The Tech Stack:**

- **Backend/Database:** Firebase Authentication (for role-based identity) & Firebase Cloud Firestore (NoSQL real-time database).
- **Dependency Injection:** Dagger Hilt.
- **Asynchronous Programming:** Kotlin Coroutines (`Dispatchers.IO` for network, `Dispatchers.Main` for UI) & `StateFlow` scoped to `ViewModelScope` to prevent memory leaks.
- **UI:** Jetpack Compose (Material Design 3).

**The Multi-Module Structure (Dependency Inversion Applied):**
Following the strict dependency rules (Higher-level modules depend on lower-level ones), the Android Studio project is structured as:

- `app` *(The entry point. Wires everything together using Hilt).*
- `core` *(High Cohesion modules)*
    - `:core:model` *(Pure Kotlin Data Classes and Enums. No Android dependencies).*
    - `:core:domain` *(Pure Kotlin Business Logic, UseCases, and Repository Interfaces).*
    - `:core:data` *(Repository Implementations for Firestore. Depends on `:core:domain`).*
    - `:core:ui` *(Shared Compose elements, typography, icons, and color palette).*
- `feature` *(Depends on `:core:domain` and `:core:ui`)*
    - `:feature:auth` *(Secure Login & Session Initialization)*
    - `:feature:checkin` *(Vehicle Registration & Intake Flow)*
    - `:feature:mechanic` *(Collaborative Service Board & Dashboards)*
    - `:feature:admin` *(Valentine's Overview & Audit Trails)*

*Architectural Flow (UDF):* Firebase ➔ `:core:data` (Impl) ➔ `:core:domain` (Interface) ➔ `ViewModel` ➔ `StateFlow` ➔ Jetpack Compose UI.

---

## **2. Low-Level Design (LLD) - Firebase Schema & Kotlin Data Models**

The Firestore NoSQL schema maps to the UI elements. These pure Kotlin definitions live in `:core:model`.

**Collection 1: `users`** *(Admin provisions accounts via Firebase Console, mechanics use "Forgot Password").*

```kotlin
data class User(
    val id: String = "",           // Firebase Auth UID
    val name: String = "",         // e.g., "John Doe"
    val initials: String = "",     // e.g., "JD" (used for UI avatars)
    val role: Role = Role.MECHANIC // Enum: MECHANIC or ADMIN
)
enum class Role { MECHANIC, ADMIN }
```

**Collection 2: `vehicles`** *(Static physical details of the truck).*

```kotlin
data class Vehicle(
    val id: String = "",
    val licensePlate: String = "",
    val model: String = ""
)
```

**Collection 3: `check_ins`** *(The core intake event: If a truck comes twice, it's two separate events).*

```kotlin
data class CheckIn(
    val id: String = "",
    val vehicleId: String = "",
    val timestamp: Long = 0L,        // Arrival time
    val kilometersDriven: Int = 0,   // "Odometer" reading
    val initialCondition: String = "",
    val checkedInBy: String = "",    // UID / Name of the mechanic who logged it
    val isCompleted: Boolean = false // Set to true when Admin clicks "VEHICLE CLEARED"
)
```

**Collection 4: `tasks`** *(The collaborative checklist supporting a Scrum/Kanban workflow).*

```kotlin
enum class TaskStatus { TODO, IN_PROGRESS, DONE }
enum class TaskPriority { HIGH, NORMAL, LOW }

data class Task(
    val id: String = "",
    val checkInId: String = "",      // Links to the specific CheckIn event
    val name: String = "",           // e.g., "Oil Filter Replacement"
    val description: String = "",    // e.g., "Full synthetic 5W-30 replacement..."
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val mechanicId: String? = null,  // Accountability: WHO claimed/finished it
    val mechanicName: String? = null,
    val mechanicInitials: String? = null, // e.g., "JD"
    val completedAt: Long? = null,   // Feeds the "Actioned by... at 14:35" Audit UI
    val notes: String = ""           // Diagnostic notes added upon completion
)
```

---

## **3. Data Processing & Functional Programming Strategy**

To populate the Admin Dashboard analytics (e.g., "Vehicles Today", "Active Repairs"), the ViewModels will utilize Kotlin Higher-Order Functions to transform the raw Firestore lists without relying on imperative `for` loops.

- `checkins.count { it.timestamp >= startOfDay }` ➔ Vehicles Today.
- `tasks.filter { it.status == TaskStatus.DONE }` ➔ Completed Tasks.
- `tasks.groupBy { it.status }` ➔ Categorizes the Service Board lists dynamically.

---

## **4. Testing Strategy (QA)**

To achieve the 20/20 Code grading criteria (which mandates Unit Tests), we will adhere to the **Testing Pyramid**:

- **Unit Tests (70%):** Located in `src/test/`, we will test the pure Kotlin logic in `:core:domain` and the ViewModels.
- **Frameworks:** We will use **JUnit4/5**, **Truth** for assertions, and **MockK** to mock Firebase dependencies, ensuring tests run instantly on the JVM without requiring an emulator.
- **Pattern:** All tests will be structured using the **Arrange-Act-Assert (AAA)** methodology.

---

## **5. UI/UX Design - Screens & Navigation Constraints**

The navigation logic is strictly separated by Role. **Bottom Navigation Bars are strictly limited to two tabs maintaining enterprise focus.**

### **Global Design System**

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

<img width="1430" height="802" alt="image" src="https://github.com/user-attachments/assets/6bc93665-9098-4cdb-8304-c71307b8e87e" />


---

### **Journey A: The Mechanic (The Garage Floor)**

**Screen 1: The Login Screen (Shared Entry Point)**

- **Navigation:** No TopAppBar, No BottomNavBar.
- **UI:** Vertical centered. Orange wrench/gear logo in a grey rounded square. Title: "VALENTINE’S GARAGE", Subtitle: "PRECISION DIAGNOSTICS & REPAIR PORTAL".
- **Fields:** "TECHNICIAN EMAIL" (user icon) and "SECURITY KEY" (padlock icon). "FORGOT PASSWORD?" link aligned right above the password field.
- **Action:** Primary Orange button: **"INITIALIZE SESSION ➔]"**.

*Security Note:* The **Login Screen** explicitly removes the "Create Account" option. It acts as an industrial "Authorised Personnel Only" portal. It routes the user based on their Firestore `role` securely.

<img width="706" height="1600" alt="image" src="https://github.com/user-attachments/assets/2f37f5e2-6675-4fd4-9a5a-c58e83655e0c" />


**Screen 2: Mechanic Dashboard (Active Repairs)**

- **Navigation:** BottomNavBar VISIBLE (Tabs: **GARAGE** [Active/Orange], **PROFILE** [Inactive/Grey]).
- **UI:** TopAppBar in Steel Grey with white text "ACTIVE REPAIRS" and a wrench/arm icon on the left.
- **Cards:** White elevated cards.
    - Top: License Plate (Bold) and a grey pill badge showing time (e.g., "2 HRS AGO").
    - Middle: Vehicle Model & Main issue (e.g., "Toyota Hilux — Engine Diagnostics").
    - Bottom: "PROGRESS" text with a percentage and an Orange LinearProgressIndicator.
- **Action:** Bottom-right Orange FAB (Rounded Square shape) with a white "+" icon.

<img width="467" height="1600" alt="image" src="https://github.com/user-attachments/assets/e41f2191-5c62-4e82-9108-e3a8eb75059d" />


**Screen 3: New Check-In ("New Vehicle Intake")**

- **Navigation:** TopAppBar (White) with back arrow, Orange title "NEW VEHICLE INTAKE", and a user profile icon on the right. BottomNavBar HIDDEN.
- **UI:** Vertically scrolling form.
    - **Section 1:** Header "SERVICE PROTOCOL: VEHICLE REGISTRATION" with subtext.
    - **Section 2:** Text fields for "LICENSE PLATE" and "VEHICLE MODEL".
    - **Section 3:** Text field "ODOMETER (KILOMETERS)" with dashboard icon.
    - **Section 4:** Text area "INTAKE CONDITION REPORT" (clipboard icon).
    - **Section 5 (Dynamic List):** "MAINTENANCE SCOPE". Two input fields: "TASK NAME" and "TASK DESCRIPTION". Large Orange "ADD" button below them. Added tasks appear below as light blue chips (e.g., "OIL CHANGE X").
- **Action:** Bottom sticky Orange button: **"AUTHORIZE CHECK-IN"**.

<img width="428" height="1600" alt="image" src="https://github.com/user-attachments/assets/cd923142-ec1e-4ed9-aa02-2f7ba7405a38" />


**Screen 4: Collaborative Service Board (Scrum Board)**

- **Navigation:** TopAppBar with back arrow, License Plate (e.g., "N 12345 W" in orange), and user icon. BottomNavBar HIDDEN.
- **Header Card:** Shows "ODOMETER" and "CONDITION" with relevant icons.
- **Tabs:** Sticky `TabRow` (**TO DO** | **IN PROGRESS** | **DONE**). Active tab has an orange underline.
    - **TO DO View:** Task cards showing Task Name (Bold) and Description. Bottom row: Mechanic Initials circle (e.g., "JD") and an outlined red/orange button **"START WORK"**.
    - **DONE View:** Task cards showing Task Name (strikethrough text) with a grey checkmark icon. Light blue pill badge: **"✔ COMPLETED BY [NAME]"**. Below it, a grey box containing the exact repair notes.
- **Modal (Triggered by moving a task to Done):**
    - A centered popup/dialog. Title: "COMPLETE TASK" with an 'X' to close.
    - Displays the Task Name.
    - Text input field: "REPAIR NOTES / PARTS USED".
    - Action: Full-width Green button **"MARK AS DONE ✔"**.
    
<img width="684" height="1600" alt="image" src="https://github.com/user-attachments/assets/8e53e4c1-0795-4a09-a650-77521ea09e3a" />
<img width="706" height="1600" alt="image" src="https://github.com/user-attachments/assets/b2f7ef88-a093-4c2a-a692-45fde309190b" />
<img width="532" height="1600" alt="image" src="https://github.com/user-attachments/assets/83120913-2ca5-4df1-8b7a-8574a671f8e0" />
<img width="588" height="1600" alt="image" src="https://github.com/user-attachments/assets/2a40623c-77e5-4285-9df9-04747cbc0918" />


---

### **Journey B: Admin / Valentine (The HQ)**

**Screen 5: Admin Dashboard (Valentine's Overview)**

- **Navigation:** BottomNavBar VISIBLE (Tabs: **OVERVIEW** [Active/Orange icon], **PROFILE** [Inactive/Grey]).
- **UI:**
    - TopAppBar: Orange Title "VALENTINE'S OVERVIEW" + Search Icon + Profile Icon.
    - **Analytics Header:** A horizontally scrolling row (`LazyRow`) of square white cards with orange icons/text. Card 1: "VEHICLES TODAY" (14). Card 2: "ACTIVE REPAIRS" (6). Card 3: "COMPLETED TODAY" (8).
    - **Search Bar:** OutlinedTextField: "Search License Plate".
    - **History List ("Daily Log"):** List of check-ins. Left: Time/Date. Middle: License Plate & Model. Right: Status pill (Light Blue "COMPLETED" or Light Orange "IN PROGRESS").
    
<img width="426" height="1600" alt="image" src="https://github.com/user-attachments/assets/1bd41f89-6b3d-4dfc-9eec-d74ec8ecefdb" />

    

**Screen 6: Accountability Report (The Audit Trail)**

- **Navigation:** TopAppBar "AUDIT: N 12345 W" with Print & Share icons. BottomNavBar HIDDEN.
- **UI:**
    - **Header:** Light grey background. Shows "ARRIVED" (Time), "CHECKED IN BY" (Name), "ODOMETER", "INITIAL CONDITION", and a calendar box with the Date.
    - **Timeline:** Title "Audit Timeline" with a blue badge "4 TASKS VERIFIED".
    - **Task Cards:**
        - Task Name with a solid orange checkmark icon.
        - **Accountability Row:** Initials circle (e.g., "JD"), text: *"Actioned by: John Doe at 14:35"*.
        - **Notes:** Pale yellow bordered box containing the exact diagnostic notes in italics.
- **Action:** Bottom sticky Vibrant Blue button: **"✔ VEHICLE CLEARED"** (Updates `CheckIn.isCompleted` to `true`).

<img width="706" height="1600" alt="image" src="https://github.com/user-attachments/assets/c8c8275f-9b75-42d0-b3a9-e2309a8a842d" />


---

### **Shared Screen**

**Screen 7: User Profile**

- **Navigation:** BottomNavBar VISIBLE (Tabs: Garage/Overview [Inactive], **PROFILE** [Active/Orange]).
- **UI:**
    - TopAppBar: Orange title "MY PROFILE".
    - Large centered User Avatar (Orange rounded square outline with Initials inside, e.g., "JD").
    - Text: "John Doe" (Large, Bold).
    - Text: "ROLE: SENIOR MECHANIC" (Grey).
    - List Item: "Change Password" row with an arrow.
- **Action:** Large Deep Red Button: **"SECURE LOGOUT"** (Terminates Firebase session).

<img width="706" height="1600" alt="image" src="https://github.com/user-attachments/assets/aa1f6a43-9694-4b95-b04f-64f438254801" />


---

# Design System Specification: Industrial Precision & Tonal Depth

## 1. Overview & Creative North Star

### The Creative North Star: "The Digital Foreman"

This design system moves beyond the generic utility of industrial software to create a "Digital Foreman" experience—an environment that feels as authoritative and well-engineered as the heavy machinery it manages. We are eschewing the "flat web" look in favor of **Organic Industrialism**.

The aesthetic is characterized by high-contrast legibility, intentional asymmetry, and a sophisticated layering of neutral tones that mimic the depth of a professional garage floor. By breaking the standard grid with overlapping elements and using tonal shifts instead of structural lines, we create a UI that feels high-end, secure, and bespoke.

## 2. Colors & Tonal Architecture

The palette is rooted in the "Amber" of industrial warning lights and the "Iron" of structural steel.

### Palette Roles

- **Primary (`#9f4200` / `#ff6d00`):** Used exclusively for high-intent actions. This is your "Caution: Action Required" signal.
- **Secondary (`#4c616c`):** The "Steel" foundation. Used for app bars and structural navigation to provide a sense of enterprise security.
- **Surface Tiers:** We utilize the MD3 surface container system to create depth without clutter.

### The "No-Line" Rule

**Explicit Instruction:** Designers are prohibited from using 1px solid borders to section off content. Content boundaries must be defined solely through background color shifts. For example, a `surface-container-lowest` card should sit atop a `surface-container-low` background. This creates a seamless, modern "editorial" feel.

### The Glass & Gradient Rule

To prevent the UI from feeling "flat" or "dated," incorporate the following:

- **Signature Textures:** Main CTAs should use a subtle vertical gradient from `primary` (#9f4200) to `primary_container` (#ff6d00). This provides a "machined" metallic sheen.
- **Glassmorphism:** Floating elements, such as navigation rails or top-tier modals, should utilize a semi-transparent `surface` color with a `20px` backdrop-blur. This ensures the industrial "Iron" background bleeds through, softening the interface while maintaining hierarchy.

## 3. Typography: The Engineered Scale

We use typography to reinforce the "Professional/Enterprise" vibe.

- **Display & Headline (Space Grotesk):** This typeface features quirky, engineered terminals that mimic technical blueprints. Use **Bold/Black** weights for `headline-lg` to create a sense of immovable authority.
- **Body & Labels (Inter):** A workhorse typeface chosen for its high x-height and readability in high-stress industrial environments.
- **Hierarchy Note:** Use wide tracking (letter-spacing: 0.05em) on `label-sm` to give the UI a premium, "dashboard" look.

| Role | Font | Size | Weight |
| --- | --- | --- | --- |
| **Display-LG** | Space Grotesk | 3.5rem | Bold |
| **Headline-MD** | Space Grotesk | 1.75rem | Medium |
| **Title-SM** | Inter | 1rem | Semi-Bold |
| **Body-MD** | Inter | 0.875rem | Regular |
| **Label-MD** | Inter | 0.75rem | Medium (All Caps) |

## 4. Elevation & Depth

In this design system, depth is a functional tool, not a decoration.

### The Layering Principle

Hierarchy is achieved by "stacking" surface tiers.

1. **Level 0 (Background):** `surface` (#f8fafb)
2. **Level 1 (Sections):** `surface_container_low` (#f2f4f5)
3. **Level 2 (Cards):** `surface_container_lowest` (#ffffff)

### Ambient Shadows

Traditional drop shadows are too "software-standard." Instead, use **Ambient Shadows**:

- **Blur:** 24dp to 32dp.
- **Opacity:** 4% - 6%.
- **Color:** Use a tinted version of `on_surface` (a deep slate) rather than pure black. This mimics natural light reflecting off metallic surfaces.

### The "Ghost Border" Fallback

If a border is required for accessibility (e.g., in high-glare environments), use the **Ghost Border**: The `outline_variant` token at **15% opacity**. Never use 100% opaque borders.

## 5. Components

### Buttons: High-Contrast Tools

- **Primary:** `primary_container` (#ff6d00) background with `on_primary` (#ffffff) text.
- **Radius:** `sm` (4px/0.25rem). A tighter radius feels more "industrial" and precise than rounded "bubbly" buttons.
- **State:** On hover, apply a `surface_tint` overlay at 8% to simulate a glowing amber light.

### Cards: The Cargo Container

- **Construction:** Elevated white cards (`surface_container_lowest`).
- **Radius:** `lg` (8px/0.5rem).
- **Padding:** Strict 16dp internal gutters.
- **Restriction:** Forbid the use of divider lines within cards. Use `8dp` of vertical white space to separate header from body content.

### Input Fields: Machined Precision

- **Style:** Outlined. Use the `outline` token (#8d7164).
- **Iconography:** Sharp Material Symbols (2pt stroke). Every input should be accompanied by an industrial icon (e.g., a `precision_manufacturing` gear for settings or `local_shipping` for logistics).

### Additional Component: The "Status Bar" Chip

For industrial workflows, use high-contrast status chips.

- **Success:** Emerald Green (#2E7D32) background, white text, `none` (0px) corner radius for a "stamped" label look.

## 6. Do’s and Don’ts

### Do:

- **Do** use asymmetrical layouts (e.g., a large headline on the left with 40% empty space to the right).
- **Do** lean into "Steel" (#455A64) for top-level navigation to anchor the experience.
- **Do** use `Space Grotesk` sparingly—only for headers—to maintain its visual impact.

### Don’t:

- **Don’t** use shadows on every card. Rely on background color shifts first.
- **Don’t** use "Rounded Full" pill buttons; they conflict with the industrial aesthetic. Stick to `sm` (4px) or `none` (0px).
- **Don’t** use standard blue for links. Everything interactive is **Amber** (#FF6D00).
- **Don’t** use 1px dividers to separate list items. Use `surface_container_high` as a subtle background strip for every other row.

---

**Director's Note:** This system is about the *weight* of the interface. Every element should feel like it was bolted into place with purpose. Respect the white space; it is the "ventilation" of your design.
