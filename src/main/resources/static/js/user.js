// 1. Save Profile
async function saveProfile() {
    const avatarUrl = document.getElementById('avatarInput').value;
    
    try {
        const res = await fetch('/api/user/update-profile', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ avatarUrl: avatarUrl })
        });
        
        const text = await res.text();
        if (res.ok) {
            alert("✅ " + text);
            location.reload(); // Load lại trang để thấy avatar mới
        } else {
            alert("❌ " + text);
        }
    } catch (e) { alert("Lỗi kết nối!"); }
}

// 2. Change Password
async function changePassword() {
    const currentPass = document.getElementById('currentPass').value;
    const newPass = document.getElementById('newPass').value;
    const confirmPass = document.getElementById('confirmPass').value;

    if(newPass !== confirmPass) {
        alert("Mật khẩu xác nhận không khớp!");
        return;
    }

    try {
        const res = await fetch('/api/user/change-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                currentPassword: currentPass,
                newPassword: newPass,
                confirmPassword: confirmPass
            })
        });
        
        const text = await res.text();
        if (res.ok) {
            alert("✅ " + text);
            document.getElementById('passwordForm').reset();
        } else {
            alert("❌ " + text);
        }
    } catch (e) { alert("Lỗi kết nối!"); }
}