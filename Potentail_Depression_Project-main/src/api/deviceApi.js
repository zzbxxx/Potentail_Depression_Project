export default class DeviceApiService {
    static BASE_URL = 'http://localhost:8080/api/devices';

    static async guestLogin(newDeviceFingerprint) {
        try {
            const response = await fetch(`${this.BASE_URL}/guest-login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    newDeviceFingerprint,
                    deviceInfo: `${navigator.userAgent} | ${screen.width}x${screen.height}`
                })
            });
            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || `游客登录失败 (${response.status})`);
            }
            return await response.json();
        } catch (e) {
            console.error('游客登录请求失败:', e);
            throw e;
        }
    }

    static async verifyRecoveryCode(request) {
        try {
            console.log("Sending recovery request:", JSON.stringify(request, null, 2));
            const response = await fetch(`${this.BASE_URL}/recover`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    code: request.code.trim(),
                    newDeviceFingerprint: request.newDeviceFingerprint,
                })
            });


            if (!response.ok) {
                const errorText = await response.text();
                console.error("API Error Response:", {
                    status: response.status,
                    statusText: response.statusText,
                    errorText
                });
                throw new Error(errorText || `验证失败 (${response.status})`);
            }

            return await response.json();
        } catch (error) {
            console.error("Request Failed:", error);
            throw new Error("网络请求失败，请检查连接");
        }
    }

    static async generateDeviceFingerprint() {
        try {
            const hardwareData = {
                screen: `${screen.width}x${screen.height}`,
                cpuCores: navigator.hardwareConcurrency,
                timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
                userAgent: navigator.userAgent,
                platform: navigator.platform,
                deviceMemory: navigator.deviceMemory || 'unknown'
            };

            // 添加Canvas指纹
            hardwareData.canvas = await this.getCanvasFingerprint();

            // 生成哈希指纹
            const encoder = new TextEncoder();
            const data = encoder.encode(JSON.stringify(hardwareData));
            const hashBuffer = await crypto.subtle.digest('SHA-256', data);
            const hashArray = Array.from(new Uint8Array(hashBuffer));
            return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
        } catch (error) {
            console.error('生成指纹失败:', error);
            throw new Error('无法生成设备标识');
        }
    }

    // 修改为静态方法
    static async getCanvasFingerprint() {
        try {
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');
            canvas.width = 200;
            canvas.height = 50;

            ctx.fillStyle = 'rgb(128, 128, 128)';
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            ctx.fillStyle = 'rgb(255, 0, 0)';
            ctx.font = '18px Arial';
            ctx.fillText('DeviceFingerprint', 10, 30);

            ctx.strokeStyle = 'rgb(0, 0, 255)';
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.lineTo(200, 50);
            ctx.stroke();

            return canvas.toDataURL();
        } catch (e) {
            console.error('Canvas指纹生成失败:', e);
            return 'canvas-unsupported';
        }
    }
}