const API_BASE = "/api/v1";
const DEMO_USER_ID = "USR-1000";

const FALLBACK_USER = {
  fullName: "Trần Thùy Dương",
  phone: "0947 568 961",
  address: "Đường số 5, TP. Thủ Đức"
};

const FALLBACK_MENU = [
  {
    id: "ITEM-1",
    name: "Margherita Pizza",
    description: "Pizza phô mai truyền thống",
    price: 89000,
    categoryCode: "PIZZA"
  },
  {
    id: "ITEM-2",
    name: "Penne Arrabbiata",
    description: "Mỳ ống sốt cà chua cay",
    price: 79900,
    categoryCode: "COMBO"
  },
  {
    id: "ITEM-3",
    name: "Caesar Salad",
    description: "Salad rau tươi sốt kem",
    price: 55900,
    categoryCode: "COMBO"
  }
];

const FALLBACK_ORDER = {
  orderId: "ORD-2048",
  userId: DEMO_USER_ID,
  items: [
    {
      menuItemId: "ITEM-1",
      name: "Margherita Pizza",
      quantity: 1,
      unitPrice: 89000,
      lineTotal: 89000
    },
    {
      menuItemId: "ITEM-2",
      name: "Penne Arrabbiata",
      quantity: 1,
      unitPrice: 79900,
      lineTotal: 79900
    },
    {
      menuItemId: "ITEM-3",
      name: "Caesar Salad",
      quantity: 1,
      unitPrice: 55900,
      lineTotal: 55900
    }
  ],
  subtotal: 224800,
  shippingFee: 10000,
  discount: 0,
  total: 234800,
  voucherCode: null,
  paymentMethod: "CASH",
  deliveryAddress: FALLBACK_USER.address,
  note: null,
  scheduledAt: null,
  status: "PLACED",
  timeline: [],
  createdAt: new Date().toISOString(),
  review: null
};

const FOOD_IMAGES = [
  "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=700&q=80",
  "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=700&q=80",
  "https://images.unsplash.com/photo-1546793665-c74683f339c1?auto=format&fit=crop&w=700&q=80",
  "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=700&q=80"
];

const state = {
  order: FALLBACK_ORDER,
  user: FALLBACK_USER,
  menuItems: FALLBACK_MENU,
  rating: 0
};

document.addEventListener("DOMContentLoaded", () => {
  initTabs();
  bindStaticActions();
  loadDataAndRender();
});

function initTabs() {
  const tabs = Array.from(document.querySelectorAll(".screen-tab"));
  tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      setActiveScreen(tab.dataset.screen || "success");
    });
  });
}

function setActiveScreen(screenKey) {
  const tabs = document.querySelectorAll(".screen-tab");
  const screens = document.querySelectorAll(".screen");

  tabs.forEach((tab) => {
    const isActive = tab.dataset.screen === screenKey;
    tab.classList.toggle("active", isActive);
    tab.setAttribute("aria-selected", String(isActive));
  });

  screens.forEach((screen) => {
    const isActive = screen.id === `screen-${screenKey}`;
    screen.classList.toggle("active", isActive);
    screen.setAttribute("aria-hidden", String(!isActive));
  });
}

function bindStaticActions() {
  const printBtn = document.getElementById("print-invoice-btn");
  const savePdfBtn = document.getElementById("save-pdf-btn");
  const reviewBtn = document.getElementById("review-submit");

  if (printBtn) {
    printBtn.addEventListener("click", () => window.print());
  }

  if (savePdfBtn) {
    savePdfBtn.addEventListener("click", () => {
      showFeedback("Nhấn Ctrl/Cmd + P và chọn Save as PDF để lưu hóa đơn.", false);
    });
  }

  bindRating();

  if (reviewBtn) {
    reviewBtn.addEventListener("click", submitReview);
  }
}

function bindRating() {
  const stars = Array.from(document.querySelectorAll(".star"));
  stars.forEach((star) => {
    star.addEventListener("click", () => {
      const rating = Number(star.dataset.value || "0");
      state.rating = rating;
      updateRatingView(rating);
      showFeedback(`Bạn đã chọn ${rating} sao.`, false);
    });
  });
}

function updateRatingView(activeRating) {
  const stars = Array.from(document.querySelectorAll(".star"));
  stars.forEach((star) => {
    const value = Number(star.dataset.value || "0");
    star.classList.toggle("active", value <= activeRating);
  });
}

async function loadDataAndRender() {
  const [order, user, menuItems] = await Promise.all([
    loadLatestOrder(),
    loadUserProfile(),
    loadMenuItems()
  ]);

  state.order = order;
  state.user = user;
  state.menuItems = menuItems;

  renderSuccess();
  renderSuggestions();
  renderInvoice();
  renderActions();
  renderReview();
}

async function loadLatestOrder() {
  try {
    const path = `${API_BASE}/orders?userId=${encodeURIComponent(DEMO_USER_ID)}`;
    const orders = await requestJson(path);
    if (Array.isArray(orders) && orders.length > 0) {
      return orders[0];
    }
  } catch (error) {
    console.warn("Cannot fetch latest order, using fallback", error);
  }

  return FALLBACK_ORDER;
}

async function loadUserProfile() {
  try {
    const path = `${API_BASE}/users/${encodeURIComponent(DEMO_USER_ID)}/profile`;
    const profile = await requestJson(path);
    return {
      fullName: profile.fullName || FALLBACK_USER.fullName,
      phone: profile.phone || FALLBACK_USER.phone,
      address: profile.address || FALLBACK_USER.address
    };
  } catch (error) {
    console.warn("Cannot fetch user profile, using fallback", error);
    return FALLBACK_USER;
  }
}

async function loadMenuItems() {
  try {
    const menu = await requestJson(`${API_BASE}/menu/items`);
    if (Array.isArray(menu) && menu.length > 0) {
      return menu;
    }
  } catch (error) {
    console.warn("Cannot fetch menu, using fallback", error);
  }

  return FALLBACK_MENU;
}

function renderSuccess() {
  const eta = estimateDelivery(state.order.createdAt, 30);
  const orderIdEl = document.getElementById("success-order-id");
  const etaEl = document.getElementById("success-eta");
  const addressEl = document.getElementById("success-address");
  const highlightEl = document.getElementById("success-highlight");

  if (orderIdEl) {
    orderIdEl.textContent = state.order.orderId || FALLBACK_ORDER.orderId;
  }
  if (etaEl) {
    etaEl.textContent = eta;
  }
  if (addressEl) {
    addressEl.textContent = state.order.deliveryAddress || state.user.address;
  }

  const leadItem = state.order.items && state.order.items.length > 0
    ? state.order.items[0].name
    : "Combo hôm nay";

  if (highlightEl) {
    highlightEl.textContent = `Món nổi bật: ${leadItem} đang được chuẩn bị nóng hổi.`;
  }
}

function renderSuggestions() {
  const host = document.getElementById("suggestion-list");
  const userChipName = document.getElementById("customer-chip-name");
  if (!host) {
    return;
  }

  if (userChipName) {
    userChipName.textContent = state.user.fullName;
  }

  const selected = state.menuItems.slice(0, 3);
  host.innerHTML = "";

  selected.forEach((item, index) => {
    const card = document.createElement("article");
    card.className = `food-card ${index === 2 ? "feature" : ""}`.trim();

    const image = document.createElement("img");
    image.src = FOOD_IMAGES[index % FOOD_IMAGES.length];
    image.alt = item.name;

    const meta = document.createElement("div");
    meta.className = "meta";
    meta.innerHTML = `
      <strong>${escapeHtml(item.name)}</strong>
      <small>${formatCurrency(item.price)}</small>
    `;

    card.appendChild(image);
    card.appendChild(meta);
    host.appendChild(card);
  });
}

function renderInvoice() {
  const invoiceId = document.getElementById("invoice-id");
  const invoiceTime = document.getElementById("invoice-time");
  const customerName = document.getElementById("invoice-customer-name");
  const customerAddress = document.getElementById("invoice-customer-address");
  const orderTotal = document.getElementById("invoice-order-total");
  const itemList = document.getElementById("invoice-item-list");

  if (invoiceId) {
    invoiceId.textContent = `Mã hóa đơn: ${state.order.orderId}`;
  }
  if (invoiceTime) {
    invoiceTime.textContent = `Ngày tạo: ${formatDateTime(state.order.createdAt)}`;
  }
  if (customerName) {
    customerName.textContent = `Tên: ${state.user.fullName}`;
  }
  if (customerAddress) {
    customerAddress.textContent = `SĐT: ${state.user.phone} | Địa chỉ: ${state.user.address}`;
  }
  if (orderTotal) {
    orderTotal.textContent = formatCurrency(state.order.total);
  }

  if (!itemList) {
    return;
  }

  itemList.innerHTML = "";
  (state.order.items || []).forEach((item) => {
    const li = document.createElement("li");
    li.innerHTML = `
      <div>
        <strong>${escapeHtml(item.name)}</strong>
        <small>Số lượng: ${item.quantity}</small>
      </div>
      <span>${formatCurrency(item.lineTotal || item.unitPrice * item.quantity)}</span>
    `;
    itemList.appendChild(li);
  });
}

function renderActions() {
  const totalEl = document.getElementById("actions-total");
  const paymentEl = document.getElementById("actions-payment-method");
  const createdDateEl = document.getElementById("actions-created-date");
  const orderCodeEl = document.getElementById("actions-order-code");

  if (totalEl) {
    totalEl.textContent = formatCurrency(state.order.total);
  }
  if (paymentEl) {
    paymentEl.textContent = mapPaymentMethod(state.order.paymentMethod);
  }
  if (createdDateEl) {
    createdDateEl.textContent = `Ngày tạo: ${formatDateTime(state.order.createdAt)}`;
  }
  if (orderCodeEl) {
    orderCodeEl.textContent = `Mã đơn: ${state.order.orderId}`;
  }
}

function renderReview() {
  const title = document.getElementById("review-order-title");
  if (!title) {
    return;
  }

  const firstItem = state.order.items && state.order.items.length > 0
    ? state.order.items[0].name
    : "bữa ăn";

  title.textContent = `Bữa ăn từ [${firstItem}] của bạn thế nào?`;
}

async function submitReview() {
  const criteriaInput = document.getElementById("review-criteria");
  const commentInput = document.getElementById("review-comment");
  const submitBtn = document.getElementById("review-submit");

  if (state.rating <= 0) {
    showFeedback("Vui lòng chọn số sao trước khi gửi đánh giá.", true);
    return;
  }

  const commentParts = [];
  if (criteriaInput && criteriaInput.value.trim()) {
    commentParts.push(`Tiêu chí: ${criteriaInput.value.trim()}`);
  }
  if (commentInput && commentInput.value.trim()) {
    commentParts.push(commentInput.value.trim());
  }

  const payload = {
    rating: state.rating,
    comment: commentParts.join(" | ") || "Đánh giá nhanh từ giao diện BILL"
  };

  if (submitBtn) {
    submitBtn.disabled = true;
  }

  try {
    await requestJson(`${API_BASE}/orders/${encodeURIComponent(state.order.orderId)}/review`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    showFeedback("Đã gửi đánh giá thành công. Cảm ơn bạn!", false);
  } catch (error) {
    console.warn("Cannot submit review", error);
    showFeedback("Không thể gửi đánh giá lên server, nhưng nội dung đã được ghi nhận tại giao diện.", true);
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
    }
  }
}

async function requestJson(path, options) {
  const response = await fetch(path, options);
  if (!response.ok) {
    throw new Error(`Request failed (${response.status})`);
  }
  return response.json();
}

function estimateDelivery(createdAtIso, defaultMinutes) {
  const baseDate = createdAtIso ? new Date(createdAtIso) : new Date();
  const eta = new Date(baseDate.getTime() + defaultMinutes * 60 * 1000);
  return `${pad2(eta.getHours())}:${pad2(eta.getMinutes())}`;
}

function formatDateTime(value) {
  if (!value) {
    return "--";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  const day = pad2(date.getDate());
  const month = pad2(date.getMonth() + 1);
  const year = date.getFullYear();
  const hour = pad2(date.getHours());
  const minute = pad2(date.getMinutes());
  return `${day}/${month}/${year} ${hour}:${minute}`;
}

function mapPaymentMethod(method) {
  const mapping = {
    CASH: "Tiền mặt",
    CARD: "Thẻ ngân hàng",
    MOMO: "Ví điện tử Momo"
  };
  return mapping[method] || "Không xác định";
}

function formatCurrency(value) {
  const amount = typeof value === "number" ? value : Number(value || 0);
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(amount);
}

function showFeedback(message, isError) {
  const feedback = document.getElementById("review-feedback");
  if (!feedback) {
    return;
  }

  feedback.textContent = message;
  feedback.style.color = isError ? "#b42318" : "#166534";
}

function pad2(value) {
  return String(value).padStart(2, "0");
}

function escapeHtml(input) {
  return String(input ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
