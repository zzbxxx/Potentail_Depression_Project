document.addEventListener('DOMContentLoaded', function () {
    const welcomeSection = document.getElementById('welcome');
    const crossLoginSection = document.getElementById('cross-device-login');
    const crossLoginBtn = document.getElementById('cross-device-btn');
    const returnBtn = document.getElementById('return-btn');
    const recoveryInput = document.getElementById('recovery-code');
    const submitBtn = document.getElementById('cross-submit-btn');
    const attemptsLeftEl = document.getElementById('attempts-left');
    const errorPopup = document.getElementById('error-popup');

    let attemptsLeft = 5;
    let isSubmitting = false;

    // 显示跨设备登录界面
    crossLoginBtn.addEventListener('click', function () {
        welcomeSection.style.display = 'none';
        crossLoginSection.style.display = 'flex';
    });

    // 返回按钮
    returnBtn.addEventListener('click', function (e) {
        e.preventDefault();
        crossLoginSection.style.display = 'none';
        welcomeSection.style.display = 'block';
    });

    // 显示错误消息
    function showFlashError(msg) {
        errorPopup.textContent = msg;
        errorPopup.classList.add('show');

        setTimeout(() => {
            errorPopup.classList.remove('show');
        }, 2000);
    }

    // 提交找回码
    async function submitRecoveryCode() {
        const recoveryCode = recoveryInput.value.trim();

        if (!recoveryCode || recoveryCode.length !== 8) {
            showFlashError('请输入8位有效的找回码');
            return;
        }

        if (isSubmitting) return;

        try {
            isSubmitting = true;
            submitBtn.textContent = '验证中...';
            submitBtn.disabled = true;
            errorPopup.classList.remove('show');

            // 1. 生成新设备指纹
            const newFingerprint = await DeviceApiService.generateDeviceFingerprint();

            // 2. 发送找回码和新设备指纹到后端验证
            const response = await DeviceApiService.verifyRecoveryCode({
                code: recoveryCode,
                newDeviceFingerprint: newFingerprint
            });


            // 3. 验证成功后处理
            localStorage.setItem('auth_token', response.token);
            window.location.href = '/index.html';

        } catch (error) {
            attemptsLeft -= 1;
            attemptsLeftEl.textContent = attemptsLeft;

            showFlashError(error.message || '登录失败，请重试');

            if (attemptsLeft <= 0) {
                showFlashError('尝试次数已用完，请重新获取找回码');
            }
        } finally {
            isSubmitting = false;
            submitBtn.textContent = '提交';
            submitBtn.disabled = false;
        }
    }

    // 提交按钮点击事件
    submitBtn.addEventListener('click', submitRecoveryCode);

    // 回车键提交
    recoveryInput.addEventListener('keyup', function (e) {
        if (e.key === 'Enter') {
            submitRecoveryCode();
        }
    });
});