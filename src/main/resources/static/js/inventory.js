const userId = document.getElementById('userId').value;
let currentType = ''; 
let currentPage = 1;

// 1. Hàm gọi API
async function loadInventory(page = 1) {
    currentPage = page;
    const search = document.getElementById('searchInput').value;
    
    // Gọi API Backend (Đường dẫn khớp với Controller REST)
    let url = `/api/v1/inventory/${userId}?page=${page}&size=12`;
    if (currentType) url += `&type=${currentType}`;
    if (search) url += `&search=${search}`;

    try {
        const response = await fetch(url);
        const data = await response.json();
        renderItems(data.content); // Spring Page trả về data trong field 'content'
        renderPagination(data.totalPages, page);
        document.getElementById('totalItems').textContent = data.totalElements;
    } catch (error) {
        console.error('Error:', error);
    }
}

// 2. Hàm vẽ HTML cho từng Item
function renderItems(items) {
    const grid = document.getElementById('inventoryGrid');
    grid.innerHTML = ''; // Xóa cũ

    if (items.length === 0) {
        grid.innerHTML = '<p class="col-span-full text-center text-gray-500">No items found.</p>';
        return;
    }

    items.forEach(item => {
        // Logic hiển thị Badge
        let badge = item.tradable ? '' : 
            `<div class="absolute bottom-0 left-0 bg-gray-900/80 text-[10px] px-2 py-1 text-gray-300 w-full text-center">Cannot be traded</div>`;

        // HTML Card
        const card = `
            <div class="bg-[#2b2b2b] rounded-xl overflow-hidden hover:scale-105 transition-transform cursor-pointer border border-transparent hover:border-gray-500 group">
                <div class="h-40 bg-gray-800 flex items-center justify-center relative p-4">
                    <img src="${item.imageUrl || 'https://via.placeholder.com/100'}" alt="${item.name}" class="max-h-full max-w-full object-contain">
                    ${badge}
                </div>
                <div class="p-3">
                    <h3 class="font-bold text-white text-sm mb-1 truncate">${item.name}</h3>
                    <div class="flex justify-between text-xs text-gray-400">
                        <span>Qty: ${item.quantity}</span>
                        <span class="${item.rarity === 'RARE' ? 'text-yellow-400' : 'text-gray-400'}">${item.rarity}</span>
                    </div>
                </div>
            </div>
        `;
        grid.innerHTML += card;
    });
}

// 3. Hàm vẽ phân trang
function renderPagination(totalPages, current) {
    const pag = document.getElementById('pagination');
    pag.innerHTML = '';
    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement('button');
        btn.innerText = i;
        btn.className = `w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold 
            ${i === current ? 'bg-lime-400 text-black' : 'bg-gray-700 text-white hover:bg-gray-600'}`;
        btn.onclick = () => loadInventory(i);
        pag.appendChild(btn);
    }
}

// 4. Sự kiện Filter Type
function filterType(type) {
    currentType = type === currentType ? '' : type; // Toggle
    
    // Update UI nút bấm
    document.querySelectorAll('.filter-btn').forEach(btn => {
        if (btn.innerText.includes(type) && currentType === type) {
            btn.classList.add('bg-lime-400', 'text-black');
            btn.classList.remove('bg-gray-700');
        } else {
            btn.classList.remove('bg-lime-400', 'text-black');
            btn.classList.add('bg-gray-700');
        }
    });
    loadInventory(1); // Reset về trang 1
}

// 5. Sự kiện Search (Debounce nhẹ)
let timeout = null;
document.getElementById('searchInput').addEventListener('input', () => {
    clearTimeout(timeout);
    timeout = setTimeout(() => loadInventory(1), 500);
});

// Load lần đầu
loadInventory();