# LifeFlow - Personal Productivity & Life Management App

> **"Your Life, Organized in One Place"**

LifeFlow is a comprehensive personal productivity Android application built using Kotlin, Jetpack Compose, Material 3, Clean Architecture, and Supabase.

---

## Features

- 🔐 **Authentication**: User Registration, Login, Forgot Password, and Auth session persistence via Supabase Auth.
- 📊 **Home Dashboard**: Real-time progress indicators, daily metrics, statistical breakdown (Total, In Progress, Completed, Overdue), quick action shortcuts, and today's tasks.
- ✅ **Task Management**: Create, edit, complete, delete, search with state debounce, priority tags (High, Medium, Low), category badges, and automated overdue task detection.
- 📅 **Calendar & Events**: Interactive month/week timeline views for tasks and schedule events with reminder support.
- 🎯 **Goals & Milestones**: Goal tracking with target metrics, milestone checklists, and dynamic percentage calculation.
- 📈 **Analytics & Productivity Score**: Dynamic multi-metric productivity calculation formula ($$40\% \text{ Tasks} + 25\% \text{ Goals} + 20\% \text{ On-Time} + 15\% \text{ Consistency}$$) and visual Canvas charts (Line & Donut).
- 📝 **Quick Notes**: Notes workspace with color tags, search, pin/unpin, archive/unarchive, and rich formatting toolbar options.
- 🔔 **Notifications & Reminders**: Local Android `NotificationManager` channels, task/event reminder alarms, and `WorkManager` daily summary sync.
- 👤 **Profile & Settings**: Dynamic streak counter, task/goal summary metrics, avatar support via Supabase Storage, and customizable Light/Dark/System Material 3 themes.

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose, Material 3, Navigation Compose
- **Architecture**: Clean Architecture (UI → ViewModel → Use Case → Repository → Supabase Remote Data)
- **Backend / Database**: Supabase Kotlin SDK, Supabase Auth, PostgreSQL, Row Level Security (RLS)
- **Local Persistence**: DataStore Preferences for app preferences & onboarding state
- **Background Tasks**: Android WorkManager & AlarmManager / NotificationManager

---

## Supabase Setup Instructions

### 1. Create a Supabase Project
1. Go to [Supabase](https://supabase.com) and create a new project.
2. Navigate to **Project Settings -> API** and copy your **Project URL** and **anon / public key**.

### 2. Configure Credentials in Android App
Open `local.properties` in your Android project directory and set:
```properties
SUPABASE_URL=https://your-supabase-project-id.supabase.co
SUPABASE_KEY=your-supabase-anon-public-key
```

### 3. Run Database Migrations
1. In the Supabase Dashboard, navigate to **SQL Editor**.
2. Open the file `supabase_schema.sql` located in the root of this project.
3. Paste the contents into the SQL Editor and click **Run**.
4. This script will automatically create:
   - Tables: `profiles`, `tasks`, `goals`, `goal_milestones`, `events`, `notes`, `notifications`, `user_settings`
   - Indexes for high performance queries
   - Row Level Security (RLS) policies ensuring users only access their own data
   - PostgreSQL trigger (`handle_new_user`) that auto-populates `profiles` and `user_settings` upon email registration.

### 4. Enable Email Authentication
1. Go to **Authentication -> Providers -> Email** in the Supabase Dashboard.
2. Enable Email provider and save.

---

## Building and Running the Application

1. Open the project folder in **Android Studio** (Koala / Ladybug or newer).
2. Sync Gradle files (`Sync Project with Gradle Files`).
3. Select an Android Emulator or connected physical device running **Android 8.0 (API 26)** or higher.
4. Click **Run 'app'**.

---

## RLS Security Verification

All application tables enforce strict Row Level Security policies:
- `auth.uid() = user_id` (or `id` for profiles) for `SELECT`, `INSERT`, `UPDATE`, and `DELETE`.
- Milestone records check user authorization through goal ownership joins.
- Anonymous clients cannot view or mutate other users' records.
