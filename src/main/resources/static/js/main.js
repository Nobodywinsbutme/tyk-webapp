/* main.js - FINAL VERSION (Fix Session & Credentials) */

document.addEventListener("DOMContentLoaded", function() {
    checkLoginState();
});

// ==========================================
// 1. AUTHENTICATION & NAVBAR
// ==========================================

function checkLoginState() {
    const savedUser = localStorage.getItem("tyk_user") || sessionStorage.getItem("tyk_user");
    const authButtons = document.getElementById('authButtons');
    const userProfile = document.getElementById('userProfile');

    if (savedUser) {
        // --- TRẠNG THÁI ĐÃ ĐĂNG NHẬP ---
        const user = JSON.parse(savedUser);
        
        if (authButtons) authButtons.classList.add('d-none');
        
        if (userProfile) {
            userProfile.classList.remove('d-none');
            userProfile.innerHTML = `
                <div class="dropdown user-dropdown">
                    <a class="nav-link dropdown-toggle text-white d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown">
                        <img src="/img/logo.png" class="rounded-circle me-2 border border-warning" style="width: 30px; height: 30px; object-fit: cover;">
                        <span>Hello, <b class="text-warning">${user.username}</b></span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end shadow bg-dark border-secondary">
                        <li><a class="dropdown-item text-white" href="/my-designs"><i class="bi bi-images me-2"></i> My Designs</a></li>
                        <li><hr class="dropdown-divider bg-secondary"></li>
                        <li><button class="dropdown-item text-danger" onclick="logout()"><i class="bi bi-box-arrow-right me-2"></i> Logout</button></li>
                    </ul>
                </div>
            `;
        }

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

    } else {
        // --- TRẠNG THÁI CHƯA ĐĂNG NHẬP ---
        if (authButtons) authButtons.classList.remove('d-none');
        if (userProfile) userProfile.classList.add('d-none');
    }
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
    // Gọi API logout để xóa session trên server
    fetch('/api/auth/logout', { method: 'POST' }); 
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

// ==========================================
// 2. LOGIN / REGISTER API
// ==========================================

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
            // QUAN TRỌNG: Gửi yêu cầu kèm credentials để nhận Cookie Session
            credentials: 'include', 
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
            
            location.reload(); 
        } else {
            const text = await response.text();
            alert("❌ " + text);
        }
    } catch (error) { alert("Server connection error!"); }
}

// ==========================================
// 3. DESIGN MANAGEMENT (Upload/Edit/Delete)
// ==========================================

function checkLoginAndOpenUpload() {
    const user = localStorage.getItem("tyk_user") || sessionStorage.getItem("tyk_user");
    if (!user) {
        alert("⚠️ Bạn phải đăng nhập để đăng bài!");
        new bootstrap.Modal(document.getElementById('loginModal')).show();
    } else {
        document.getElementById('uploadForm').reset();
        document.getElementById('uploadId').value = ''; 
        document.getElementById('modalTitleLabel').innerText = "Tạo bài viết mới";
        const preview = document.getElementById('previewImage');
        if(preview) preview.style.display = 'none';
        new bootstrap.Modal(document.getElementById('uploadModal')).show();
    }
}

function editDesign(id, title, category, desc, img) {
    document.getElementById('uploadId').value = id;
    document.getElementById('uploadTitle').value = title;
    document.getElementById('uploadCategory').value = category;
    document.getElementById('uploadDesc').value = desc;
    document.getElementById('modalTitleLabel').innerText = "Chỉnh sửa bài viết";
    
    const preview = document.getElementById('previewImage');
    if(preview) {
        preview.src = img;
        preview.style.display = 'block';
    }
    document.getElementById('uploadImage').value = ""; 
    new bootstrap.Modal(document.getElementById('uploadModal')).show();
}

async function submitUpload() {
    const id = document.getElementById('uploadId').value;
    const title = document.getElementById('uploadTitle').value;
    const category = document.getElementById('uploadCategory').value;
    const description = document.getElementById('uploadDesc').value;
    const imageInput = document.getElementById('uploadImage');
    const imageFile = imageInput.files[0]; 

    if (!title || !category) return alert("Vui lòng điền đủ thông tin!");
    if (!id && !imageFile) return alert("Vui lòng chọn ảnh minh họa!");

    const formData = new FormData();
    formData.append("title", title);
    formData.append("category", category);
    formData.append("description", description);
    if (imageFile) formData.append("image", imageFile);

    const url = id ? `/api/designs/${id}` : '/api/designs/upload';
    const method = id ? 'PUT' : 'POST'; 

    try {
        const response = await fetch(url, {
            method: method,
            // QUAN TRỌNG: Gửi kèm Cookie Session để Server biết ai đang đăng bài
            credentials: 'include',
            body: formData 
        });
        
        const text = await response.text();
        if (response.ok) {
            alert("✅ Thành công!");
            location.reload();
        } else {
            alert("❌ Lỗi: " + text);
        }
    } catch (e) { alert("Lỗi kết nối server!"); }
}

async function deleteDesign(id) {
    if (!confirm("Bạn có chắc muốn xóa bài này không?")) return;
    try {
        const response = await fetch(`/api/designs/${id}`, { 
            method: 'DELETE',
            // QUAN TRỌNG: Gửi kèm Cookie Session
            credentials: 'include'
        });
        if (response.ok) {
            alert("🗑️ Đã xóa bài viết!");
            loadMyManagementDesigns(); 
        } else {
            alert("Không thể xóa bài viết này.");
        }
    } catch (e) { alert("Lỗi server"); }
}

// ==========================================
// 4. LOAD DATA (CỘNG ĐỒNG & CÁ NHÂN)
// ==========================================

// 4.1. Trang COMMUNITY
async function loadCommunityDesigns() {
    const listDiv = document.getElementById('communityList');
    if (!listDiv) return;

    try {
        const response = await fetch('/api/designs/public');
        
        if (!response.ok) {
            throw new Error(`Lỗi Server (${response.status})`);
        }
        
        const designs = await response.json();
        
        if (!designs || designs.length === 0) {
            listDiv.innerHTML = `
                <div class="col-12 text-center text-secondary py-5">
                    <i class="bi bi-inbox display-1 opacity-25"></i>
                    <h4 class="mt-3">Cộng đồng chưa có bài viết</h4>
                    <p>Hiện chưa có thiết kế nào trạng thái APPROVED.</p>
                </div>`;
            return;
        }

        let html = '';
        designs.forEach(d => {
            html += `
            <div class="col-md-3 col-sm-6 mb-4">
                <div class="card h-100 bg-dark border-secondary text-white shadow-sm hover-effect">
                    <div style="height: 200px; overflow: hidden;">
                        <img src="${d.imageUrl}" class="w-100 h-100" style="object-fit: cover;" onerror="this.src='/img/logo.png'">
                    </div>
                    <div class="card-body">
                        <div class="d-flex justify-content-between mb-1">
                            <span class="badge bg-primary">${d.category}</span>
                            <small class="text-muted">by ${d.creator ? d.creator.username : 'Ẩn danh'}</small>
                        </div>
                        <h6 class="card-title text-warning text-truncate">${d.title}</h6>
                    </div>
                </div>
            </div>`;
        });
        listDiv.innerHTML = html;

    } catch (e) { 
        console.error(e);
        listDiv.innerHTML = `<div class="col-12 text-center text-danger py-5">Không thể tải dữ liệu: ${e.message}</div>`;
    }
}

// 4.2. Trang MY DESIGNS
async function loadMyManagementDesigns() {
    const listDiv = document.getElementById('myDesignList');
    if (!listDiv) return; 

    const savedUserStr = localStorage.getItem("tyk_user") || sessionStorage.getItem("tyk_user");
    if(!savedUserStr) {
        window.location.href = "/";
        return;
    }

    try {
        const response = await fetch('/api/designs/my-designs', {
            method: 'GET',
            // QUAN TRỌNG: Gửi kèm Cookie để Server nhận ra user
            credentials: 'include'
        });

        // Kiểm tra content-type để tránh lỗi Unexpected token <
        const contentType = response.headers.get("content-type");
        const isJson = contentType && contentType.indexOf("application/json") !== -1;

        if (response.status === 401 || response.status === 403 || !isJson) {
            localStorage.removeItem("tyk_user");
            sessionStorage.removeItem("tyk_user");
            alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
            window.location.href = "/";
            return;
        }

        if (!response.ok) throw new Error("Lỗi tải dữ liệu");

        const designs = await response.json();
        
        if (!designs || designs.length === 0) {
            listDiv.innerHTML = `
                <div class="col-12 text-center text-muted py-5">
                    <i class="bi bi-folder2-open display-1 opacity-25"></i>
                    <h4 class="mt-3">Bạn chưa có thiết kế nào</h4>
                    <p>Bấm nút <b>+ Thêm mới</b> ở trên để bắt đầu nhé.</p>
                </div>`;
            return;
        }

        let html = '';
        designs.forEach(d => {
            const safeTitle = d.title.replace(/'/g, "\\'");
            const safeDesc = d.description ? d.description.replace(/'/g, "\\'") : "";
            
            let badgeClass = 'bg-secondary';
            if (d.status === 'APPROVED') badgeClass = 'bg-success';
            if (d.status === 'REJECTED') badgeClass = 'bg-danger';
            if (d.status === 'PENDING') badgeClass = 'bg-warning text-dark';

            html += `
            <div class="col-md-4 mb-4">
                <div class="card h-100 bg-dark border-secondary text-white shadow-sm">
                    <div style="height: 180px; overflow: hidden; position: relative;">
                         <img src="${d.imageUrl}" class="w-100 h-100" style="object-fit: cover;" onerror="this.src='/img/logo.png'">
                         <span class="position-absolute top-0 end-0 badge ${badgeClass} m-2">${d.status}</span>
                    </div>
                    <div class="card-body">
                        <small class="text-info fw-bold">${d.category}</small>
                        <h5 class="card-title text-warning text-truncate mt-1">${d.title}</h5>
                        <p class="card-text small text-secondary text-truncate">${d.description || ''}</p>
                    </div>
                    <div class="card-footer border-secondary bg-transparent d-flex justify-content-end gap-2">
                        <button class="btn btn-sm btn-outline-info" 
                            onclick="editDesign(${d.id}, '${safeTitle}', '${d.category}', '${safeDesc}', '${d.imageUrl}')">
                            <i class="bi bi-pencil"></i> Sửa
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteDesign(${d.id})">
                            <i class="bi bi-trash"></i> Xóa
                        </button>
                    </div>
                </div>
            </div>`;
        });
        listDiv.innerHTML = html;

    } catch (e) { 
        console.error(e);
        listDiv.innerHTML = `<div class="col-12 text-center text-danger py-5">Lỗi: ${e.message}</div>`;
    }
}