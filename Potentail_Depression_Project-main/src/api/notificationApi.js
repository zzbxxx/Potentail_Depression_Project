export default class NotificationService {
    static BASE_URL = "/api/notifications";

    // 获取未读通知
    static async getUnreadNotifications(userId) {
        try {
            const response = await fetch(`${this.BASE_URL}/user/${userId}/unread`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${localStorage.getItem('token')}`,
                },
                credentials: 'include',
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`获取未读通知失败: ${response.status} - ${errorText}`);
            }
            return await response.json();
        } catch (error) {
            console.error("获取未读通知错误:", error);
            throw error;
        }
    }

    // 获取已读通知
    static async getReadNotifications(userId) {
        try {
            const response = await fetch(`${this.BASE_URL}/user/${userId}/read`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${localStorage.getItem('token')}`,
                },
                credentials: 'include',
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`获取已读通知失败: ${response.status} - ${errorText}`);
            }
            return await response.json();
        } catch (error) {
            console.error("获取已读通知错误:", error);
            throw error;
        }
    }

    static async getNotifications(userId) {
        console.log("userId:" + userId);

        try {
            const response = await fetch(`${this.BASE_URL}/user/${userId}`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: 'include',
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`获取通知失败: ${response.status} - ${errorText}`);
            }
            return await response.json();
        } catch (error) {
            console.error("获取通知错误:", error);
            throw error;
        }
    }

    static async createNotification(data) {
        try {
            console.log(data);

            const response = await fetch(this.BASE_URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(data),
                credentials: 'include',
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`创建通知失败: ${response.status} - ${errorText}`);
            }
            return await response.json();
        } catch (error) {
            console.error("创建通知错误:", error);
            throw error;
        }
    }

    // 标记通知为已读
    static async markAsRead(notificationId) {
        try {
            const response = await fetch(`${this.BASE_URL}/${notificationId}/read`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: 'include',
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`标记通知失败: ${response.status} - ${errorText}`);
            }
            return await response.status === 200;
        } catch (error) {
            console.error("标记通知错误:", error);
            throw error;
        }
    }
}