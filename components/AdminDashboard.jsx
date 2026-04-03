import React from "react";
import {
  Home,
  LayoutDashboard,
  ShoppingCart,
  Users,
  Bike,
  BarChart3,
  LogOut,
  Bell,
  Search,
  UserCircle,
  ChevronDown,
  FileSpreadsheet,
  Pencil,
  Trash2,
  Clock3,
  Truck,
  CheckCircle2,
  XCircle,
} from "lucide-react";

export default function AdminDashboard() {
  const orderStats = [
    {
      label: "Đơn xử lý",
      value: 21,
      bg: "bg-[#f4df8f]",
      icon: Clock3,
    },
    {
      label: "Đang giao",
      value: 12,
      bg: "bg-[#98b5e8]",
      icon: Truck,
    },
    {
      label: "Đã giao",
      value: 58,
      bg: "bg-[#9bd89e]",
      icon: CheckCircle2,
    },
    {
      label: "Đã hủy",
      value: 7,
      bg: "bg-[#efb2b2]",
      icon: XCircle,
    },
  ];

  const orderRows = [
    {
      code: "ĐH1204",
      customer: "Nguyễn Văn A",
      phone: "0909123456",
      time: "03/04/2026 10:42",
      total: "350.000đ",
      driver: "Tài xế Bảo",
      status: "Đang giao",
    },
    {
      code: "ĐH1205",
      customer: "Trần Thị B",
      phone: "0918345678",
      time: "03/04/2026 11:10",
      total: "420.000đ",
      driver: "Tài xế Long",
      status: "Đã giao",
    },
    {
      code: "ĐH1206",
      customer: "Lê Văn C",
      phone: "0987123456",
      time: "03/04/2026 11:40",
      total: "190.000đ",
      driver: "Tài xế Quang",
      status: "Đơn xử lý",
    },
    {
      code: "ĐH1207",
      customer: "Phạm Thị D",
      phone: "0903456721",
      time: "03/04/2026 12:00",
      total: "265.000đ",
      driver: "Tài xế Nam",
      status: "Đã hủy",
    },
  ];

  const statusColor = {
    "Đơn xử lý": "bg-[#fff4cf] text-[#8a6a05]",
    "Đang giao": "bg-[#e0edff] text-[#2853a6]",
    "Đã giao": "bg-[#dcf8de] text-[#1f7e33]",
    "Đã hủy": "bg-[#fde2e2] text-[#a53434]",
  };

  return (
    <div className="min-h-screen bg-[#ebebeb] text-[#111827]">
      <header className="h-32 md:h-40 bg-[#d6d6d6] border-b border-[#c0c0c0] px-6 md:px-10 flex items-center justify-between">
        <div className="flex items-center gap-5">
          <div className="size-16 rounded-2xl bg-[#4f5561] text-white grid place-items-center shadow-md">
            <Home size={32} />
          </div>
          <div>
            <p className="text-3xl md:text-4xl font-black tracking-wide">
              ADMIN
            </p>
            <p className="text-sm md:text-base text-[#374151] mt-1">
              Bảng điều khiển vận hành
            </p>
          </div>
        </div>
        <div className="flex items-center gap-3 md:gap-4">
          <button className="size-12 grid place-items-center rounded-xl hover:bg-white/50 transition">
            <Bell size={24} />
          </button>
          <div className="flex items-center gap-2 rounded-xl bg-white/60 px-3 py-2">
            <UserCircle size={24} />
            <span className="hidden md:block font-semibold">Quản trị viên</span>
          </div>
        </div>
      </header>

      <div className="flex min-h-[calc(100vh-8rem)] md:min-h-[calc(100vh-10rem)]">
        <aside className="hidden md:flex w-[295px] bg-[#515868] text-white flex-col px-4 py-8 gap-4">
          <button className="flex items-center gap-4 px-4 py-3 rounded-xl hover:bg-white/10 transition">
            <LayoutDashboard size={22} />
            <span className="font-semibold">Tổng quan</span>
          </button>
          <button className="flex items-center gap-4 px-4 py-3 rounded-xl bg-white text-[#2f3541] font-semibold">
            <ShoppingCart size={22} />
            <span>Đơn hàng</span>
          </button>
          <button className="flex items-center gap-4 px-4 py-3 rounded-xl hover:bg-white/10 transition">
            <Users size={22} />
            <span className="font-semibold">Người dùng</span>
          </button>
          <button className="flex items-center gap-4 px-4 py-3 rounded-xl hover:bg-white/10 transition">
            <Bike size={22} />
            <span className="font-semibold">Tài xế</span>
          </button>
          <button className="flex items-center gap-4 px-4 py-3 rounded-xl hover:bg-white/10 transition">
            <BarChart3 size={22} />
            <span className="font-semibold">Thống kê</span>
          </button>

          <div className="mt-auto border-t border-white/25 pt-5">
            <button className="flex items-center gap-4 px-4 py-3 rounded-xl hover:bg-white/10 transition w-full">
              <LogOut size={22} />
              <span className="font-semibold">Đăng xuất</span>
            </button>
          </div>
        </aside>

        <main className="flex-1 p-4 md:p-7">
          <section className="bg-[#d8d8d8] rounded-2xl p-4 md:p-6 shadow-sm border border-[#cccccc]">
            <h1 className="text-2xl md:text-4xl font-black tracking-tight mb-6">
              QUẢN LÝ ĐƠN HÀNG
            </h1>

            <div className="grid grid-cols-2 xl:grid-cols-4 gap-3 mb-6">
              {orderStats.map((stat) => {
                const Icon = stat.icon;
                return (
                  <div
                    key={stat.label}
                    className={`${stat.bg} rounded-2xl p-4 md:p-5 min-h-[96px]`}
                  >
                    <div className="flex items-center justify-between">
                      <p className="font-bold text-sm md:text-base">
                        {stat.label}
                      </p>
                      <Icon size={18} />
                    </div>
                    <p className="text-2xl md:text-3xl font-black mt-2">
                      {stat.value}
                    </p>
                  </div>
                );
              })}
            </div>

            <div className="flex flex-col lg:flex-row gap-3 mb-4">
              <div className="flex-1 relative">
                <Search
                  size={18}
                  className="absolute left-4 top-3.5 text-[#6b7280]"
                />
                <input
                  type="text"
                  placeholder="Tìm kiếm theo mã đơn, tên, SDT..."
                  className="w-full rounded-xl border border-[#c8c8c8] bg-white py-3 pl-11 pr-4 outline-none focus:ring-2 focus:ring-[#7198de]"
                />
              </div>

              <button className="rounded-xl border border-[#c8c8c8] bg-white px-4 py-3 flex items-center justify-between gap-3 min-w-[220px]">
                Tất cả trạng thái
                <ChevronDown size={18} />
              </button>

              <button className="rounded-xl bg-[#f8d8a8] hover:bg-[#f6cd8f] transition px-4 py-3 font-semibold flex items-center justify-center gap-2 min-w-[185px]">
                <FileSpreadsheet size={18} />
                Xuất file excel
              </button>
            </div>

            <div className="rounded-xl overflow-hidden border border-[#c8c8c8] bg-white/75">
              <div className="px-4 md:px-6 py-4 border-b border-[#c8c8c8] bg-[#e6e6e6]">
                <h2 className="text-xl md:text-2xl font-black">
                  Bảng đơn hàng
                </h2>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="bg-[#f2f2f2] text-left">
                      <th className="px-4 md:px-6 py-3 font-bold">Mã đơn</th>
                      <th className="px-4 md:px-6 py-3 font-bold">
                        Khách hàng
                      </th>
                      <th className="px-4 md:px-6 py-3 font-bold">SDT</th>
                      <th className="px-4 md:px-6 py-3 font-bold">Thời gian</th>
                      <th className="px-4 md:px-6 py-3 font-bold">Tổng tiền</th>
                      <th className="px-4 md:px-6 py-3 font-bold">Tài xế</th>
                      <th className="px-4 md:px-6 py-3 font-bold">
                        Trạng thái
                      </th>
                      <th className="px-4 md:px-6 py-3 font-bold text-right">
                        Thao tác
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {orderRows.map((order) => (
                      <tr
                        key={order.code}
                        className="border-t border-[#d8d8d8] hover:bg-white transition"
                      >
                        <td className="px-4 md:px-6 py-4 font-semibold">
                          {order.code}
                        </td>
                        <td className="px-4 md:px-6 py-4">{order.customer}</td>
                        <td className="px-4 md:px-6 py-4">{order.phone}</td>
                        <td className="px-4 md:px-6 py-4 whitespace-nowrap">
                          {order.time}
                        </td>
                        <td className="px-4 md:px-6 py-4 font-semibold">
                          {order.total}
                        </td>
                        <td className="px-4 md:px-6 py-4">{order.driver}</td>
                        <td className="px-4 md:px-6 py-4">
                          <span
                            className={`px-3 py-1 rounded-full text-xs font-bold ${statusColor[order.status]}`}
                          >
                            {order.status}
                          </span>
                        </td>
                        <td className="px-4 md:px-6 py-4">
                          <div className="flex items-center justify-end gap-2">
                            <button className="size-8 rounded-lg bg-white hover:bg-[#eff3ff] border border-[#cfd7ea] grid place-items-center">
                              <Pencil size={16} />
                            </button>
                            <button className="size-8 rounded-lg bg-white hover:bg-[#fff0f0] border border-[#f2c7c7] grid place-items-center">
                              <Trash2 size={16} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}
