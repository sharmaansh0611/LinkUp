# LinkUp

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Screenshots](#screenshots)
- [Installation](#installation)
- [Usage](#usage)
- [Firebase Integration](#firebase-integration)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Introduction

**LinkUp** is a modern and interactive mobile application built in Kotlin, integrated with Firebase for real-time data management and storage. The app allows users to share photos, interact with posts, manage their profiles, and explore short video reels. With a responsive and intuitive user interface, LinkUp ensures smooth social engagement.

## Features

- 📸 **Post Creation**  
  Users can upload photos with captions, which are stored in Firebase Storage and displayed in a real-time feed.

- 🎥 **Reel Feature**  
  Users can upload short videos that are showcased in the reels section for interactive content browsing.

- 🔄 **Real-Time Feed**  
  Posts are updated instantly using Firestore, allowing seamless interaction with other users’ posts.

- ❤️ **Likes & Comments**  
  Users can like posts and leave comments, updated in real time.

- 👤 **User Profiles**  
  Users can create and edit their profiles, including changing profile pictures and updating bios.

- ➕ **Follow System**  
  Users can follow/unfollow others to customize their feeds.

- 🖼️ **Full-Screen Post View**  
  Users can view posts in full-screen mode by tapping on the post image.

- 📤 **Share Post**  
  Users can share posts to other platforms or directly with friends.

- 🗑️ **Delete Post**  
  Users can delete their own posts from their feed or profile.

- 📱 **Responsive Design**  
  The UI is designed for a smooth and intuitive experience across various screen sizes.

## Screenshots

Here are some screenshots of the **LinkUp** app:

|    |  |  |
|:---------:|:--------------:|:-------------:|
| ![Home Feed](./pic1.png) | ![Search](./pic2.png) | ![Follow List](./pic4.png) |

| |   |  |
|:------------:|:----------------:|:------------:|
| ![Schedule Post](./pic4.png) | ![Reel](./Screenshot_20240907_015104.png) | ![Delete And Share Post](./pic6.png) |

## Installation

Step-by-step instructions to set up the **LinkUp** app:

1. **Clone the repository:**

    ```bash
    git clone https://github.com/Aadarsh45/LinkUp.git
    ```

2. **Open the project in Android Studio.**

3. **Build the project:**
    - Sync Gradle by clicking on "Sync Now" in the top right corner if prompted.

4. **Run the application:**
    - Connect your Android device or start an emulator.
    - Click on the "Run" button or use the shortcut `Shift + F10`.

## Usage

Instructions for using the app:

1. Open the **LinkUp** app on your device.
2. Create and share posts with photos or short videos.
3. Interact with posts by liking or commenting.
4. View posts in full-screen mode by tapping on the post image.
5. Share posts with others or delete your own posts.
6. Explore the reel feature for short video content.
7. Edit your profile, follow other users, and customize your feed.

## Firebase Integration

### **Firebase Authentication**

```kotlin
// Firebase Authentication for User Login
val auth = FirebaseAuth.getInstance()

fun loginUser(email: String, password: String) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Login", "Login successful")
            } else {
                Log.d("Login", "Login failed: ${task.exception?.message}")
            }
        }
}
```
### Firebase Firestore Setup
```
// Firestore Database for Storing Posts
val db = FirebaseFirestore.getInstance()

fun addPost(post: Post) {
    db.collection("posts")
        .add(post)
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "Post added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding post", e)
        }
}

```
### Firebase Storage for Image Upload
```
// Uploading Image to Firebase Storage
val storageRef = FirebaseStorage.getInstance().reference.child("posts/${UUID.randomUUID()}.jpg")

fun uploadImage(fileUri: Uri) {
    storageRef.putFile(fileUri)
        .addOnSuccessListener {
            Log.d("Storage", "Image upload successful")
        }
        .addOnFailureListener { e ->
            Log.w("Storage", "Image upload failed", e)
        }
}

```



## Contributing

Contributions are welcome! Please follow these steps to contribute:

1. **Fork the repository.**

2. **Create a new branch:**

    ```bash
    git checkout -b feature-name
    ```

3. **Make your changes.**

4. **Commit your changes:**

    ```bash
    git commit -m 'Add some feature'
    ```

5. **Push to the branch:**

    ```bash
    git push origin feature-name
    ```

6. **Open a pull request.**

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For any questions or suggestions, feel free to reach out at [aadarshchaurasia45@gmail.com](mailto:aadarshchaurasia45@gmail.com).
