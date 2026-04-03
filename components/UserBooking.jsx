import React, { useState } from "react";
import {
  House,
  BadgeCheck,
  ShoppingCart,
  Bell,
  Settings,
  Menu,
  Search,
  User,
  BellRing,
} from "lucide-react";

export default function UserBooking() {
  const [activeTab, setActiveTab] = useState("home");

  const cards = [
    {
      title: "Đơn chờ xác nhận",
      value: 16,
      bg: "bg-[#f6c169]",
    },
    {
      title: "Đơn đang chuẩn bị",
      value: 9,
      bg: "bg-[#7198de]",
    },
    {
      title: "Đơn đã hoàn tất",
      value: 34,
      bg: "bg-[#65d06a]",
    },
  ];

  const reservations = [
    {
      id: "LT001",
      customer: "Nguyễn Văn A",
      phone: "0909456123",
      slot: "18:30 - 03/04/2026",
      people: "4 người",
      note: "Sinh nhật",
    },
    {
      id: "LT002",
      customer: "Trần Thị B",
      phone: "0988776123",
      slot: "19:00 - 03/04/2026",
      people: "2 người",
      note: "Gần cửa sổ",
    },
    {
      id: "LT003",
      customer: "Lê Văn C",
      phone: "0912777123",
      slot: "20:00 - 03/04/2026",
      people: "6 người",
      note: "Đặt phòng riêng",
    },
  ];

  const sideItems = [
    { key: "home", label: "Trang chủ", icon: House },
    { key: "verify", label: "Xác nhận", icon: BadgeCheck },
    { key: "orders", label: "Đơn hàng", icon: ShoppingCart },
  ];

  return (
    <div className="min-h-screen bg-[#e6e6e6] text-black flex">
      <aside className="w-[260px] bg-[#f59f54] hidden md:flex flex-col px-6 py-8">
        <div className="mt-20 space-y-6">
          {sideItems.map((item) => {
            const Icon = item.icon;
            const active = activeTab === item.key;
            return (
              <button
                key={item.key}
                onClick={() => setActiveTab(item.key)}
                className={`w-full flex items-center gap-4 px-4 py-3 rounded-l-2xl rounded-r-xl transition ${
                  active
                    ? "bg-white text-black"
                    : "text-black/90 hover:bg-white/20"
                }`}
              >
                <Icon size={28} />
                <span className="font-semibold">{item.label}</span>
              </button>
            );
          })}
        </div>

        <div className="mt-auto space-y-4 pb-8">
          <button className="flex items-center gap-4 px-4 py-3 text-black/90 hover:bg-white/20 rounded-xl transition w-full">
            <Bell size={24} />
            <span className="font-semibold">Thông báo</span>
          </button>
          <button className="flex items-center gap-4 px-4 py-3 text-black/90 hover:bg-white/20 rounded-xl transition w-full">
            <Settings size={24} />
            <span className="font-semibold">Cài đặt</span>
          </button>
          <button className="flex items-center gap-4 px-4 py-3 text-black/90 hover:bg-white/20 rounded-xl transition w-full">
            <Menu size={24} />
            <span className="font-semibold">Menu</span>
          </button>
        </div>
      </aside>

      <main className="flex-1">
        <header className="h-[115px] bg-[#d3d3d3] px-5 md:px-8 flex items-center justify-between border-b border-[#c5c5c5]">
          <h1 className="text-3xl md:text-[44px] font-black tracking-tight">
            Quản lí đặt trước
          </h1>
          <div className="flex items-center gap-2 md:gap-4">
            <button className="size-12 rounded-xl grid place-items-center hover:bg-white/40 transition">
              <Search size={28} />
            </button>
            <button className="size-12 rounded-xl grid place-items-center hover:bg-white/40 transition">
              <BellRing size={28} />
            </button>
            <button className="size-12 rounded-xl grid place-items-center hover:bg-white/40 transition">
              <User size={28} />
            </button>
          </div>
        </header>

        <div className="p-4 md:p-8">
          <div className="grid grid-cols-1 lg:grid-cols-[1fr_1fr_1fr_1.3fr] gap-4 mb-8">
            {cards.map((card) => (
              <article
                key={card.title}
                className={`${card.bg} rounded-2xl h-[171px] p-5 flex flex-col justify-between`}
              >
                <p className="text-[28px] leading-[1.1] font-black max-w-[180px]">
                  {card.title}
                </p>
                <p className="text-4xl font-black">{card.value}</p>
              </article>
            ))}

            <article className="bg-[#cbcbcb] rounded-2xl h-[171px] p-5">
              <h2 className="text-[38px] font-black leading-none mb-8">
                Lịch đặt trước
              </h2>
              <div className="flex gap-4">
                <div className="h-11 w-24 rounded-full bg-[#fdcda0]" />
                <div className="h-11 w-24 rounded-full bg-[#fdcda0]" />
              </div>
            </article>
          </div>

          <section className="bg-[#d9d9d9] rounded-t-2xl border border-[#cdcdcd] overflow-hidden">
            <div className="px-4 md:px-7 py-4 border-b border-[#c7c7c7]">
              <h3 className="text-3xl md:text-5xl font-black">
                Danh sách lịch đặt trước
              </h3>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-sm bg-white/35">
                <thead>
                  <tr className="bg-white/45 text-left">
                    <th className="px-4 md:px-6 py-4 font-bold">Mã lịch</th>
                    <th className="px-4 md:px-6 py-4 font-bold">Khách hàng</th>
                    <th className="px-4 md:px-6 py-4 font-bold">SĐT</th>
                    <th className="px-4 md:px-6 py-4 font-bold">Khung giờ</th>
                    <th className="px-4 md:px-6 py-4 font-bold">Số người</th>
                    <th className="px-4 md:px-6 py-4 font-bold">Ghi chú</th>
                  </tr>
                </thead>
                <tbody>
                  {reservations.map((item) => (
                    <tr
                      key={item.id}
                      className="border-t border-[#d2d2d2] hover:bg-white/60"
                    >
                      <td className="px-4 md:px-6 py-4 font-semibold">
                        {item.id}
                      </td>
                      <td className="px-4 md:px-6 py-4">{item.customer}</td>
                      <td className="px-4 md:px-6 py-4">{item.phone}</td>
                      <td className="px-4 md:px-6 py-4">{item.slot}</td>
                      <td className="px-4 md:px-6 py-4">{item.people}</td>
                      <td className="px-4 md:px-6 py-4">{item.note}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}
