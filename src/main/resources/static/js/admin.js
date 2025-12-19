/* admin.js - Admin Logic Only */

document.addEventListener("DOMContentLoaded", function() {
    loadPendingDesigns();
});

async function loadPendingDesigns() {
    const tableBody = document.getElementById('pendingList');
    if (!tableBody) return; 

    try {
        const response = await fetch('/api/designs/pending', {
            method: 'GET',
            credentials: 'include'
        });

        if (!response.ok) throw new Error("Could not load data: " + response.status);

        const designs = await response.json();

        if (!designs || designs.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted py-5">
                        <i class="bi bi-clipboard-check display-4 opacity-25"></i>
                        <p class="mt-3">There are no posts to review.</p>
                    </td>
                </tr>`;
            return;
        }

        let html = '';
        designs.forEach(d => {
            html += `
            <tr>
                <td class="ps-4">
                    <img src="${d.imageUrl}" class="rounded border border-secondary" style="width: 60px; height: 60px; object-fit: cover;" onerror="this.src='/img/logo.png'">
                </td>
                <td>
                    <div class="fw-bold text-warning">${d.title}</div>
                    <div class="small text-secondary text-truncate" style="max-width: 250px;">${d.description || 'No description'}</div>
                </td>
                <td><span class="badge bg-dark border border-secondary text-info">${d.category}</span></td>
                <td>${d.creatorName || 'Unknown'}</td>
                <td class="text-end pe-4">
                    <button class="btn btn-sm btn-success me-2" onclick="updateDesignStatus(${d.id}, 'APPROVED')">
                        <i class="bi bi-check-lg"></i> Approve
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="updateDesignStatus(${d.id}, 'REJECTED')">
                        <i class="bi bi-x-lg"></i> Reject
                    </button>
                </td>
            </tr>
            `;
        });
        tableBody.innerHTML = html;
    } catch (e) {
        console.error(e);
        tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Could not load data!<br><small>${e.message}</small></td></tr>`;
    }
}

async function updateDesignStatus(id, newStatus) {
    const actionName = newStatus === 'APPROVED' ? 'APPROVE' : 'REJECT';
    if (!confirm(`Are you sure you want to ${actionName} this design?`)) return;

    try {
        const response = await fetch(`/api/designs/${id}/status?status=${newStatus}`, {
            method: 'PUT',
            credentials: 'include'
        });

        if (response.ok) {
            alert("✅ Successfully processed!");
            loadPendingDesigns(); 
        } else {
            const text = await response.text();
            alert("❌ Error: " + text);
        }
    } catch (e) {
        console.error(e);
        alert("Connection error!");
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
            alert("🗑️ Successfully deleted the design!");
            location.reload();
        } else {
            alert("Could not delete this design.");
        }
    } catch (e) { alert("Server error"); }
}