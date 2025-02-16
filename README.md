**PharmacIST - Mobile and Ubiquitous Computing Project (IST 2023/24)**

This repository contains the implementation of the PharmacIST project, developed for the Mobile and Ubiquitous Computing course at Instituto Superior Técnico (IST). PharmacIST is an Android-based mobile application designed to help users locate pharmacies, search for medicines, and manage pharmacy stocks efficiently. The app integrates key mobile development principles, including location awareness, network efficiency, and real-time synchronization.

## **Key Features**
### **Mandatory Features (75%)**
- **User Authentication & Guest Login**: Users can register, log in, or explore the app as guests. Guest users can later upgrade their accounts without losing their preferences.
- **Pharmacy Management**: Users can add new pharmacies by providing a name, selecting a location from a map, or using their current GPS position.
- **Medicine Search & Reservation**: Users can search for medicines by name and find the nearest pharmacies that stock them. A reservation system allows users to secure medicines for later pickup.
- **Map Integration**: The app displays a map with pharmacy locations, highlights favorites, and provides navigation options using Google Maps.
- **Pharmacy & Medicine Information Panels**:
  - Display pharmacy details, including name, location, and available medicines.
  - Show medicine details, including purpose, stock levels, and availability at different pharmacies.
  - Allow users to receive notifications when a medicine becomes available in a favorite pharmacy.
- **Stock Management**: Pharmacies can update their stock by scanning barcodes or manually adding medicines.
- **Caching & Offline Support**: The app caches pharmacy and medicine data to minimize network usage and provide offline access.
- **Back-End Service**: PharmacIST synchronizes pharmacy and medicine data across devices using a lightweight server.

## **Additional Features (25%+)**
- **Securing Communication (5%)**: All data exchanges between the app and the server use SSL encryption to protect user information.
- **User Ratings (10%)**: Users can rate pharmacies and submit reviews, helping others choose the best locations.
- **Meta Moderation (10%)**: Users can flag suspicious pharmacies, and flagged pharmacies are automatically hidden after reaching a threshold.
- **Localization (5%)**: The app supports multiple languages (English & Portuguese) and allows users to switch languages dynamically.
- **UI Adaptability (10%)**:
  - Dark mode support for better visibility in low-light conditions.
  - Adaptive layouts for smooth transitions between portrait and landscape modes.
- **Social Sharing (5%)**: Users can share pharmacy and medicine information via messaging and social media apps.
- **Recommendations (10%)**: A recommendation engine suggests new medicines based on user purchase history.

## **Installation & Setup**
To set up the PharmacIST application on an Android device:

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/pharmacist.git
   cd pharmacist
   ```

2. **Install Dependencies**
   - Install Android Studio.
   - Import the project and sync dependencies.
   - Enable Google Maps API for location services.

3. **Run the Application**
   - Build and install the app using an Android emulator or a physical device.

## **Results Summary**
- **Best performing features**:
  - Fast and accurate pharmacy search and medicine lookup.
  - Seamless real-time updates for medicine availability.
  - Secure and private data handling through encrypted communication.
- **Challenges & Future Improvements**:
  - Improved real-time synchronization strategies to enhance network efficiency.
  - Enhanced UI design for better usability and accessibility.

## **Contributors**
- **Francisco Gil Mata**
- **Gonçalo Aroeira Gonçalves**
- **Marta Marques Félix**

