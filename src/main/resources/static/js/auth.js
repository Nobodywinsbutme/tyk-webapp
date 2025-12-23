/* auth.js - Xử lý xác thực và dùng chung */

document.addEventListener("DOMContentLoaded", function() {
    checkLoginState();
});

// 1. AUTH & NAVBAR LOGIC
function checkLoginState() {
    const savedUser = localStorage.getItem("tyk_user") || sessionStorage.getItem("tyk_user");
    const authButtons = document.getElementById('authButtons'); // Nút Login/Signup
    const userProfile = document.getElementById('userProfile'); // Avatar User
    const inventoryBtn = document.getElementById('btnInventoryNav'); // Nút Inventory to trên Marketplace
    const btnGuest = document.getElementById('btnGuestDownload');
    const btnUser = document.getElementById('btnUserDownload');

    if (savedUser) {
        const user = JSON.parse(savedUser);
        
        if (authButtons) authButtons.classList.add('d-none');
        if (userProfile) userProfile.classList.remove('d-none');
        if (inventoryBtn) inventoryBtn.classList.remove('d-none');
        if (btnGuest) btnGuest.classList.add('d-none');       // Ẩn nút Guest
        if (btnUser) btnUser.classList.remove('d-none');
        
        if (userProfile) {
            let menuItems = '';

            if (user.role === 'ADMIN') {
                menuItems = `<li><a class="dropdown-item text-warning fw-bold" href="/admin"><i class="bi bi-shield-lock me-2"></i> Admin Panel</a></li>
                             <li><hr class="dropdown-divider bg-secondary"></li>`;
            } else {
                menuItems = `<li><a class="dropdown-item text-warning fw-bold" href="/inventory"><i class="bi bi-box-seam me-2"></i> My Inventory</a></li>
                             <li><a class="dropdown-item text-white" href="/my-designs"><i class="bi bi-images me-2"></i> My Designs</a></li>
                             <li><a class="dropdown-item text-white" href="/settings"><i class="bi bi-gear me-2"></i> Settings</a></li>
                             <li><hr class="dropdown-divider bg-secondary"></li>`;
            }

            userProfile.innerHTML = `
                <div class="dropdown user-dropdown">
                    <a class="nav-link dropdown-toggle text-white d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown">
                        <img src="/img/logo.png" class="rounded-circle me-2 border border-warning" style="width: 30px; height: 30px; object-fit: cover;">
                        <span class="d-none d-md-block">Hello, <b class="text-warning">${user.username}</b></span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end shadow bg-dark border-secondary">
                        ${menuItems}  
                        <li><button class="dropdown-item text-danger" onclick="logout()"><i class="bi bi-box-arrow-right me-2"></i> Logout</button></li>
                    </ul>
                </div>
            `;
        }

        // Nếu đang ở trang Marketplace thì fetch Coin
        if (window.location.pathname === "/marketplace") {
            fetchRealCoin(user.username);
        }
        if (window.location.pathname === "/marketplace/market") {
            fetchRealCoin(user.username);
        }
        if (window.location.pathname === "/inventory") {
            fetchRealCoin(user.username);
        }

    } else {
        // Chưa đăng nhập
        if (authButtons) authButtons.classList.remove('d-none');
        if (userProfile) userProfile.classList.add('d-none');
        if (inventoryBtn) inventoryBtn.classList.add('d-none'); // Ẩn nút Inventory to
        if (btnGuest) btnGuest.classList.remove('d-none');
        if (btnUser) btnUser.classList.add('d-none');
    }
}

// ... (Giữ nguyên các hàm fetchRealCoin, logout, submitRegister, submitLogin bên dưới như cũ) ...
async function fetchRealCoin(username) {
    try {
        const response = await fetch(`/api/auth/profile/${username}`);
        if (response.ok) {
            const user = await response.json();
            if(localStorage.getItem("tyk_user")) localStorage.setItem("tyk_user", JSON.stringify(user));
            else sessionStorage.setItem("tyk_user", JSON.stringify(user));
            
            const displayCoin = document.getElementById('displayCoin');
            if (displayCoin) displayCoin.innerText = user.coinBalance.toLocaleString();
        }
    } catch (e) { console.error("Error coin:", e); }
}

async function fetchRealCoin(username) {
    try {
        const response = await fetch(`/api/auth/profile/${username}`);
        if (response.ok) {
            const user = await response.json();
            if(localStorage.getItem("tyk_user")) localStorage.setItem("tyk_user", JSON.stringify(user));
            else sessionStorage.setItem("tyk_user", JSON.stringify(user));
            
            const displayCoin = document.getElementById('displayCoin');
            if (displayCoin) displayCoin.innerText = user.coinBalance.toLocaleString();
        }
    } catch (e) { console.error("Error coin:", e); }
}

function logout() {
    localStorage.removeItem("tyk_user");
    sessionStorage.removeItem("tyk_user");
    fetch('/api/auth/logout', { method: 'POST' }); 
    window.location.href = "/";
}

// 2. MODAL SWITCHING & API CALLS
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

async function submitRegister() {
    const username = document.getElementById('regUsername').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('regConfirmPassword').value;

    if(username.length < 3) return alert("Username too short!");
    if(password.length < 6) return alert("Password must have at least 6 digits!");
    if(password !== confirmPassword) return alert("❌ Confirm password does not match!");

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

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include', 
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const user = await response.json();
            if (isRemember) {
                localStorage.setItem("tyk_user", JSON.stringify(user));
            } else {
                sessionStorage.setItem("tyk_user", JSON.stringify(user));
            }

            const modalEl = document.getElementById('loginModal');
            const modalInstance = bootstrap.Modal.getInstance(modalEl);
            if(modalInstance) modalInstance.hide();
            
            // Xử lý chuyển hướng sau khi login
            const redirectUrl = sessionStorage.getItem("redirect_after_login");
            if (redirectUrl) {
                sessionStorage.removeItem("redirect_after_login");
                window.location.href = redirectUrl;
            } else {
                location.reload();
            } 
        } else {
            const text = await response.text();
            alert("❌ " + text);
        }
    } catch (error) { alert("Server connection error!"); }
}
