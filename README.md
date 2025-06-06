Bitequest
 
Bitequest is a modern Android app that redefines food discovery and ordering. Powered by Firebase for real-time data management and Glide for fast image loading, it delivers a seamless experience for browsing menus, placing orders, and managing user profiles.

Key Features

Secure sign-in with Firebase Authentication (Email, Google).
Visually rich menu browsing with Glide-powered image loading.
Real-time menu and order updates via Firebase Firestore.
Easy order placement and tracking.
Customizable user profiles.

Tech Stack

Frontend: Android (Kotlin)
Backend: Firebase (Authentication, Firestore, Storage)
Image Loading: Glide

Setup

Clone the repository:git clone https://github.com/alaafawzyy/Bitequest.git


Open in Android Studio.
Add your Firebase google-services.json to the app/ directory.
Build and run the app.

Usage

Sign in with email or Google.
Browse the menu with fast-loading images.
Place orders and track them in real time.
Manage your profile and order history.

Flowchart
The diagram below outlines the Bitequest user journey:
graph TD
    A[Open App] --> B{User Login}
    B -->|Success| C[View Menu]
    B -->|Failure| D[Show Login Error]
    C --> E[Select Items]
    E --> F[Place Order]
    F --> G[Track Order Status]
    G --> H[Order Delivered]

Contributing
Contributions are welcome! Fork the repo, create a feature branch, and submit a pull request. See CONTRIBUTING.md for details.
License
Licensed under the MIT License.
