const audioCtx = new(window.AudioContext || window.webkitAudioContext)();
const frequencyInput = document.getElementById('frequency');
const intensityInput = document.getElementById('intensity');
const frequencyValueDisplay = document.getElementById('frequencyValue');
const intensityValueDisplay = document.getElementById('intensityValue');
const playSineButton = document.getElementById('playSineButton');
const pauseSineButton = document.getElementById('pauseSineButton');

// Declare sineOscillator variable
let sineOscillator;

const frequencyChartCtx = document.getElementById('frequencyChart').getContext('2d');
const intensityChartCtx = document.getElementById('intensityChart').getContext('2d');
const intensityVsFrequencyChartCtx = document.getElementById('intensityVsFrequencyChart').getContext('2d');

const frequencyData = {
    labels: [],
    datasets: [{
        label: 'Frequency',
        borderColor: 'rgb(75, 192, 192)',
        data: [],
    }]
};

const intensityData = {
    labels: [],
    datasets: [{
        label: 'Intensity',
        borderColor: 'rgb(255, 99, 132)',
        data: [],
    }]
};

const intensityVsFrequencyData = {
    labels: [],
    datasets: [{
        label: 'Intensity vs Frequency',
        borderColor: 'rgb(255, 206, 86)',
        data: [],
    }]
};

const frequencyChart = new Chart(frequencyChartCtx, {
    type: 'line',
    data: frequencyData,
});

const intensityChart = new Chart(intensityChartCtx, {
    type: 'line',
    data: intensityData,
});

const intensityVsFrequencyChart = new Chart(intensityVsFrequencyChartCtx, {
    type: 'line',
    data: intensityVsFrequencyData,
    options: {
        scales: {
            x: {
                type: 'linear',
                position: 'bottom',
                title: {
                    display: true,
                    text: 'Frequency (Hz)'
                }
            },
            y: {
                type: 'linear',
                position: 'left',
                title: {
                    display: true,
                    text: 'Intensity (dB)'
                }
            }
        }
    }
});

playSineButton.addEventListener('click', playSineSound);
pauseSineButton.addEventListener('click', pauseSineSound);

frequencyInput.addEventListener('input', updateCharts);
intensityInput.addEventListener('input', updateCharts);

window.addEventListener('beforeunload', () => {
    if (sineOscillator) {
        sineOscillator.stop();
    }
});

function updateCharts() {
    const currentTime = new Date().toLocaleTimeString();
    const frequencyValue = parseFloat(frequencyInput.value);
    const intensityValue = parseFloat(intensityInput.value);

    frequencyData.labels.push(currentTime);
    intensityData.labels.push(currentTime);
    intensityVsFrequencyData.labels.push(frequencyValue);

    frequencyData.datasets[0].data.push(frequencyValue);
    intensityData.datasets[0].data.push(intensityValue);
    intensityVsFrequencyData.datasets[0].data.push(intensityValue);

    frequencyChart.update();
    intensityChart.update();
    intensityVsFrequencyChart.update();
}

function playSineSound() {
    if (sineOscillator) {
        sineOscillator.disconnect();
    }

    sineOscillator = audioCtx.createOscillator();
    const frequency = parseFloat(frequencyInput.value);
    const intensity = parseFloat(intensityInput.value);

    sineOscillator.type = 'sine';
    sineOscillator.frequency.setValueAtTime(frequency, audioCtx.currentTime);

    const gainNode = audioCtx.createGain();
    gainNode.gain.setValueAtTime(intensity, audioCtx.currentTime);

    sineOscillator.connect(gainNode);
    gainNode.connect(audioCtx.destination);

    sineOscillator.start();

    updateCharts();
    frequencyValueDisplay.textContent = `${frequency} Hz`;
    intensityValueDisplay.textContent = `${intensity} dB`;
}

function pauseSineSound() {
    if (sineOscillator) {
        sineOscillator.stop();
    }
}