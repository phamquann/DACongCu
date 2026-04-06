(function () {
  const API_BASE = "/api/v1";
  const SESSION_KEY = "food-delivery-session";
  const SCHEDULE_KEY = "food-delivery-schedule";
  const PENDING_VOUCHER_KEY = "food-delivery-pending-voucher";
  const LAST_ORDER_KEY = "food-delivery-last-order-id";

  function readSession() {
    try {
      const raw = window.localStorage.getItem(SESSION_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (error) {
      return null;
    }
  }

  function writeSession(session) {
    window.localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  }

  function clearSession() {
    window.localStorage.removeItem(SESSION_KEY);
  }

  function logout(redirectPath) {
    clearSession();
    window.localStorage.removeItem(SCHEDULE_KEY);
    window.localStorage.removeItem(PENDING_VOUCHER_KEY);
    window.localStorage.removeItem(LAST_ORDER_KEY);
    window.location.href = redirectPath || "/login";
  }

  function getCurrentUser() {
    const session = readSession();
    return session && session.user ? session.user : null;
  }

  function getCurrentUserId() {
    const user = getCurrentUser();
    return user ? user.id : null;
  }

  function requireAuth() {
    const userId = getCurrentUserId();
    if (!userId) {
      const next = encodeURIComponent(window.location.pathname + window.location.search);
      window.location.href = `/login?next=${next}`;
      throw new Error("AUTH_REQUIRED");
    }
    return userId;
  }

  async function api(path, options) {
    const response = await fetch(`${API_BASE}${path}`, options);
    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
      ? await response.json()
      : await response.text();

    if (!response.ok) {
      const message =
        payload && typeof payload === "object" && payload.message
          ? payload.message
          : typeof payload === "string" && payload
            ? payload
            : `Request failed (${response.status})`;
      throw new Error(message);
    }

    return payload;
  }

  function setSchedule(schedule) {
    if (!schedule) {
      window.localStorage.removeItem(SCHEDULE_KEY);
      return;
    }
    window.localStorage.setItem(SCHEDULE_KEY, JSON.stringify(schedule));
  }

  function getSchedule() {
    try {
      const raw = window.localStorage.getItem(SCHEDULE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (error) {
      return null;
    }
  }

  function setPendingVoucher(code) {
    if (code) {
      window.localStorage.setItem(PENDING_VOUCHER_KEY, code);
    } else {
      window.localStorage.removeItem(PENDING_VOUCHER_KEY);
    }
  }

  function consumePendingVoucher() {
    const code = window.localStorage.getItem(PENDING_VOUCHER_KEY);
    window.localStorage.removeItem(PENDING_VOUCHER_KEY);
    return code;
  }

  function setLastOrderId(orderId) {
    if (orderId) {
      window.localStorage.setItem(LAST_ORDER_KEY, orderId);
    }
  }

  function getLastOrderId() {
    return window.localStorage.getItem(LAST_ORDER_KEY);
  }

  function getOrderIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get("orderId") || getLastOrderId();
  }

  function withOrderId(path, orderId) {
    if (!orderId) {
      return path;
    }
    const separator = path.includes("?") ? "&" : "?";
    return `${path}${separator}orderId=${encodeURIComponent(orderId)}`;
  }

  function formatCurrency(value) {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      maximumFractionDigits: 0
    }).format(Number(value || 0));
  }

  function formatDateTime(value) {
    if (!value) {
      return "--";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }
    return `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()} ${pad(date.getHours())}:${pad(
      date.getMinutes()
    )}`;
  }

  function formatDate(value) {
    if (!value) {
      return "--";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }
    return `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()}`;
  }

  function formatTime(value) {
    if (!value) {
      return "--";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }
    return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  function paymentLabel(method) {
    return (
      {
        CASH: "Tiền mặt",
        MOMO: "Ví MoMo",
        CARD: "Thẻ ngân hàng"
      }[method] || "Chưa xác định"
    );
  }

  function statusLabel(status) {
    return (
      {
        PLACED: "Chờ xác nhận",
        PREPARING: "Đang chuẩn bị",
        DELIVERING: "Đang giao",
        DELIVERED: "Đã giao",
        CANCELLED: "Đã hủy"
      }[status] || status
    );
  }

  function escapeHtml(value) {
    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function summarizeError(error) {
    if (!error) {
      return "Đã có lỗi xảy ra.";
    }
    if (typeof error === "string") {
      return error;
    }
    return error.message || "Đã có lỗi xảy ra.";
  }

  function pad(value) {
    return String(value).padStart(2, "0");
  }

  window.FoodApp = {
    api,
    clearSession,
    consumePendingVoucher,
    escapeHtml,
    formatCurrency,
    formatDate,
    formatDateTime,
    formatTime,
    getCurrentUser,
    getCurrentUserId,
    getOrderIdFromUrl,
    getSchedule,
    getSession: readSession,
    paymentLabel,
    requireAuth,
    logout,
    setLastOrderId,
    setPendingVoucher,
    setSchedule,
    setSession: writeSession,
    statusLabel,
    summarizeError,
    withOrderId
  };
})();
