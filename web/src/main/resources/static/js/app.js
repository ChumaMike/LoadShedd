// --- GLOBAL INIT ---
document.addEventListener("DOMContentLoaded", () => {
    // Start polling for stage updates
    setInterval(fetchStage, 5000);
});

// --- STAGE UPDATER ---
async function fetchStage() {
    try {
        const res = await fetch("/api/stage");
        const data = await res.json();
        const el = document.getElementById("stageNumber");
        if(el && el.innerText != data.stage) {
            el.innerText = data.stage;
            // Visual flair: change color if stage is high
            el.style.color = data.stage >= 6 ? "#ff3333" : "#ff9800";
        }
    } catch(e) { /* ignore silent failures */ }
}

// --- LOCATION LOGIC (THE FIX) ---
async function loadTowns() {
    const provinceSelect = document.getElementById("province");
    const townSelect = document.getElementById("town");

    // 1. Get Value
    const province = provinceSelect.value;
    console.log("Selected Province:", province); // Debug log

    // 2. Set Loading State
    townSelect.innerHTML = "<option>Loading Areas...</option>";
    townSelect.disabled = true;

    // 3. Validation
    if (!province) {
        townSelect.innerHTML = "<option value=''>Select Province First</option>";
        return;
    }

    try {
        // 4. Fetch with Encoding
        // 'encodeURIComponent' handles spaces like in "Eastern Cape"
        const url = `/api/towns/${encodeURIComponent(province)}`;
        const response = await fetch(url);

        if (!response.ok) throw new Error("Server returned " + response.status);

        const towns = await response.json();

        // 5. Populate
        townSelect.innerHTML = "<option value=''>Select Town...</option>";
        townSelect.disabled = false; // UNLOCK THE DROPDOWN

        // Sort
        towns.sort((a,b) => a.name.localeCompare(b.name));

        towns.forEach(t => {
            const opt = document.createElement("option");
            opt.value = t.name;
            opt.text = t.name;
            townSelect.appendChild(opt);
        });

    } catch (e) {
        console.error("Load Towns Error:", e);
        townSelect.innerHTML = "<option>Error loading data</option>";
    }
}

// --- SCHEDULE LOGIC ---
async function getSchedule() {
    const province = document.getElementById("province").value;
    const town = document.getElementById("town").value;
    const container = document.getElementById("scheduleResult");

    if (!town) {
        alert("Please select a town first.");
        return;
    }

    container.innerHTML = `<div class="text-center py-5"><div class="spinner-border text-warning"></div></div>`;

    try {
        const res = await fetch(`/api/schedule/${province}/${encodeURIComponent(town)}`);
        const data = await res.json();

        let html = `<h3 class="text-white mb-4">${town}, <span style="color:var(--accent-teal)">${province}</span></h3>`;

        if (data.days && data.days.length > 0) {
            data.days.forEach(day => {
                html += `<div class="day-label">${day.name}</div>`;

                if (day.slots.length === 0) {
                    html += `<div class="schedule-row" style="border-color: var(--accent-teal)">
                                <span>No load shedding expected</span>
                                <i class="bi bi-check-circle-fill text-success"></i>
                             </div>`;
                } else {
                    day.slots.forEach(slot => {
                        html += `<div class="schedule-row">
                                    <span class="fs-5">${slot.start}</span>
                                    <span class="badge bg-danger">OUTAGE</span>
                                 </div>`;
                    });
                }
            });
        }
        container.innerHTML = html;

    } catch (e) {
        container.innerHTML = `<div class="alert alert-danger">Could not retrieve schedule.</div>`;
    }
}