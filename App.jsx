import React, { useState } from "react";
import AdminDashboard from "./components/AdminDashboard";
import UserBooking from "./components/UserBooking";

export default function App() {
  const [currentPage, setCurrentPage] = useState("orders");

  return (
    <div className="min-h-screen bg-[#e8e8e8]">
      <div className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex gap-4">
          <button
            onClick={() => setCurrentPage("orders")}
            className={`px-6 py-2 rounded-lg font-semibold transition ${
              currentPage === "orders"
                ? "bg-[#4f5561] text-white"
                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
            }`}
          >
            Quản lý đơn hàng
          </button>
          <button
            onClick={() => setCurrentPage("booking")}
            className={`px-6 py-2 rounded-lg font-semibold transition ${
              currentPage === "booking"
                ? "bg-[#f28f3b] text-white"
                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
            }`}
          >
            Quản lí đặt trước
          </button>
        </div>
      </div>

      <div className="pt-20">
        {currentPage === "orders" && <AdminDashboard />}
        {currentPage === "booking" && <UserBooking />}
      </div>
    </div>
  );
}
