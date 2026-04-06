package com.example.fooddelivery.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppDataService {

    private final List<CategoryDto> categories = List.of(
            new CategoryDto("BURGER", "Burger"),
            new CategoryDto("PIZZA", "Pizza"),
            new CategoryDto("FRIED_CHICKEN", "Ga ran"),
            new CategoryDto("COMBO", "Combo"),
            new CategoryDto("DRINK", "Do uong"));

    private final List<MenuItemDto> menuItems = List.of(
            new MenuItemDto("ITEM-100", "Burger Bo Pho Mai", "Burger bo sot pho mai", 79000, "BURGER"),
            new MenuItemDto("ITEM-101", "Burger Ga Gion", "Ga gion sot cay nhe", 69000, "BURGER"),
            new MenuItemDto("ITEM-102", "Pizza Hai San", "Pizza hai san vi tom cua", 149000, "PIZZA"),
            new MenuItemDto("ITEM-103", "Pizza Xuc Xich", "Pizza xuc xich pho mai", 129000, "PIZZA"),
            new MenuItemDto("ITEM-104", "Ga Ran 6 Mieng", "Combo ga ran 6 mieng", 159000, "FRIED_CHICKEN"),
            new MenuItemDto("ITEM-105", "Combo Tiet Kiem", "Burger + khoai + pepsi", 119000, "COMBO"),
            new MenuItemDto("ITEM-106", "Khoai Tay Chien", "Khoai tay chien gion", 39000, "COMBO"),
            new MenuItemDto("ITEM-107", "Pepsi Lon", "Nuoc ngot lon 330ml", 15000, "DRINK"));

    private final Map<String, UserProfileDto> profilesByUserId = new ConcurrentHashMap<>();
    private final Map<String, CartState> cartsByUserId = new ConcurrentHashMap<>();
    private final Map<String, OrderState> ordersById = new ConcurrentHashMap<>();
    private final Map<String, List<String>> orderIdsByUserId = new ConcurrentHashMap<>();
    private final Map<String, ConversationState> conversationsById = new ConcurrentHashMap<>();
    private final Map<String, List<String>> conversationIdsByUserId = new ConcurrentHashMap<>();
    private final Map<String, VoucherState> vouchersByCode = new ConcurrentHashMap<>();

    private final AtomicLong cartItemSequence = new AtomicLong(3000);
    private final AtomicLong orderSequence = new AtomicLong(2000);
    private final AtomicLong messageSequence = new AtomicLong(5000);

    public AppDataService() {
        seedUsers();
        seedVouchers();
        seedOrders();
        ensureConversation("USR-1000");
        ensureConversation("USR-1001");
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    public List<MenuItemDto> getMenuItems(String keyword, String categoryCode) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedCategory = trimToNull(categoryCode);

        return menuItems.stream()
                .filter(item -> normalizedKeyword == null
                        || item.name().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || item.description().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .filter(item -> normalizedCategory == null
                        || item.categoryCode().equalsIgnoreCase(normalizedCategory))
                .toList();
    }

    public CartResponse getCart(String userId) {
        String normalizedUserId = requireUserId(userId);
        ensureUser(normalizedUserId);
        return toCartResponse(normalizedUserId, cartsByUserId.get(normalizedUserId));
    }

    public synchronized CartResponse addCartItem(String userId, String menuItemId, Integer quantity) {
        String normalizedUserId = requireUserId(userId);
        ensureUser(normalizedUserId);

        int safeQuantity = quantity == null || quantity < 1 ? 1 : quantity;
        MenuItemDto menuItem = findMenuItem(requireText(menuItemId, "menuItemId is required"));

        CartState cartState = cartsByUserId.computeIfAbsent(normalizedUserId, ignored -> new CartState());

        CartItemState existing = cartState.itemsById.values().stream()
                .filter(item -> item.menuItemId.equals(menuItem.id()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.quantity += safeQuantity;
        } else {
            String cartItemId = "CIT-" + cartItemSequence.incrementAndGet();
            cartState.itemsById.put(cartItemId,
                    new CartItemState(cartItemId, menuItem.id(), menuItem.name(), safeQuantity, menuItem.price()));
        }

        return toCartResponse(normalizedUserId, cartState);
    }

    public synchronized CartResponse updateCartItem(String userId, String cartItemId, Integer quantity) {
        String normalizedUserId = requireUserId(userId);
        ensureUser(normalizedUserId);
        String normalizedCartItemId = requireText(cartItemId, "cartItemId is required");

        CartState cartState = cartsByUserId.computeIfAbsent(normalizedUserId, ignored -> new CartState());
        CartItemState item = cartState.itemsById.get(normalizedCartItemId);

        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }

        int nextQuantity = quantity == null ? item.quantity : quantity;
        if (nextQuantity <= 0) {
            cartState.itemsById.remove(normalizedCartItemId);
        } else {
            item.quantity = nextQuantity;
        }

        return toCartResponse(normalizedUserId, cartState);
    }

    public synchronized CartResponse removeCartItem(String userId, String cartItemId) {
        String normalizedUserId = requireUserId(userId);
        ensureUser(normalizedUserId);
        String normalizedCartItemId = requireText(cartItemId, "cartItemId is required");

        CartState cartState = cartsByUserId.computeIfAbsent(normalizedUserId, ignored -> new CartState());
        cartState.itemsById.remove(normalizedCartItemId);
        return toCartResponse(normalizedUserId, cartState);
    }

    public synchronized CartResponse applyVoucher(String userId, String code) {
        String normalizedUserId = requireUserId(userId);
        ensureUser(normalizedUserId);

        CartState cartState = cartsByUserId.computeIfAbsent(normalizedUserId, ignored -> new CartState());
        String normalizedCode = trimToNull(code);

        if (normalizedCode == null) {
            cartState.appliedVoucherCode = null;
            return toCartResponse(normalizedUserId, cartState);
        }

        VoucherState voucherState = findVoucherByCode(normalizedCode);
        if (!voucherState.active) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher is not active");
        }
        if (isVoucherExpired(voucherState)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher has expired");
        }

        long subtotal = cartState.itemsById.values().stream()
                .mapToLong(item -> item.unitPrice * item.quantity)
                .sum();

        if (subtotal < voucherState.minimumOrder) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order does not meet voucher minimum");
        }

        cartState.appliedVoucherCode = voucherState.code;
        return toCartResponse(normalizedUserId, cartState);
    }

    public UserProfileDto getProfile(String userId) {
        String normalizedUserId = requireUserId(userId);
        ensureUser(normalizedUserId);
        return profilesByUserId.get(normalizedUserId);
    }

    public synchronized UserProfileDto updateProfile(String userId, UserProfileUpdateRequest request) {
        String normalizedUserId = requireUserId(userId);
        ensureUser(normalizedUserId);

        UserProfileDto current = profilesByUserId.get(normalizedUserId);
        UserProfileDto updated = new UserProfileDto(
                normalizedUserId,
                defaultString(request.fullName(), current.fullName()),
                defaultString(request.email(), current.email()),
                defaultString(request.phone(), current.phone()),
                defaultString(request.address(), current.address()),
                request.dateOfBirth() == null ? current.dateOfBirth() : request.dateOfBirth());

        profilesByUserId.put(normalizedUserId, updated);
        return updated;
    }

    public List<VoucherDto> getPublicVouchers() {
        return vouchersByCode.values().stream()
                .filter(voucher -> voucher.active && !isVoucherExpired(voucher))
                .sorted(Comparator.comparing(voucher -> voucher.expiryDate))
                .map(this::toVoucherDto)
                .toList();
    }

    public synchronized OrderDto checkout(CheckoutRequest request) {
        String userId = requireUserId(request.userId());
        ensureUser(userId);

        CartState cartState = cartsByUserId.computeIfAbsent(userId, ignored -> new CartState());
        if (cartState.itemsById.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        CartResponse cart = toCartResponse(userId, cartState);
        String orderId = "ORD-" + orderSequence.incrementAndGet();
        String now = nowIso();

        List<OrderItemDto> orderItems = cart.items().stream()
                .map(item -> new OrderItemDto(item.menuItemId(), item.name(), item.quantity(), item.unitPrice(), item.lineTotal()))
                .toList();

        OrderState orderState = new OrderState(
                orderId,
                userId,
                new ArrayList<>(orderItems),
                cart.subtotal(),
                cart.shippingFee(),
                cart.discount(),
                cart.total(),
                cart.appliedVoucherCode(),
                normalizePaymentMethod(request.paymentMethod()),
                defaultString(request.deliveryAddress(), profilesByUserId.get(userId).address()),
                trimToNull(request.note()),
                trimToNull(request.scheduledAt()),
                "PLACED",
                new ArrayList<>(List.of(new TimelineStepDto("PLACED", now))),
                now,
                null);

        ordersById.put(orderId, orderState);
        orderIdsByUserId.computeIfAbsent(userId, ignored -> new ArrayList<>()).add(0, orderId);

        cartState.itemsById.clear();
        cartState.appliedVoucherCode = null;

        return toOrderDto(orderState);
    }

    public List<OrderDto> getOrdersByUser(String userId) {
        String normalizedUserId = requireUserId(userId);
        ensureUser(normalizedUserId);

        return orderIdsByUserId.getOrDefault(normalizedUserId, List.of()).stream()
                .map(ordersById::get)
                .filter(Objects::nonNull)
                .map(this::toOrderDto)
                .toList();
    }

    public OrderDto getOrderById(String orderId) {
        OrderState orderState = findOrder(orderId);
        return toOrderDto(orderState);
    }

    public synchronized OrderDto cancelOrder(String orderId) {
        OrderState orderState = findOrder(orderId);
        if (!"CANCELLED".equals(orderState.status) && !"DELIVERED".equals(orderState.status)) {
            orderState.status = "CANCELLED";
            orderState.timeline.add(new TimelineStepDto("CANCELLED", nowIso()));
        }
        return toOrderDto(orderState);
    }

    public synchronized OrderDto reviewOrder(String orderId, Integer rating, String comment) {
        OrderState orderState = findOrder(orderId);
        int safeRating = rating == null ? 5 : Math.max(1, Math.min(5, rating));
        String reviewedAt = nowIso();

        orderState.review = new ReviewDto(
                safeRating,
                trimToNull(comment),
                reviewedAt,
                false,
                null);

        if ("PLACED".equals(orderState.status)) {
            orderState.status = "DELIVERED";
            orderState.timeline.add(new TimelineStepDto("DELIVERED", reviewedAt));
        }

        return toOrderDto(orderState);
    }

    public List<ConversationSummaryDto> getConversationSummaries(String userId) {
        String normalizedUserId = requireUserId(userId);
        ensureConversation(normalizedUserId);

        return conversationIdsByUserId.getOrDefault(normalizedUserId, List.of()).stream()
                .map(conversationsById::get)
                .filter(Objects::nonNull)
                .map(this::toConversationSummary)
                .toList();
    }

    public ConversationDetailDto getConversationDetail(String userId, String conversationId) {
        String normalizedUserId = requireUserId(userId);
        ensureConversation(normalizedUserId);

        ConversationState state = findConversation(normalizedUserId, conversationId);
        return toConversationDetail(state);
    }

    public synchronized ConversationDetailDto sendConversationMessage(String userId, String conversationId, String message) {
        String normalizedUserId = requireUserId(userId);
        ensureConversation(normalizedUserId);
        String safeMessage = requireText(message, "message is required");

        ConversationState state = findConversation(normalizedUserId, conversationId);

        String now = nowIso();
        state.messages.add(new ConversationMessageDto("MSG-" + messageSequence.incrementAndGet(), "USER", safeMessage, now));
        state.messages.add(new ConversationMessageDto(
                "MSG-" + messageSequence.incrementAndGet(),
                "SUPPORT",
                "Da nhan tin cua ban. Ben minh dang xu ly ngay.",
                nowIso()));

        return toConversationDetail(state);
    }

    public AdminDashboardDto getAdminDashboard(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<OrderState> allOrders = ordersById.values().stream()
                .sorted(Comparator.comparing((OrderState order) -> order.createdAt).reversed())
                .toList();

        List<AdminOrderSummaryDto> filtered = allOrders.stream()
                .map(this::toAdminOrderSummary)
                .filter(summary -> normalizedKeyword == null
                        || summary.orderId().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || summary.customerName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || summary.itemSummary().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .limit(50)
                .toList();

        long placed = allOrders.stream().filter(order -> "PLACED".equals(order.status)).count();
        long preparing = allOrders.stream().filter(order -> "PREPARING".equals(order.status)).count();
        long delivered = allOrders.stream().filter(order -> "DELIVERED".equals(order.status)).count();
        long scheduled = allOrders.stream().filter(order -> order.scheduledAt != null).count();

        return new AdminDashboardDto((int) placed, (int) preparing, (int) delivered, (int) scheduled, filtered);
    }

    public AdminReviewsDto getAdminReviews(String keyword, Integer rating, Boolean responded) {
        String normalizedKeyword = normalizeKeyword(keyword);

        List<AdminReviewRowDto> reviewRows = ordersById.values().stream()
                .filter(order -> order.review != null)
                .map(this::toAdminReviewRow)
                .filter(row -> normalizedKeyword == null
                        || row.customerName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || row.productSummary().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || (row.comment() != null && row.comment().toLowerCase(Locale.ROOT).contains(normalizedKeyword)))
                .filter(row -> rating == null || row.rating() == rating)
                .filter(row -> responded == null || row.replied() == responded)
                .sorted(Comparator.comparing(AdminReviewRowDto::reviewedAt).reversed())
                .toList();

        List<ReviewDto> allReviews = ordersById.values().stream()
                .map(order -> order.review)
                .filter(Objects::nonNull)
                .toList();

        double avg = allReviews.isEmpty()
                ? 0.0
                : allReviews.stream().mapToInt(ReviewDto::rating).average().orElse(0.0);

        int pending = (int) allReviews.stream().filter(review -> !review.replied()).count();
        int negative = (int) allReviews.stream().filter(review -> review.rating() <= 3).count();

        return new AdminReviewsDto(roundOneDecimal(avg), pending, negative, reviewRows);
    }

    public synchronized void replyToReview(String orderId, String message) {
        OrderState orderState = findOrder(orderId);
        if (orderState.review == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order has no review");
        }

        orderState.review = new ReviewDto(
                orderState.review.rating(),
                orderState.review.comment(),
                orderState.review.reviewedAt(),
                true,
                requireText(message, "message is required"));
    }

    public List<VoucherDto> getAdminVouchers() {
        return vouchersByCode.values().stream()
                .sorted(Comparator.comparing((VoucherState voucher) -> voucher.code))
                .map(this::toVoucherDto)
                .toList();
    }

    public synchronized VoucherDto createVoucher(AdminVoucherRequest request) {
        String code = requireText(request.code(), "code is required").toUpperCase(Locale.ROOT);
        if (vouchersByCode.containsKey(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voucher code already exists");
        }

        VoucherState voucher = toVoucherState(request, code);
        vouchersByCode.put(code, voucher);
        return toVoucherDto(voucher);
    }

    public synchronized VoucherDto updateVoucher(String code, AdminVoucherRequest request) {
        String normalizedCode = requireText(code, "code is required").toUpperCase(Locale.ROOT);
        VoucherState existing = findVoucherByCode(normalizedCode);
        VoucherState merged = mergeVoucher(existing, request);
        vouchersByCode.put(normalizedCode, merged);
        return toVoucherDto(merged);
    }

    public synchronized void deleteVoucher(String code) {
        String normalizedCode = requireText(code, "code is required").toUpperCase(Locale.ROOT);
        vouchersByCode.remove(normalizedCode);
    }

    private void seedUsers() {
        profilesByUserId.put("USR-1000", new UserProfileDto(
                "USR-1000",
                "Demo User",
                "user@example.com",
                "0911111111",
                "123 Le Loi, Q1, TP.HCM",
                "2000-01-01"));

        profilesByUserId.put("USR-1001", new UserProfileDto(
                "USR-1001",
                "Tran Thi Thanh Mai",
                "mai@example.com",
                "0947689615",
                "Duong 5, Thu Duc, Ho Chi Minh",
                "2000-01-01"));

        cartsByUserId.put("USR-1000", new CartState());
        cartsByUserId.put("USR-1001", new CartState());
    }

    private void seedVouchers() {
        vouchersByCode.put("BURGER20", new VoucherState(
                "BURGER20",
                "Giam 20% Burger",
                "Ap dung cho burger",
                "PERCENT",
                20,
                50000,
                LocalDate.now().plusDays(60).toString(),
                List.of("BURGER"),
                true));

        vouchersByCode.put("PIZZA30K", new VoucherState(
                "PIZZA30K",
                "Giam 30K Pizza",
                "Giam truc tiep 30k",
                "FIXED_AMOUNT",
                30000,
                120000,
                LocalDate.now().plusDays(45).toString(),
                List.of("PIZZA"),
                true));

        vouchersByCode.put("COMBO15", new VoucherState(
                "COMBO15",
                "Giam 15% Combo",
                "Combo tiet kiem",
                "PERCENT",
                15,
                80000,
                LocalDate.now().plusDays(30).toString(),
                List.of("COMBO"),
                true));
    }

    private void seedOrders() {
        List<OrderItemDto> demoItems = List.of(
                new OrderItemDto("ITEM-102", "Pizza Hai San", 1, 149000, 149000),
                new OrderItemDto("ITEM-107", "Pepsi Lon", 2, 15000, 30000));

        OrderState orderOne = new OrderState(
                "ORD-2001",
                "USR-1001",
                new ArrayList<>(demoItems),
                179000,
                10000,
                30000,
                159000,
                "PIZZA30K",
                "CASH",
                "Duong 5, Thu Duc, Ho Chi Minh",
                "Goi truoc khi giao",
                null,
                "DELIVERED",
                new ArrayList<>(List.of(
                        new TimelineStepDto("PLACED", offsetIsoMinutes(120)),
                        new TimelineStepDto("PREPARING", offsetIsoMinutes(110)),
                        new TimelineStepDto("DELIVERING", offsetIsoMinutes(95)),
                        new TimelineStepDto("DELIVERED", offsetIsoMinutes(75)))),
                offsetIsoMinutes(120),
                new ReviewDto(5, "Mon an ngon va giao nhanh", offsetIsoMinutes(70), true, "Cam on ban da ung ho"));

        OrderState orderTwo = new OrderState(
                "ORD-2002",
                "USR-1001",
                new ArrayList<>(List.of(
                        new OrderItemDto("ITEM-104", "Ga Ran 6 Mieng", 1, 159000, 159000))),
                159000,
                10000,
                0,
                169000,
                null,
                "MOMO",
                "Duong 5, Thu Duc, Ho Chi Minh",
                null,
                LocalDate.now().toString() + "T19:00:00",
                "PREPARING",
                new ArrayList<>(List.of(
                        new TimelineStepDto("PLACED", offsetIsoMinutes(30)),
                        new TimelineStepDto("PREPARING", offsetIsoMinutes(20)))),
                offsetIsoMinutes(30),
                null);

        ordersById.put(orderOne.orderId, orderOne);
        ordersById.put(orderTwo.orderId, orderTwo);
        orderIdsByUserId.put("USR-1001", new ArrayList<>(List.of(orderTwo.orderId, orderOne.orderId)));

        orderSequence.set(2002);
    }

    private void ensureUser(String userId) {
        profilesByUserId.computeIfAbsent(userId, id -> new UserProfileDto(
                id,
                "Khach " + id,
                "",
                "",
                "",
                null));

        cartsByUserId.computeIfAbsent(userId, ignored -> new CartState());
        orderIdsByUserId.computeIfAbsent(userId, ignored -> new ArrayList<>());
        conversationIdsByUserId.computeIfAbsent(userId, ignored -> new ArrayList<>());
        ensureConversation(userId);
    }

    private void ensureConversation(String userId) {
        List<String> ids = conversationIdsByUserId.computeIfAbsent(userId, ignored -> new ArrayList<>());
        if (!ids.isEmpty()) {
            return;
        }

        String conversationId = "CONV-" + userId;
        ConversationState state = new ConversationState(
                conversationId,
                "CS",
                "Ho tro don hang",
                true,
                new ArrayList<>(List.of(
                        new ConversationMessageDto("MSG-" + messageSequence.incrementAndGet(), "SUPPORT", "Xin chao, ban can ho tro gi?", offsetIsoMinutes(15)))));

        conversationsById.put(conversationId, state);
        ids.add(conversationId);
    }

    private MenuItemDto findMenuItem(String menuItemId) {
        return menuItems.stream()
                .filter(item -> item.id().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));
    }

    private VoucherState findVoucherByCode(String code) {
        String normalized = requireText(code, "code is required").toUpperCase(Locale.ROOT);
        VoucherState state = vouchersByCode.get(normalized);
        if (state == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found");
        }
        return state;
    }

    private OrderState findOrder(String orderId) {
        String normalized = requireText(orderId, "orderId is required");
        OrderState state = ordersById.get(normalized);
        if (state == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return state;
    }

    private ConversationState findConversation(String userId, String conversationId) {
        String normalizedConversationId = requireText(conversationId, "conversationId is required");
        if (!conversationIdsByUserId.getOrDefault(userId, List.of()).contains(normalizedConversationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }

        ConversationState state = conversationsById.get(normalizedConversationId);
        if (state == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        return state;
    }

    private CartResponse toCartResponse(String userId, CartState cartState) {
        List<CartItemResponse> itemResponses = cartState.itemsById.values().stream()
                .map(item -> new CartItemResponse(
                        item.cartItemId,
                        item.menuItemId,
                        item.name,
                        item.quantity,
                        item.unitPrice,
                        item.unitPrice * item.quantity))
                .toList();

        int itemCount = itemResponses.stream().mapToInt(CartItemResponse::quantity).sum();
        long subtotal = itemResponses.stream().mapToLong(CartItemResponse::lineTotal).sum();
        long shippingFee = subtotal > 0 ? 10000 : 0;
        long discount = calculateDiscount(subtotal, cartState.appliedVoucherCode);
        long total = Math.max(0, subtotal + shippingFee - discount);

        return new CartResponse(userId, itemResponses, itemCount, subtotal, shippingFee, discount, total,
                cartState.appliedVoucherCode);
    }

    private long calculateDiscount(long subtotal, String voucherCode) {
        if (voucherCode == null || subtotal <= 0) {
            return 0;
        }

        VoucherState voucherState = vouchersByCode.get(voucherCode);
        if (voucherState == null || !voucherState.active || isVoucherExpired(voucherState)) {
            return 0;
        }
        if (subtotal < voucherState.minimumOrder) {
            return 0;
        }

        long discount;
        if ("PERCENT".equals(voucherState.discountType)) {
            discount = subtotal * voucherState.discountValue / 100;
        } else {
            discount = voucherState.discountValue;
        }

        return Math.min(discount, subtotal);
    }

    private boolean isVoucherExpired(VoucherState voucherState) {
        try {
            LocalDate expiry = LocalDate.parse(voucherState.expiryDate);
            return LocalDate.now().isAfter(expiry);
        } catch (Exception ignored) {
            return false;
        }
    }

    private VoucherDto toVoucherDto(VoucherState state) {
        return new VoucherDto(
                state.code,
                state.title,
                state.description,
                state.discountType,
                state.discountValue,
                state.minimumOrder,
                state.expiryDate,
                state.categoryCodes,
                state.active,
                isVoucherExpired(state));
    }

    private VoucherState toVoucherState(AdminVoucherRequest request, String defaultCode) {
        return new VoucherState(
                defaultCode,
                defaultString(request.title(), "Voucher " + defaultCode),
                trimToNull(request.description()),
                normalizeDiscountType(request.discountType()),
                positiveLong(request.discountValue(), 0),
                positiveLong(request.minimumOrder(), 0),
                defaultString(request.expiryDate(), LocalDate.now().plusDays(30).toString()),
                normalizeCategoryCodes(request.categoryCodes()),
                request.active() == null || request.active());
    }

    private VoucherState mergeVoucher(VoucherState existing, AdminVoucherRequest request) {
        return new VoucherState(
                existing.code,
                defaultString(request.title(), existing.title),
                request.description() == null ? existing.description : trimToNull(request.description()),
                request.discountType() == null ? existing.discountType : normalizeDiscountType(request.discountType()),
                request.discountValue() == null ? existing.discountValue : positiveLong(request.discountValue(), existing.discountValue),
                request.minimumOrder() == null ? existing.minimumOrder : positiveLong(request.minimumOrder(), existing.minimumOrder),
                defaultString(request.expiryDate(), existing.expiryDate),
                request.categoryCodes() == null ? existing.categoryCodes : normalizeCategoryCodes(request.categoryCodes()),
                request.active() == null ? existing.active : request.active());
    }

    private OrderDto toOrderDto(OrderState state) {
        return new OrderDto(
                state.orderId,
                state.userId,
                List.copyOf(state.items),
                state.subtotal,
                state.shippingFee,
                state.discount,
                state.total,
                state.voucherCode,
                state.paymentMethod,
                state.deliveryAddress,
                state.note,
                state.scheduledAt,
                state.status,
                List.copyOf(state.timeline),
                state.createdAt,
                state.review);
    }

    private ConversationSummaryDto toConversationSummary(ConversationState state) {
        String lastMessage = state.messages.isEmpty()
                ? ""
                : state.messages.get(state.messages.size() - 1).message();

        return new ConversationSummaryDto(
                state.conversationId,
                state.avatarLabel,
                state.title,
                state.online,
                lastMessage);
    }

    private ConversationDetailDto toConversationDetail(ConversationState state) {
        return new ConversationDetailDto(
                state.conversationId,
                state.avatarLabel,
                state.title,
                state.online,
                List.copyOf(state.messages));
    }

    private AdminOrderSummaryDto toAdminOrderSummary(OrderState state) {
        String customerName = profilesByUserId.getOrDefault(state.userId,
                new UserProfileDto(state.userId, "Khach", "", "", "", null)).fullName();

        String itemSummary = state.items.stream()
                .map(OrderItemDto::name)
                .collect(Collectors.joining(", "));

        return new AdminOrderSummaryDto(
                state.orderId,
                customerName,
                state.scheduledAt,
                state.createdAt,
                itemSummary,
                state.status);
    }

    private AdminReviewRowDto toAdminReviewRow(OrderState state) {
        String customerName = profilesByUserId.getOrDefault(state.userId,
                new UserProfileDto(state.userId, "Khach", "", "", "", null)).fullName();

        String productSummary = state.items.stream()
                .map(OrderItemDto::name)
                .collect(Collectors.joining(", "));

        return new AdminReviewRowDto(
                state.orderId,
                customerName,
                productSummary,
                state.review.comment(),
                state.review.rating(),
                state.review.reviewedAt(),
                state.review.replied(),
                state.review.adminReply());
    }

    private String requireUserId(String userId) {
        return requireText(userId, "userId is required");
    }

    private String requireText(String input, String message) {
        String normalized = trimToNull(input);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String trimToNull(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeKeyword(String keyword) {
        String normalized = trimToNull(keyword);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String defaultString(String candidate, String fallback) {
        String normalized = trimToNull(candidate);
        return normalized == null ? fallback : normalized;
    }

    private String normalizePaymentMethod(String method) {
        String normalized = trimToNull(method);
        if (normalized == null) {
            return "CASH";
        }

        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "MOMO" -> "MOMO";
            case "CARD" -> "CARD";
            default -> "CASH";
        };
    }

    private String normalizeDiscountType(String type) {
        String normalized = trimToNull(type);
        if (normalized == null) {
            return "PERCENT";
        }

        return "FIXED_AMOUNT".equalsIgnoreCase(normalized) ? "FIXED_AMOUNT" : "PERCENT";
    }

    private List<String> normalizeCategoryCodes(List<String> categoryCodes) {
        if (categoryCodes == null) {
            return List.of();
        }

        List<String> normalized = categoryCodes.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();

        return normalized;
    }

    private long positiveLong(Number value, long fallback) {
        if (value == null) {
            return fallback;
        }
        return Math.max(0, value.longValue());
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String nowIso() {
        return Instant.now().toString();
    }

    private String offsetIsoMinutes(int minutesAgo) {
        return Instant.now().minusSeconds(minutesAgo * 60L).toString();
    }

    public record CategoryDto(String code, String name) {
    }

    public record MenuItemDto(String id, String name, String description, long price, String categoryCode) {
    }

    public record UserProfileDto(
            String id,
            String fullName,
            String email,
            String phone,
            String address,
            String dateOfBirth) {
    }

    public record UserProfileUpdateRequest(
            String fullName,
            String email,
            String phone,
            String address,
            String dateOfBirth) {
    }

    public record CartItemResponse(
            String cartItemId,
            String menuItemId,
            String name,
            int quantity,
            long unitPrice,
            long lineTotal) {
    }

    public record CartResponse(
            String userId,
            List<CartItemResponse> items,
            int itemCount,
            long subtotal,
            long shippingFee,
            long discount,
            long total,
            String appliedVoucherCode) {
    }

    public record VoucherDto(
            String code,
            String title,
            String description,
            String discountType,
            long discountValue,
            long minimumOrder,
            String expiryDate,
            List<String> categoryCodes,
            boolean active,
            boolean expired) {
    }

    public record CheckoutRequest(
            String userId,
            String deliveryAddress,
            String note,
            String paymentMethod,
            String scheduledAt) {
    }

    public record OrderItemDto(
            String menuItemId,
            String name,
            int quantity,
            long unitPrice,
            long lineTotal) {
    }

    public record TimelineStepDto(String status, String changedAt) {
    }

    public record ReviewDto(
            int rating,
            String comment,
            String reviewedAt,
            boolean replied,
            String adminReply) {
    }

    public record OrderDto(
            String orderId,
            String userId,
            List<OrderItemDto> items,
            long subtotal,
            long shippingFee,
            long discount,
            long total,
            String voucherCode,
            String paymentMethod,
            String deliveryAddress,
            String note,
            String scheduledAt,
            String status,
            List<TimelineStepDto> timeline,
            String createdAt,
            ReviewDto review) {
    }

    public record ConversationSummaryDto(
            String conversationId,
            String avatarLabel,
            String title,
            boolean online,
            String lastMessage) {
    }

    public record ConversationMessageDto(
            String messageId,
            String sender,
            String message,
            String createdAt) {
    }

    public record ConversationDetailDto(
            String conversationId,
            String avatarLabel,
            String title,
            boolean online,
            List<ConversationMessageDto> messages) {
    }

    public record AdminOrderSummaryDto(
            String orderId,
            String customerName,
            String scheduledAt,
            String createdAt,
            String itemSummary,
            String status) {
    }

    public record AdminDashboardDto(
            int placedOrders,
            int preparingOrders,
            int deliveredOrders,
            int scheduledOrders,
            List<AdminOrderSummaryDto> orders) {
    }

    public record AdminReviewRowDto(
            String orderId,
            String customerName,
            String productSummary,
            String comment,
            int rating,
            String reviewedAt,
            boolean replied,
            String adminReply) {
    }

    public record AdminReviewsDto(
            double averageRating,
            int pendingReplyCount,
            int negativeReviewCount,
            List<AdminReviewRowDto> reviews) {
    }

    public record AdminVoucherRequest(
            String code,
            String title,
            String description,
            String discountType,
            Number discountValue,
            Number minimumOrder,
            String expiryDate,
            List<String> categoryCodes,
            Boolean active) {
    }

    private static final class CartState {
        private final Map<String, CartItemState> itemsById = new LinkedHashMap<>();
        private String appliedVoucherCode;
    }

    private static final class CartItemState {
        private final String cartItemId;
        private final String menuItemId;
        private final String name;
        private int quantity;
        private final long unitPrice;

        private CartItemState(String cartItemId, String menuItemId, String name, int quantity, long unitPrice) {
            this.cartItemId = cartItemId;
            this.menuItemId = menuItemId;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }

    private static final class OrderState {
        private final String orderId;
        private final String userId;
        private final List<OrderItemDto> items;
        private final long subtotal;
        private final long shippingFee;
        private final long discount;
        private final long total;
        private final String voucherCode;
        private final String paymentMethod;
        private final String deliveryAddress;
        private final String note;
        private final String scheduledAt;
        private String status;
        private final List<TimelineStepDto> timeline;
        private final String createdAt;
        private ReviewDto review;

        private OrderState(
                String orderId,
                String userId,
                List<OrderItemDto> items,
                long subtotal,
                long shippingFee,
                long discount,
                long total,
                String voucherCode,
                String paymentMethod,
                String deliveryAddress,
                String note,
                String scheduledAt,
                String status,
                List<TimelineStepDto> timeline,
                String createdAt,
                ReviewDto review) {
            this.orderId = orderId;
            this.userId = userId;
            this.items = items;
            this.subtotal = subtotal;
            this.shippingFee = shippingFee;
            this.discount = discount;
            this.total = total;
            this.voucherCode = voucherCode;
            this.paymentMethod = paymentMethod;
            this.deliveryAddress = deliveryAddress;
            this.note = note;
            this.scheduledAt = scheduledAt;
            this.status = status;
            this.timeline = timeline;
            this.createdAt = createdAt;
            this.review = review;
        }
    }

    private static final class ConversationState {
        private final String conversationId;
        private final String avatarLabel;
        private final String title;
        private final boolean online;
        private final List<ConversationMessageDto> messages;

        private ConversationState(String conversationId, String avatarLabel, String title, boolean online,
                List<ConversationMessageDto> messages) {
            this.conversationId = conversationId;
            this.avatarLabel = avatarLabel;
            this.title = title;
            this.online = online;
            this.messages = messages;
        }
    }

    private static final class VoucherState {
        private final String code;
        private final String title;
        private final String description;
        private final String discountType;
        private final long discountValue;
        private final long minimumOrder;
        private final String expiryDate;
        private final List<String> categoryCodes;
        private final boolean active;

        private VoucherState(String code, String title, String description, String discountType, long discountValue,
                long minimumOrder, String expiryDate, List<String> categoryCodes, boolean active) {
            this.code = code;
            this.title = title;
            this.description = description;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.minimumOrder = minimumOrder;
            this.expiryDate = expiryDate;
            this.categoryCodes = categoryCodes;
            this.active = active;
        }
    }
}
