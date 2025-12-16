/* main.js - Code xử lý chung cho toàn bộ website */

document.addEventListener("DOMContentLoaded", function() {
    checkLoginState();
});

function checkLoginState() {
    const savedUser = localStorage.getItem("tyk_user") || sessionStorage.getItem("tyk_user");
    if (savedUser) {
        const user = JSON.parse(savedUser);
        
        // 1. Cập nhật Navbar
        const authButtons = document.getElementById('authButtons');
        const userProfile = document.getElementById('userProfile');
        const displayUsername = document.getElementById('displayUsername');

        if (authButtons && userProfile && displayUsername) {
            authButtons.classList.add('d-none');
            userProfile.classList.remove('d-none');
            displayUsername.innerText = user.username;
        }

        // 2. Nếu ở trang Marketplace -> Lấy coin
        if (window.location.pathname === "/marketplace") {
            fetchRealCoin(user.username);
        }

        // 3. Nếu ở Home -> Xử lý nút Download
        const btnGuest = document.getElementById('btnGuestDownload');
        const btnUser = document.getElementById('btnUserDownload');
        if (btnGuest && btnUser) {
            btnGuest.classList.add('d-none');
            btnUser.classList.remove('d-none');
        }
    }
}

async function fetchRealCoin(username) {
    try {
        const response = await fetch(`/api/auth/profile/${username}`);
        if (response.ok) {
            const user = await response.json();
            localStorage.setItem("tyk_user", JSON.stringify(user));
            
            const displayCoin = document.getElementById('displayCoin');
            const coinBadge = document.getElementById('coinBadge');
            
            if (displayCoin && coinBadge) {
                displayCoin.innerText = user.coinBalance.toLocaleString();
                coinBadge.classList.remove('d-none');
            }
        }
    } catch (e) { console.error("Error coin:", e); }
}

function logout() {
    localStorage.removeItem("tyk_user");
    sessionStorage.removeItem("tyk_user");
    window.location.href = "/";
}

function switchToLogin() {
    const regModal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
    if(regModal) regModal.hide();
    new bootstrap.Modal(document.getElementById('loginModal')).show();
}

function switchToRegister() {
    const loginModal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
    if(loginModal) loginModal.hide();
    new bootstrap.Modal(document.getElementById('registerModal')).show();
}

// --- THÊM PHẦN QUAN TRỌNG NÀY (Hàm Submit) ---

async function submitRegister() {
    const username = document.getElementById('regUsername').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('regConfirmPassword').value;

    if(username.length < 3) return alert("Username too short!");
    if(password.length < 6) return alert("Password must have at least 6 digits!");
    if(password !== confirmPassword) {
        return alert("❌ Confirm password does not match!");
    }

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });
        const text = await response.text();
        
        if (response.ok) {
            alert("✅ " + text);
            switchToLogin();
        } else {
            alert("❌ " + text);
        }
    } catch (error) { alert("Server connection error!"); }
}

async function submitLogin() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    const isRemember = document.getElementById('rememberMe').checked;
    // cookies sẽ được set trong response từ server
    let url = '/api/auth/login';
    if (isRemember) {
        url += '?remember-me=true';
    }

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const user = await response.json();
            
            if (isRemember) {
                localStorage.setItem("tyk_user", JSON.stringify(user));
            } else {
                // Nếu không tích remember thì lưu vào sessionStorage (tắt trình duyệt là mất)
                sessionStorage.setItem("tyk_user", JSON.stringify(user));
            }

            // Tắt modal login
            const modalEl = document.getElementById('loginModal');
            const modalInstance = bootstrap.Modal.getInstance(modalEl);
            if(modalInstance) modalInstance.hide();
            
            // Reload trang để cập nhật UI
            location.reload(); 
        } else {
            const text = await response.text();
            alert("❌ " + text);
        }
    } catch (error) { alert("Server connection error!"); }
}