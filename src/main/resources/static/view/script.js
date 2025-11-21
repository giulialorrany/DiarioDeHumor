const moods = {};
const notes = {};
let currentMonth = new Date().getMonth();
let currentYear = new Date().getFullYear();
let selectedMood = null;
let currentPeriod = 'week';

const testDate = null
// const testDate = new Date('Wed Nov 19 2025').toDateString();

const moodEmojis = {
    'terrible': '😫',
    'bad': '😔',
    'ok': '😐',
    'good': '😊',
    'excellent': '😄'
};

const moodLabels = {
    'terrible': 'Terrível',
    'bad': 'Mau',
    'ok': 'Ok',
    'good': 'Bom',
    'excellent': 'Excelente'
};

function openNoteModal() {
    const today = testDate || new Date().toDateString();
    const todayMood = moods[today] || selectedMood;

    // Permitir abrir o modal mesmo sem humor selecionado
    if (todayMood) {
        document.getElementById('moodEmojiLarge').textContent = moodEmojis[todayMood];
        document.getElementById('moodTextDisplay').textContent = `Seu humor: ${moodLabels[todayMood]}`;
        document.getElementById('currentMoodDisplay').style.display = 'block';
    } else {
        document.getElementById('currentMoodDisplay').style.display = 'none';
    }

    const existingNote = notes[today] || '';
    document.getElementById('noteTextarea').value = existingNote;

    document.getElementById('noteModal').classList.add('active');
    document.body.style.overflow = 'hidden';
}

function testWithDate(testDate) {
    // Formata a data para o formato adequado (como testWithDate('Wed Nov 19 2025'))
    const formattedDate = new Date(testDate).toDateString();
    openNoteModal(formattedDate);
}

function closeNoteModal() {
    document.getElementById('noteModal').classList.remove('active');
    document.getElementById('noteTextarea').value = '';
    document.body.style.overflow = 'auto';
}

function saveNote() {
    const today = testDate || new Date().toDateString();
    const noteText = document.getElementById('noteTextarea').value.trim();
    const todayMood = moods[today] || selectedMood;

    if (noteText) {
        notes[today] = noteText;

        // Chamar saveData para salvar no servidor
        saveData(today, todayMood, noteText);

        if (!todayMood) {
            closeNoteModal();
            alert('Anotação salva! Não esqueça de registrar seu humor de hoje também! 😊');
            showPage('home');
        } else {
            closeNoteModal();
            alert('Anotação salva com sucesso! ✓');
        }
    } else {
        alert('Por favor, escreva algo antes de salvar!');
    }
}

function toggleTheme() {
    document.body.classList.toggle('light-mode');
    const themeToggle = document.getElementById('themeToggle');
    if (document.body.classList.contains('light-mode')) {
        themeToggle.textContent = '☀️';
        localStorage.setItem('theme', 'light');
    } else {
        themeToggle.textContent = '🌙';
        localStorage.setItem('theme', 'dark');
    }
}

function loadTheme() {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light') {
        document.body.classList.add('light-mode');
        document.getElementById('themeToggle').textContent = '☀️';
    }
}

async function showPage(page) {
    document.getElementById('homePage').style.display = 'none';
    document.getElementById('calendarPage').style.display = 'none';
    document.getElementById('analysisPage').style.display = 'none';

    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));

    if (page === 'home') {
        document.getElementById('homePage').style.display = 'block';
        document.querySelectorAll('.nav-btn')[0].classList.add('active');
    } else if (page === 'calendar') {
        document.getElementById('calendarPage').style.display = 'block';
        document.querySelectorAll('.nav-btn')[1].classList.add('active');
        await renderCalendar();
    } else if (page === 'analysis') {
        document.getElementById('analysisPage').style.display = 'block';
        document.querySelectorAll('.nav-btn')[3].classList.add('active');
        renderAnalysis();
    }
}

function selectMood(mood) {
     const today = testDate || new Date().toDateString();
     moods[today] = mood;
     selectedMood = mood;

     // Chama saveData para salvar no BD do server
     saveData(today, mood, notes[today] || '');

     document.querySelectorAll('.mood-btn').forEach(btn => btn.classList.remove('selected'));
     event.currentTarget.classList.add('selected');

     updateStreak();
 }


async function updateStreak() {
    await loadData(); // Espera o carregamento dos dados

    const days = Object.keys(moods).length;
    document.getElementById('streakCount').textContent = days;
}

async function renderCalendar() {
    await loadData(); // Espera o carregamento dos dados

    const firstDay = new Date(currentYear, currentMonth, 1);
    const lastDay = new Date(currentYear, currentMonth + 1, 0);
    const startDay = firstDay.getDay();
    const daysInMonth = lastDay.getDate();

    document.getElementById('monthYear').textContent =
        firstDay.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });

    const grid = document.getElementById('calendarGrid');
    grid.innerHTML = '';

    for (let i = 0; i < startDay; i++) {
        grid.innerHTML += '<div class="calendar-day"></div>';
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(currentYear, currentMonth, day);
        const dateStr = date.toDateString();
        const isToday = dateStr === new Date().toDateString();
        const hasMood = moods[dateStr];
        const hasNote = notes[dateStr];

        const emoji = hasMood ? moodEmojis[hasMood] : '';
        const displayContent = emoji ? `<span class="day-number">${day}</span><span class="day-emoji">${emoji}</span>` : `<span>${day}</span>`;

        grid.innerHTML += `
            <div class="calendar-day ${isToday ? 'today' : ''} ${hasMood ? 'has-mood' : ''}" onclick="viewDayNote('${dateStr}')">
                ${displayContent}
            </div>
        `;
    }
}

function viewDayNote(dateStr) {
    const dayMood = moods[dateStr];
    const dayNote = notes[dateStr];

    if (dayMood || dayNote) {
        const date = new Date(dateStr);
        const formattedDate = date.toLocaleDateString('pt-BR');

        let message = `📅 ${formattedDate}\n\n`;

        if (dayMood) {
            message += `Humor: ${moodEmojis[dayMood]} ${moodLabels[dayMood]}\n\n`;
        }

        if (dayNote) {
            message += `Anotação:\n${dayNote}`;
        } else {
            message += 'Nenhuma anotação neste dia.';
        }

        alert(message);
    }
}

async function changeMonth(direction) {
    currentMonth += direction;
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    } else if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    }
    await renderCalendar();
}

function renderAnalysis() {
    renderChart();
    renderTips();
}

function renderChart() {
    const moodCounts = {
        'Terrível': 1,
        'Mau': 2,
        'Ok': 3,
        'Bom': 4,
        'Excelente': 5
    };

    const maxCount = Math.max(...Object.values(moodCounts));
    const chartBars = document.getElementById('chartBars');
    chartBars.innerHTML = '';

    for (const [mood, count] of Object.entries(moodCounts)) {
        const height = (count / maxCount) * 100;
        chartBars.innerHTML += `
            <div class="chart-bar" style="height: ${height}%">
                <span class="bar-label">${mood}</span>
            </div>
        `;
    }
}

function renderTips() {
    const tips = [
        'Pratique 15 minutos de meditação diária para reduzir o estresse.',
        'Mantenha uma rotina de sono regular, dormindo 7-8 horas por noite.',
        'Pratique exercícios físicos pelo menos 3 vezes por semana.',
        'Reserve tempo para atividades que você gosta e relaxam.',
        'Conecte-se com amigos e família regularmente.',
        'Evite excesso de cafeína e alimentos processados.',
        'Pratique gratidão: liste 3 coisas boas do seu dia antes de dormir.'
    ];

    const tipsContainer = document.getElementById('tipsContainer');
    tipsContainer.innerHTML = '';

    const randomTips = tips.sort(() => 0.5 - Math.random()).slice(0, 3);
    randomTips.forEach(tip => {
        tipsContainer.innerHTML += `<div class="tip-item">${tip}</div>`;
    });
}

function changePeriod(period) {
    currentPeriod = period;
    document.querySelectorAll('.period-btn').forEach(btn => btn.classList.remove('active'));
    event.currentTarget.classList.add('active');

    const days = period === 'week' ? 7 : period === 'biweekly' ? 14 : 30;
    document.getElementById('totalDays').textContent = days;

    renderAnalysis();
}


// ---------------------------------------- Funções de fetch do server ----------------------------------------
async function loadData() {
    try {
        const response = await fetch('http://localhost:8080/api/humor');
        const data = await response.json();
        console.log("Lista de humores: ", data);

        // Preenche os objetos moods e notes com os dados recebidos
        data.forEach(entry => {
            moods[entry.date] = entry.mood;
            notes[entry.date] = entry.note || ''; // Se não houver nota, atribui string vazia
        });
    } catch (err) {
        console.error("Erro:", err);
    }
}

function saveData(date, mood, note) {
    const newMood = { date, mood, note };

    fetch('http://localhost:8080/api/humor', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(newMood)
    })
        .then(response => response.text())
        .then(result => {
            console.log("Resposta do servidor: ", result);
        })
        .catch(err => console.error("Erro:", err));
}