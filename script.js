//Seite neu laden bei Klick auf reload Button
function refreshPage(){
    window.location.reload();
} 

const toggleButton = document.getElementById('button-9');
let lastScrollTop = 0;

//Scroll event um header blur und Farbe zu ändern
window.addEventListener('scroll', () => {
    const header = document.querySelector('header');
    let scrollTop = window.scrollY || document.documentElement.scrollTop;
    let alpha = 1 - (scrollTop / 100) +0.4; // Alpha-Wert nimmt schnell ab
    if (alpha < 0) alpha = 0.3;
    //header.style.backgroundColor = `rgb(0, 0, 0, ${alpha})`;

    let blurAmount = scrollTop / 100 + 8; // Berechne die Unschärfe basierend auf der Scrollposition
    if (blurAmount > 5) blurAmount = 8; // Begrenze die Unschärfe auf 5px
    header.style.backdropFilter = `blur(${blurAmount}px)`;
});

function closePopup() {
    document.getElementById('popup').style.display = 'none';
}

//Popup mit Feiertagsnamen anzeigen
function showPopup(holidayName, holidayDate, mouseX, mouseY) {
    const popup = document.getElementById('popup');
    const popupContent = document.querySelector('.popup-content');

    // Position des Popups festlegen
    popup.style.display = 'block';
    popup.style.left = mouseX + 'px';
    popup.style.top = mouseY + 'px';

    // Inhalt des Popups setzen
    document.getElementById('holidayName').textContent = holidayName;
    document.getElementById('holidayDate').textContent = holidayDate;

    // Überprüfen, ob das Popup vollständig im sichtbaren Bereich ist
    const rect = popup.getBoundingClientRect();
    if (rect.right > window.innerWidth) {
        popup.style.left = (window.innerWidth - rect.width) + 'px';
    }
    if (rect.bottom > window.innerHeight) {
        popup.style.top = (window.innerHeight - rect.height) + 'px';
    }
}

toggleButton.addEventListener('click', () => {      //toggleButton animation go up
    toggleButton.classList.toggle('hidden');
});

function createYearDropdown() {
    const selectElement = document.getElementById('yearSelect');
    const currentYear = new Date().getFullYear();

    for (let i = 2000; i <= 2099; i++) {    //Jahre 2000 bis 2099 anzeigen
        const optionElement = document.createElement('option');
        optionElement.value = i;
        optionElement.textContent = i;
        selectElement.appendChild(optionElement);
    }

    //aktuelles Jahr als default einstellen
    selectElement.value = currentYear;
    updateCurrentYear(currentYear);
    document.title = currentYear;
}

//Jahr ändern
function changeYear() {
    const selectedYear = parseInt(document.getElementById('yearSelect').value);
    const currentMonth = new Date().getMonth();
    const numMonthsToShow = 12;
    createCalendar(selectedYear, startMonth, numMonthsToShow);
    updateCurrentYear(selectedYear); 
    document.title = selectedYear;
}

//aktualisieren des aktuellen Jahres mit Animation
function updateCurrentYear(year) {
    const currentYearElement = document.getElementById('currentYear');
    currentYearElement.innerHTML = ''; //aktuellen Inhalt löschen

    const yearString = year.toString();
    for (let i = 0; i < yearString.length; i++) {
        const span = document.createElement('span');
        span.classList.add('letter');
        span.style.animationDelay = `${i * 0.1}s`;
        span.textContent = yearString[i];
        currentYearElement.appendChild(span);
    }
}

function toggleHeader() {       //animation for header and calendar items (go up)
    const header = document.querySelector('header');

    if (header.classList.contains('hidden')) {
        header.classList.remove('hidden');
        calendar.classList.remove('hidden');
    } else {
        header.classList.add('hidden');
        calendar.classList.add('hidden');
    }
}

document.getElementById("currentYear").textContent = new Date().getFullYear();

//Wochentag ermitteln (anfangen mit Montag als 0)
function getWeekdayIndex(day) {
    return (day + 6) % 7; //kompensiere für die unterschiedliche Indizierung
}

function isHoliday(date) {

    if (!(date instanceof Date)) {
        return ''; //wenn kein gültiges date Objekt übergeben wurde
    }
    const staticHolidays = {
        'Neujahr': { month: 0, day: 1 },
        'Tag der Arbeit': { month: 4, day: 1 },
        'Tag der deutschen Einheit': { month: 9, day: 3 },
        'Reformationstag': { month: 9, day: 31 },
        'Erster Weihnachtsfeiertag': { month: 11, day: 25 },
        'Zweiter Weihnachtsfeiertag': { month: 11, day: 26 }
    };

    const dynamicHolidays = calculateDynamicHolidays(date.getFullYear());    

    const month = date.getMonth();
    const day = date.getDate();
    const dateString = (month + 1 < 10 ? '0' : '') + (month + 1) + '-' + (day < 10 ? '0' : '') + day;

    if (staticHolidays.hasOwnProperty(dateString)) {
        return staticHolidays[dateString];
    }

    //statische Feiertage
    for (const holiday in staticHolidays) {
        if (staticHolidays.hasOwnProperty(holiday)) {
            const holidayDate = staticHolidays[holiday];
            if (holidayDate.month === month && holidayDate.day === day) {
                console.log(holiday)
                return holiday; //gib Namen des statischen Feiertags zurück
            }
        }
    }

    //dynamische Feiertage
    for (const holiday in dynamicHolidays) {
        if (dynamicHolidays.hasOwnProperty(holiday)) {
            const holidayDate = dynamicHolidays[holiday];
            if (holidayDate.getMonth() === month && holidayDate.getDate() === day) {
                console.log(holiday)
                return holiday; //gib Namen des dynamischen Feiertags zurück
            }
        }
    }

    return ''; //wenn kein Feiertag gefunden wurde
}

function calculateEaster(year) {
    const a = year % 19;
    const b = Math.floor(year / 100);
    const c = year % 100;
    const d = Math.floor(b / 4);
    const e = b % 4;
    const f = Math.floor((b + 8) / 25);
    const g = Math.floor((b - f + 1) / 3);
    const h = (19 * a + b - d - g + 15) % 30;
    const i = Math.floor(c / 4);
    const k = c % 4;
    const l = (32 + 2 * e + 2 * i - h - k) % 7;
    const m = Math.floor((a + 11 * h + 22 * l) / 451);
    const month = Math.floor((h + l - 7 * m + 114) / 31);
    const day = ((h + l - 7 * m + 114) % 31) + 1;
    return new Date(year, month - 1, day);
}

function calculateDynamicHolidays(year) {
    const easterSunday = calculateEaster(year);

    //Karfreitag
    const goodFriday = new Date(easterSunday);
    goodFriday.setDate(easterSunday.getDate() - 2);

    //Ostermontag
    const easterMonday = new Date(easterSunday);
    easterMonday.setDate(easterSunday.getDate() + 1);

    //Himmelfahrt
    const ascensionDay = new Date(easterSunday);
    ascensionDay.setDate(easterSunday.getDate() + 39);

    //Pfingstmontag
    const whitMonday = new Date(easterSunday);
    whitMonday.setDate(easterSunday.getDate() + 50);

    //Buß- und Bettag
    const november23 = new Date(year, 10, 23);
    const wednesdayBeforeNovember23 = new Date(november23);
    wednesdayBeforeNovember23.setDate(november23.getDate() - november23.getDay() + 3); //Mittwoch (Tag 3) vor dem 23. November

    return {
        'Karfreitag': goodFriday,
        'Ostermontag': easterMonday,
        'Himmelfahrt': ascensionDay,
        'Pfingstmontag': whitMonday,
        'Buß- und Bettag': wednesdayBeforeNovember23
    };
}

// Funktion zum Anzeigen des Feiertagsnamens
function showHolidayName(holidayName) {
    //alert('Feiertag: ' + holidayName);
    if (holidayName !== '') {
        //Popup-Fenster mit Feiertagsnamen öffnen
        showPopup('Feiertag: ' + holidayName, '');
    }
}

// Funktion zur Aktualisierung des Eventlisteners für Feiertagszellen
function updateHolidayClickEventListeners() {
    const holidayCells = document.querySelectorAll('.holiday');

    //über alle Feiertagszellen gehen und sie dem Eventlistener hinzufügen
    holidayCells.forEach(cell => {
        cell.addEventListener('click', function(event) {
            const holidayDate = this.getAttribute('data-holiday'); //Datum des entsprechenden Feiertags holen
            console.log("angeklickter Feiertag: " + holidayDate)
            const mouseX = event.clientX;
            const mouseY = event.clientY;
            showHolidayName(holidayDate, mouseX, mouseY); // Zeige den Feiertagsnamen an
        });
    });
}

function getCalendarWeek(d) {
    //Datum kopieren um Originaldatum erhalten zu lassen
    d = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
    //aktuelles Datum + 4 (Donnerstag) - Nummer aktueller Tag -> nähster Donnerstag
    //Sonntag als Tag 7 festlegen
    d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay()||7));
    //erster Tag des Jahres
    var yearStart = new Date(Date.UTC(d.getUTCFullYear(),0,1));
    //ganze Wochen zu nähstem Donnerstag berechnen
    var weekNo = Math.ceil(( ( (d - yearStart) / 86400000) + 1)/7);
    //return Array aus Wochennummer und Jahr und 
    return weekNo;
}


function addDays(date, days) {
    var result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}

  function removeDays(date, days) {
    var result = new Date(date);
    result.setDate(result.getDate() - days);
    return result;
}

//funktion zum Erstellen des Kalenders
function createCalendar(startYear, startMonth, numMonths) {
    const calendarElement = document.getElementById('calendar');
    calendarElement.innerHTML = '';

    //aktuelles Datum
    const today = new Date();
    const currentYear = today.getFullYear();
    const currentMonth = today.getMonth();
    const currentDay = today.getDate();

    for (let i = 0; i < numMonths; i++) {
        const currentDate = new Date(startYear, startMonth + i, 1);
        const monthName = currentDate.toLocaleString('de-DE', { month: 'long' });

        //Monatselement erstellen
        const monthElement = document.createElement('div');
        monthElement.classList.add('month');

        //Monatstitel erstellen und dem Monatselement hinzufügen
        const monthHeader = document.createElement('h2');
        monthHeader.textContent = monthName;
        monthElement.appendChild(monthHeader);

        //Wochentage des aktuellen Monats erstellen
        const weekdays = ['KW', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'];

        //Zeile für Wochentage erstellen
        const weekdaysRow = document.createElement('div');
        weekdaysRow.classList.add('days');
        weekdays.forEach(day => {
            const dayElement = document.createElement('div');
            dayElement.classList.add('weekday');
            dayElement.textContent = day;
            if(day === "Sa")
                dayElement.classList.add('weekend');
            if(day === "So")
                dayElement.classList.add('weekend');
            if(day === "KW")
                dayElement.classList.add('KW');
            weekdaysRow.appendChild(dayElement);
        });
        monthElement.appendChild(weekdaysRow);
        //console.log(weekdaysRow)
        
        //parent-Element für Tage des Monats erstellen
        const daysElement = document.createElement('div');
        daysElement.classList.add('days');
        monthElement.appendChild(daysElement);

        //KW für ersten Tag im Monat erstellen
        const firstKwElement = document.createElement('div');
        firstKwElement.classList.add('calendarWeeksOne');
        firstKwElement.textContent = getCalendarWeek(currentDate); // Inhalt zuweisen
        daysElement.appendChild(firstKwElement); // Zum daysElement hinzufügen

        //leere Tage vor erstem Tag des Monats hinzufügen
        const firstDayOfWeek = getWeekdayIndex(new Date(currentDate.getFullYear(), currentDate.getMonth(), 1).getDay());

        for (let k = 0; k < firstDayOfWeek; k++) {
            const emptyDayElement = document.createElement('div');
            emptyDayElement.classList.add('day');
            daysElement.appendChild(emptyDayElement);
        }

        //Tage des Monats hinzufügen und erstellen
        const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate();
        //console.log("current Date: " + currentDate)
        const counterForKw = firstDayOfWeek;
        for (let j = 1; j <= daysInMonth; j++) {
            counterForKwInWeek = counterForKw + j;
            
            const dayElement = document.createElement('div');
            dayElement.classList.add('day');
            dayElement.textContent = j;
            //const kwElement = document.createElement('div');

            //aktuellen Tag markieren
            if (currentYear === startYear && currentMonth === startMonth + i && j === currentDay) {
                dayElement.classList.add('current-day');
            }

            //überprüfen, ob Tag Feiertag ist und entsprechend markieren
            const currentDate = new Date(startYear, startMonth + i, j);
            const holidayName = isHoliday(currentDate);
            if (holidayName !== '') {
                dayElement.classList.add('holiday');
                dayElement.setAttribute('data-holiday', holidayName);
            }

            const dayOfWeek = currentDate.getDay();
            if (dayOfWeek === 6 ||dayOfWeek === 0) dayElement.classList.add('weekend');

            daysElement.appendChild(dayElement);
            //daysElement.appendChild(kwElement);

            if (counterForKwInWeek % 7 === 0 && j < daysInMonth) {
                //console.log("weekcounter" + counterForKwInWeek + ", Datum: " + j);
                const dayElement = document.createElement('div');
                dayElement.classList.add('calendarWeeks');
                const trueDate = addDays(currentDate, 1);
                dayElement.textContent = getCalendarWeek(trueDate);    //um den folgenden Tag zur Berechnung zu benutzen
                daysElement.appendChild(dayElement);
            }
        }

        // Füge das Monatselement zum Kalender hinzu
        calendarElement.appendChild(monthElement);
        setTimeout(() => {
            monthElement.classList.add('show');
            window.scroll({
                top: 0,
                left: 0,
                behavior: 'smooth'
            });
        }, i * 50); // Verzögerung für Animation jedes Monatselement, Seite hochscrollen
        window.scrollTo(0, 0);
    }
    updateHolidayClickEventListeners();
    console.log(">>> Kalender erstellt");
}


// Kalender für das aktuelle Jahr erstellen (beginnen mit aktuellem Monat)
const today = new Date();
const currentYear = today.getFullYear();
const startMonth = 0; // Januar
const numMonthsToShow = 12; // Anzahl der Monate im Kalender
createCalendar(currentYear, startMonth, numMonthsToShow);
createYearDropdown();
