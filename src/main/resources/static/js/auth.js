/**
 * auth.js — jQuery AJAX handler cho JWT demo (Bai 4).
 * - Dang nhap qua POST /auth/login → luu token vao localStorage
 * - Load thong tin user qua GET /api/users/me (Bearer token)
 * - Dang xuat (xoa localStorage, chuyen ve /login)
 */

$(document).ready(function () {

    /* ====================================================
     * Load profile (tren trang /user/profile)
     * ==================================================== */
    if ($('#profile-name').length) {
        loadProfile();
    }

    /* ====================================================
     * Nut dang nhap qua JWT API (tren trang /login)
     * ==================================================== */
    $('#btn-api-login').on('click', function () {
        var email    = $('#api-email').val().trim();
        var password = $('#api-password').val().trim();

        if (!email || !password) {
            showApiMessage('Vui lòng nhập email và mật khẩu.', 'error');
            return;
        }

        var $btn = $(this);
        $btn.prop('disabled', true).text('Đang xử lý...');

        $.ajax({
            type: 'POST',
            url: '/auth/login',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify({ email: email, password: password }),
            success: function (data) {
                localStorage.setItem('jwt_token', data.token);
                window.location.href = '/user/profile';
            },
            error: function (xhr) {
                var msg = 'Đăng nhập thất bại.';
                if (xhr.responseJSON) {
                    msg = xhr.responseJSON.detail || xhr.responseJSON.message || msg;
                }
                showApiMessage(msg, 'error');
                $btn.prop('disabled', false).text('Đăng nhập (JWT)');
            }
        });
    });

    /* ====================================================
     * Nut dang xuat (tren trang /user/profile)
     * ==================================================== */
    $('#btn-logout-api').on('click', function () {
        localStorage.removeItem('jwt_token');
        window.location.href = '/login';
    });

    /* ====================================================
     * Helper: hien thi thong bao
     * ==================================================== */
    function showApiMessage(msg, type) {
        var $box = $('#api-message');
        $box
            .removeClass('alert-error alert-success')
            .addClass(type === 'error' ? 'alert-error' : 'alert-success')
            .text(msg)
            .show();
    }

    /* ====================================================
     * Load thong tin user tu /api/users/me
     * ==================================================== */
    function loadProfile() {
        var token = localStorage.getItem('jwt_token');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        $.ajax({
            type: 'GET',
            url: '/api/users/me',
            contentType: 'application/json; charset=utf-8',
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            },
            success: function (data) {
                var name = data.fullName || data.username || 'Người dùng';
                var initial = name.charAt(0).toUpperCase();

                $('#profile-name').text(name);
                $('#profile-username').text('@' + data.username);
                $('#profile-email').text(data.email);
                $('#profile-role').text(data.role);
                $('#profile-initial').text(initial);

                if (data.images && data.images !== '') {
                    $('#profile-avatar').attr('src', data.images).show();
                    $('#profile-avatar-placeholder').hide();
                }
            },
            error: function () {
                localStorage.removeItem('jwt_token');
                window.location.href = '/login';
            }
        });
    }
});
