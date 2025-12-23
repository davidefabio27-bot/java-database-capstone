import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

// ----------------------------
// DEFINIZIONE ENDPOINT
// ----------------------------
const ADMIN_API = API_BASE_URL + "/admin/login";
const DOCTOR_API = API_BASE_URL + "/doctor/login";
const PATIENT_API = API_BASE_URL + "/patient/login";

// ----------------------------
// CARICAMENTO PAGINA
// ----------------------------
window.onload = function () {
    const adminBtn = document.getElementById("adminLogin");
    const doctorBtn = document.getElementById("doctorLogin");
    const patientBtn = document.getElementById("patientLogin");

    if (adminBtn) adminBtn.addEventListener("click", () => openModal("adminLogin"));
    if (doctorBtn) doctorBtn.addEventListener("click", () => openModal("doctorLogin"));
    if (patientBtn) patientBtn.addEventListener("click", () => openModal("patientLogin"));
};

// ----------------------------
// FUNZIONI LOGIN
// ----------------------------
window.adminLoginHandler = async function (event) {
    if (event) event.preventDefault(); // blocca submit implicito

    const emailInput = document.getElementById("adminEmail");
    const passwordInput = document.getElementById("adminPassword");

    if (!emailInput || !passwordInput) {
        alert("Form not loaded yet!");
        return;
    }

    const admin = { email: emailInput.value, password: passwordInput.value };

    try {
        const response = await fetch(ADMIN_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(admin),
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("token", data.token);

            // Chiudi modal prima dell’alert
            const modal = document.getElementById('modal');
            if (modal) modal.style.display = 'none';

            alert("Admin logged in!");
            // Redirect Admin Dashboard
            window.location.href = "/admin/dashboard";
        } else {
            alert("Invalid credentials!");
        }
    } catch (err) {
        console.error(err);
        alert("Something went wrong. Try again!");
    }
};

window.doctorLoginHandler = async function (event) {
    if (event) event.preventDefault(); // blocca submit implicito

    const emailInput = document.getElementById("doctorEmail");
    const passwordInput = document.getElementById("doctorPassword");

    if (!emailInput || !passwordInput) {
        alert("Form not loaded yet!");
        return;
    }

    const doctor = { email: emailInput.value, password: passwordInput.value };

    try {
        const response = await fetch(DOCTOR_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(doctor),
        });

        if (response.ok) {
            const data = await response.json();

            // SALVA TOKEN E RUOLO
            localStorage.setItem("token", data.token);
            localStorage.setItem("userRole", "doctor");

            // Chiudi modal se presente
            const modal = document.getElementById('modal');
            if (modal) modal.style.display = 'none';

            // Redirect al controller Thymeleaf
            window.location.href = "/doctor/dashboard"; 
        } else {
            alert("Invalid credentials!");
        }
    } catch (err) {
        console.error(err);
        alert("Something went wrong! Try again.");
    }
};

window.patientLoginHandler = async function (event) {
    if (event) event.preventDefault(); // blocca submit implicito

    const emailInput = document.getElementById("patientEmail");
    const passwordInput = document.getElementById("patientPassword");

    if (!emailInput || !passwordInput) {
        alert("Form not loaded yet!");
        return;
    }

    const patient = { email: emailInput.value, password: passwordInput.value };

    try {
        const response = await fetch(PATIENT_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(patient),
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("token", data.token);

            // Chiudi modal prima del redirect
            const modal = document.getElementById('modal');
            if (modal) modal.style.display = 'none';

            // Redirect Patient Dashboard
            window.location.href = "/pages/patientDashboard.html";
        } else {
            alert("Invalid credentials!");
        }
    } catch (err) {
        console.error(err);
        alert("Something went wrong! Try again.");
    }
};

/*
  Import the openModal function to handle showing login popups/modals
  Import the base API URL from the config file
  Define constants for the admin and doctor login API endpoints using the base URL

  Use the window.onload event to ensure DOM elements are available after page load
  Inside this function:
    - Select the "adminLogin" and "doctorLogin" buttons using getElementById
    - If the admin login button exists:
        - Add a click event listener that calls openModal('adminLogin') to show the admin login modal
    - If the doctor login button exists:
        - Add a click event listener that calls openModal('doctorLogin') to show the doctor login modal


  Define a function named adminLoginHandler on the global window object
  This function will be triggered when the admin submits their login credentials

  Step 1: Get the entered username and password from the input fields
  Step 2: Create an admin object with these credentials

  Step 3: Use fetch() to send a POST request to the ADMIN_API endpoint
    - Set method to POST
    - Add headers with 'Content-Type: application/json'
    - Convert the admin object to JSON and send in the body

  Step 4: If the response is successful:
    - Parse the JSON response to get the token
    - Store the token in localStorage
    - Call selectRole('admin') to proceed with admin-specific behavior

  Step 5: If login fails or credentials are invalid:
    - Show an alert with an error message

  Step 6: Wrap everything in a try-catch to handle network or server errors
    - Show a generic error message if something goes wrong


  Define a function named doctorLoginHandler on the global window object
  This function will be triggered when a doctor submits their login credentials

  Step 1: Get the entered email and password from the input fields
  Step 2: Create a doctor object with these credentials

  Step 3: Use fetch() to send a POST request to the DOCTOR_API endpoint
    - Include headers and request body similar to admin login

  Step 4: If login is successful:
    - Parse the JSON response to get the token
    - Store the token in localStorage
    - Call selectRole('doctor') to proceed with doctor-specific behavior

  Step 5: If login fails:
    - Show an alert for invalid credentials

  Step 6: Wrap in a try-catch block to handle errors gracefully
    - Log the error to the console
    - Show a generic error message
*/
