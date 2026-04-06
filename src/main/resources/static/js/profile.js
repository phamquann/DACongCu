document.addEventListener("DOMContentLoaded", async () => {
  try {
    FoodApp.requireAuth();
  } catch (error) {
    return;
  }

  const editButton = document.getElementById("profile-edit-btn");
  const saveButton = document.getElementById("profile-save-btn");
  const form = document.getElementById("profile-form");
  let editable = false;

  try {
    const [profile, orders] = await Promise.all([
      FoodApp.api(`/users/${encodeURIComponent(FoodApp.getCurrentUserId())}/profile`),
      FoodApp.api(`/orders?userId=${encodeURIComponent(FoodApp.getCurrentUserId())}`)
    ]);
    renderProfile(profile, orders);
  } catch (error) {
    renderProfileMessage(FoodApp.summarizeError(error), true);
  }

  if (editButton) {
    editButton.addEventListener("click", (event) => {
      event.preventDefault();
      editable = !editable;
      toggleEditable(form, editable);
      if (saveButton) {
        saveButton.style.display = editable ? "inline-flex" : "none";
      }
    });
  }

  if (saveButton && form) {
    saveButton.addEventListener("click", async () => {
      try {
        const payload = {
          fullName: document.getElementById("profile-full-name").value.trim(),
          email: document.getElementById("profile-email").value.trim(),
          phone: document.getElementById("profile-phone").value.trim(),
          address: document.getElementById("profile-address").value.trim(),
          dateOfBirth: document.getElementById("profile-dob").value || null
        };
        const profile = await FoodApp.api(`/users/${encodeURIComponent(FoodApp.getCurrentUserId())}/profile`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const session = FoodApp.getSession();
        if (session && session.user) {
          session.user = profile;
          FoodApp.setSession(session);
        }
        editable = false;
        toggleEditable(form, false);
        saveButton.style.display = "none";
        renderProfileMessage("Đã cập nhật hồ sơ thành công.");
      } catch (error) {
        renderProfileMessage(FoodApp.summarizeError(error), true);
      }
    });
  }
});

function renderProfile(profile, orders) {
  const safeOrders = Array.isArray(orders) ? orders : [];
  setValue("profile-full-name", profile.fullName || "");
  setValue("profile-email", profile.email || "");
  setValue("profile-phone", profile.phone || "");
  setValue("profile-address", profile.address || "");
  setValue("profile-dob", profile.dateOfBirth || "");
  setText("profile-avatar", initials(profile.fullName));
  setText("profile-name", profile.fullName || "Khách hàng");
  setText("profile-stat-orders", `${safeOrders.length} đơn`);
  setText("profile-stat-active", `${safeOrders.filter((order) => order.status !== "DELIVERED" && order.status !== "CANCELLED").length} đang xử lý`);
  setText("profile-stat-reviewed", `${safeOrders.filter((order) => order.review).length} đã đánh giá`);
  toggleEditable(document.getElementById("profile-form"), false);
}

function toggleEditable(form, editable) {
  if (!form) {
    return;
  }
  form.querySelectorAll("input").forEach((input) => {
    input.disabled = !editable;
  });
}

function renderProfileMessage(text, isError) {
  const host = document.getElementById("profile-message");
  if (!host) {
    return;
  }
  host.textContent = text || "";
  host.style.color = isError ? "#b42318" : "#166534";
}

function setValue(id, value) {
  const element = document.getElementById(id);
  if (element) {
    element.value = value;
  }
}

function setText(id, value) {
  const element = document.getElementById(id);
  if (element) {
    element.textContent = value;
  }
}

function initials(name) {
  return String(name || "")
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0] || "")
    .join("")
    .toUpperCase();
}
