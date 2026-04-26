# PvZOnline 🧟‍♂️🌻

PvZOnline is a real-time multiplayer Android game inspired by the classic "Plants vs. Zombies". Developed in Android Studio using Kotlin, the game features both a Solo mode and an Online Multiplayer mode where players can battle it out in real-time using Firebase Firestore for synchronization.

## ✨ Features

* **Online Multiplayer Mode (2 players):** Create a room or join a friend's room using a secret 6-character code. The game board, plants, zombies, and suns are synchronized in real-time between the Host and the Guest.
* **Solo Mode:** Play locally with automatically spawning zombies and increasing difficulty.
* **User Authentication:** Secure registration and login system powered by Firebase Authentication.
* **Custom Game Loop:** The game utilizes background threads (Runnables/Coroutines) to calculate zombie movements, attacks, and hitboxes independently of standard Android animations.
* **User Progression:** The game tracks your wins, calculates XP, and levels up your profile, saving your progress in the cloud.

## 🛠️ Technologies Used

* **Language:** Kotlin
* **Environment:** Android Studio
* **Backend & Database:** Firebase Firestore (Real-time synchronization & NoSQL Data Storage)
* **Authentication:** Firebase Auth
* **Architecture:** Host-Client Authority model to prevent multiplayer conflicts.

## 🚀 How to Run

1. Clone this repository to your local machine.
2. Open the project in Android Studio.
3. Connect the project to your own Firebase console and add your `google-services.json` file to the `app` directory.
4. Build and run the project on an Android Emulator or a physical Android device.

<img width="1920" height="1080" alt="Plants_pic01" src="https://github.com/user-attachments/assets/d0f70db9-bc10-4804-93a5-32c5b827c504" />
