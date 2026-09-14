# Sample JSON Payloads

Copy-paste ready requests and responses for all Day 3+ endpoints. Uses seed data GUIDs so you can run these against a fresh database.

---

## Seed Data Reference

| Entity | GUID | Name |
|--------|------|------|
| Restaurant | `a1b2c3d4-0001-4000-8000-000000000001` | Meghana Foods |
| Restaurant | `a1b2c3d4-0002-4000-8000-000000000002` | Truffles |
| Restaurant | `a1b2c3d4-0003-4000-8000-000000000003` | Vidyarthi Bhavan |
| Menu Item | `b1b2c3d4-0001-4000-8000-000000000001` | Chicken Biryani (₹299) |
| Menu Item | `b1b2c3d4-0002-4000-8000-000000000002` | Mutton Biryani (₹399) |
| Menu Item | `b1b2c3d4-0003-4000-8000-000000000003` | Paneer Butter Masala (₹249) |
| Menu Item | `b1b2c3d4-0004-4000-8000-000000000004` | Veg Biryani (₹199) |
| Menu Item | `b1b2c3d4-0005-4000-8000-000000000005` | Masala Dosa (₹149) |
| Menu Item | `b1b2c3d4-0006-4000-8000-000000000006` | Gulab Jamun (₹99) |
| Menu Item | `b1b2c3d4-0007-4000-8000-000000000007` | Classic Smash Burger (₹349) |
| Menu Item | `b1b2c3d4-0008-4000-8000-000000000008` | Truffle Fries (₹199) |
| Menu Item | `b1b2c3d4-0009-4000-8000-000000000009` | BBQ Chicken Burger (₹399) |
| Menu Item | `b1b2c3d4-0010-4000-8000-000000000010` | Chocolate Milkshake (₹179) |
| Menu Item | `b1b2c3d4-0010-4000-8000-000000000011` | Chicken Wings (₹279) |
| Menu Item | `b1b2c3d4-0010-4000-8000-000000000012` | Masala Dosa (₹89) |
| Menu Item | `b1b2c3d4-0010-4000-8000-000000000013` | Benne Masala Dosa (₹109) |
| Menu Item | `b1b2c3d4-0010-4000-8000-000000000014` | Idli Vada Combo (₹79) |
| Menu Item | `b1b2c3d4-0010-4000-8000-000000000015` | Kesari Bath (₹69) |
| Menu Item | `b1b2c3d4-0010-4000-8000-000000000016` | Filter Coffee (₹49) |

---

## Restaurant Endpoints

### GET /api/v1/restaurants

**Request:**
```
GET http://localhost:8080/api/v1/restaurants?page=1&pageSize=10
```

**Response (200):**
```json
{
  "items": [
    {
      "id": "a1b2c3d4-0001-4000-8000-000000000001",
      "name": "Meghana Foods",
      "address": {
        "line1": "75, 12th Main Road",
        "city": "Bangalore",
        "pincode": "560034"
      },
      "isActive": true,
      "avgPrepTimeMinutes": 25
    },
    {
      "id": "a1b2c3d4-0002-4000-8000-000000000002",
      "name": "Truffles",
      "address": {
        "line1": "93, 100 Feet Road",
        "city": "Bangalore",
        "pincode": "560038"
      },
      "isActive": true,
      "avgPrepTimeMinutes": 30
    },
    {
      "id": "a1b2c3d4-0003-4000-8000-000000000003",
      "name": "Vidyarthi Bhavan",
      "address": {
        "line1": "32, Gandhi Bazaar Main Road",
        "city": "Bangalore",
        "pincode": "560004"
      },
      "isActive": true,
      "avgPrepTimeMinutes": 20
    }
  ],
  "page": 1,
  "pageSize": 10,
  "totalCount": 3,
  "totalPages": 1
}
```

### GET /api/v1/restaurants?city=Bangalore

Same shape, filtered by city.

---

### GET /api/v1/restaurants/{restaurantId}

**Request:**
```
GET http://localhost:8080/api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001
```

**Response (200):**
```json
{
  "id": "a1b2c3d4-0001-4000-8000-000000000001",
  "name": "Meghana Foods",
  "address": {
    "line1": "75, 12th Main Road",
    "city": "Bangalore",
    "pincode": "560034"
  },
  "isActive": true,
  "avgPrepTimeMinutes": 25
}
```

---

### POST /api/v1/restaurants

**Request:**
```
POST http://localhost:8080/api/v1/restaurants
Content-Type: application/json

{
  "name": "MTR",
  "address": {
    "line1": "14, Lalbagh Road",
    "line2": "Near Lalbagh Main Gate",
    "city": "Bangalore",
    "pincode": "560027",
    "latitude": 12.9490,
    "longitude": 77.5847
  },
  "avgPrepTimeMinutes": 35
}
```

**Response (201):**
```json
{
  "id": "newly-generated-uuid",
  "name": "MTR",
  "address": {
    "line1": "14, Lalbagh Road",
    "city": "Bangalore",
    "pincode": "560027"
  },
  "isActive": true,
  "avgPrepTimeMinutes": 35
}
```

---

### GET /api/v1/restaurants/{restaurantId}/menu

**Request:**
```
GET http://localhost:8080/api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001/menu
```

**Response (200):**
```json
[
  {
    "id": "b1b2c3d4-0001-4000-8000-000000000001",
    "name": "Chicken Biryani",
    "description": "Hyderabadi style dum biryani with tender chicken pieces",
    "price": { "amount": 299.00, "currency": "INR" },
    "category": "Main Course",
    "isAvailable": true,
    "isVeg": false
  },
  {
    "id": "b1b2c3d4-0002-4000-8000-000000000002",
    "name": "Mutton Biryani",
    "description": "Slow-cooked mutton biryani with aromatic spices",
    "price": { "amount": 399.00, "currency": "INR" },
    "category": "Main Course",
    "isAvailable": true,
    "isVeg": false
  },
  {
    "id": "b1b2c3d4-0003-4000-8000-000000000003",
    "name": "Paneer Butter Masala",
    "description": "Cottage cheese in rich tomato gravy",
    "price": { "amount": 249.00, "currency": "INR" },
    "category": "Main Course",
    "isAvailable": true,
    "isVeg": true
  }
]
```

### Filtered: Veg only

```
GET http://localhost:8080/api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001/menu?vegOnly=true
```

Returns only items where `isVeg: true`.

---

### PATCH /api/v1/restaurants/{restaurantId}/menu/{menuItemId}/availability

**Request:**
```
PATCH http://localhost:8080/api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001/menu/b1b2c3d4-0001-4000-8000-000000000001/availability
Content-Type: application/json

{
  "isAvailable": false
}
```

**Response: 204 No Content** (empty body)

---

## Order Endpoints

### POST /api/v1/orders — Create order

**Request:**
```
POST http://localhost:8080/api/v1/orders
Content-Type: application/json
Idempotency-Key: abc123

{
  "customerId": "c1b2c3d4-0001-4000-8000-000000000001",
  "restaurantId": "a1b2c3d4-0001-4000-8000-000000000001",
  "items": [
    {
      "menuItemId": "b1b2c3d4-0001-4000-8000-000000000001",
      "quantity": 2,
      "specialInstructions": "Extra spicy"
    },
    {
      "menuItemId": "b1b2c3d4-0003-4000-8000-000000000003",
      "quantity": 1
    }
  ],
  "deliveryAddress": {
    "line1": "302, Prestige Lakeside Habitat",
    "line2": "Whitefield",
    "city": "Bangalore",
    "pincode": "560066",
    "latitude": 12.9698,
    "longitude": 77.7500
  }
}
```

**Response (201):**
```json
{
  "id": "newly-generated-uuid",
  "customerId": "c1b2c3d4-0001-4000-8000-000000000001",
  "restaurantId": "a1b2c3d4-0001-4000-8000-000000000001",
  "status": "Created",
  "items": [
    {
      "id": "generated-uuid-1",
      "menuItemId": "b1b2c3d4-0001-4000-8000-000000000001",
      "name": "Chicken Biryani",
      "quantity": 2,
      "unitPrice": { "amount": 299.00, "currency": "INR" }
    },
    {
      "id": "generated-uuid-2",
      "menuItemId": "b1b2c3d4-0003-4000-8000-000000000003",
      "name": "Paneer Butter Masala",
      "quantity": 1,
      "unitPrice": { "amount": 249.00, "currency": "INR" }
    }
  ],
  "totalAmount": { "amount": 847.00, "currency": "INR" },
  "deliveryAddress": {
    "line1": "302, Prestige Lakeside Habitat",
    "line2": "Whitefield",
    "city": "Bangalore",
    "pincode": "560066"
  },
  "createdAt": "2025-01-15T13:30:00Z"
}
```

Total breakdown: 2 × ₹299 (Chicken Biryani) + 1 × ₹249 (Paneer Butter Masala) = ₹847

---

### GET /api/v1/orders/{orderId}

**Request:**
```
GET http://localhost:8080/api/v1/orders/{orderId-from-create}
```

**Response (200):** Same shape as POST response above.

---

### GET /api/v1/orders — List orders

**Request:**
```
GET http://localhost:8080/api/v1/orders?customerId=c1b2c3d4-0001-4000-8000-000000000001&page=1&pageSize=10
```

**Response (200):**
```json
{
  "items": [
    {
      "id": "the-order-id",
      "customerId": "c1b2c3d4-0001-4000-8000-000000000001",
      "restaurantId": "a1b2c3d4-0001-4000-8000-000000000001",
      "status": "Created",
      "items": [],
      "totalAmount": { "amount": 847.00, "currency": "INR" },
      "deliveryAddress": {
        "line1": "302, Prestige Lakeside Habitat",
        "city": "Bangalore",
        "pincode": "560066"
      },
      "createdAt": "2025-01-15T13:30:00Z"
    }
  ],
  "page": 1,
  "pageSize": 10,
  "totalCount": 1,
  "totalPages": 1
}
```

---

### PATCH /api/v1/orders/{orderId}/status — Update status

**Request (happy path):**
```
PATCH http://localhost:8080/api/v1/orders/{orderId}/status
Content-Type: application/json

{
  "status": "Confirmed"
}
```

**Response: 204 No Content**

**Request (invalid transition):**
```
PATCH http://localhost:8080/api/v1/orders/{orderId}/status
Content-Type: application/json

{
  "status": "Confirmed"
}
```

(Sending "Confirmed" again on an already-confirmed order)

**Response (422):**
```json
{
  "type": "https://tools.ietf.org/html/rfc7807",
  "title": "Domain Rule Violation",
  "status": 422,
  "detail": "Cannot transition order from 'Confirmed' to 'Confirmed'."
}
```

---

### POST /api/v1/orders/{orderId}/cancel — Cancel order

**Request:**
```
POST http://localhost:8080/api/v1/orders/{orderId}/cancel
Content-Type: application/json

{
  "reason": "Changed my mind, ordering from somewhere else"
}
```

**Response: 204 No Content**

**Request (order already delivered):**

**Response (422):**
```json
{
  "type": "https://tools.ietf.org/html/rfc7807",
  "title": "Domain Rule Violation",
  "status": 422,
  "detail": "Cannot cancel order in status 'Delivered'. Only orders in 'Created' or 'Confirmed' status can be cancelled."
}
```

---

### POST /api/v1/orders — Idempotency demo

**Request (first call, key "abc123"):**
```
POST http://localhost:8080/api/v1/orders
Content-Type: application/json
Idempotency-Key: abc123

{ "customerId": "...", "restaurantId": "...", "items": [...] }
```

**Response (201):** `{ "id": "...", ... }` — order created.

**Request (retry, same key "abc123"):**
```
POST http://localhost:8080/api/v1/orders
Content-Type: application/json
Idempotency-Key: abc123

{ "customerId": "...", "restaurantId": "...", "items": [...] }
```

**Response (200):** `{ "id": "<same-order-id>", ... }` — no duplicate.

---

## Validation Error Example

**Request (missing items, bad pincode):**
```
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "customerId": "c1b2c3d4-0001-4000-8000-000000000001",
  "restaurantId": "a1b2c3d4-0001-4000-8000-000000000001",
  "items": [],
  "deliveryAddress": {
    "line1": "302, Prestige Lakeside Habitat",
    "city": "Bangalore",
    "pincode": "56006"
  }
}
```

**Response (400):**
```json
{
  "type": "https://tools.ietf.org/html/rfc7807",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more validation errors occurred.",
  "errors": {
    "Items": ["Items must not be empty."],
    "DeliveryAddress.Pincode": ["Pincode must be exactly 6 digits."]
  }
}
```

## 409 Conflict Example (Optimistic Concurrency)

Two requests read the same order. First commits, second fails.

**Request (lost race):**
```
PATCH http://localhost:8080/api/v1/orders/{orderId}/status
Content-Type: application/json

{ "status": "Confirmed" }
```

**Response (409):**
```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.10",
  "title": "Concurrent Update Conflict",
  "status": 409,
  "detail": "This order was modified by another request. Reload the order and try again."
}
```
