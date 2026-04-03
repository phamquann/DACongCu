import React, { useState } from "react";
import { Plus, Paperclip, Camera, Send } from "lucide-react";

export default function ChatSupport() {
  const [selectedChat, setSelectedChat] = useState("shipper");
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: "shipper",
      text: "Đơn hàng của bạn sắp đến",
      time: "14:30",
    },
    { id: 2, sender: "user", text: "Ok, cảm ơn bạn", time: "14:31" },
    {
      id: 3,
      sender: "shipper",
      text: "Shipper sẽ gọi bạn trước 5 phút",
      time: "14:32",
    },
    { id: 4, sender: "user", text: "Vâng, tôi sẽ đợi", time: "14:32" },
  ]);
  const [inputMessage, setInputMessage] = useState("");

  const chatList = [
    {
      id: "shipper",
      name: "Shipper",
      status: "online",
      lastMessage: "Đã giao đơn hàng",
    },
    {
      id: "support",
      name: "Hỗ trợ khách hàng",
      status: "online",
      lastMessage: "Chúng tôi sẵn sàng hỗ trợ",
    },
    {
      id: "store",
      name: "Nhà hàng A",
      status: "online",
      lastMessage: "Đơn hàng đang chuẩn bị",
    },
  ];

  const handleSendMessage = () => {
    if (inputMessage.trim()) {
      const newMessage = {
        id: messages.length + 1,
        sender: "user",
        text: inputMessage,
        time: new Date().toLocaleTimeString("vi-VN", {
          hour: "2-digit",
          minute: "2-digit",
        }),
      };
      setMessages([...messages, newMessage]);
      setInputMessage("");

      // Simulate shipper response
      setTimeout(() => {
        setMessages((prev) => [
          ...prev,
          {
            id: prev.length + 1,
            sender: "shipper",
            text: "Cảm ơn bạn đã chờ đợi!",
            time: new Date().toLocaleTimeString("vi-VN", {
              hour: "2-digit",
              minute: "2-digit",
            }),
          },
        ]);
      }, 1000);
    }
  };

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Chat List Sidebar */}
      <aside className="w-80 bg-white shadow-lg flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-gray-200 flex items-center justify-between">
          <h2 className="text-2xl font-bold text-gray-800">Chat</h2>
          <button className="p-2 text-orange-500 hover:bg-orange-50 rounded-lg transition">
            <Plus size={24} />
          </button>
        </div>

        {/* Chat List */}
        <div className="flex-1 overflow-y-auto">
          {chatList.map((chat) => (
            <button
              key={chat.id}
              onClick={() => setSelectedChat(chat.id)}
              className={`w-full px-4 py-4 border-b border-gray-100 transition text-left ${
                selectedChat === chat.id
                  ? "bg-orange-100 border-l-4 border-l-orange-500"
                  : "hover:bg-gray-50"
              }`}
            >
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-orange-400 to-orange-500 flex items-center justify-center text-white font-bold text-lg shadow-md">
                  {chat.name.charAt(0)}
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-semibold text-gray-800 truncate">
                    {chat.name}
                  </h3>
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 bg-green-500 rounded-full"></span>
                    <p className="text-xs text-gray-500 truncate">
                      {chat.lastMessage}
                    </p>
                  </div>
                </div>
              </div>
            </button>
          ))}
        </div>
      </aside>

      {/* Chat Content */}
      <main className="flex-1 flex flex-col">
        {/* Chat Header */}
        <header className="bg-white shadow-sm p-6 flex items-center justify-between border-b border-gray-200">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-full bg-gradient-to-br from-orange-400 to-orange-500 flex items-center justify-center text-white font-bold text-xl shadow-md">
              S
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-800">Order</h2>
              <p className="text-sm text-green-600 font-semibold">
                Đang hoạt động
              </p>
            </div>
          </div>
          <button className="p-3 text-orange-500 hover:bg-orange-50 rounded-lg transition">
            <Plus size={24} />
          </button>
        </header>

        {/* Messages Area */}
        <div className="flex-1 overflow-y-auto p-6 space-y-4">
          {messages.map((message) => (
            <div
              key={message.id}
              className={`flex ${message.sender === "user" ? "justify-end" : "justify-start"}`}
            >
              <div
                className={`max-w-xs lg:max-w-md rounded-lg px-4 py-3 shadow-sm ${
                  message.sender === "user"
                    ? "bg-gray-700 text-white rounded-br-none"
                    : "bg-gray-200 text-gray-800 rounded-bl-none"
                }`}
              >
                <p className="text-sm">{message.text}</p>
                <span
                  className={`text-xs mt-1 block ${
                    message.sender === "user"
                      ? "text-gray-300"
                      : "text-gray-500"
                  }`}
                >
                  {message.time}
                </span>
              </div>
            </div>
          ))}
        </div>

        {/* Input Area */}
        <div className="bg-orange-500 p-4 shadow-lg">
          <div className="flex items-center gap-3">
            <button className="p-2 text-white hover:bg-orange-600 rounded-lg transition">
              <Paperclip size={24} />
            </button>
            <button className="p-2 text-white hover:bg-orange-600 rounded-lg transition">
              <Camera size={24} />
            </button>
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={(e) => e.key === "Enter" && handleSendMessage()}
              placeholder="Nhập tin nhắn..."
              className="flex-1 px-4 py-3 rounded-full border-none focus:outline-none text-gray-800 placeholder-gray-500"
            />
            <button
              onClick={handleSendMessage}
              className="p-3 bg-white text-orange-500 hover:bg-gray-100 rounded-full transition font-bold"
            >
              <Send size={20} />
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
