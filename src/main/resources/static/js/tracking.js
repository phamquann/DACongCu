document.addEventListener("DOMContentLoaded", async () => {
  try {
    FoodApp.requireAuth();
  } catch (error) {
    return;
  }

  try {
    const order = await loadTrackingOrder();
    const profile = await FoodApp.api(`/users/${encodeURIComponent(FoodApp.getCurrentUserId())}/profile`);
    renderTracking(order, profile);
  } catch (error) {
    renderTrackingMessage(FoodApp.summarizeError(error), true);
  }
});

async function loadTrackingOrder() {
  const orderId = FoodApp.getOrderIdFromUrl();
  if (orderId) {
    return FoodApp.api(`/orders/${encodeURIComponent(orderId)}`);
  }

  const orders = await FoodApp.api(`/orders?userId=${encodeURIComponent(FoodApp.getCurrentUserId())}`);
  if (!orders.length) {
    throw new Error("Bạn chưa có đơn hàng nào để theo dõi.");
  }
  return orders[0];
}

function renderTracking(order, profile) {
  setText("tracking-order-code", `Mã đơn: ${order.orderId}`);
  setText("tracking-recipient-name", profile.fullName || "Khách hàng");
  setText("tracking-recipient-phone", profile.phone || "");
  setText("tracking-recipient-address", order.deliveryAddress || profile.address || "");
  setText("tracking-payment-method", FoodApp.paymentLabel(order.paymentMethod));
  setText("tracking-subtotal", FoodApp.formatCurrency(order.subtotal));
  setText("tracking-shipping", FoodApp.formatCurrency(order.shippingFee));
  setText("tracking-total", FoodApp.formatCurrency(order.total));
  setText("tracking-status-pill", FoodApp.statusLabel(order.status));
  renderTimeline(order);
  renderActivity(order.timeline);
  renderTrackingItems(order.items);
  bindTrackingActions(order);
}

function renderTimeline(order) {
  const host = document.getElementById("tracking-progress");
  if (!host) {
    return;
  }

  const orderSteps = ["PLACED", "PREPARING", "DELIVERING", "DELIVERED"];
  const lookup = new Map((order.timeline || []).map((step) => [step.status, step]));

  host.innerHTML = orderSteps
    .map((status) => {
      const statusOrder = orderSteps.indexOf(status);
      const currentOrder = orderSteps.indexOf(order.status);
      const stateClass =
        status === order.status
          ? "active"
          : currentOrder > statusOrder || order.status === "DELIVERED"
            ? "completed"
            : "";
      const step = lookup.get(status);
      return `
        <div class="step ${stateClass}">
          <span class="step-icon">${iconForStatus(status)}</span>
          <span class="step-text">${FoodApp.escapeHtml(FoodApp.statusLabel(status))}<br><small>${
            step ? FoodApp.escapeHtml(FoodApp.formatTime(step.changedAt)) : "--"
          }</small></span>
        </div>
      `;
    })
    .join("");
}

function renderTrackingItems(items) {
  const host = document.getElementById("tracking-items");
  if (!host) {
    return;
  }

  const safeItems = Array.isArray(items) ? items : [];

  if (safeItems.length === 0) {
    host.innerHTML = '<p style="color:#666">Đơn hàng chưa có món nào.</p>';
    return;
  }

  host.innerHTML = safeItems
    .map(
      (item) => `
      <div class="item-row">
        <span>${FoodApp.escapeHtml(item.name)} x${item.quantity}</span>
        <span>${FoodApp.formatCurrency(item.lineTotal)}</span>
      </div>
    `
    )
    .join("");
}

function renderActivity(timeline) {
  const host = document.getElementById("tracking-activity");
  if (!host) {
    return;
  }

  const safeTimeline = Array.isArray(timeline) ? timeline : [];
  if (safeTimeline.length === 0) {
    host.innerHTML = '<p style="color:#666">Chưa có hoạt động mới.</p>';
    return;
  }

  const recent = [...safeTimeline]
    .sort((left, right) => new Date(right.changedAt).getTime() - new Date(left.changedAt).getTime())
    .slice(0, 6);

  host.innerHTML = recent
    .map(
      (step) => `
      <div class="activity-item">
        <span class="activity-dot"></span>
        <div>
          <div class="activity-title">${FoodApp.escapeHtml(FoodApp.statusLabel(step.status))}</div>
          <small class="activity-time">${FoodApp.escapeHtml(FoodApp.formatDateTime(step.changedAt))}</small>
        </div>
      </div>
    `
    )
    .join("");
}

function bindTrackingActions(order) {
  const cancelButton = document.getElementById("tracking-cancel-btn");
  const chatButton = document.getElementById("tracking-chat-btn");

  if (cancelButton) {
    const canCancel = order.status === "PLACED" || order.status === "PREPARING";
    cancelButton.style.display = canCancel ? "inline-flex" : "none";
    cancelButton.addEventListener("click", async () => {
      try {
        await FoodApp.api(`/orders/${encodeURIComponent(order.orderId)}/cancel`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ reason: "Khách hàng hủy từ trang theo dõi đơn hàng" })
        });
        window.location.reload();
      } catch (error) {
        renderTrackingMessage(FoodApp.summarizeError(error), true);
      }
    });
  }

  if (chatButton) {
    chatButton.addEventListener("click", () => {
      window.location.href = "/chat";
    });
  }
}

function renderTrackingMessage(text, isError) {
  const host = document.getElementById("tracking-message");
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

function iconForStatus(status) {
  return (
    {
      PLACED: "🔔",
      PREPARING: "📋",
      DELIVERING: "🚴",
      DELIVERED: "🏠"
    }[status] || "•"
  );
}
