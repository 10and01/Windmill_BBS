function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme');
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
}

/* ---------- 图片预览 ---------- */
function openImagePreview(url) {
    let overlay = document.getElementById('imgPreviewOverlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'imgPreviewOverlay';
        overlay.innerHTML = '<img id="imgPreviewImg" src="" alt="">';
        document.body.appendChild(overlay);
        overlay.addEventListener('click', function() {
            overlay.classList.remove('active');
        });
    }
    const img = document.getElementById('imgPreviewImg');
    img.src = url;
    overlay.classList.add('active');
}

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        const overlay = document.getElementById('imgPreviewOverlay');
        if (overlay) overlay.classList.remove('active');
    }
});
