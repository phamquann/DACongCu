function switchPage(pageId, btn) {
    // Hide all pages
    const pages = document.querySelectorAll('.page');
    pages.forEach(p => {
        p.classList.remove('active');
        p.style.display = 'none';
    });

    // Show selected page
    const selectedPage = document.getElementById(pageId);
    if (selectedPage) {
        selectedPage.style.display = 'grid';
        
        // Reset and trigger animations
        const cards = selectedPage.querySelectorAll('.card');
        cards.forEach((card, index) => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            setTimeout(() => {
                card.style.transition = 'all 0.6s cubic-bezier(0.16, 1, 0.3, 1)';
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, 100 + (index * 100));
        });

        setTimeout(() => {
            selectedPage.classList.add('active');
        }, 10);
    }

    // Update active button
    const buttons = document.querySelectorAll('.nav-btn');
    buttons.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    // Re-initialize icons if needed (Lucide sometimes needs it for dynamically added content)
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
}

// Initial state
document.addEventListener('DOMContentLoaded', () => {
    // Initial icons
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
    
    // Animate initial cards
    const initialCards = document.querySelectorAll('.page.active .card');
    initialCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        setTimeout(() => {
            card.style.transition = 'all 0.6s cubic-bezier(0.16, 1, 0.3, 1)';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 300 + (index * 100));
    });
    
    // Add some interactivity to the progress bar
    const progressFill = document.querySelector('.progress-bar-fill');
    if (progressFill) {
        progressFill.style.width = '0%';
        setTimeout(() => {
            progressFill.style.width = '66%';
        }, 800);
    }

    // Setup interactive elements
    const cancelBtn = document.querySelector('.cancel-order-btn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            if (confirm('Bạn có chắc chắn muốn hủy đơn hàng này không?')) {
                alert('Đơn hàng đã được yêu cầu hủy.');
                cancelBtn.innerText = 'Đã hủy đơn hàng';
                cancelBtn.style.background = '#f0f0f0';
                cancelBtn.style.color = '#999';
                cancelBtn.disabled = true;
            }
        });
    }

    const editAction = document.querySelector('.edit-action');
    if (editAction) {
        editAction.addEventListener('click', (e) => {
            e.preventDefault();
            const inputs = document.querySelectorAll('.input-group input');
            const isReadonly = inputs[0].readOnly;
            
            inputs.forEach(input => {
                input.readOnly = !isReadonly;
                input.style.background = isReadonly ? '#fff' : '#F3F4F6';
                input.style.border = isReadonly ? '1px solid var(--primary)' : 'none';
            });

            editAction.innerHTML = isReadonly ? 
                '<i data-lucide="save" size="16"></i><span>Lưu thay đổi</span>' : 
                '<i data-lucide="edit-3" size="16"></i><span>Chỉnh sửa</span>';
            
            if (typeof lucide !== 'undefined') {
                lucide.createIcons();
            }
        });
    }
});
