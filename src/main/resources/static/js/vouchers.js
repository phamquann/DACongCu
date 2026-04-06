document.addEventListener("DOMContentLoaded", async () => {
  const container = document.getElementById("voucher-list");
  if (!container) {
    return;
  }

  try {
    const vouchers = await FoodApp.api("/vouchers");
    bindVoucherFilters(vouchers, container);
    renderVoucherCards(container, vouchers);
  } catch (error) {
    container.innerHTML = `<p style="color:#b42318">${FoodApp.escapeHtml(FoodApp.summarizeError(error))}</p>`;
  }
});

function bindVoucherFilters(vouchers, host) {
  document.querySelectorAll(".filter-btn").forEach((button) => {
    button.addEventListener("click", () => {
      document.querySelectorAll(".filter-btn").forEach((candidate) => candidate.classList.remove("active"));
      button.classList.add("active");
      const categoryCode = button.dataset.categoryCode || "";
      const filtered = !categoryCode
        ? vouchers
        : vouchers.filter(
            (voucher) => !voucher.categoryCodes.length || voucher.categoryCodes.includes(categoryCode)
          );
      renderVoucherCards(host, filtered);
    });
  });
}

function renderVoucherCards(host, vouchers) {
  host.innerHTML = "";

  vouchers.forEach((voucher) => {
    const card = document.createElement("div");
    card.className = "voucher-card";
    card.innerHTML = `
      <div class="card-image">
        <span class="discount-badge">${voucher.discountType === "PERCENT" ? `${voucher.discountValue}%` : `${voucher.discountValue / 1000}K`}</span>
        <img src="/images/voucher.jpg" alt="${FoodApp.escapeHtml(voucher.title)}">
      </div>
      <div class="card-content">
        <h3 class="card-title">${FoodApp.escapeHtml(voucher.title)}</h3>
        <p class="card-desc">${FoodApp.escapeHtml(voucher.description || "Ưu đãi dùng ngay cho đơn hàng của bạn")}</p>
        <div class="card-conditions">
          <p><i class="fa-solid fa-cart-shopping"></i> Đơn tối thiểu: ${FoodApp.formatCurrency(voucher.minimumOrder)}</p>
          <p><i class="fa-regular fa-clock"></i> HSD: ${FoodApp.escapeHtml(FoodApp.formatDate(voucher.expiryDate))}</p>
        </div>
        <div class="card-action">
          <div class="promo-code">${FoodApp.escapeHtml(voucher.code)}</div>
          <button class="btn-copy" type="button" title="Dùng mã"><i class="fa-solid fa-check"></i></button>
        </div>
      </div>
    `;

    card.querySelector(".promo-code").addEventListener("click", async () => {
      await navigator.clipboard?.writeText(voucher.code);
    });
    card.querySelector(".btn-copy").addEventListener("click", () => {
      FoodApp.setPendingVoucher(voucher.code);
      window.location.href = "/checkout";
    });
    host.appendChild(card);
  });
}
