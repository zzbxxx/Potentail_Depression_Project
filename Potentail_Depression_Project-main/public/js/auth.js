import { login, register } from './authInterface.js';
import DeviceApiService from './apiService.js'
document.addEventListener('DOMContentLoaded', function () {
    // DOM 元素
    const authContainer = document.getElementById('auth-container');
    const welcomeSection = document.getElementById('welcome');
    const showAuthBtn = document.getElementById('show-auth');
    const toggleTabBtn = document.getElementById('toggle-tab');
    const loginTab = document.getElementById('login-tab');
    const registerTab = document.getElementById('register-tab');
    const loginBtn = document.getElementById('login-btn');
    const registerBtn = document.getElementById('register-btn');
    const errorText = document.getElementById('error-text');
    const errorToast = document.getElementById('error-toast');
    const submitBtn = document.getElementById('cross-submit-btn');
    const recoveryCodeInput = document.getElementById('recovery-code');
    const attemptsLeftSpan = document.getElementById('attempts-left');
    const touristLogin = document.querySelector('#tourist_login')
    let attemptsLeft = 5;

    //提示器
    const showToast = (msg = '', duration = 2000) => {
        errorText.textContent = msg;
        errorToast.style.display = 'block';
        setTimeout(() => (errorToast.style.display = 'none'), duration);
    };

    touristLogin.addEventListener('click', async (e) => {
        e.preventDefault();

        try {
            const fingerprint = await DeviceApiService.generateDeviceFingerprint();

            // 2) 调后端游客登录
            const result = await DeviceApiService.guestLogin(fingerprint);
            // result: { tokenValue, expiresAt, usedFingerprint, userId? }
            console.log("result" + JSON.stringify(result));
            localStorage.setItem('auth_token', result.token);
            localStorage.setItem('token_expires_at', result.expiresAt);
            if (result.userId) {
                localStorage.setItem('user_id', result.userId);
            } else {
                // 如果后端没有 userId，你可以自己生成一个游客ID仅用于前端标识
                const anonId = crypto.randomUUID();
                localStorage.setItem('user_id', anonId);
            }
            const token = localStorage.getItem("token")
            if (token) {
                console.log(token);

                localStorage.removeItem("token")
                localStorage.removeItem("userId")
            }
            window.location.href = '/main-page.html';
        } catch (err) {
            alert(err.message || '游客登录失败，请稍后重试');
        }
    });


    submitBtn.addEventListener('click', async function () {
        attemptsLeft--;
        console.log(attemptsLeft);

        if (attemptsLeft === 0) {
            showToast('验证码错误次数过多，请重新获取');
            return;
        }
        const code = recoveryCodeInput.value.trim();
        if (!code || code.length !== 8) {
            showToast('请输入8位有效的找回码');
            return;
        }
        submitBtn.disabled = true;
        submitBtn.textContent = '验证中...';

        try {
            // 1. 生成新设备指纹
            const newFingerprint = await DeviceApiService.generateDeviceFingerprint();

            // 2. 发送给后端
            const res = await DeviceApiService.verifyRecoveryCode({ code, newDeviceFingerprint: newFingerprint });

            localStorage.setItem('auth_token', res.token);
            location.href = '/main-page.html';
        } catch (err) {
            console.log(err);

            attemptsLeft--;
            attemptsLeftSpan.textContent = attemptsLeft;

            if (attemptsLeft <= 0) {
                showToast('尝试次数已用完，请重新获取找回码');
                submitBtn.disabled = true;
            } else {
                showToast(err.message || '找回码验证失败');
            }
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = '提交';
        }
    });


    const loginForm = {
        username: document.getElementById('login-username'),
        password: document.getElementById('login-password')
    };

    const registerForm = {
        username: document.getElementById('register-username'),
        phone: document.getElementById('register-phone'),
        password: document.getElementById('register-password')
    };

    let activeTab = 'login';

    // 显示认证页面
    showAuthBtn.addEventListener('click', function (e) {
        e.preventDefault();
        welcomeSection.style.display = 'none';
        authContainer.style.display = 'flex';
    });

    // 切换登录/注册标签页
    toggleTabBtn.addEventListener('click', function () {
        if (activeTab === 'login') {
            loginTab.style.display = 'none';
            registerTab.style.display = 'block';
            toggleTabBtn.textContent = '已有账户？直接登录';
            activeTab = 'register';
        } else {
            // 切换到登录
            registerTab.style.display = 'none';
            loginTab.style.display = 'block';
            toggleTabBtn.textContent = '没有账户？立即注册';
            activeTab = 'login';
        }
    });

    // 登录处理
    loginBtn.addEventListener('click', async function () {
        const loginData = {
            username: loginForm.username.value,
            password: loginForm.password.value
        };

        try {
            const res = await login(loginData);

            if (res.code === 200) {
                console.log("登录成功！");
                localStorage.setItem('token', res.token || '');
                localStorage.setItem('userId', res.id || '');

                const auth_token = localStorage.getItem("auth_token")
                if (auth_token) {
                    localStorage.removeItem("auth_token")
                    localStorage.removeItem("token_expires_at")
                    localStorage.removeItem("user_id")
                }
                window.location.href = '/main-page.html';
            } else {
                console.log(res.message);
                alert(res.message || '登录失败');
            }
        } catch (error) {
            console.error('登录错误:', error);
            alert('登录失败，请重试');
        }
    });

    // 注册处理
    registerBtn.addEventListener('click', async function () {
        const registerData = {
            username: registerForm.username.value,
            phone: registerForm.phone.value,
            password: registerForm.password.value
        };

        try {
            const res = await register(registerData);
            if (res.code === 0) {
                alert('注册成功，请登录');
                // 切换回登录页
                registerTab.style.display = 'none';
                loginTab.style.display = 'block';
                toggleTabBtn.textContent = '没有账户？立即注册';
                activeTab = 'login';

                // 清空注册表单
                registerForm.username.value = '';
                registerForm.phone.value = '';
                registerForm.password.value = '';
            } else {
                alert(res.message || '注册失败');
            }
        } catch (error) {
            console.error('注册错误:', error);
            alert('注册失败，请重试');
        }
    });

    // 添加回车键提交支持
    document.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            if (activeTab === 'login') {
                loginBtn.click();
            } else {
                registerBtn.click();
            }
        }
    });

});