# Mental Health and Well-being Application

## Project Overview

This Android application is developed as part of the Mobile Application Development (MAD) curriculum. It serves as a holistic mental health companion designed to bridge the gap between users and mental wellness resources. The application provides a comprehensive platform for users to track their emotional well-being, access professional counselling services, engage with a supportive community, and utilise self-help tools for relaxation.

## Key Features

The application allows users to navigate through several dedicated modules, each serving a specific aspect of mental wellness:

### 1. Dashboard and Mood Tracking
* **Daily Check-in:** Users can log their daily emotional state using an intuitive interface.
* **Mood Analytics:** The application utilises the MPAndroidChart library to visualise mood trends over time, allowing users to monitor their emotional patterns.
* **Dynamic Interface:** The home screen provides personalised greetings based on the time of day and displays daily motivational tips.

### 2. Professional Counselling and Booking
* **Counsellor Directory:** Users can browse profiles of professional counsellors to view their specialisations and locations.
* **Appointment Booking:** A custom booking system calculates and displays real-time availability (e.g., specific dates and time slots).
* **Real-time Chat:** Integrated with Firebase Firestore, this feature enables secure, real-time messaging between users and counsellors or support groups.

### 3. Self-Care and Relaxation Tools
* **Breathing Exercises:** An interactive module featuring visual animation guides to assist users in performing rhythmic breathing for stress reduction.
* **Music Therapy:** A built-in audio player offering a curated selection of relaxation and Lo-Fi music tracks to aid in meditation and focus.

### 4. Community and Social Engagement
* **Community Feed:** A social platform where users can share their thoughts and experiences.
* **User Interaction:** Supports interactive features such as liking and commenting on posts to foster a supportive peer environment.
* **Profile Management:** Users can customise their profiles, including avatars and personal details.

### 5. Educational Workshops
* **Event Listing:** A dedicated section for viewing upcoming mental health workshops and events.
* **Registration System:** Streamlined functionality for users to register for events directly within the application.

## Technical Architecture

* **Development Environment:** Android Studio
* **Programming Language:** Java
* **Minimum SDK:** API 24 (Android 7.0 Nougat)
* **Target SDK:** API 36

### Backend and Cloud Services (Firebase)
* **Firebase Authentication:** Secure user registration and login management.
* **Cloud Firestore:** NoSQL database for real-time storage of user data, chat messages, posts, and appointments.

### Key Libraries
* **MPAndroidChart:** For data visualisation and graphing.
* **Glide:** For efficient image loading and caching.
* **Material Design Components:** For implementing modern UI/UX standards.

## Installation and Execution Guide

To set up and run this project locally, please follow the steps below.

### Prerequisites
1.  **Java Development Kit (JDK):** Ensure a compatible JDK is installed.
    * [Download JDK](https://www.oracle.com/asean/java/technologies/downloads/#jdk25-linux)
3.  **Android Studio:** Download and install the latest version of Android Studio from the official website:
    * [Download Android Studio](https://developer.android.com/studio)

### Setup Instructions

1.  **Clone the Repository**
    Clone the project to your local machine using Git.
    ```bash
    git clone https://github.com/Kejunfang/MAD_GP.git
    ```

2.  **Open the Project**
    * Launch Android Studio.
    * Select **Open** and navigate to the directory where you cloned the repository.

3.  **Sync Gradle**
    * Allow Android Studio to download the necessary dependencies.
    * If prompted, click **Sync Now** in the notification bar.

4.  **Firebase Configuration**
    * This project requires a `google-services.json` file to connect to the Firebase backend.
    * *Note:* If this file is not included in the repository for security reasons, you must generate it via the Firebase Console (ensure the package name matches `com.example.mad_gp`) and place it in the `app/` directory.

### Running the Application

You can run the application using either a physical Android device or the Android Emulator (Virtual Device).

**Option A: Using an Android Virtual Device (Emulator)**
1.  In Android Studio, open the **Device Manager**.
2.  Click **Create Device** to set up a new virtual device (e.g., Pixel 6) if one does not exist.
3.  Select a system image (Android 10.0+ is recommended) and finish the setup.
4.  Select the virtual device from the run target dropdown menu in the toolbar.
5.  Click the **Run** button (green arrow) to deploy the app to the emulator.

**Option B: Using a Physical Device via USB**
1.  Enable **Developer Options** and **USB Debugging** on your Android device.
    * [How to open Developer Options and USB Debugging](https://developer.android.com/studio/debug/dev-options)
3.  Connect the device to your computer via a USB cable.
4.  Select your device from the run target dropdown menu in Android Studio.
5.  Click the **Run** button to deploy the app.

## User Guide

* **Account Access:** New users must register an account to access the application's features. Existing users may log in using their credentials.
* **Navigation:** The bottom navigation bar provides access to the Home, Event, Community, and Profile sections.
* **Logging Mood:** On the Home screen, select the icon that best represents your current emotion to update your mood chart.
* **Booking:** Navigate to the Counsellor list, select a professional, and choose a date and time slot to book an appointment.

## License

This project is created for educational purposes as part of the Mobile Application Development assignment. All rights reserved.
