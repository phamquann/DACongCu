// Dữ liệu mẫu cho voucher
const vouchers = [
  {
    code: 'BUGER20',
    name: 'Giảm 20% Burger',
    image: '🍔',
    type: 'Burger',
    condition: 'Giảm 20% (Đơn tối thiểu: 50.000)',
    expiry: '20/04/2026',
    status: 'Đang hoạt động'
  }
];

function renderVouchers() {
  const tbody = document.getElementById('voucherList');
  tbody.innerHTML = '';
  vouchers.forEach((v, idx) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><input type="checkbox"></td>
      <td>${v.code}</td>
      <td>${v.image} ${v.name}</td>
      <td>${v.type}</td>
      <td>${v.condition}</td>
      <td>${v.expiry}</td>
      <td>${v.status}</td>
      <td class="actions">
        <button title="Sửa">✏️</button>
        <button title="Xóa">❌</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

renderVouchers();
