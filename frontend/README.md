# Silk-Road — Frontend (JavaFX Client)

## Project Overview

Silk-Road is a second-hand marketplace application built as a project for the Advanced
Programming course. Users can post ads, search and browse other users' ads, chat with
sellers, add ads to their favorites, and rate sellers. There is also an admin panel for
reviewing and approving/rejecting ads.

This part of the repository (`frontend/`) is the desktop client, written in **JavaFX**,
which communicates with the backend (Spring Boot) over HTTP/JSON. This part was developed
by **Fardad Shaghaghi**.

## Team Members

| Name | Role |
|---|---|
| Fardad Shaghaghi | Frontend (JavaFX) |
| Sajjad Heydari | Backend (Spring Boot) |

## Technologies Used

- Java 21
- JavaFX 21 (UI) + SceneBuilder for FXML design
- Maven for build and dependency management
- Java `HttpClient` for communicating with the backend REST API
- Gson for JSON serialization/deserialization
- JWT-based authentication (token sent via the `Authorization: Bearer` header)

## Prerequisites

- JDK 21
- Maven 3.9+
- The backend (`backend/market`) is running on port `8080`, since the
  frontend has no data of its own and every screen pulls data from the backend.

## How to Run the Frontend

```bash
cd frontend
mvn clean javafx:run
```

⚠️ Note: always use the command above (from inside the `frontend` folder), not
IntelliJ's green Run button — running it that way causes a
`JavaFX runtime components are missing` error.

The backend address is hardcoded to `http://localhost:8080` (`ApiClient.java`), so if the
backend runs on a different port, that value must be changed manually in that file.

## How Storage Works

The frontend itself has no database or local file, and it doesn't persist anything to
disk between runs; all data (users, ads, categories, chats, ratings) is stored and
managed by the backend.

- During a single run of the app, only the **logged-in user's JWT token** is kept in
  memory (in the `Session.java` class) — not in a file, not in a local database.
  Closing the app discards this token, and the user has to log in again.
- The actual data storage happens on the backend side, using **SQLite**
  (the `project_db.sqlite` file) through Hibernate/JPA. This file is created/updated
  automatically (via `ddl-auto=update`) when the backend runs, and is intentionally
  listed in `.gitignore` so it isn't tracked in Git (see `backend/README.md` for
  more detail).

## Test Accounts

The frontend doesn't ship with any pre-built accounts; accounts have to be created
through the Sign Up screen:

- **Regular user:** create a new account from the Sign Up screen (the default role is `USER`).
- **Admin:** there is currently no endpoint or screen to promote a user to Admin.
  To test the admin panel, after creating an account through Sign Up you have to
  manually change that user's role to `ADMIN` directly in the database:
```sql
  UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```
(using a tool like DB Browser for SQLite on the `project_db.sqlite` file)

## Implemented Features

### Authentication
Sign up and login with backend-side validation, and storing the received JWT token.

> ![img.png](../docs/images/img.png)

> ![img_1.png](../docs/images/img_1.png)

### Browsing and Searching Ads (Home)
Displays approved ads in a grid, with keyword search and sorting.

> ![img_12.png](../docs/images/img_12.png)

### Creating an Ad
Form for creating a new ad, with category (and subcategory), city, price, description, and multiple image uploads.

> ![img_3.png](../docs/images/img_3.png)

### Editing an Ad
Lets users edit their own ads.

> ![img_4.png](../docs/images/img_4.png)

### Ad Details
Full view of a single ad, including images, seller info, seller rating, and buttons to start a chat / add to favorites.

>![img_5.png](../docs/images/img_5.png)

### My Ads
List of ads posted by the logged-in user, with status (Pending/Approved/Rejected/Sold).

> ![img_6.png](../docs/images/img_6.png)

### Favorites
List of ads the user has added to favorites.

> ![img_7.png](../docs/images/img_7.png)

### Chat with Seller
List of conversations, and a chat screen for sending/receiving messages between buyer and seller.

> ![img_8.png](../docs/images/img_8.png)

> ![img_9.png](../docs/images/img_9.png)

### Rating a Seller
Lets a user leave a rating and comment for a seller after a deal, and shows those ratings on the seller's profile/ad details.

> ![img_10.png](../docs/images/img_10.png)

### Admin Panel
Lets an admin review pending ads and approve/reject them.

>![img_13.png](../docs/images/img_13.png)