document.addEventListener("DOMContentLoaded", async () => {
  if (document.getElementById("admin-dashboard-table-body")) {
    await initAdminDashboard();
  }

  if (document.getElementById("admin-review-table-body")) {
    await initAdminReviews();
  }

  if (document.getElementById("admin-voucher-table-body")) {
    await initAdminVouchers();
  }
});

async function initAdminDashboard() {
  const searchInput = document.getElementById("admin-dashboard-search");
  const load = async () => {
    const keyword = searchInput?.value.trim() || "";
    const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : "";
    const data = await FoodApp.api(`/admin/dashboard${query}`);
    setText("admin-stat-placed", data.placedOrders);
    setText("admin-stat-preparing", data.preparingOrders);
    setText("admin-stat-delivered", data.deliveredOrders);
    setText("admin-stat-scheduled", data.scheduledOrders);
    renderAdminOrders(data.orders || []);
  };

  searchInput?.addEventListener("keydown", async (event) => {
    if (event.key === "Enter") {
      await load();
    }
  });

  try {
    await load();
  } catch (error) {
    renderAdminBanner("admin-dashboard-message", FoodApp.summarizeError(error), true);
  }
}

function renderAdminOrders(orders) {
  const host = document.getElementById("admin-dashboard-table-body");
  if (!host) {
    return;
  }
  host.innerHTML = "";

  orders.forEach((order) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td><strong>${FoodApp.escapeHtml(order.orderId)}</strong></td>
      <td>${FoodApp.escapeHtml(order.customerName)}</td>
      <td>${FoodApp.escapeHtml(order.scheduledAt ? FoodApp.formatDateTime(order.scheduledAt) : FoodApp.formatDateTime(order.createdAt))}</td>
      <td>${FoodApp.escapeHtml(order.itemSummary)}</td>
      <td><span class="status-badge ${adminStatusClass(order.status)}">${FoodApp.escapeHtml(FoodApp.statusLabel(order.status))}</span></td>
    `;
    host.appendChild(row);
  });
}

async function initAdminReviews() {
  const searchInput = document.getElementById("admin-review-search");
  const ratingSelect = document.getElementById("admin-review-rating-filter");
  const statusSelect = document.getElementById("admin-review-status-filter");

  const load = async () => {
    const params = new URLSearchParams();
    if (searchInput?.value.trim()) {
      params.set("keyword", searchInput.value.trim());
    }
    if (ratingSelect?.value) {
      params.set("rating", ratingSelect.value);
    }
    if (statusSelect?.value) {
      params.set("responded", statusSelect.value);
    }

    const query = params.toString() ? `?${params.toString()}` : "";
    const data = await FoodApp.api(`/admin/reviews${query}`);
    setText("review-avg-rating", data.averageRating);
    setText("review-pending-count", data.pendingReplyCount);
    setText("review-negative-count", data.negativeReviewCount);
    renderAdminReviewsTable(data.reviews || []);
  };

  [searchInput, ratingSelect, statusSelect].forEach((element) => {
    element?.addEventListener("change", load);
  });
  searchInput?.addEventListener("keydown", async (event) => {
    if (event.key === "Enter") {
      await load();
    }
  });

  try {
    await load();
  } catch (error) {
    renderAdminBanner("admin-review-message", FoodApp.summarizeError(error), true);
  }
}

function renderAdminReviewsTable(reviews) {
  const host = document.getElementById("admin-review-table-body");
  if (!host) {
    return;
  }
  host.innerHTML = "";

  reviews.forEach((review) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${FoodApp.escapeHtml(review.customerName)}</td>
      <td>${FoodApp.escapeHtml(review.productSummary)}</td>
      <td>${FoodApp.escapeHtml(review.comment || "Không có nhận xét")} (${review.rating}★)</td>
      <td>${FoodApp.escapeHtml(FoodApp.formatDateTime(review.reviewedAt))}</td>
      <td><span class="status-badge ${review.replied ? "success" : "warning"}">${review.replied ? "Đã phản hồi" : "Chờ phản hồi"}</span></td>
      <td><button class="btn-action ${review.replied ? "" : "primary"}" type="button">${review.replied ? "Chi tiết" : "Phản hồi"}</button></td>
    `;

    row.querySelector("button").addEventListener("click", async () => {
      if (review.replied) {
        window.alert(review.adminReply || "Đã phản hồi.");
        return;
      }

      const reply = window.prompt("Nhập nội dung phản hồi cho khách hàng:");
      if (!reply) {
        return;
      }

      try {
        await FoodApp.api(`/admin/reviews/${encodeURIComponent(review.orderId)}/reply`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ message: reply })
        });
        window.location.reload();
      } catch (error) {
        renderAdminBanner("admin-review-message", FoodApp.summarizeError(error), true);
      }
    });
    host.appendChild(row);
  });
}

async function initAdminVouchers() {
  try {
    let vouchers = await FoodApp.api("/admin/vouchers");
    const searchInput = document.getElementById("admin-voucher-search");
    const createButton = document.getElementById("admin-voucher-create-btn");

    const render = (categoryCode) => {
      const keyword = searchInput?.value.trim().toLowerCase() || "";
      const filtered = vouchers.filter((voucher) => {
        const matchCategory =
          !categoryCode || !voucher.categoryCodes.length || voucher.categoryCodes.includes(categoryCode);
        const matchKeyword =
          !keyword ||
          voucher.code.toLowerCase().includes(keyword) ||
          voucher.title.toLowerCase().includes(keyword) ||
          (voucher.description || "").toLowerCase().includes(keyword);
        return matchCategory && matchKeyword;
      });
      renderAdminVoucherTable(filtered);
    };

    document.querySelectorAll(".admin-voucher-filter").forEach((button) => {
      button.addEventListener("click", () => {
        document.querySelectorAll(".admin-voucher-filter").forEach((candidate) => candidate.classList.remove("active"));
        button.classList.add("active");
        render(button.dataset.categoryCode || "");
      });
    });

    searchInput?.addEventListener("input", () => {
      const active = document.querySelector(".admin-voucher-filter.active");
      render(active?.dataset.categoryCode || "");
    });

    createButton?.addEventListener("click", async () => {
      const payload = promptVoucherPayload();
      if (!payload) {
        return;
      }
      try {
        await FoodApp.api("/admin/vouchers", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        window.location.reload();
      } catch (error) {
        renderAdminBanner("admin-voucher-message", FoodApp.summarizeError(error), true);
      }
    });

    render();
  } catch (error) {
    renderAdminBanner("admin-voucher-message", FoodApp.summarizeError(error), true);
  }
}

function renderAdminVoucherTable(vouchers) {
  const host = document.getElementById("admin-voucher-table-body");
  if (!host) {
    return;
  }
  host.innerHTML = "";

  vouchers.forEach((voucher) => {
    const row = document.createElement("tr");
    row.className = voucher.active && !voucher.expired ? "active-row" : "";
    row.innerHTML = `
      <td><input type="checkbox"></td>
      <td><strong>${FoodApp.escapeHtml(voucher.code)}</strong></td>
      <td>
        <div class="voucher-name-col">
          <img src="/images/voucher.jpg" alt="Voucher">
          <span>${FoodApp.escapeHtml(voucher.title)}</span>
        </div>
      </td>
      <td>${FoodApp.escapeHtml((voucher.categoryCodes[0] || "ALL").replaceAll("_", " "))}</td>
      <td>${voucher.discountType === "PERCENT" ? `${voucher.discountValue}%` : FoodApp.formatCurrency(voucher.discountValue)}<br>(Tối thiểu: ${FoodApp.formatCurrency(voucher.minimumOrder)})</td>
      <td>${FoodApp.escapeHtml(FoodApp.formatDate(voucher.expiryDate))}</td>
      <td class="${voucher.active && !voucher.expired ? "status-active" : ""}">${voucher.active && !voucher.expired ? "Đang hoạt động" : "Ngừng áp dụng"}</td>
      <td class="action-icons">
        <i class="fa-regular fa-pen-to-square" title="Sửa"></i>
        <i class="fa-regular fa-circle-xmark" title="Xóa"></i>
      </td>
    `;

    const [editButton, deleteButton] = row.querySelectorAll(".action-icons i");
    editButton.addEventListener("click", async () => {
      const payload = promptVoucherPayload(voucher);
      if (!payload) {
        return;
      }
      try {
        await FoodApp.api(`/admin/vouchers/${encodeURIComponent(voucher.code)}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        window.location.reload();
      } catch (error) {
        renderAdminBanner("admin-voucher-message", FoodApp.summarizeError(error), true);
      }
    });

    deleteButton.addEventListener("click", async () => {
      if (!window.confirm(`Xóa voucher ${voucher.code}?`)) {
        return;
      }
      try {
        await FoodApp.api(`/admin/vouchers/${encodeURIComponent(voucher.code)}`, {
          method: "DELETE"
        });
        window.location.reload();
      } catch (error) {
        renderAdminBanner("admin-voucher-message", FoodApp.summarizeError(error), true);
      }
    });

    host.appendChild(row);
  });
}

function promptVoucherPayload(existing) {
  const code = existing?.code || window.prompt("Mã voucher:", "NEWCODE");
  if (!code) return null;
  const title = window.prompt("Tên voucher:", existing?.title || "");
  if (!title) return null;
  const description = window.prompt("Mô tả:", existing?.description || "") || "";
  const type = window.prompt("Loại giảm giá (PERCENT hoặc FIXED_AMOUNT):", existing?.discountType || "PERCENT");
  if (!type) return null;
  const discountValue = window.prompt("Giá trị giảm:", existing?.discountValue || "10");
  const minimumOrder = window.prompt("Đơn tối thiểu:", existing?.minimumOrder || "50000");
  const expiryDate = window.prompt("Hạn sử dụng (yyyy-MM-dd):", existing?.expiryDate || "2026-12-31");
  const categoryCodes = window.prompt(
    "Danh mục áp dụng, cách nhau bởi dấu phẩy:",
    existing?.categoryCodes?.join(",") || ""
  );
  const active = window.confirm("Voucher đang hoạt động?");

  return {
    code,
    title,
    description,
    discountType: type,
    discountValue: Number(discountValue || 0),
    minimumOrder: Number(minimumOrder || 0),
    expiryDate,
    categoryCodes: categoryCodes
      ? categoryCodes
          .split(",")
          .map((value) => value.trim())
          .filter(Boolean)
      : [],
    active
  };
}

function adminStatusClass(status) {
  return (
    {
      PLACED: "status-pending",
      PREPARING: "status-preparing",
      DELIVERING: "status-preparing",
      DELIVERED: "status-completed",
      CANCELLED: "status-pending"
    }[status] || "status-pending"
  );
}

function renderAdminBanner(id, text, isError) {
  const host = document.getElementById(id);
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
