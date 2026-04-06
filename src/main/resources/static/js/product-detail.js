document.addEventListener("DOMContentLoaded", async () => {
  try {
    FoodApp.requireAuth();
  } catch (error) {
    return;
  }

  const state = {
    item: null,
    quantity: 1
  };

  bindTopActions();
  bindQuantityControls(state);

  try {
    const item = await loadCurrentItem();
    state.item = item;
    renderItem(item);
    renderAddButton(state);
    bindAddToCart(state);
  } catch (error) {
    renderMessage(FoodApp.summarizeError(error), true);
  }
});

function bindTopActions() {
  const logoutButton = document.getElementById("product-logout-btn");
  if (logoutButton) {
    logoutButton.addEventListener("click", () => {
      FoodApp.logout("/login");
    });
  }
}

async function loadCurrentItem() {
  const params = new URLSearchParams(window.location.search);
  const menuItemId = params.get("menuItemId");

  const items = await FoodApp.api("/menu/items");
  const safeItems = Array.isArray(items) ? items : [];
  if (safeItems.length === 0) {
    throw new Error("Khong tim thay san pham.");
  }

  if (!menuItemId) {
    return safeItems[0];
  }

  return (
    safeItems.find((item) => item.id === menuItemId) ||
    safeItems[0]
  );
}

function renderItem(item) {
  setText("product-name", item.name || "San pham");
  setText("product-price", toMoney(item.price));
  setText("product-desc", item.description || "San pham dac biet trong ngay.");

  const image = document.getElementById("product-image");
  if (image) {
    image.src = imageByCategory(item.categoryCode);
    image.alt = item.name || "San pham";
  }
}

function bindQuantityControls(state) {
  const minus = document.getElementById("qty-minus");
  const plus = document.getElementById("qty-plus");

  if (minus) {
    minus.addEventListener("click", () => {
      state.quantity = Math.max(1, state.quantity - 1);
      renderQuantity(state.quantity);
      renderAddButton(state);
    });
  }

  if (plus) {
    plus.addEventListener("click", () => {
      state.quantity = Math.min(99, state.quantity + 1);
      renderQuantity(state.quantity);
      renderAddButton(state);
    });
  }
}

function bindAddToCart(state) {
  const addButton = document.getElementById("add-cart-btn");
  if (!addButton) {
    return;
  }

  addButton.addEventListener("click", async () => {
    if (!state.item) {
      return;
    }

    addButton.disabled = true;
    try {
      await FoodApp.api(`/carts/${encodeURIComponent(FoodApp.getCurrentUserId())}/items`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          menuItemId: state.item.id,
          quantity: state.quantity,
          options: {}
        })
      });
      renderMessage("Da them vao gio hang. Ban co the tiep tuc dat mon hoac thanh toan.");
    } catch (error) {
      renderMessage(FoodApp.summarizeError(error), true);
    } finally {
      addButton.disabled = false;
    }
  });
}

function renderQuantity(quantity) {
  setText("qty-value", String(quantity));
}

function renderAddButton(state) {
  const addButton = document.getElementById("add-cart-btn");
  if (!addButton || !state.item) {
    return;
  }
  const total = Number(state.item.price || 0) * Number(state.quantity || 1);
  addButton.textContent = `Them vao gio hang - ${toMoney(total)}`;
}

function renderMessage(text, isError) {
  const host = document.getElementById("product-message");
  if (!host) {
    return;
  }
  host.textContent = text || "";
  host.style.color = isError ? "#b42318" : "#166534";
}

function setText(id, value) {
  const element = document.getElementById(id);
  if (element) {
    element.textContent = value;
  }
}

function toMoney(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(Number(value || 0));
}

function imageByCategory(categoryCode) {
  const map = {
    BURGER: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=900&q=80",
    PIZZA: "https://images.unsplash.com/photo-1548365328-9f547fb0953f?auto=format&fit=crop&w=900&q=80",
    FRIED_CHICKEN: "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=900&q=80",
    COMBO: "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=900&q=80",
    DRINK: "https://images.unsplash.com/photo-1544145945-f90425340c7e?auto=format&fit=crop&w=900&q=80"
  };

  return map[categoryCode] || "https://images.unsplash.com/photo-1515003197210-e0cd71810b5f?auto=format&fit=crop&w=900&q=80";
}
