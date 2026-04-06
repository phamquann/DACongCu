(function () {
  function ensureStyles() {
    if (document.getElementById("user-shortcuts-style")) {
      return;
    }

    var style = document.createElement("style");
    style.id = "user-shortcuts-style";
    style.textContent = [
      ".user-shortcuts {",
      "  position: fixed;",
      "  top: 12px;",
      "  right: 12px;",
      "  z-index: 9999;",
      "  display: flex;",
      "  gap: 8px;",
      "  align-items: center;",
      "  max-width: calc(100vw - 20px);",
      "  overflow-x: auto;",
      "  background: rgba(255,255,255,0.92);",
      "  border: 1px solid #e5e7eb;",
      "  box-shadow: 0 8px 20px rgba(0,0,0,0.12);",
      "  border-radius: 999px;",
      "  padding: 6px;",
      "}",
      ".user-shortcuts .shortcut-btn {",
      "  width: 36px;",
      "  height: 36px;",
      "  border-radius: 50%;",
      "  border: none;",
      "  display: inline-flex;",
      "  align-items: center;",
      "  justify-content: center;",
      "  text-decoration: none;",
      "  color: #374151;",
      "  background: #ffffff;",
      "  cursor: pointer;",
      "  font-size: 16px;",
      "}",
      ".user-shortcuts .shortcut-btn:hover {",
      "  background: #fff7ed;",
      "  color: #ea580c;",
      "}",
      ".user-shortcuts .shortcut-btn.active {",
      "  background: #f97316;",
      "  color: #ffffff;",
      "}",
      ".user-shortcuts .shortcut-btn.logout {",
      "  background: #ef4444;",
      "  color: #ffffff;",
      "}",
      "@media (max-width: 640px) {",
      "  .user-shortcuts { top: auto; bottom: 10px; right: 10px; gap: 6px; }",
      "  .user-shortcuts .shortcut-btn { width: 34px; height: 34px; font-size: 15px; }",
      "}"
    ].join("\n");

    document.head.appendChild(style);
  }

  function isActive(pathname, candidates) {
    return candidates.some(function (candidate) {
      return pathname === candidate || pathname.indexOf(candidate + "?") === 0;
    });
  }

  function makeAction(pathname, path, title, iconEntity, isButton, activePaths) {
    var candidates = Array.isArray(activePaths) && activePaths.length ? activePaths : [path];
    var active = isActive(pathname, candidates);

    if (isButton) {
      var btn = document.createElement("button");
      btn.type = "button";
      btn.className = "shortcut-btn logout";
      btn.title = title;
      btn.setAttribute("aria-label", title);
      btn.innerHTML = iconEntity;
      btn.addEventListener("click", function () {
        FoodApp.logout("/login");
      });
      return btn;
    }

    var anchor = document.createElement("a");
    anchor.className = "shortcut-btn" + (active ? " active" : "");
    anchor.href = path;
    anchor.title = title;
    anchor.setAttribute("aria-label", title);
    anchor.innerHTML = iconEntity;
    return anchor;
  }

  function initUserShortcuts() {
    if (!window.FoodApp || document.body.dataset.disableShortcuts === "true") {
      return;
    }

    var pathname = window.location.pathname;
    if (pathname.indexOf("/admin") === 0 || pathname === "/login" || pathname === "/register") {
      return;
    }

    ensureStyles();

    var wrapper = document.createElement("div");
    wrapper.className = "user-shortcuts";

    wrapper.appendChild(makeAction(pathname, "/orders/history", "Lich su don", "&#128196;", false, ["/orders/history", "/order-management.html"]));
    wrapper.appendChild(makeAction(pathname, "/orders/tracking", "Thong bao", "&#128276;", false, ["/orders/tracking", "/thong-bao.html"]));
    wrapper.appendChild(makeAction(pathname, "/chat", "Chat", "&#128172;", false, ["/chat", "/figma-bill/ChatSupport.html"]));
    wrapper.appendChild(makeAction(pathname, "/profile", "Ho so", "&#128100;", false, ["/profile", "/ho-so.html"]));
    wrapper.appendChild(makeAction(pathname, "", "Dang xuat", "&#10162;", true));

    document.body.appendChild(wrapper);
  }

  document.addEventListener("DOMContentLoaded", initUserShortcuts);
})();
