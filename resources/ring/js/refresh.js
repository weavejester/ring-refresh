var pageLoadTime = new Date().getTime();


async function reloadIfSourceChanged() {
    try {
        const response = await fetch('/__source_changed', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        const text = await response.text()
        const sourceChanged = parseInt(text);

        if (sourceChanged >= pageLoadTime) {
            window.location.reload();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function reloadIfSourceChangedJob() {
    await reloadIfSourceChanged()
    setTimeout(reloadIfSourceChangedJob, 200);
}

window.onload = reloadIfSourceChangedJob;
