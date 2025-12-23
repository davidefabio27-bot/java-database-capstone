/*
Import the overlay function for booking appointments from loggedPatient.js

  Import the deleteDoctor API function to remove doctors (admin role) from docotrServices.js

  Import function to fetch patient details (used during booking) from patientServices.js

  Function to create and return a DOM element for a single doctor card
    Create the main container for the doctor card
    Retrieve the current user role from localStorage
    Create a div to hold doctor information
    Create and set the doctor’s name
    Create and set the doctor's specialization
    Create and set the doctor's email
    Create and list available appointment times
    Append all info elements to the doctor info container
    Create a container for card action buttons
    === ADMIN ROLE ACTIONS ===
      Create a delete button
      Add click handler for delete button
     Get the admin token from localStorage
        Call API to delete the doctor
        Show result and remove card if successful
      Add delete button to actions container
   
    === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
      Create a book now button
      Alert patient to log in before booking
      Add button to actions container
  
    === LOGGED-IN PATIENT ROLE ACTIONS === 
      Create a book now button
      Handle booking logic for logged-in patient   
        Redirect if token not available
        Fetch patient data with token
        Show booking overlay UI with doctor and patient info
      Add button to actions container
   
  Append doctor info and action buttons to the car
  Return the complete doctor card element
*/

// 1. Import necessary functions from other modules
import { showBookingOverlay } from "../pages/loggedPatient.js"; // For booking overlay
import { deleteDoctor } from "../services/doctorServices.js"; // Admin delete doctor
import { getPatientData } from "../services/patientServices.js"; // Fetch logged-in patient info

// 2. Define the function and export it
export function createDoctorCard(doctor) {
    // 3. Create the main card container
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    // 4. Fetch the user's role from localStorage
    const role = localStorage.getItem("userRole");

    // 5. Create a container for doctor information
    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    // 6. Create and set doctor details
    const name = document.createElement("h3");
    name.textContent = doctor.name;

    const specialization = document.createElement("p");
    specialization.textContent = `Specialty: ${doctor.specialty}`;

    const email = document.createElement("p");
    email.textContent = `Email: ${doctor.email}`;

    const availability = document.createElement("p");
    availability.textContent = `Availability: ${doctor.availability.join(", ")}`;

    // 7. Append all info elements to the info container
    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(availability);

    // 8. Create container for action buttons
    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    // 9. Admin role actions
    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";
        removeBtn.classList.add("adminBtn");

        removeBtn.addEventListener("click", async () => {
            const confirmDelete = confirm(`Are you sure you want to delete Dr. ${doctor.name}?`);
            if (!confirmDelete) return;

            const token = localStorage.getItem("token");
            const success = await deleteDoctor(doctor.id, token); // call API

            if (success) {
                alert("Doctor deleted successfully");
                card.remove(); // Remove card from DOM
            } else {
                alert("Failed to delete doctor. Try again.");
            }
        });

        actionsDiv.appendChild(removeBtn);
    }
    // 10. Doctor logged-in role (nuova parte corretta)
    else if (role === "doctor") {
        const dashboardBtn = document.createElement("button");
        dashboardBtn.textContent = "View Appointments";
        dashboardBtn.addEventListener("click", () => {
            window.location.href = "/doctorDashboard.html";
        });
        actionsDiv.appendChild(dashboardBtn);
    }
    // 11. Patient not logged-in
    else if (role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.addEventListener("click", () => {
            alert("Please login to book an appointment.");
        });
        actionsDiv.appendChild(bookNow);
    }
    // 12. Logged-in Patient
    else if (role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";

        bookNow.addEventListener("click", async (e) => {
            const token = localStorage.getItem("token");
            if (!token) {
                alert("Session expired. Please login again.");
                window.location.href = "/"; 
                return;
            }
            const patientData = await getPatientData(token);
            showBookingOverlay(e, doctor, patientData);
        });

        actionsDiv.appendChild(bookNow);
    }

    // 13. Final assembly
    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    // 14. Return the complete doctor card element
    return card;
}