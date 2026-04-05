// Toggle password visibility
const togglePassword = document.getElementById('togglePassword');
const passwordInput = document.getElementById('password');
togglePassword.addEventListener('click', () => {
    const type = passwordInput.type === 'password' ? 'text' : 'password';
    passwordInput.type = type;
    togglePassword.textContent = type === 'password' ? '👁️' : '🙈';
});

// Handle login form submit
const loginForm = document.getElementById('loginForm');
const loginResult = document.getElementById('loginResult');
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    loginResult.textContent = '';
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    try {
        const res = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (data.success) {
            loginResult.style.color = '#388e3c';
            loginResult.textContent = 'Đăng nhập thành công!';
            // Chuyển hướng hoặc lưu token ở đây nếu cần
        } else {
            loginResult.style.color = '#d32f2f';
            loginResult.textContent = data.message || 'Đăng nhập thất bại!';
        }
    } catch (err) {
        loginResult.style.color = '#d32f2f';
        loginResult.textContent = 'Không thể kết nối máy chủ!';
    }
});

// Google Sign-In (giả lập)
document.getElementById('googleSignInBtn').addEventListener('click', async () => {
    // Thực tế sẽ dùng Google API, ở đây giả lập popup chọn tài khoản
    const email = prompt('Nhập email Google để đăng nhập:');
    if (!email) return;
    loginResult.textContent = 'Đang xác thực Google...';
    try {
        const res = await fetch('http://localhost:8080/api/auth/google', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });
        const data = await res.json();
        if (data.success) {
            loginResult.style.color = '#388e3c';
            loginResult.textContent = 'Đăng nhập Google thành công!';
        } else {
            loginResult.style.color = '#d32f2f';
            loginResult.textContent = data.message || 'Đăng nhập Google thất bại!';
        }
    } catch (err) {
        loginResult.style.color = '#d32f2f';
        loginResult.textContent = 'Không thể kết nối máy chủ!';
    }
});

// Quên mật khẩu (modal)
const forgotModal = document.getElementById('forgotModal');
const forgotLink = document.getElementById('forgotPasswordLink');
const forgotCloseBtn = document.getElementById('forgotCloseBtn');
const forgotSendBtn = document.getElementById('forgotSendBtn');
const forgotResult = document.getElementById('forgotResult');
forgotLink.addEventListener('click', (e) => {
    e.preventDefault();
    forgotModal.style.display = 'flex';
    forgotResult.textContent = '';
    document.getElementById('forgotEmail').value = '';
});
forgotCloseBtn.addEventListener('click', () => {
    forgotModal.style.display = 'none';
});
forgotSendBtn.addEventListener('click', async () => {
    const email = document.getElementById('forgotEmail').value;
    if (!email || !email.includes('@')) {
        forgotResult.textContent = 'Vui lòng nhập email hợp lệ!';
        return;
    }
    forgotResult.textContent = 'Đang gửi...';
    try {
        const res = await fetch('http://localhost:8080/api/auth/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });
        const data = await res.json();
        if (data.success) {
            forgotResult.style.color = '#388e3c';
            forgotResult.textContent = 'Đã gửi hướng dẫn đặt lại mật khẩu!';
        } else {
            forgotResult.style.color = '#d32f2f';
            forgotResult.textContent = data.message || 'Không gửi được email!';
        }
    } catch (err) {
        forgotResult.style.color = '#d32f2f';
        forgotResult.textContent = 'Không thể kết nối máy chủ!';
    }
});
