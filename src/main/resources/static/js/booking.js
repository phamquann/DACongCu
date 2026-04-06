document.addEventListener("DOMContentLoaded", async () => {
  try {
    FoodApp.requireAuth();
  } catch (error) {
    return;
  }

  const state = {
    cart: null,
    menuItems: [],
    filters: readFiltersFromUrl()
  };

  initScheduleControls();
  bindSearch(state);

  try {
    const [menuItems, cart] = await Promise.all([
      FoodApp.api(buildMenuPath(state.filters)),
      FoodApp.api(`/carts/${encodeURIComponent(FoodApp.getCurrentUserId())}`)
    ]);
    state.menuItems = Array.isArray(menuItems) ? menuItems : [];
    state.cart = cart;
    renderMenu(state.menuItems);
    renderHighlights(state.menuItems);
    renderCartSummary(state.cart);
  } catch (error) {
    renderBookingMessage(FoodApp.summarizeError(error), true);
  }
});

function readFiltersFromUrl() {
  const params = new URLSearchParams(window.location.search);
  return {
    keyword: params.get("keyword") || "",
    categoryCode: params.get("categoryCode") || "",
    menuItemId: params.get("menuItemId") || ""
  };
}

function buildMenuPath(filters) {
  const params = new URLSearchParams();
  if (filters.keyword) {
    params.set("keyword", filters.keyword);
  }
  if (filters.categoryCode) {
    params.set("categoryCode", filters.categoryCode);
  }
  const query = params.toString();
  return `/menu/items${query ? `?${query}` : ""}`;
}

function bindSearch(state) {
  const input = document.querySelector(".search-input-wrapper input");
  const button = document.querySelector(".search-btn");
  if (input) {
    input.value = state.filters.keyword || "";
  }

  const submitSearch = () => {
    const keyword = input ? input.value.trim() : "";
    const path = keyword ? `/booking?keyword=${encodeURIComponent(keyword)}` : "/booking";
    window.location.href = path;
  };

  if (button) {
    button.addEventListener("click", submitSearch);
  }
  if (input) {
    input.addEventListener("keydown", (event) => {
      if (event.key === "Enter") {
        submitSearch();
      }
    });
  }
}

function initScheduleControls() {
  const buttons = Array.from(document.querySelectorAll(".date-btn")).filter((button) => /^\d+$/.test(button.textContent.trim()));
  const hourSelect = document.querySelector('.select-group select');
  const minuteSelect = document.querySelectorAll('.select-group select')[1];
  const peopleSelect = document.querySelectorAll('.select-group select')[2];

  buttons.forEach((button, index) => {
    button.dataset.offset = String(index);
    button.addEventListener("click", () => {
      buttons.forEach((candidate) => candidate.classList.remove("active"));
      button.classList.add("active");
      persistSchedule(hourSelect, minuteSelect, peopleSelect);
    });
  });

  [hourSelect, minuteSelect, peopleSelect].forEach((select) => {
    if (!select) {
      return;
    }
    select.addEventListener("change", () => persistSchedule(hourSelect, minuteSelect, peopleSelect));
  });

  persistSchedule(hourSelect, minuteSelect, peopleSelect);
}

function persistSchedule(hourSelect, minuteSelect, peopleSelect) {
  const activeButton = document.querySelector(".date-btn.active");
  const offset = Number(activeButton?.dataset.offset || 0);
  const baseDate = new Date();
  baseDate.setDate(baseDate.getDate() + offset);
  const hourValue = (hourSelect?.value || "10:00").split(":")[0];
  const minuteValue = minuteSelect?.value || "00";
  const schedule = {
    displayDate: `${FoodApp.formatDate(baseDate.toISOString())}`,
    displayTime: `${hourValue}:${minuteValue}`,
    people: peopleSelect?.value || "1 người",
    scheduledAt: `${baseDate.getFullYear()}-${String(baseDate.getMonth() + 1).padStart(2, "0")}-${String(
      baseDate.getDate()
    ).padStart(2, "0")}T${hourValue}:${minuteValue}:00`
  };
  FoodApp.setSchedule(schedule);
}

function renderMenu(items) {
  const host = document.querySelector(".menu-grid");
  if (!host) {
    return;
  }

  host.innerHTML = "";
  items.forEach((item) => {
    const card = document.createElement("div");
    card.className = "menu-card";
    card.innerHTML = `
      <div class="menu-card-header">
        <div class="menu-image">${iconForCategory(item.categoryCode)}</div>
        <div class="menu-info">
          <h3 class="menu-name">${FoodApp.escapeHtml(item.name)}</h3>
          <div class="menu-rating">
            <div class="stars">
              <span class="star">⭐</span>
              <span class="star">⭐</span>
              <span class="star">⭐</span>
              <span class="star">⭐</span>
              <span class="star">⭐</span>
            </div>
            <span class="rating-text">${scoreFromPrice(item.price)}</span>
          </div>
          <span class="review-count">${FoodApp.escapeHtml(item.description || "Món bán chạy của quán")}</span>
          <div class="item-price-inline" style="margin-top:10px;font-weight:700;color:#f97316">${FoodApp.formatCurrency(
            item.price
          )}</div>
        </div>
      </div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:10px;">
        <button class="detail-btn" type="button" data-item-id="${FoodApp.escapeHtml(item.id)}" style="border:1px solid #d1d5db;background:#fff;color:#374151;border-radius:8px;padding:10px;font-weight:700;cursor:pointer;">Chi tiết</button>
        <button class="add-btn" type="button" data-item-id="${FoodApp.escapeHtml(item.id)}" style="margin-top:0;">Thêm vào giỏ</button>
      </div>
    `;

    card.querySelector(".detail-btn").addEventListener("click", () => openProductDetail(item.id));
    card.querySelector(".add-btn").addEventListener("click", () => addToCart(item.id));
    host.appendChild(card);
  });
}

function renderHighlights(items) {
  const host = document.querySelector(".menu-list");
  if (!host) {
    return;
  }
  host.innerHTML = "";

  items.slice(0, 4).forEach((item) => {
    const node = document.createElement("div");
    node.className = "menu-item";
    node.innerHTML = `
      <div class="menu-item-avatar">${iconForCategory(item.categoryCode)}</div>
      <div class="menu-item-info">
        <div class="menu-item-name">${FoodApp.escapeHtml(item.name)}</div>
        <div class="menu-item-rating">
          <span class="star">⭐</span>
          <span class="menu-item-rating-value">${scoreFromPrice(item.price)}</span>
        </div>
      </div>
    `;
    node.addEventListener("click", () => openProductDetail(item.id));
    host.appendChild(node);
  });
}

function openProductDetail(menuItemId) {
  window.location.href = `/product?menuItemId=${encodeURIComponent(menuItemId)}`;
}

function renderCartSummary(cart) {
  const infoHost = document.querySelector(".reservation-info");
  const button = document.querySelector(".reservation-confirm");
  const title = document.querySelector(".reservation-title");

  if (!infoHost || !button || !title) {
    return;
  }

  const schedule = FoodApp.getSchedule();
  const itemCount = cart?.itemCount || 0;
  title.textContent = "Giỏ hàng & lịch giao";
  infoHost.innerHTML = `
    <div class="reservation-row">
      <span>Ngày:</span>
      <strong>${FoodApp.escapeHtml(schedule?.displayDate || "--")}</strong>
    </div>
    <div class="reservation-row">
      <span>Giờ:</span>
      <strong>${FoodApp.escapeHtml(schedule?.displayTime || "--")}</strong>
    </div>
    <div class="reservation-row">
      <span>Số món:</span>
      <strong>${itemCount}</strong>
    </div>
    <div class="reservation-row">
      <span>Tạm tính:</span>
      <strong>${FoodApp.formatCurrency(cart?.subtotal || 0)}</strong>
    </div>
  `;
  button.textContent = itemCount > 0 ? "Xem giỏ và thanh toán" : "Chọn món để tiếp tục";
  button.disabled = itemCount <= 0;
  button.style.opacity = itemCount > 0 ? "1" : "0.7";
  button.onclick = () => {
    if (itemCount > 0) {
      window.location.href = "/checkout";
    }
  };
}

async function addToCart(menuItemId) {
  try {
    const userId = FoodApp.getCurrentUserId();
    const cart = await FoodApp.api(`/carts/${encodeURIComponent(userId)}/items`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ menuItemId, quantity: 1, options: {} })
    });
    renderCartSummary(cart);
    renderBookingMessage("Đã thêm món vào giỏ hàng.");
  } catch (error) {
    renderBookingMessage(FoodApp.summarizeError(error), true);
  }
}

function renderBookingMessage(text, isError) {
  let host = document.getElementById("booking-feedback");
  if (!host) {
    host = document.createElement("p");
    host.id = "booking-feedback";
    host.style.marginTop = "12px";
    host.style.fontWeight = "600";
    const sidebar = document.querySelector(".reservation-box");
    if (sidebar) {
      sidebar.appendChild(host);
    }
  }

  host.textContent = text || "";
  host.style.color = isError ? "#fecaca" : "#ffffff";
}

function iconForCategory(categoryCode) {
  return (
    {
      BURGER: "🍔",
      PIZZA: "🍕",
      FRIED_CHICKEN: "🍗",
      COMBO: "🍟",
      DRINK: "🥤"
    }[categoryCode] || "🍽️"
  );
}

function scoreFromPrice(price) {
  const numeric = Number(price || 0);
  return (4.4 + (numeric % 5) / 10).toFixed(1);
}
