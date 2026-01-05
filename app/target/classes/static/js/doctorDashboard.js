/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment


  Get the table body where patient rows will be added
  Initialize selectedDate with today's date in 'YYYY-MM-DD' format
  Get the saved token from localStorage (used for authenticated API calls)
  Initialize patientName to null (used for filtering by name)


  Add an 'input' event listener to the search bar
  On each keystroke:
    - Trim and check the input value
    - If not empty, use it as the patientName for filtering
    - Else, reset patientName to "null" (as expected by backend)
    - Reload the appointments list with the updated filter


  Add a click listener to the "Today" button
  When clicked:
    - Set selectedDate to today's date
    - Update the date picker UI to match
    - Reload the appointments for today


  Add a change event listener to the date picker
  When the date changes:
    - Update selectedDate with the new value
    - Reload the appointments for that specific date


  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name

  Step 1: Call getAllAppointments with selectedDate, patientName, and token
  Step 2: Clear the table body content before rendering new rows

  Step 3: If no appointments are returned:
    - Display a message row: "No Appointments found for today."

  Step 4: If appointments exist:
    - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
    - Call createPatientRow to generate a table row for the appointment
    - Append each row to the table body

  Step 5: Catch and handle any errors during fetch:
    - Show a message row: "Error loading appointments. Try again later."


  When the page is fully loaded (DOMContentLoaded):
    - Call renderContent() (assumes it sets up the UI layout)
    - Call loadAppointments() to display today's appointments by default
*/


// 1. Import dei moduli necessari
import { getAllAppointments } from './services/appointmentRecordService.js';
import { createPatientRecordRow  } from "./components/patientRecordRow.js";

// 2. Inizializzazione variabili globali
const tableBody = document.getElementById('patientTableBody'); // dove inserire le righe
let selectedDate = new Date().toISOString().split('T')[0]; // oggi in formato YYYY-MM-DD
const token = localStorage.getItem('token'); // token per autenticazione API
    if (!token) window.location.href = "/index.html";
let patientName = "null"; // filtro per nome paziente

// 3. Gestione barra di ricerca
const searchBar = document.getElementById('searchBar');
if (searchBar) {
  searchBar.addEventListener('input', () => {
    const value = searchBar.value.trim();
    patientName = value !== '' ? value : "null"; // se vuoto, backend si aspetta "null"
    loadAppointments();
  });
}

// 4. Pulsante "Today"
const todayButton = document.getElementById("todayButton");
if (todayButton) {
  todayButton.addEventListener("click", () => {
    selectedDate = new Date().toISOString().split("T")[0];
    document.getElementById("datePicker").value = selectedDate;
    loadAppointments();
  });
}


// 5. Selettore data
const datePicker = document.getElementById('datePicker');
if (datePicker) {
  datePicker.addEventListener('change', () => {
    selectedDate = datePicker.value;
    loadAppointments();
  });
}

// 6. Funzione principale: loadAppointments
async function loadAppointments() {
  try {
    // Step 1: fetch dal backend
    const response = await getAllAppointments(selectedDate, patientName, token);

    // Step 2: estrai l'array degli appuntamenti dall'oggetto
    const appointments = response.appointments || [];

    // Step 3: pulisci la tabella prima di inserire le nuove righe
    tableBody.innerHTML = "";

    // Step 4: nessun appuntamento trovato
    if (appointments.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="5" class="noPatientRecord">No Appointments found for selected date.</td></tr>`;
      return;
    }

    // Step 5: crea le righe per ogni appuntamento
    appointments.forEach(app => {
      const row = createPatientRecordRow(app); // passa l'intero oggetto app
      tableBody.appendChild(row);
    });

  } catch (err) {
    // Step 6: gestione errori
    console.error("Error loading appointments:", err);
    tableBody.innerHTML = `<tr><td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td></tr>`;
  }
}
// 7. Render iniziale al caricamento della pagina
document.addEventListener("DOMContentLoaded", () => {
    renderContent(); // Injects header/footer layout
    loadAppointments(); // Loads today's appointments
  });