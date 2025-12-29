# eGatepass - Android Gatepass Management Application

## Overview
eGatepass is a comprehensive Android application designed for managing gatepass requests in educational institutions or organizations. The app streamlines the process of requesting, approving, and tracking gatepass entries and exits.

## Features
- **User Authentication**: Secure login and registration system for students and administrators
- **Gatepass Requests**: Students can create and submit gatepass requests with details
- **QR Code Integration**: QR code generation and scanning for quick verification
- **Role-based Access**: Different interfaces for students and administrators
- **Real-time Notifications**: Firebase-powered push notifications for request updates
- **Request Tracking**: Comprehensive history and status tracking
- **Profile Management**: User profile viewing and editing capabilities

## Technology Stack
- **Platform**: Android (Java)
- **Backend**: Firebase (Authentication, Firestore, Cloud Messaging)
- **QR Code**: QR code generation and scanning functionality
- **Architecture**: Activity-based Android application with adapters for list management

## Project Structure
```
app/
├── java/
│   └── com/example/egatepass/
│       ├── activities/
│       │   ├── LoginActivity.java
│       │   ├── RegisterActivity.java
│       │   ├── HomeActivity.java
│       │   ├── CreateGatepassActivity.java
│       │   ├── MyRequestsActivity.java
│       │   ├── AdminHomeActivity.java
│       │   ├── AdminScanActivity.java
│       │   ├── HistoryActivity.java
│       │   ├── ProfileActivity.java
│       │   ├── RequestDetailActivity.java
│       │   └── StudentQrActivity.java
│       ├── adapters/
│       │   ├── RequestAdapter.java
│       │   └── AdminRequestAdapter.java
│       ├── models/
│       │   ├── RequestModel.java
│       │   └── GatepassRequest.java
│       ├── utils/
│       │   ├── QrUtil.java
│       │   ├── FirebaseHelper.java
│       │   ├── LocalNotificationHelper.java
│       │   └── RolePref.java
│       └── services/
│           └── MyFirebaseMessagingService.java
├── res/
│   ├── layout/
│   ├── drawable/
│   ├── values/
│   └── xml/
└── AndroidManifest.xml
```

## Key Components

### Activities
- **LoginActivity**: User authentication interface
- **RegisterActivity**: New user registration
- **HomeActivity**: Main dashboard for students
- **AdminHomeActivity**: Admin dashboard with request management
- **CreateGatepassActivity**: Interface for creating new gatepass requests
- **MyRequestsActivity**: View and track personal requests
- **AdminScanActivity**: QR code scanning for verification
- **HistoryActivity**: Request history view
- **ProfileActivity**: User profile management
- **RequestDetailActivity**: Detailed view of specific requests
- **StudentQrActivity**: QR code display for students

### Models
- **RequestModel**: Data structure for gatepass requests
- **GatepassRequest**: Extended request model with additional functionality

### Utilities
- **FirebaseHelper**: Firebase database operations
- **QrUtil**: QR code generation and scanning utilities
- **LocalNotificationHelper**: Local notification handling
- **RolePref**: User role preference management

## Setup Instructions

### Prerequisites
- Android Studio
- Android SDK
- Firebase account and project setup

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/DhangarRohit18/egatepass.git
   ```

2. Open the project in Android Studio

3. Add your `google-services.json` file to the `app/` directory

4. Sync the project with Gradle files

5. Build and run the application

## Firebase Configuration
This application requires Firebase setup for authentication and database functionality:
1. Create a Firebase project at https://console.firebase.google.com
2. Add an Android app to your Firebase project
3. Download the `google-services.json` file and place it in the `app/` directory
4. Enable Firebase Authentication and Firestore Database
5. Configure authentication methods (email/password)

## Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments
- Firebase for backend services
- Android SDK for application development
- Material Design components for UI elements