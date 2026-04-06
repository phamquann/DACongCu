document.addEventListener("DOMContentLoaded", async () => {
  const categoryNav = document.getElementById("home-category-nav");
  const menuGrid = document.getElementById("home-menu-grid");
  const searchInput = document.getElementById("home-search-input");
  const searchButton = document.getElementById("home-search-button");
  const loginButton = document.getElementById("home-login-btn");
  const browseButton = document.getElementById("home-browse-btn");

  if (!menuGrid) {
    return;
  }

  const user = FoodApp.getCurrentUser();
  if (loginButton) {
    loginButton.textContent = user ? "Vào đặt món" : "Đăng nhập";
    loginButton.addEventListener("click", () => {
      window.location.href = user ? "/booking" : "/login";
    });
  }

  if (browseButton) {
    browseButton.addEventListener("click", () => {
      const keyword = searchInput ? searchInput.value.trim() : "";
      const path = keyword ? `/booking?keyword=${encodeURIComponent(keyword)}` : "/booking";
      window.location.href = path;
    });
  }

  if (searchButton) {
    searchButton.addEventListener("click", () => {
      const keyword = searchInput ? searchInput.value.trim() : "";
      const path = keyword ? `/booking?keyword=${encodeURIComponent(keyword)}` : "/booking";
      window.location.href = path;
    });
  }

  try {
    const [categories, items] = await Promise.all([
      FoodApp.api("/categories"),
      FoodApp.api("/menu/items")
    ]);

    renderCategories(categoryNav, categories);
    renderMenu(menuGrid, items.slice(0, 6));
  } catch (error) {
    menuGrid.innerHTML = `<p style="color:#b42318">Không tải được thực đơn: ${FoodApp.escapeHtml(
      FoodApp.summarizeError(error)
    )}</p>`;
  }
});

function renderCategories(host, categories) {
  if (!host || !Array.isArray(categories)) {
    return;
  }

  host.innerHTML = "";
  categories.forEach((category, index) => {
    const li = document.createElement("li");
    li.innerHTML = `<a href="/booking?categoryCode=${encodeURIComponent(category.code)}" class="${
      index === 0 ? "active" : ""
    }">${FoodApp.escapeHtml(category.name)}</a>`;
    host.appendChild(li);
  });
}

function renderMenu(host, items) {
  host.innerHTML = "";

  items.forEach((item, index) => {
    const card = document.createElement("div");
    card.className = "food-item";
    card.innerHTML = `
      <div class="img-wrapper">
        ${index % 2 === 0 ? '<span class="discount-badge">Hot</span>' : ""}
        <img src="images/fast_food_combo_1775239978559.png" alt="${FoodApp.escapeHtml(item.name)}">
      </div>
      <p class="food-name">${FoodApp.escapeHtml(item.name)}</p>
      <small class="food-price">${FoodApp.formatCurrency(item.price)}</small>
    `;
    card.style.cursor = "pointer";
    card.addEventListener("click", () => {
      window.location.href = `/booking?menuItemId=${encodeURIComponent(item.id)}`;
    });
    host.appendChild(card);
  });
}
