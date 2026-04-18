# 🎟 Ticket Booking System (Backend)

A Spring Boot-based backend service for a dynamic ticket booking system.
It manages seat availability, booking requests, and pricing logic based on booking order.

---

## 🚀 Features

* 🎯 Initialize event with 100 seats
* 🪑 View all seats with status (AVAILABLE / BOOKED)
* 📦 Book multiple seats in a single request
* 💰 Dynamic pricing based on booking order:

    * Seats 1–50 → $50
    * Seats 51–80 → $75
    * Seats 81–100 → $100
* ⚠️ Prevent booking of already booked seats
* 🗂 RESTful APIs

---

## 🛠 Tech Stack

* **Language:** Java
* **Framework:** Spring Boot
* **Database:** (MySQL)
* **ORM:** Hibernate / JPA

---

## ▶️ How to Run Locally

### 1️⃣ Clone the repository

```bash
https://github.com/arindamsamanta5507/dynamic-event-ticketing-system-backend.git
cd dynamic-event-ticketing-system-backend
```

---

### 2️⃣ Spin up the stack

```bash
docker-compose up -d --build
```

---

### 3️⃣ RVerify the logs

```bash
docker logs -f ticketing-app
```

---

### 4️⃣ Application will start on:

```text
http://localhost:8081
```

---

## 🔗 API Endpoints

### 🔹 Initialize Event

```http
POST /api/v1/tickets/initialize?eventName=Taylor Swift
```

**Response:**

```text
Successfully initialized event: 'Taylor Swift' with 100 available seats.
```

---

### 🔹 Get Seats

```http
GET /api/v1/tickets/seats?eventId=1
```

**Response:**

```json
[
  {
    "id": 1,
    "seatNumber": 1,
    "status": "BOOKED",
    "event": {
      "id": 50,
      "eventName": "Taylor Swift",
      "totalCapacity": 100,
      "ticketsSold": 21,
      "createdAt": "2026-04-18T08:38:41.132522",
      "updatedAt": "2026-04-18T10:04:50.209139",
      "hibernateLazyInitializer": {}
    }
  }
]
```

---

### 🔹 Book Seats

```http
POST /api/v1/tickets/book
```

**Request Body:**

```json
{
  "eventId": 1,
  "userName": "Arindam",
  "seats": [45, 46, 47]
}
```

**Response:**

```json
{
  "id": 14,
  "userName": "Arindam Samanta",
  "totalPrice": 100.00,
  "createdAt": "2026-04-18T10:29:36.675584"
}
```

---

## ⚠️ Important Notes

* Pricing is based on **order of booking**, not seat number
* If seats are already booked → API returns error
* Ensure CORS is enabled for frontend integration:

```java
@CrossOrigin(origins = "*") OR @CrossOrigin(origins = "http://localhost:5173")
```

---

## 🧪 Testing

You can test APIs using:

* Postman
* cURL

---

## 💡 Future Improvements

* Seat locking for concurrency handling
* Payment gateway integration
* User authentication
* Multiple event support
* Email notifications

---

## 👨‍💻 Author

Arindam Samanta
