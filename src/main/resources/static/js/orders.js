document.addEventListener("DOMContentLoaded", async () => {
  try {
    FoodApp.requireAuth();
  } catch (error) {
    return;
  }

  const host = document.getElementById("orders-history-list");
  if (!host) {
    return;
  }

  try {
    const orders = await FoodApp.api(`/orders?userId=${encodeURIComponent(FoodApp.getCurrentUserId())}`);
    renderOrders(host, orders);
  } catch (error) {
    host.innerHTML = `<p style="color:#b42318">${FoodApp.escapeHtml(FoodApp.summarizeError(error))}</p>`;
  }
});

function renderOrders(host, orders) {
  const safeOrders = Array.isArray(orders) ? orders : [];
  host.innerHTML = "";
  if (safeOrders.length === 0) {
    host.innerHTML = '<p style="color:#666">Bạn chưa có đơn hàng nào.</p>';
    return;
  }

  safeOrders.forEach((order) => {
    const items = Array.isArray(order.items) ? order.items : [];
    const timeline = Array.isArray(order.timeline) ? order.timeline : [];
    const card = document.createElement("div");
    card.className = "order-card";
    card.innerHTML = `
      <div class="order-header">
        <div class="order-info">
          <div class="order-id">Đơn hàng #${FoodApp.escapeHtml(order.orderId)}</div>
          <div class="order-status">
            Trạng thái:
            <span class="order-status-badge ${statusClass(order.status)}">${FoodApp.statusLabel(order.status)}</span>
          </div>
        </div>
        <div class="order-change">${FoodApp.formatDateTime(order.createdAt)}</div>
      </div>

      <div class="order-details">
        <div class="detail-item">
          <div class="detail-icon">📍</div>
          <div class="detail-content">
            <div class="detail-label">Địa chỉ giao hàng</div>
            <div class="detail-value">${FoodApp.escapeHtml(order.deliveryAddress || "Chưa cập nhật")}</div>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-icon">⏱</div>
          <div class="detail-content">
            <div class="detail-label">Lịch giao</div>
            <div class="detail-value">${FoodApp.escapeHtml(order.scheduledAt ? FoodApp.formatDateTime(order.scheduledAt) : "Giao ngay")}</div>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-icon">💳</div>
          <div class="detail-content">
            <div class="detail-label">Thanh toán</div>
            <div class="detail-value">${FoodApp.escapeHtml(FoodApp.paymentLabel(order.paymentMethod))}</div>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-icon">💰</div>
          <div class="detail-content">
            <div class="detail-label">Tổng tiền</div>
            <div class="detail-value"><strong>${FoodApp.formatCurrency(order.total)}</strong></div>
          </div>
        </div>
      </div>

      <div class="order-items">
        <div class="order-items-title">📦 Chi tiết đơn hàng</div>
        ${items
          .map(
            (item) => `
          <div class="item-row">
            <span class="item-name">${FoodApp.escapeHtml(item.name)}</span>
            <span class="item-qty">x ${item.quantity}</span>
            <span class="item-price">${FoodApp.formatCurrency(item.lineTotal)}</span>
          </div>
        `
          )
          .join("")}
      </div>

      <div class="order-timeline">
        <div class="timeline-title">🗓 Tình trạng đơn hàng</div>
        ${timeline
          .map(
            (step, index) => `
          <div class="timeline-item">
            <div class="timeline-dot ${step.status === order.status ? "active" : index < timeline.length - 1 ? "completed" : ""}"></div>
            <div class="timeline-content">
              <div class="timeline-time">${FoodApp.escapeHtml(FoodApp.formatDateTime(step.changedAt))}</div>
              <div class="timeline-status">${FoodApp.escapeHtml(FoodApp.statusLabel(step.status))}</div>
            </div>
          </div>
        `
          )
          .join("") || '<p style="color:#666">Đang cập nhật tiến trình đơn hàng.</p>'}
      </div>

      <div class="order-summary">
        <div class="summary-row"><span>Tạm tính</span><span>${FoodApp.formatCurrency(order.subtotal)}</span></div>
        <div class="summary-row"><span>Giảm giá</span><span>-${FoodApp.formatCurrency(order.discount)}</span></div>
        <div class="summary-row"><span>Phí giao</span><span>${FoodApp.formatCurrency(order.shippingFee)}</span></div>
        <div class="summary-row total"><span>Tổng cộng</span><span class="summary-price">${FoodApp.formatCurrency(order.total)}</span></div>
      </div>

      <div class="order-actions">
        <button class="action-btn" type="button" data-action="track">Theo dõi đơn</button>
        <button class="action-btn" type="button" data-action="bill">Hóa đơn</button>
        <button class="action-btn primary" type="button" data-action="review">${order.review ? "Xem đánh giá" : "Đánh giá đơn"}</button>
        ${
          order.status === "PLACED" || order.status === "PREPARING"
            ? '<button class="action-btn" type="button" data-action="cancel">Hủy đơn</button>'
            : ""
        }
      </div>
    `;

    card.querySelectorAll("[data-action]").forEach((button) => {
      button.addEventListener("click", async () => {
        const action = button.dataset.action;
        if (action === "track") {
          window.location.href = FoodApp.withOrderId("/orders/tracking", order.orderId);
        } else if (action === "bill") {
          window.location.href = FoodApp.withOrderId("/bill/invoice", order.orderId);
        } else if (action === "review") {
          window.location.href = FoodApp.withOrderId("/bill/review", order.orderId);
        } else if (action === "cancel") {
          await cancelOrder(order.orderId);
        }
      });
    });

    host.appendChild(card);
  });
}

async function cancelOrder(orderId) {
  try {
    await FoodApp.api(`/orders/${encodeURIComponent(orderId)}/cancel`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ reason: "Khách hàng hủy từ trang lịch sử đơn hàng" })
    });
    window.location.reload();
  } catch (error) {
    window.alert(FoodApp.summarizeError(error));
  }
}

function statusClass(status) {
  return (
    {
      PLACED: "status-processing",
      PREPARING: "status-confirmed",
      DELIVERING: "status-processing",
      DELIVERED: "status-delivered",
      CANCELLED: "status-processing"
    }[status] || "status-processing"
  );
}
