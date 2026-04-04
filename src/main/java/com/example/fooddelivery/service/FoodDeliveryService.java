package com.example.fooddelivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.example.fooddelivery.entity.CategoryEntity;
import com.example.fooddelivery.entity.MenuItemEntity;
import com.example.fooddelivery.entity.OrderEntity;
import com.example.fooddelivery.entity.OrderItemEntity;
import com.example.fooddelivery.entity.OrderReviewEntity;
import com.example.fooddelivery.entity.OrderTimelineEntity;
import com.example.fooddelivery.entity.UserEntity;
import com.example.fooddelivery.exception.ApiException;
import com.example.fooddelivery.repository.CategoryRepository;
import com.example.fooddelivery.repository.MenuItemRepository;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

@Service
public class FoodDeliveryService {

    private static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(10000);
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(300000);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;

    private final Map<String, Voucher> vouchersByCode = new ConcurrentHashMap<>();
    private final Map<String, Cart> cartsByUserId = new ConcurrentHashMap<>();

    private final AtomicLong userSequence = new AtomicLong(1000);
    private final AtomicLong cartItemSequence = new AtomicLong(1);
    private final AtomicLong orderSequence = new AtomicLong(2000);

    public FoodDeliveryService(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            MenuItemRepository menuItemRepository,
            OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
    }

    @PostConstruct
    void initializeData() {
        seedCategories();
        seedMenuItems();
        seedDefaultUser();
        seedVouchers();
        initializeSequences();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        requireNotBlank(request.fullName(), "fullName");
        requireNotBlank(request.email(), "email");
        requireNotBlank(request.phone(), "phone");
        requireNotBlank(request.password(), "password");

        String email = normalizeEmail(request.email());
        String phone = normalizePhone(request.phone());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already in use");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new ApiException(HttpStatus.CONFLICT, "Phone is already in use");
        }

        String userId = "USR-" + userSequence.incrementAndGet();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(trimToNull(request.address()));
        user.setDateOfBirth(parseDate(request.dateOfBirth(), "dateOfBirth"));
        user.setPassword(request.password());

        userRepository.save(user);
        cartsByUserId.computeIfAbsent(userId, Cart::new);

        return new AuthResponse("dev-token-" + userId, toUserProfileResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        requireNotBlank(request.credential(), "credential");
        requireNotBlank(request.password(), "password");

        Optional<UserEntity> user = userRepository.findByEmailIgnoreCase(request.credential().trim());
        if (user.isEmpty()) {
            user = userRepository.findByPhone(normalizePhone(request.credential()));
        }

        if (user.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credential or password");
        }

        UserEntity account = user.get();
        if (!account.getPassword().equals(request.password())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credential or password");
        }

        return new AuthResponse("dev-token-" + account.getId(), toUserProfileResponse(account));
    }

    public UserProfileResponse getProfile(String userId) {
        return toUserProfileResponse(requireUser(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        UserEntity account = requireUser(userId);

        String newFullName = trimToNull(request.fullName());
        String newEmail = trimToNull(request.email());
        String newPhone = trimToNull(request.phone());
        String newAddress = trimToNull(request.address());

        if (newFullName != null) {
            account.setFullName(newFullName);
        }

        if (newEmail != null) {
            String normalized = normalizeEmail(newEmail);
            if (!normalized.equalsIgnoreCase(account.getEmail())
                    && userRepository.existsByEmailIgnoreCaseAndIdNot(normalized, userId)) {
                throw new ApiException(HttpStatus.CONFLICT, "Email is already in use");
            }
            account.setEmail(normalized);
        }

        if (newPhone != null) {
            String normalized = normalizePhone(newPhone);
            if (!normalized.equals(account.getPhone()) && userRepository.existsByPhoneAndIdNot(normalized, userId)) {
                throw new ApiException(HttpStatus.CONFLICT, "Phone is already in use");
            }
            account.setPhone(normalized);
        }

        if (newAddress != null) {
            account.setAddress(newAddress);
        }

        if (request.dateOfBirth() != null) {
            account.setDateOfBirth(parseDate(request.dateOfBirth(), "dateOfBirth"));
        }

        userRepository.save(account);
        return toUserProfileResponse(account);
    }

    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(CategoryEntity::getName))
                .map(category -> new CategoryResponse(category.getId(), category.getCode(), category.getName()))
                .toList();
    }

    public List<MenuItemResponse> listMenuItems(String keyword, String categoryCode) {
        String normalizedKeyword = trimToNull(keyword);
        String normalizedCategory = trimToNull(categoryCode);

        return menuItemRepository.findAll()
                .stream()
                .filter(MenuItemEntity::isAvailable)
                .filter(item -> normalizedCategory == null
                        || item.getCategoryCode().equalsIgnoreCase(normalizedCategory))
                .filter(item -> normalizedKeyword == null || containsIgnoreCase(item.getName(), normalizedKeyword)
                        || containsIgnoreCase(item.getDescription(), normalizedKeyword))
                .sorted(Comparator.comparing(MenuItemEntity::getName))
                .map(this::toMenuItemResponse)
                .toList();
    }

    public MenuItemResponse getMenuItem(String itemId) {
        return toMenuItemResponse(requireMenuItem(itemId));
    }

    public List<VoucherResponse> listVouchers(String categoryCode) {
        String normalizedCategory = trimToNull(categoryCode);

        return vouchersByCode.values()
                .stream()
                .filter(voucher -> !isVoucherExpired(voucher))
                .filter(voucher -> normalizedCategory == null
                        || voucher.categoryCodes.isEmpty()
                        || voucher.categoryCodes.stream().anyMatch(code -> code.equalsIgnoreCase(normalizedCategory)))
                .sorted(Comparator.comparing(voucher -> voucher.code))
                .map(this::toVoucherResponse)
                .toList();
    }

    public VoucherValidationResponse validateVoucher(VoucherValidationRequest request) {
        requireNotBlank(request.code(), "code");
        if (request.subtotal() == null || request.subtotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "subtotal must be non-negative");
        }

        Voucher voucher = requireVoucher(request.code());
        VoucherValidationResult result = evaluateVoucher(voucher, request.subtotal(), request.categoryCodes());

        return new VoucherValidationResponse(
                result.applicable,
                voucher.code,
                result.discountAmount,
                result.reason,
                toVoucherResponse(voucher));
    }

    public CartResponse getCart(String userId) {
        requireUser(userId);
        Cart cart = cartsByUserId.computeIfAbsent(userId, Cart::new);
        return buildCartResponse(cart);
    }

    public CartResponse addCartItem(String userId, AddCartItemRequest request) {
        requireUser(userId);
        requireNotBlank(request.menuItemId(), "menuItemId");

        MenuItemEntity item = requireMenuItem(request.menuItemId());
        int quantity = request.quantity() == null ? 1 : request.quantity();
        if (quantity <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "quantity must be greater than 0");
        }

        Map<String, String> options = sanitizeOptions(request.options());
        Cart cart = cartsByUserId.computeIfAbsent(userId, Cart::new);

        synchronized (cart) {
            CartItem existing = cart.items.stream()
                    .filter(cartItem -> cartItem.menuItemId.equals(item.getId())
                            && cartItem.options.equals(options))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.quantity += quantity;
            } else {
                String cartItemId = "CI-" + cartItemSequence.incrementAndGet();
                cart.items.add(new CartItem(cartItemId, item.getId(), quantity, options));
            }

            return buildCartResponse(cart);
        }
    }

    public CartResponse updateCartItemQuantity(String userId, String cartItemId,
            UpdateCartItemQuantityRequest request) {
        requireUser(userId);
        requireNotBlank(cartItemId, "cartItemId");
        if (request.quantity() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "quantity is required");
        }

        Cart cart = cartsByUserId.computeIfAbsent(userId, Cart::new);

        synchronized (cart) {
            CartItem item = cart.items.stream()
                    .filter(cartItem -> cartItem.id.equals(cartItemId))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart item not found"));

            if (request.quantity() <= 0) {
                cart.items.remove(item);
            } else {
                item.quantity = request.quantity();
            }

            return buildCartResponse(cart);
        }
    }

    public CartResponse removeCartItem(String userId, String cartItemId) {
        requireUser(userId);
        requireNotBlank(cartItemId, "cartItemId");

        Cart cart = cartsByUserId.computeIfAbsent(userId, Cart::new);
        synchronized (cart) {
            boolean removed = cart.items.removeIf(item -> item.id.equals(cartItemId));
            if (!removed) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Cart item not found");
            }
            return buildCartResponse(cart);
        }
    }

    public CartResponse applyVoucher(String userId, ApplyVoucherRequest request) {
        requireUser(userId);
        Cart cart = cartsByUserId.computeIfAbsent(userId, Cart::new);

        synchronized (cart) {
            if (request == null || trimToNull(request.code()) == null) {
                cart.voucherCode = null;
                return buildCartResponse(cart);
            }

            Voucher voucher = requireVoucher(request.code());
            CartPricing pricing = calculatePricing(cart);
            VoucherValidationResult validation = evaluateVoucher(voucher, pricing.subtotal, pricing.categoryCodes);

            if (!validation.applicable) {
                throw new ApiException(HttpStatus.BAD_REQUEST, validation.reason);
            }

            cart.voucherCode = voucher.code;
            return buildCartResponse(cart);
        }
    }

    public CartResponse clearVoucher(String userId) {
        requireUser(userId);
        Cart cart = cartsByUserId.computeIfAbsent(userId, Cart::new);

        synchronized (cart) {
            cart.voucherCode = null;
            return buildCartResponse(cart);
        }
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        requireNotBlank(request.userId(), "userId");
        UserEntity account = requireUser(request.userId());

        Cart cart = cartsByUserId.computeIfAbsent(request.userId(), Cart::new);

        synchronized (cart) {
            if (cart.items.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");
            }

            CartResponse cartResponse = buildCartResponse(cart);
            String deliveryAddress = trimToNull(request.deliveryAddress());
            if (deliveryAddress == null) {
                deliveryAddress = trimToNull(account.getAddress());
            }
            if (deliveryAddress == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "deliveryAddress is required");
            }

            String orderId = "ORD-" + orderSequence.incrementAndGet();
            LocalDateTime createdAt = LocalDateTime.now();

            OrderEntity order = new OrderEntity();
            order.setId(orderId);
            order.setUserId(request.userId());
            order.setSubtotal(cartResponse.subtotal());
            order.setShippingFee(cartResponse.shippingFee());
            order.setDiscount(cartResponse.discount());
            order.setTotal(cartResponse.total());
            order.setVoucherCode(cartResponse.appliedVoucherCode());
            order.setPaymentMethod(request.paymentMethod() == null ? PaymentMethod.CASH : request.paymentMethod());
            order.setDeliveryAddress(deliveryAddress);
            order.setNote(trimToNull(request.note()));
            order.setScheduledAt(parseDateTime(request.scheduledAt(), "scheduledAt"));
            order.setCreatedAt(createdAt);
            order.setStatus(OrderStatus.PLACED);

            for (CartItemResponse item : cartResponse.items()) {
                OrderItemEntity orderItem = new OrderItemEntity();
                orderItem.setOrder(order);
                orderItem.setCartItemId(item.cartItemId());
                orderItem.setMenuItemId(item.menuItemId());
                orderItem.setName(item.name());
                orderItem.setUnitPrice(item.unitPrice());
                orderItem.setQuantity(item.quantity());
                orderItem.setLineTotal(item.lineTotal());
                orderItem.setOptionsEncoded(encodeOptions(item.options()));
                order.getItems().add(orderItem);
            }

            OrderTimelineEntity placed = new OrderTimelineEntity();
            placed.setOrder(order);
            placed.setStatus(OrderStatus.PLACED);
            placed.setChangedAt(createdAt);
            placed.setNote("Order placed");
            order.getTimeline().add(placed);

            orderRepository.save(order);

            cart.items.clear();
            cart.voucherCode = null;

            return toOrderResponse(order);
        }
    }

    public List<OrderResponse> getOrdersByUser(String userId) {
        requireUser(userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    public OrderResponse getOrder(String orderId) {
        return toOrderResponse(requireOrder(orderId));
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusRequest request) {
        OrderEntity order = requireOrder(orderId);
        if (request.status() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "status is required");
        }

        if (order.getStatus() == OrderStatus.CANCELLED && request.status() != OrderStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, "Cancelled order cannot change status");
        }
        if (order.getStatus() == OrderStatus.DELIVERED && request.status() != OrderStatus.DELIVERED) {
            throw new ApiException(HttpStatus.CONFLICT, "Delivered order cannot change status");
        }

        if (order.getStatus() != request.status()) {
            order.setStatus(request.status());
            OrderTimelineEntity statusEvent = new OrderTimelineEntity();
            statusEvent.setOrder(order);
            statusEvent.setStatus(request.status());
            statusEvent.setChangedAt(LocalDateTime.now());
            statusEvent.setNote("Status updated");
            order.getTimeline().add(statusEvent);
            orderRepository.save(order);
        }

        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String orderId, CancelOrderRequest request) {
        OrderEntity order = requireOrder(orderId);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new ApiException(HttpStatus.CONFLICT, "Delivered order cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        String reason = request == null ? null : trimToNull(request.reason());
        if (reason == null) {
            reason = "Cancelled by user";
        }

        OrderTimelineEntity statusEvent = new OrderTimelineEntity();
        statusEvent.setOrder(order);
        statusEvent.setStatus(OrderStatus.CANCELLED);
        statusEvent.setChangedAt(LocalDateTime.now());
        statusEvent.setNote(reason);
        order.getTimeline().add(statusEvent);

        orderRepository.save(order);
        return toOrderResponse(order);
    }

    @Transactional
    public ReviewResponse submitReview(String orderId, ReviewRequest request) {
        OrderEntity order = requireOrder(orderId);

        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "rating must be from 1 to 5");
        }

        OrderReviewEntity review = order.getReview();
        if (review == null) {
            review = new OrderReviewEntity();
            review.setOrder(order);
            review.setOrderId(order.getId());
            order.setReview(review);
        }

        review.setRating(request.rating());
        review.setComment(trimToNull(request.comment()));
        review.setReviewedAt(LocalDateTime.now());

        orderRepository.save(order);
        return toReviewResponse(review);
    }

    public List<NotificationResponse> getNotifications(String userId) {
        requireUser(userId);

        List<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<NotificationResponse> notifications = new ArrayList<>();

        int index = 1;
        for (OrderEntity order : orders) {
            String message = switch (order.getStatus()) {
                case PLACED -> "Your order has been placed";
                case PREPARING -> "Kitchen is preparing your order";
                case DELIVERING -> "Order is on the way";
                case DELIVERED -> "Order delivered successfully";
                case CANCELLED -> "Order has been cancelled";
            };

            notifications.add(new NotificationResponse(
                    "NOTI-" + index,
                    "Order " + order.getId(),
                    message,
                    order.getCreatedAt().toString(),
                    false));
            index++;
        }

        return notifications;
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<CategoryEntity> categories = new ArrayList<>();
        categories.add(newCategory("CAT-1", "BURGER", "Burger"));
        categories.add(newCategory("CAT-2", "PIZZA", "Pizza"));
        categories.add(newCategory("CAT-3", "FRIED_CHICKEN", "Fried Chicken"));
        categories.add(newCategory("CAT-4", "COMBO", "Combo"));
        categories.add(newCategory("CAT-5", "DRINK", "Drink"));

        categoryRepository.saveAll(categories);
    }

    private void seedMenuItems() {
        if (menuItemRepository.count() > 0) {
            return;
        }

        List<MenuItemEntity> items = new ArrayList<>();
        items.add(newMenuItem("ITEM-1", "Burger Bo Thanh Mai", "Signature beef burger with cheese",
                BigDecimal.valueOf(85000),
                "BURGER", true));
        items.add(newMenuItem("ITEM-2", "Ga Ran Co Ba Hang", "9 pieces fried chicken combo", BigDecimal.valueOf(159000),
                "FRIED_CHICKEN", true));
        items.add(newMenuItem("ITEM-3", "Pepsi Can", "Cold drink", BigDecimal.valueOf(20000), "DRINK", true));
        items.add(
                newMenuItem("ITEM-4", "Khoai Tay Chien", "French fries with optional sauces", BigDecimal.valueOf(30000),
                        "COMBO", true));
        items.add(newMenuItem("ITEM-5", "Hamburger", "Bread, meat, veggies and sauce", BigDecimal.valueOf(35000),
                "BURGER",
                true));
        items.add(newMenuItem("ITEM-6", "Pizza Hai San", "Seafood pizza", BigDecimal.valueOf(199000), "PIZZA", true));

        menuItemRepository.saveAll(items);
    }

    private void seedDefaultUser() {
        boolean exists = userRepository.existsById("USR-1000")
                || userRepository.existsByEmailIgnoreCase("mai@example.com")
                || userRepository.existsByPhone("0947689615");

        if (exists) {
            return;
        }

        UserEntity demoUser = new UserEntity();
        demoUser.setId("USR-1000");
        demoUser.setFullName("Tran Thi Thanh Mai");
        demoUser.setEmail("mai@example.com");
        demoUser.setPhone("0947689615");
        demoUser.setAddress("Duong 5, Thu Duc, Ho Chi Minh");
        demoUser.setDateOfBirth(LocalDate.of(2000, 1, 1));
        demoUser.setPassword("123456");

        userRepository.save(demoUser);
    }

    private void seedVouchers() {
        vouchersByCode.clear();

        addVoucher(new Voucher("BURGER50", "50% Burger", "Discount for burger orders", DiscountType.PERCENT,
                BigDecimal.valueOf(50), BigDecimal.valueOf(100000), LocalDate.of(2026, 12, 31), List.of("BURGER")));
        addVoucher(new Voucher("PIZZA30K", "30K Pizza", "Fixed discount for pizza", DiscountType.FIXED_AMOUNT,
                BigDecimal.valueOf(30000), BigDecimal.valueOf(150000), LocalDate.of(2026, 12, 31), List.of("PIZZA")));
        addVoucher(new Voucher("GARAN20", "20% Fried Chicken", "Discount for fried chicken", DiscountType.PERCENT,
                BigDecimal.valueOf(20), BigDecimal.valueOf(120000), LocalDate.of(2026, 12, 31),
                List.of("FRIED_CHICKEN")));
        addVoucher(new Voucher("COMBO3X", "Combo offer", "Combo promotion", DiscountType.FIXED_AMOUNT,
                BigDecimal.valueOf(25000), BigDecimal.valueOf(100000), LocalDate.of(2026, 12, 31), List.of("COMBO")));
        addVoucher(new Voucher("FREESHIP", "Shipping support", "Reduce shipping cost", DiscountType.FIXED_AMOUNT,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(50000), LocalDate.of(2026, 12, 31), List.of()));
    }

    private void initializeSequences() {
        long maxUser = userRepository.findAll().stream()
                .mapToLong(user -> extractSequence(user.getId(), "USR-", 1000))
                .max()
                .orElse(1000);

        long maxOrder = orderRepository.findAll().stream()
                .mapToLong(order -> extractSequence(order.getId(), "ORD-", 2000))
                .max()
                .orElse(2000);

        userSequence.set(Math.max(1000, maxUser));
        orderSequence.set(Math.max(2000, maxOrder));
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        List<String> categoryCodes = new ArrayList<>();

        for (CartItem item : cart.items) {
            MenuItemEntity menuItem = requireMenuItem(item.menuItemId);
            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(item.quantity));
            subtotal = subtotal.add(lineTotal);
            categoryCodes.add(menuItem.getCategoryCode());

            items.add(new CartItemResponse(
                    item.id,
                    menuItem.getId(),
                    menuItem.getName(),
                    menuItem.getPrice(),
                    item.quantity,
                    lineTotal,
                    item.options));
        }

        BigDecimal shippingFee = subtotal.compareTo(BigDecimal.ZERO) > 0 ? DEFAULT_SHIPPING_FEE : BigDecimal.ZERO;
        if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            shippingFee = BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        String appliedVoucherCode = cart.voucherCode;

        if (appliedVoucherCode != null) {
            Voucher voucher = vouchersByCode.get(appliedVoucherCode);
            if (voucher != null) {
                VoucherValidationResult validation = evaluateVoucher(voucher, subtotal, categoryCodes);
                if (validation.applicable) {
                    discount = validation.discountAmount;
                }
            }
        }

        BigDecimal total = subtotal.add(shippingFee).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        int itemCount = items.stream().mapToInt(CartItemResponse::quantity).sum();

        return new CartResponse(
                cart.userId,
                items,
                subtotal,
                shippingFee,
                discount,
                total,
                appliedVoucherCode,
                itemCount,
                20);
    }

    private CartPricing calculatePricing(Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<String> categoryCodes = new ArrayList<>();

        for (CartItem item : cart.items) {
            MenuItemEntity menuItem = requireMenuItem(item.menuItemId);
            subtotal = subtotal.add(menuItem.getPrice().multiply(BigDecimal.valueOf(item.quantity)));
            categoryCodes.add(menuItem.getCategoryCode());
        }

        return new CartPricing(subtotal, categoryCodes);
    }

    private VoucherValidationResult evaluateVoucher(Voucher voucher, BigDecimal subtotal,
            List<String> cartCategoryCodes) {
        if (isVoucherExpired(voucher)) {
            return new VoucherValidationResult(false, BigDecimal.ZERO, "Voucher has expired");
        }

        if (subtotal.compareTo(voucher.minimumOrder) < 0) {
            return new VoucherValidationResult(
                    false,
                    BigDecimal.ZERO,
                    "Order does not meet minimum value " + voucher.minimumOrder.toPlainString());
        }

        if (!voucher.categoryCodes.isEmpty()) {
            boolean hasMatch = cartCategoryCodes != null
                    && cartCategoryCodes.stream().anyMatch(code -> voucher.categoryCodes.stream()
                            .anyMatch(voucherCode -> voucherCode.equalsIgnoreCase(code)));
            if (!hasMatch) {
                return new VoucherValidationResult(false, BigDecimal.ZERO, "Voucher does not apply to cart category");
            }
        }

        BigDecimal discount = switch (voucher.discountType) {
            case PERCENT -> subtotal.multiply(voucher.discountValue)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> voucher.discountValue;
        };

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return new VoucherValidationResult(true, discount, "Voucher is applicable");
    }

    private boolean isVoucherExpired(Voucher voucher) {
        return voucher.expiryDate != null && voucher.expiryDate.isBefore(LocalDate.now());
    }

    private UserEntity requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private MenuItemEntity requireMenuItem(String itemId) {
        return menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Menu item not found"));
    }

    private Voucher requireVoucher(String code) {
        Voucher voucher = vouchersByCode.get(code.trim().toUpperCase(Locale.ROOT));
        if (voucher == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Voucher not found");
        }
        return voucher;
    }

    private OrderEntity requireOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private void addVoucher(Voucher voucher) {
        vouchersByCode.put(voucher.code, voucher);
    }

    private UserProfileResponse toUserProfileResponse(UserEntity account) {
        return new UserProfileResponse(
                account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getAddress(),
                account.getDateOfBirth() == null ? null : account.getDateOfBirth().toString());
    }

    private MenuItemResponse toMenuItemResponse(MenuItemEntity item) {
        return new MenuItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCategoryCode(),
                item.isAvailable());
    }

    private VoucherResponse toVoucherResponse(Voucher voucher) {
        return new VoucherResponse(
                voucher.code,
                voucher.title,
                voucher.description,
                voucher.discountType,
                voucher.discountValue,
                voucher.minimumOrder,
                voucher.expiryDate == null ? null : voucher.expiryDate.toString(),
                voucher.categoryCodes);
    }

    private OrderResponse toOrderResponse(OrderEntity order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItemEntity::getId, Comparator.nullsLast(Long::compareTo)))
                .map(item -> new OrderItemResponse(
                        item.getCartItemId(),
                        item.getMenuItemId(),
                        item.getName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineTotal(),
                        decodeOptions(item.getOptionsEncoded())))
                .toList();

        List<StatusTimelineResponse> timeline = order.getTimeline().stream()
                .sorted(Comparator.comparing(OrderTimelineEntity::getChangedAt))
                .map(event -> new StatusTimelineResponse(event.getStatus(), event.getChangedAt().toString(),
                        event.getNote()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                items,
                order.getSubtotal(),
                order.getShippingFee(),
                order.getDiscount(),
                order.getTotal(),
                order.getVoucherCode(),
                order.getPaymentMethod(),
                order.getDeliveryAddress(),
                order.getNote(),
                order.getScheduledAt() == null ? null : order.getScheduledAt().toString(),
                order.getStatus(),
                timeline,
                order.getCreatedAt().toString(),
                order.getReview() == null ? null : toReviewResponse(order.getReview()));
    }

    private ReviewResponse toReviewResponse(OrderReviewEntity review) {
        return new ReviewResponse(
                review.getOrderId(),
                review.getRating(),
                review.getComment(),
                review.getReviewedAt().toString());
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replace(" ", "").trim();
    }

    private static boolean containsIgnoreCase(String text, String keyword) {
        return text != null
                && keyword != null
                && text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (trimToNull(value) == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
    }

    private static LocalDate parseDate(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }

        try {
            return LocalDate.parse(normalized);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " must follow format yyyy-MM-dd");
        }
    }

    private static LocalDateTime parseDateTime(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }

        try {
            return LocalDateTime.parse(normalized);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " must follow format yyyy-MM-ddTHH:mm:ss");
        }
    }

    private static Map<String, String> sanitizeOptions(Map<String, String> options) {
        if (options == null || options.isEmpty()) {
            return Map.of();
        }

        Map<String, String> cleaned = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String key = trimToNull(entry.getKey());
            String value = trimToNull(entry.getValue());
            if (key != null && value != null) {
                cleaned.put(key, value);
            }
        }

        return cleaned.isEmpty() ? Map.of() : Map.copyOf(cleaned);
    }

    private static String encodeOptions(Map<String, String> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        return options.entrySet().stream()
                .map(entry -> encodeUrlComponent(entry.getKey()) + "=" + encodeUrlComponent(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static Map<String, String> decodeOptions(String encoded) {
        String normalized = trimToNull(encoded);
        if (normalized == null) {
            return Map.of();
        }

        Map<String, String> decoded = new LinkedHashMap<>();
        for (String pair : normalized.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            String key = parts.length > 0 ? trimToNull(decodeUrlComponent(parts[0])) : null;
            String value = parts.length > 1 ? trimToNull(decodeUrlComponent(parts[1])) : null;
            if (key != null && value != null) {
                decoded.put(key, value);
            }
        }

        return decoded.isEmpty() ? Map.of() : Map.copyOf(decoded);
    }

    private static String encodeUrlComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String decodeUrlComponent(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static long extractSequence(String id, String prefix, long fallback) {
        if (id == null || !id.startsWith(prefix)) {
            return fallback;
        }

        String numeric = id.substring(prefix.length());
        try {
            return Long.parseLong(numeric);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static CategoryEntity newCategory(String id, String code, String name) {
        CategoryEntity category = new CategoryEntity();
        category.setId(id);
        category.setCode(code);
        category.setName(name);
        return category;
    }

    private static MenuItemEntity newMenuItem(
            String id,
            String name,
            String description,
            BigDecimal price,
            String categoryCode,
            boolean available) {
        MenuItemEntity item = new MenuItemEntity();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategoryCode(categoryCode);
        item.setAvailable(available);
        return item;
    }

    public record RegisterRequest(
            String fullName,
            String email,
            String phone,
            String address,
            String dateOfBirth,
            String password) {
    }

    public record LoginRequest(String credential, String password) {
    }

    public record UpdateProfileRequest(
            String fullName,
            String email,
            String phone,
            String address,
            String dateOfBirth) {
    }

    public record AddCartItemRequest(String menuItemId, Integer quantity, Map<String, String> options) {
    }

    public record UpdateCartItemQuantityRequest(Integer quantity) {
    }

    public record ApplyVoucherRequest(String code) {
    }

    public record VoucherValidationRequest(String code, BigDecimal subtotal, List<String> categoryCodes) {
    }

    public record CheckoutRequest(
            String userId,
            String deliveryAddress,
            String note,
            PaymentMethod paymentMethod,
            String scheduledAt) {
    }

    public record UpdateOrderStatusRequest(OrderStatus status) {
    }

    public record CancelOrderRequest(String reason) {
    }

    public record ReviewRequest(Integer rating, String comment) {
    }

    public record AuthResponse(String token, UserProfileResponse user) {
    }

    public record UserProfileResponse(
            String id,
            String fullName,
            String email,
            String phone,
            String address,
            String dateOfBirth) {
    }

    public record CategoryResponse(String id, String code, String name) {
    }

    public record MenuItemResponse(
            String id,
            String name,
            String description,
            BigDecimal price,
            String categoryCode,
            boolean available) {
    }

    public record VoucherResponse(
            String code,
            String title,
            String description,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minimumOrder,
            String expiryDate,
            List<String> categoryCodes) {
    }

    public record VoucherValidationResponse(
            boolean applicable,
            String code,
            BigDecimal discountAmount,
            String message,
            VoucherResponse voucher) {
    }

    public record CartResponse(
            String userId,
            List<CartItemResponse> items,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            BigDecimal discount,
            BigDecimal total,
            String appliedVoucherCode,
            int itemCount,
            int estimatedDeliveryMinutes) {
    }

    public record CartItemResponse(
            String cartItemId,
            String menuItemId,
            String name,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal,
            Map<String, String> options) {
    }

    public record OrderResponse(
            String orderId,
            String userId,
            List<OrderItemResponse> items,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            BigDecimal discount,
            BigDecimal total,
            String voucherCode,
            PaymentMethod paymentMethod,
            String deliveryAddress,
            String note,
            String scheduledAt,
            OrderStatus status,
            List<StatusTimelineResponse> timeline,
            String createdAt,
            ReviewResponse review) {
    }

    public record OrderItemResponse(
            String cartItemId,
            String menuItemId,
            String name,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal,
            Map<String, String> options) {
    }

    public record StatusTimelineResponse(OrderStatus status, String changedAt, String note) {
    }

    public record ReviewResponse(String orderId, int rating, String comment, String reviewedAt) {
    }

    public record NotificationResponse(String id, String title, String message, String createdAt, boolean read) {
    }

    public enum PaymentMethod {
        CASH,
        MOMO,
        CARD
    }

    public enum OrderStatus {
        PLACED,
        PREPARING,
        DELIVERING,
        DELIVERED,
        CANCELLED
    }

    public enum DiscountType {
        PERCENT,
        FIXED_AMOUNT
    }

    private static final class Voucher {
        private final String code;
        private final String title;
        private final String description;
        private final DiscountType discountType;
        private final BigDecimal discountValue;
        private final BigDecimal minimumOrder;
        private final LocalDate expiryDate;
        private final List<String> categoryCodes;

        private Voucher(
                String code,
                String title,
                String description,
                DiscountType discountType,
                BigDecimal discountValue,
                BigDecimal minimumOrder,
                LocalDate expiryDate,
                List<String> categoryCodes) {
            this.code = code.toUpperCase(Locale.ROOT);
            this.title = title;
            this.description = description;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.minimumOrder = minimumOrder;
            this.expiryDate = expiryDate;
            this.categoryCodes = categoryCodes == null
                    ? List.of()
                    : categoryCodes.stream().map(value -> value.toUpperCase(Locale.ROOT)).toList();
        }
    }

    private static final class Cart {
        private final String userId;
        private final List<CartItem> items = new ArrayList<>();
        private String voucherCode;

        private Cart(String userId) {
            this.userId = userId;
        }
    }

    private static final class CartItem {
        private final String id;
        private final String menuItemId;
        private int quantity;
        private final Map<String, String> options;

        private CartItem(String id, String menuItemId, int quantity, Map<String, String> options) {
            this.id = id;
            this.menuItemId = menuItemId;
            this.quantity = quantity;
            this.options = options;
        }
    }

    private record CartPricing(BigDecimal subtotal, List<String> categoryCodes) {
    }

    private record VoucherValidationResult(boolean applicable, BigDecimal discountAmount, String reason) {
    }
}
