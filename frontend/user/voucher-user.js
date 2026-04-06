// Dữ liệu mẫu cho voucher
const vouchers = [
  {
    code: 'BURGER50',
    name: 'Giảm 50% Burger',
    emoji: '🍔',
    discount: '50%',
    desc: 'Áp dụng cho tất cả các loại burger',
    info: 'Đơn tối thiểu: 100.000đ',
    expiry: '28/02/2026'
  },
  {
    code: 'PIZZA30K',
    name: 'Giảm 30K Pizza',
    emoji: '🍕',
    discount: '30K',
    desc: 'Giảm 30.000đ cho đơn pizza từ 150.000đ',
    info: 'Đơn tối thiểu: 150.000đ',
    expiry: '15/02/2026'
  },
  {
    code: 'GARAN20',
    name: 'Giảm 40% Gà Rán',
    emoji: '🍗',
    discount: '20%',
    desc: 'Ưu đãi đặc biệt cho gà rán',
    info: 'Đơn tối thiểu: 120.000đ',
    expiry: '10/03/2026'
  },
  {
    code: 'COMBO3X',
    name: 'Mua 1 Tặng 2',
    emoji: '🍟',
    discount: '1+2',
    desc: 'Mua combo gà rán tặng 1 coca và khoai tây chiên',
    info: '',
    expiry: '25/02/2026'
  }
];

function renderVouchers() {
  const list = document.getElementById('voucherList');
  list.innerHTML = '';
  vouchers.forEach(v => {
    const card = document.createElement('div');
    card.className = 'voucher-card';
    card.innerHTML = `
      <div class="discount">${v.discount}</div>
      <div class="emoji">${v.emoji}</div>
      <div class="name">${v.name}</div>
      <div class="desc">${v.desc}</div>
      <div class="info">${v.info}</div>
      <div class="info">HSD: ${v.expiry}</div>
      <button class="code">${v.code}</button>
    `;
    list.appendChild(card);
  });
}

renderVouchers();
