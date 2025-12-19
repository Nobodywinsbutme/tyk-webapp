/* design.js - User Upload & Display Logic */

// 1. UPLOAD & EDIT FORM LOGIC
function checkLoginAndOpenUpload() {
    const user = localStorage.getItem("tyk_user") || sessionStorage.getItem("tyk_user");
    if (!user) {
        alert("⚠️ You must log in to upload a design!");
        sessionStorage.setItem("redirect_after_login", "/my-designs");
        new bootstrap.Modal(document.getElementById('loginModal')).show();
    } else {
        document.getElementById('uploadForm').reset();
        document.getElementById('uploadId').value = ''; 
        document.getElementById('modalTitleLabel').innerText = "New design";
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
    document.getElementById('modalTitleLabel').innerText = "Edit design";
    
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

    if (!title || !category) return alert("Please fill in all required fields!");
    if (!id && !imageFile) return alert("Please select an image!");

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
            credentials: 'include',
            body: formData 
        });
        
        const text = await response.text();
        if (response.ok) {
            alert("✅ Thành công!");
            location.reload();
        } else {
            alert("❌ Error: " + text);
        }
    } catch (e) { alert("Connection error!"); }
}

// 2. LOAD DATA FUNCTIONS
async function loadCommunityDesigns() {
    const listDiv = document.getElementById('communityList');
    if (!listDiv) return;

    try {
        const response = await fetch('/api/designs/public');
        if (!response.ok) throw new Error(`Server error (${response.status})`);
        
        const designs = await response.json();
        
        if (!designs || designs.length === 0) {
            listDiv.innerHTML = `
                <div class="col-12 text-center text-secondary py-5">
                    <i class="bi bi-inbox display-1 opacity-25"></i>
                    <h4 class="mt-3">Community has no posts yet</h4>
                    <p>There are no designs with APPROVED status.</p>
                </div>`;
            return;
        }

        const savedUser = localStorage.getItem("tyk_user") || sessionStorage.getItem("tyk_user");
        const currentUser = savedUser ? JSON.parse(savedUser) : null;
        const isAdmin = currentUser && currentUser.role === 'ADMIN';
        
        let html = '';
        designs.forEach(d => {
            let adminAction = '';
            if (isAdmin) {
                adminAction = `
                    <button class="btn btn-sm btn-danger position-absolute top-0 start-0 m-2" 
                            onclick="deleteDesign(${d.id})" title="Admin delete post">
                        <i class="bi bi-trash"></i>
                    </button>`;
            }

            html += `
            <div class="col-md-3 col-sm-6 mb-4">
                <div class="card h-100 bg-dark border-secondary text-white shadow-sm hover-effect position-relative">
                    ${adminAction} 
                    <div style="height: 200px; overflow: hidden;">
                        <img src="${d.imageUrl}" class="w-100 h-100" style="object-fit: cover;" onerror="this.src='/img/logo.png'">
                    </div>
                    <div class="card-body">
                        <div class="d-flex justify-content-between mb-1">
                            <span class="badge bg-primary">${d.category}</span>
                            <small class="text-muted">by ${d.creatorName || 'Unknow'}</small>
                        </div>
                        <h6 class="card-title text-warning text-truncate">${d.title}</h6>
                    </div>
                </div>
            </div>`;
        });
        listDiv.innerHTML = html;
    } catch (e) { 
        console.error(e);
        listDiv.innerHTML = `<div class="col-12 text-center text-danger py-5">Could not load data: ${e.message}</div>`;
    }
}

async function loadMyManagementDesigns() {
    const listDiv = document.getElementById('myDesignList');
    if (!listDiv) return; 

    try {
        const response = await fetch('/api/designs/my-designs', {
            method: 'GET',
            credentials: 'include'
        });

        const contentType = response.headers.get("content-type");
        const isJson = contentType && contentType.indexOf("application/json") !== -1;

        if (response.status === 401 || response.status === 403 || !isJson) {
            localStorage.removeItem("tyk_user");
            sessionStorage.removeItem("tyk_user");
            alert("Please login again!");
            window.location.href = "/";
            return;
        }

        if (!response.ok) throw new Error("Could not load designs: " + response.status);
        const designs = await response.json();
        
        if (!designs || designs.length === 0) {
            listDiv.innerHTML = `
                <div class="col-12 text-center text-muted py-5">
                    <i class="bi bi-folder2-open display-1 opacity-25"></i>
                    <h4 class="mt-3">You do not have any designs yet</h4>
                    <p>Click the <b>+ Add New</b> button above to get started.</p>
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
                            <i class="bi bi-pencil"></i> Edit
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteDesign(${d.id})">
                            <i class="bi bi-trash"></i> Delete
                        </button>
                    </div>
                </div>
            </div>`;
        });
        listDiv.innerHTML = html;
    } catch (e) { 
        console.error(e);
        listDiv.innerHTML = `<div class="col-12 text-center text-danger py-5">Error: ${e.message}</div>`;
    }
}

async function deleteDesign(id) {
    if (!confirm("Are you sure you want to delete this design?")) return;
    try {
        const response = await fetch(`/api/designs/${id}`, { 
            method: 'DELETE',
            credentials: 'include'
        });
        if (response.ok) {
            alert("🗑️ Deleted design successfully!");
            location.reload();
        } else {
            alert("Could not delete this design.");
        }
    } catch (e) { alert("Server error"); }
}