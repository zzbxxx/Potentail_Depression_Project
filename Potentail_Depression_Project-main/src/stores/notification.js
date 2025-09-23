import { defineStore } from 'pinia';

export const useNotificationStore = defineStore('notification', {
    state: () => ({
        notifications: [],
        unreadCount: 0,
    }),
    actions: {
        initializeNotifications(notifications) {
            this.notifications = notifications.map(n => ({
                ...n,
                read: n.read || false,
            }));
            this.updateUnreadCount();
        },
        markAsRead(notificationId) {
            const notification = this.notifications.find(n => n.id === notificationId);
            if (notification && !notification.read) {
                notification.read = true;
                this.updateUnreadCount();
            }
        },
        markAllAsRead() {
            this.notifications.forEach(n => { if (!n.read) n.read = true; });
            this.updateUnreadCount();
        },
        updateUnreadCount(count = null) {
            if (count !== null) {
                this.unreadCount = count;
            } else {
                this.unreadCount = this.notifications.filter(n => !n.read).length;
            }
        },
    },
});