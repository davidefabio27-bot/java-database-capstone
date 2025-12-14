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
import { createPatientRow } from './components/patientRows.js';

// 2. Inizializzazione variabili globali
const tableBody = document.getElementById('patientTableBody'); // dove inserire le righe
let selectedDate = new Date().toISOString().split('T')[0]; // oggi in formato YYYY-MM-DD
const token = localStorage.getItem('token'); // token per autenticazione API
    if (!token) window.location.href = "/index.html";
let patientName = null; // filtro per nome paziente

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
const todayBtn = document.getElementById('todayButton');
if (todayBtn) {
  todayBtn.addEventListener('click', () => {
    selectedDate = new Date().toISOString().split('T')[0];
    const datePicker = document.getElementById('datePicker');
    if (datePicker) datePicker.value = selectedDate;
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
    // Step 1: Fetch dati dal backend
    const appointments = await getAllAppointments(selectedDate, patientName, token);

    // Step 2: Pulisci il contenuto della tabella
    tableBody.innerHTML = '';

    // Step 3: Nessun appuntamento trovato
    if (!appointments || appointments.length === 0) {
      const row = document.createElement('tr');
      row.innerHTML = `<td colspan="4" style="text-align:center">No Appointments found for today.</td>`;
      tableBody.appendChild(row);
      return;
    }

    // Step 4: Itera sugli appuntamenti
    appointments.forEach(app => {
      const patient = {
        id: app.patientId,
        name: app.patientName,
        phone: app.patientPhone,
        email: app.patientEmail
      };
      const tr = createPatientRow(patient);
      tableBody.appendChild(tr);
    });

  } catch (error) {
    // Step 5: Gestione errori
    tableBody.innerHTML = `<tr><td colspan="4" style="text-align:center">Error loading appointments. Try again later.</td></tr>`;
    console.error("Error fetching appointments:", error);
  }
}

// 7. Render iniziale al caricamento della pagina
window.addEventListener('DOMContentLoaded', () => {
  if (typeof renderContent === 'function') renderContent();
  loadAppointments(); // mostra appuntamenti di oggi
}); 