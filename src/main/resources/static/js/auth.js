document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("login-form");
  const registerForm = document.getElementById("register-form");

  if (loginForm) {
    initLogin(loginForm);
  }

  if (registerForm) {
    initRegister(registerForm);
  }
});

function initLogin(form) {
  const message = document.getElementById("login-message");
  const submitButton = form.querySelector('button[type="submit"]');

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    renderMessage(message, "");
    submitButton.disabled = true;

    try {
      const credential = form.querySelector('input[name="username"]').value.trim();
      const password = form.querySelector('input[name="password"]').value;

      const result = await FoodApp.api("/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ credential, password })
      });

      FoodApp.setSession(result);
      const params = new URLSearchParams(window.location.search);
      const next = params.get("next") || "/booking";
      window.location.href = next;
    } catch (error) {
      renderMessage(message, FoodApp.summarizeError(error), true);
    } finally {
      submitButton.disabled = false;
    }
  });
}

function initRegister(form) {
  const message = document.getElementById("register-message");
  const submitButton = form.querySelector('button[type="submit"]');

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    renderMessage(message, "");

    const payload = {
      fullName: form.querySelector('input[name="fullName"]').value.trim(),
      email: form.querySelector('input[name="email"]').value.trim(),
      phone: form.querySelector('input[name="phone"]').value.trim(),
      address: form.querySelector('input[name="address"]').value.trim(),
      dateOfBirth: form.querySelector('input[name="dateOfBirth"]')?.value || null,
      password: form.querySelector('input[name="password"]').value
    };
    const confirmPassword = form.querySelector('input[name="confirmPassword"]').value;

    if (payload.password !== confirmPassword) {
      renderMessage(message, "Mật khẩu xác nhận chưa khớp.", true);
      return;
    }

    submitButton.disabled = true;

    try {
      const result = await FoodApp.api("/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      });

      FoodApp.setSession(result);
      window.location.href = "/booking";
    } catch (error) {
      renderMessage(message, FoodApp.summarizeError(error), true);
    } finally {
      submitButton.disabled = false;
    }
  });
}

function renderMessage(host, text, isError) {
  if (!host) {
    return;
  }

  host.textContent = text || "";
  host.style.color = isError ? "#b42318" : "#166534";
}
