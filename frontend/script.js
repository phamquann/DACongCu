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
