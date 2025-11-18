## MySQL Database Design

This section defines the main relational tables for the Smart Clinic system.  
Each table includes columns, data types, primary and foreign keys, and recommended constraints.

---

### Table: patients
- `id` INT PRIMARY KEY AUTO_INCREMENT
- `first_name` VARCHAR(100) NOT NULL
- `last_name` VARCHAR(100) NOT NULL
- `email` VARCHAR(255) NOT NULL UNIQUE
- `phone` VARCHAR(30) NULL
- `date_of_birth` DATE NULL
- `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
- `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
- `is_active` TINYINT(1) NOT NULL DEFAULT 1  -- soft-delete flag

**Notes**
- `email` must be unique to prevent duplicate accounts.
- Soft-delete (`is_active`) keeps appointment and prescription history intact.

---

### Table: doctors
- `id` INT PRIMARY KEY AUTO_INCREMENT
- `first_name` VARCHAR(100) NOT NULL
- `last_name` VARCHAR(100) NOT NULL
- `email` VARCHAR(255) NOT NULL UNIQUE
- `phone` VARCHAR(30) NULL
- `specialty` VARCHAR(100) NULL
- `is_active` TINYINT(1) NOT NULL DEFAULT 1
- `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
- `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

**Notes**
- Email must be unique. `is_active` allows disabling a doctor without deleting historical data.
- Availability is managed in a separate table (`doctor_availability`).

---

### Table: appointments
- `id` INT PRIMARY KEY AUTO_INCREMENT
- `doctor_id` INT NOT NULL  -- FK → doctors(id)
- `patient_id` INT NOT NULL -- FK → patients(id)
- `start_time` DATETIME NOT NULL
- `end_time` DATETIME NOT NULL
- `status` TINYINT NOT NULL DEFAULT 0  -- 0=Scheduled,1=Completed,2=Cancelled,3=NoShow
- `notes` TEXT NULL
- `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
- `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

**Foreign keys**
- FOREIGN KEY (`doctor_id`) REFERENCES `doctors`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
- FOREIGN KEY (`patient_id`) REFERENCES `patients`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE

**Notes**
- Validate `start_time < end_time` at the application level.
- `ON DELETE RESTRICT` prevents accidental removal of historical appointments.

---

### Table: admin
- `id` INT PRIMARY KEY AUTO_INCREMENT
- `username` VARCHAR(100) NOT NULL UNIQUE
- `email` VARCHAR(255) NOT NULL UNIQUE
- `password_hash` VARCHAR(255) NOT NULL
- `role` VARCHAR(50) NOT NULL DEFAULT 'admin'
- `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
- `last_login` DATETIME NULL
- `is_active` TINYINT(1) NOT NULL DEFAULT 1

**Notes**
- Passwords must be hashed (bcrypt/argon2).
- `is_active` allows disabling accounts.

---

### Table: doctor_availability
- `id` INT PRIMARY KEY AUTO_INCREMENT
- `doctor_id` INT NOT NULL -- FK → doctors(id)
- `day_of_week` TINYINT NOT NULL -- 0=Sunday..6=Saturday
- `start_time` TIME NOT NULL
- `end_time` TIME NOT NULL
- `start_date` DATE NULL
- `end_date` DATE NULL
- `is_recurring` TINYINT(1) NOT NULL DEFAULT 1
- `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP

**Foreign keys**
- FOREIGN KEY (`doctor_id`) REFERENCES `doctors`(`id`) ON DELETE CASCADE

**Notes**
- Defines recurring work slots. Real appointment slots can be generated dynamically.

---

### Table: payments (optional)
- `id` INT PRIMARY KEY AUTO_INCREMENT
- `appointment_id` INT NOT NULL -- FK → appointments(id)
- `amount` DECIMAL(10,2) NOT NULL
- `currency` VARCHAR(3) NOT NULL DEFAULT 'EUR'
- `status` VARCHAR(20) NOT NULL DEFAULT 'pending' -- pending/completed/refunded
- `payment_method` VARCHAR(50) NULL
- `transaction_id` VARCHAR(255) NULL
- `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP

**Foreign keys**
- FOREIGN KEY (`appointment_id`) REFERENCES `appointments`(`id`) ON DELETE CASCADE

**Notes**
- Payments linked to appointments; delete cascade is safe here.

---

### Design Considerations
- Soft-delete for `patients` and `doctors` keeps historical data.
- Prevent overlapping appointments via Service Layer validation.
- Prescription can be linked to `appointment_id` or just `patient_id`.
- Retain historical appointments and prescriptions for medical record purposes.

---

## MongoDB Collection Design

Flexible data such as doctor notes, patient feedback, prescriptions, logs, and attachments are stored in MongoDB.

### Collection: prescriptions

```json
{
  "_id": { "$oid": "64abc1234567890abcdef0001" },
  "patientId": 123,
  "appointmentId": 456,
  "doctorId": 12,
  "issuedAt": "2025-11-17T09:30:00Z",
  "medications": [
    {
      "name": "Amoxicillin",
      "dose": "500mg",
      "frequency": "3 times a day",
      "duration_days": 7,
      "instructions": "Take with food"
    },
    {
      "name": "Ibuprofen",
      "dose": "200mg",
      "frequency": "as needed",
      "duration_days": 5,
      "instructions": "Max 1200mg/day"
    }
  ],
  "notes": "Patient reported allergy to penicillin; prescribe alternative if needed.",
  "pharmacy": {
    "name": "Central Pharmacy",
    "address": "10 Main Street, Milan"
  },
  "tags": ["urgent", "follow-up"],
  "attachments": [
    {
      "fileName": "lab-result-2025-11-10.pdf",
      "fileUrl": "/filestore/prescriptions/64abc1234_lab.pdf",
      "meta": { "size": 34567, "contentType": "application/pdf" }
    }
  ],
  "history": [
    { "changedBy": "doctor:12", "changedAt": "2025-11-17T10:00:00Z", "change": "Initial issue" }
  ]
}
