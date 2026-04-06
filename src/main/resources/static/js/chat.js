document.addEventListener("DOMContentLoaded", async () => {
  try {
    FoodApp.requireAuth();
  } catch (error) {
    return;
  }

  const state = { conversations: [], activeConversationId: null };
  window.chatPageState = state;

  try {
    state.conversations = await FoodApp.api(`/support/conversations?userId=${encodeURIComponent(FoodApp.getCurrentUserId())}`);
    renderConversationList(state);
    if (state.conversations.length > 0) {
      await loadConversation(state, state.conversations[0].conversationId);
    }
  } catch (error) {
    renderChatMessage(FoodApp.summarizeError(error), true);
  }

  bindChatComposer(state);
});

function renderConversationList(state) {
  const host = document.getElementById("chat-list");
  if (!host) {
    return;
  }
  host.innerHTML = "";

  state.conversations.forEach((conversation) => {
    const item = document.createElement("div");
    item.className = `chat-item ${conversation.conversationId === state.activeConversationId ? "active" : ""}`;
    item.innerHTML = `
      <div class="chat-avatar">${FoodApp.escapeHtml(conversation.avatarLabel)}</div>
      <div class="chat-info">
        <div class="chat-name">${FoodApp.escapeHtml(conversation.title)}</div>
        <div class="chat-status">
          <span class="status-dot"></span>
          <span>${conversation.online ? "Online" : "Offline"}</span>
        </div>
        <div class="chat-last-msg">${FoodApp.escapeHtml(conversation.lastMessage || "")}</div>
      </div>
    `;
    item.addEventListener("click", async () => {
      await loadConversation(state, conversation.conversationId);
    });
    host.appendChild(item);
  });
}

async function loadConversation(state, conversationId) {
  state.activeConversationId = conversationId;
  renderConversationList(state);

  const detail = await FoodApp.api(
    `/support/conversations/${encodeURIComponent(conversationId)}?userId=${encodeURIComponent(FoodApp.getCurrentUserId())}`
  );
  renderConversationDetail(detail);
}

function renderConversationDetail(detail) {
  setText("chat-header-avatar", detail.avatarLabel);
  setText("chat-header-title", detail.title);
  setText("chat-header-status", detail.online ? "Đang hoạt động" : "Ngoại tuyến");

  const host = document.getElementById("chat-messages");
  if (!host) {
    return;
  }
  host.innerHTML = "";

  detail.messages.forEach((message) => {
    const group = document.createElement("div");
    const isUser = message.sender === "USER";
    group.className = `message-group ${isUser ? "user" : "shipper"}`;
    group.innerHTML = `
      <div>
        <div class="message-bubble">${FoodApp.escapeHtml(message.message)}</div>
        <div class="message-time">${FoodApp.escapeHtml(FoodApp.formatTime(message.createdAt))}</div>
      </div>
    `;
    host.appendChild(group);
  });

  host.scrollTop = host.scrollHeight;
}

function bindChatComposer(state) {
  const input = document.getElementById("messageInput");
  const button = document.getElementById("chat-send-btn");

  const submit = async () => {
    const message = input?.value.trim();
    if (!message || !state.activeConversationId) {
      return;
    }

    try {
      const detail = await FoodApp.api(
        `/support/conversations/${encodeURIComponent(state.activeConversationId)}/messages?userId=${encodeURIComponent(
          FoodApp.getCurrentUserId()
        )}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({ message })
        }
      );
      input.value = "";
      renderConversationDetail(detail);
      state.conversations = await FoodApp.api(
        `/support/conversations?userId=${encodeURIComponent(FoodApp.getCurrentUserId())}`
      );
      renderConversationList(state);
    } catch (error) {
      renderChatMessage(FoodApp.summarizeError(error), true);
    }
  };

  button?.addEventListener("click", submit);
  input?.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      event.preventDefault();
      submit();
    }
  });
}

function renderChatMessage(text, isError) {
  const host = document.getElementById("chat-message");
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
