document.addEventListener('DOMContentLoaded', function () {

    //检查token正确
    const token = localStorage.getItem('token');
    const hasQuery = window.location.search.length > 0;
    (async () => {
        if (!hasQuery) {
            return
        };

        if (!token) {
            window.location.href = '/';
            return;
        }

        try {
            const res = await fetch('/api/auth/check', {
                headers: { Authorization: `Bearer ${token}` }
            });
            const { valid } = await res.json();
            if (!valid) throw new Error('invalid');
        } catch {
            localStorage.removeItem('token');
            window.location.href = '/';          // token 无效
        }
    })();
    // DOM元素
    const elements = {
        generateBtn: document.getElementById('generateBtn'),
        codeSection: document.getElementById('codeSection'),
        recoveryCode: document.getElementById('recoveryCode'),
        attempts: document.getElementById('attempts'),
        homeBth: document.getElementById('homeBtn'),
    };

    // 应用状态
    const state = {
        attemptsLeft: 3,
        lastGenerated: null
    };

    // 初始化事件监听
    function initEventListeners() {
        elements.generateBtn.addEventListener('click', handleGenerateCode);
        elements.homeBth.addEventListener('click', handleHome);
    }


    function handleHome() {
        //跳转到首页
        window.location.href = '/';
    }

    // 处理生成找回码
    async function handleGenerateCode() {
        console.log(1);

        if (state.attemptsLeft <= 0) {
            alert('操作次数已用完');
            return;
        }

        try {
            setLoadingState(true);

            const { code, expiresAt } = await DeviceApiService.registerDevice();

            console.log(code, expiresAt);

            // 更新UI
            showRecoveryCode(code, expiresAt);
            updateAttemptsCounter();

        } catch (error) {
            console.error('生成失败:', error);
            alert(`生成失败: ${error.message || '服务器错误'}`);
        } finally {
            setLoadingState(false);
        }
    }


    // 辅助函数：显示找回码
    function showRecoveryCode(code, expiresAt) {
        elements.recoveryCode.textContent = code;
        // 先设置为block，然后添加show类触发动画
        elements.codeSection.style.display = 'block';
        setTimeout(() => {
            elements.codeSection.classList.add('show');
        }, 10); // 小延迟确保display:block已应用

        const expiresDate = new Date(expiresAt).toLocaleString();
    }
    // 辅助函数：更新尝试次数
    function updateAttemptsCounter() {
        state.attemptsLeft--;
        elements.attempts.textContent = state.attemptsLeft;

        if (state.attemptsLeft <= 0) {
            elements.generateBtn.disabled = true;
        }
    }

    // 辅助函数：设置加载状态
    function setLoadingState(isLoading) {
        elements.generateBtn.disabled = isLoading;
        elements.generateBtn.textContent = isLoading ? '生成中...' : '发送';
    }

    // 初始化应用
    initEventListeners();
});