document.addEventListener("DOMContentLoaded", async () => {
  try {
    FoodApp.requireAuth();
  } catch (error) {
    return;
  }

  const state = {
    cart: null,
    profile: null,
    vouchers: [],
    paymentMethod: "CASH"
  };

  bindPaymentOptions(state);
  bindCheckoutActions(state);

  try {
    await loadCheckoutState(state);
    const pendingVoucher = FoodApp.consumePendingVoucher();
    if (pendingVoucher) {
      await applyVoucher(state, pendingVoucher, false);
    }
  } catch (error) {
    renderCheckoutMessage(FoodApp.summarizeError(error), true);
  }
});

async function loadCheckoutState(state) {
  const userId = FoodApp.getCurrentUserId();
  const [profile, cart, vouchers] = await Promise.all([
    FoodApp.api(`/users/${encodeURIComponent(userId)}/profile`),
    FoodApp.api(`/carts/${encodeURIComponent(userId)}`),
    FoodApp.api("/vouchers")
  ]);

  state.profile = profile;
  state.cart = cart;
  state.vouchers = vouchers;
  renderDelivery(profile);
  renderCheckoutItems(cart);
  renderVoucherTags(state, vouchers);
  renderSummary(cart);
}

function bindPaymentOptions(state) {
  document.querySelectorAll(".payment-option").forEach((option) => {
    const method = option.dataset.method;
    option.addEventListener("click", () => {
      state.paymentMethod = method;
      document.querySelectorAll(".radio").forEach((radio) => radio.classList.remove("checked"));
      option.querySelector(".radio")?.classList.add("checked");
    });
  });
}

function bindCheckoutActions(state) {
  const voucherButton = document.getElementById("checkout-apply-voucher");
  const voucherInput = document.getElementById("checkout-voucher-input");
  const orderButton = document.getElementById("checkout-submit");

  if (voucherButton) {
    voucherButton.addEventListener("click", async () => {
      await applyVoucher(state, voucherInput?.value || "", true);
    });
  }

  if (orderButton) {
    orderButton.addEventListener("click", async () => {
      await submitCheckout(state);
    });
  }
}

function renderDelivery(profile) {
  const nameEl = document.getElementById("checkout-name");
  const phoneEl = document.getElementById("checkout-phone");
  const addressEl = document.getElementById("checkout-address");

  if (nameEl) {
    nameEl.value = profile.fullName || "";
  }
  if (phoneEl) {
    phoneEl.value = profile.phone || "";
  }
  if (addressEl) {
    addressEl.value = profile.address || "";
  }
}

function renderCheckoutItems(cart) {
  const host = document.getElementById("checkout-items");
  if (!host) {
    return;
  }

  host.innerHTML = "";
  if (!cart.items || cart.items.length === 0) {
    host.innerHTML = '<p style="color:#666">Giỏ hàng đang trống. Vui lòng quay lại trang đặt món.</p>';
    return;
  }

  cart.items.forEach((item) => {
    const row = document.createElement("div");
    row.className = "menu-item";
    row.innerHTML = `
      <div class="item-image">${iconForCart(item.name)}</div>
      <div class="item-details">
        <div class="item-name">${FoodApp.escapeHtml(item.name)}</div>
        <div class="item-price">${FoodApp.formatCurrency(item.unitPrice)}</div>
        <div class="item-controls">
          <div class="qty-control">
            <button class="qty-btn" type="button" data-change="-1">−</button>
            <input type="number" class="qty-input" value="${item.quantity}" readonly>
            <button class="qty-btn" type="button" data-change="1">+</button>
          </div>
          <button type="button" class="remove-btn" style="border:none;background:none;color:#b42318;font-weight:700;cursor:pointer">Xóa</button>
        </div>
      </div>
    `;

    row.querySelectorAll(".qty-btn").forEach((button) => {
      button.addEventListener("click", async () => {
        const nextQuantity = item.quantity + Number(button.dataset.change || "0");
        await updateCartItem(item.cartItemId, nextQuantity);
      });
    });

    row.querySelector(".remove-btn").addEventListener("click", async () => {
      await removeCartItem(item.cartItemId);
    });

    host.appendChild(row);
  });
}

function renderVoucherTags(state, vouchers) {
  const host = document.getElementById("checkout-voucher-tags");
  if (!host) {
    return;
  }

  host.innerHTML = "";
  vouchers.slice(0, 4).forEach((voucher) => {
    const tag = document.createElement("div");
    tag.className = "tag";
    tag.textContent = voucher.code;
    tag.title = voucher.title;
    tag.addEventListener("click", async () => {
      document.getElementById("checkout-voucher-input").value = voucher.code;
      await applyVoucher(state, voucher.code, true);
    });
    host.appendChild(tag);
  });
}

function renderSummary(cart) {
  setText("checkout-subtotal", FoodApp.formatCurrency(cart.subtotal));
  setText("checkout-shipping", FoodApp.formatCurrency(cart.shippingFee));
  setText("checkout-discount", `-${FoodApp.formatCurrency(cart.discount)}`);
  setText("checkout-total", FoodApp.formatCurrency(cart.total));
  setText(
    "checkout-schedule",
    FoodApp.getSchedule()
      ? `${FoodApp.getSchedule().displayDate} ${FoodApp.getSchedule().displayTime}`
      : "Giao ngay"
  );
  const voucherInput = document.getElementById("checkout-voucher-input");
  if (voucherInput) {
    voucherInput.value = cart.appliedVoucherCode || voucherInput.value || "";
  }
}

async function updateCartItem(cartItemId, quantity) {
  try {
    const userId = FoodApp.getCurrentUserId();
    const cart = await FoodApp.api(`/carts/${encodeURIComponent(userId)}/items/${encodeURIComponent(cartItemId)}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ quantity })
    });
    await refreshCheckout(cart);
  } catch (error) {
    renderCheckoutMessage(FoodApp.summarizeError(error), true);
  }
}

async function removeCartItem(cartItemId) {
  try {
    const userId = FoodApp.getCurrentUserId();
    const cart = await FoodApp.api(`/carts/${encodeURIComponent(userId)}/items/${encodeURIComponent(cartItemId)}`, {
      method: "DELETE"
    });
    await refreshCheckout(cart);
  } catch (error) {
    renderCheckoutMessage(FoodApp.summarizeError(error), true);
  }
}

async function applyVoucher(state, code, showMessage) {
  const userId = FoodApp.getCurrentUserId();
  const normalizedCode = code.trim();
  try {
    const cart = await FoodApp.api(`/carts/${encodeURIComponent(userId)}/apply-voucher`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code: normalizedCode })
    });
    state.cart = cart;
    renderSummary(cart);
    if (showMessage) {
      renderCheckoutMessage(normalizedCode ? "Đã áp dụng mã ưu đãi." : "Đã xóa mã ưu đãi.");
    }
  } catch (error) {
    renderCheckoutMessage(FoodApp.summarizeError(error), true);
  }
}

async function submitCheckout(state) {
  const button = document.getElementById("checkout-submit");
  const address = document.getElementById("checkout-address")?.value.trim() || "";
  const note = document.getElementById("checkout-note")?.value.trim() || "";
  const schedule = FoodApp.getSchedule();

  button.disabled = true;
  try {
    const order = await FoodApp.api("/orders/checkout", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        userId: FoodApp.getCurrentUserId(),
        deliveryAddress: address,
        note,
        paymentMethod: state.paymentMethod,
        scheduledAt: schedule?.scheduledAt || null
      })
    });

    FoodApp.setLastOrderId(order.orderId);
    FoodApp.setSchedule(null);
    window.location.href = FoodApp.withOrderId("/bill/success", order.orderId);
  } catch (error) {
    renderCheckoutMessage(FoodApp.summarizeError(error), true);
  } finally {
    button.disabled = false;
  }
}

async function refreshCheckout(cart) {
  renderCheckoutItems(cart);
  renderSummary(cart);
}

function renderCheckoutMessage(text, isError) {
  const host = document.getElementById("checkout-message");
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

function iconForCart(name) {
  if (/pizza/i.test(name)) return "🍕";
  if (/ga|chicken/i.test(name)) return "🍗";
  if (/burger/i.test(name)) return "🍔";
  if (/pepsi|drink/i.test(name)) return "🥤";
  return "🍽️";
}
