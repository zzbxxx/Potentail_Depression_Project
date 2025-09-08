class DeviceApiService {
    static BASE_URL = '/api/devices';

    /**
     * 注册设备并获取找回码
     * @returns {Promise<{recoveryCode: string, expiresAt: string}>}
     */
    static async registerDevice() {
        const fingerprint = await this._generateDeviceFingerprint();
        const response = await fetch(`${this.BASE_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({
                fingerprint,
                deviceInfo: JSON.stringify({
                    userAgent: navigator.userAgent,
                    screen: `${screen.width}x${screen.height}`
                })
            })
        });

        if (!response.ok) {
            throw new Error(`请求失败: ${response.status}`);
        }
        console.log(response);

        return await response.json();
    }

    static async _generateDeviceFingerprint() {
        try {
            const hardwareData = {
                screen: `${screen.width}x${screen.height}`,
                cpuCores: navigator.hardwareConcurrency,
                timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
                userAgent: navigator.userAgent,
                platform: navigator.platform,
                deviceMemory: navigator.deviceMemory || 'unknown'
            };

            hardwareData.canvas = await this._getCanvasFingerprint();
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

    /**
     * 获取Canvas指纹
     */
    static async _getCanvasFingerprint() {
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
            return 'canvas-unsupported';
        }
    }
}

export default DeviceApiService;