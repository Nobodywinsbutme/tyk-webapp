/* news.js - News Management Logic based on design.js pattern */

document.addEventListener("DOMContentLoaded", function() {
    loadNews();
    checkAdminControls();
});

// 1. LOAD DATA FUNCTION
async function loadNews() {
    const listDiv = document.getElementById('newsList');
    if (!listDiv) return;

    try {
        // Giả sử API của bạn là /api/news (Bạn cần tạo Controller Java tương ứng)
        const response = await fetch('/api/news/list'); 
        
        if (!response.ok) {
            // Nếu chưa có API, nó sẽ lỗi 404, ta hiện thông báo giả để không nát giao diện
            console.warn("API /api/news/list chưa sẵn sàng hoặc lỗi.");
            if(response.status === 404) {
                 // Fallback dữ liệu giả nếu chưa có API (để bạn test giao diện)
                 renderNewsList(listDiv, [
                    { id: 1, title: "Chào mừng đến với TYK", description: "Website đang trong quá trình thử nghiệm.", createdDate: "2025-05-01" }
                 ]);
                 return;
            }
            throw new Error(`Server error (${response.status})`);
        }
        
        const newsList = await response.json();
        renderNewsList(listDiv, newsList);

    } catch (e) { 
        console.error(e);
        listDiv.innerHTML = `<div class="col-12 text-center text-danger py-5">Could not load news: ${e.message}</div>`;
    }
}

// Hàm phụ trợ để vẽ HTML (tách ra cho gọn)
function renderNewsList(container, newsList) {
    if (!newsList || newsList.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center text-secondary py-5">
                <i class="bi bi-newspaper display-1 opacity-25"></i>
                <h4 class="mt-3">No news available</h4>
            </div>`;
        return;
    }

    // Lấy user từ localStorage giống hệt design.js
    const savedUser = localStorage.getItem("tyk_user") || sessionStorage.getItem("tyk_user");
    const currentUser = savedUser ? JSON.parse(savedUser) : null;
    const isAdmin = currentUser && currentUser.role === 'ADMIN';

    let html = '';
    newsList.forEach(n => {
        // Xử lý ký tự đặc biệt để tránh lỗi khi truyền vào hàm onclick
        const safeTitle = n.title.replace(/'/g, "\\'");
        const safeDesc = n.description ? n.description.replace(/'/g, "\\'").replace(/\n/g, " ") : "";

        let adminButtons = '';
        if (isAdmin) {
            adminButtons = `
                <div class="mt-3 border-top border-secondary pt-2 d-flex justify-content-end gap-2">
                    <button class="btn btn-sm btn-outline-info" 
                        onclick="openEditNewsModal(${n.id}, '${safeTitle}', '${safeDesc}')">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteNews(${n.id})">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </div>`;
        }

        html += `
        <div class="col-md-6 col-lg-4 mb-4">
            <div class="card h-100 bg-dark border-secondary text-white shadow-sm">
                <div class="card-body d-flex flex-column">
                    <h5 class="card-title text-warning">${n.title}</h5>
                    <h6 class="card-subtitle mb-2 text-muted small">${n.createdDate || 'Just now'}</h6>
                    <p class="card-text flex-grow-1" style="white-space: pre-line;">${n.description}</p>
                    ${adminButtons}
                </div>
            </div>
        </div>`;
    });
    container.innerHTML = html;
}

// 2. CHECK ADMIN BUTTON VISIBILITY (Nút Add News to ở trên)
function checkAdminControls() {
    const addBtn = document.getElementById("adminNewsControls");
    const savedUser = localStorage.getItem("tyk_user") || sessionStorage.getItem("tyk_user");
    const currentUser = savedUser ? JSON.parse(savedUser) : null;
    
    // Logic check quyền chuẩn theo design.js
    if (currentUser && currentUser.role === 'ADMIN') {
        addBtn.style.display = "block";
    } else {
        addBtn.style.display = "none";
    }
}

// 3. MODAL & FORM LOGIC
function openCreateNewsModal() {
    document.getElementById("newsForm").reset();
    document.getElementById("newsId").value = ""; 
    document.getElementById("modalTitle").innerText = "Create new post";
    new bootstrap.Modal(document.getElementById('newsModal')).show();
}

function openEditNewsModal(id, title, desc) {
    document.getElementById("newsId").value = id;
    document.getElementById("newsTitle").value = title;
    document.getElementById("newsDesc").value = desc;
    document.getElementById("modalTitle").innerText = "Edit post";
    new bootstrap.Modal(document.getElementById('newsModal')).show();
}

// 4. SUBMIT (CREATE & UPDATE)
async function saveNews() {
    const id = document.getElementById("newsId").value;
    const title = document.getElementById("newsTitle").value;
    const description = document.getElementById("newsDesc").value;

    if (!title || !description) return alert("Please fill in all required fields!");

    // Chuẩn bị dữ liệu gửi đi (JSON)
    const data = {
        title: title,
        description: description
    };

    let url, method;
    if (id) {
        url = `/api/news/update/${id}`; // Khớp với Java thêm bên dưới
        method = 'PUT';
    } else {
        url = '/api/news/create';       // Khớp với Java bạn gửi
        method = 'POST';
    }

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include', // Quan trọng để gửi kèm session cookie
            body: JSON.stringify(data)
        });

        if (response.ok) {
            alert("✅ Success!");
            // Ẩn modal
            const modalEl = document.getElementById('newsModal');
            const modalInstance = bootstrap.Modal.getInstance(modalEl);
            modalInstance.hide();
            // Load lại danh sách
            loadNews();
        } else {
            const text = await response.text();
            alert("❌ Error: " + text);
        }
    } catch (e) {
        alert("Connection error!");
        console.error(e);
    }
}

// 5. DELETE FUNCTION
async function deleteNews(id) {
    if (!confirm("Are you sure you want to delete this post?")) return;

    try {
        const response = await fetch(`/api/news/delete/${id}`, { 
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.ok) {
            alert("🗑️ Deleted successfully!");
            loadNews();
        } else {
            alert("Could not delete this post.");
        }
    } catch (e) {
        alert("Server error");
    }
}