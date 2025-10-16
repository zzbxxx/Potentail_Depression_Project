export default class RoomService {
    static BASE_URL = "/api/rooms"

    static async createRoom(data) {
        const response = await fetch(`${this.BASE_URL}/createRoom`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        })
        return response.json()
    }

    static async getActiveRooms() {
        const response = await fetch(`${this.BASE_URL}/active`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        })
        return response.json()
    }

    static async getRoomById(roomId) {
        const response = await fetch(`${this.BASE_URL}/${roomId}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        })
        const data = await response.json();
        if (!data.success) {
            throw new Error(data.message || '獲取房間失敗')
        }

        if (data.data.users) {
            data.data.users = data.data.users.map(u => ({
                ...u,
                seatIndex: u.seatIndex || 0
            }))
        }
        return data
    }

    static async joinRoom(roomId, userId) {
        const response = await fetch(`${this.BASE_URL}/join/${roomId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ userId })
        })
        return response.json()
    }

    static async leaveRoom(roomId, userId) {
        console.log("id:" + userId);
        const response = await fetch(`${this.BASE_URL}/leave/${roomId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ userId })
        })
        return response.json()
    }

    static async getCurrentRoom(userId) {
        const response = await fetch(`${this.BASE_URL}/current/${userId}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            }
        });
        return response.json();
    }

    // 新增方法：查詢我的房間
    static async getMyRoom(userId) {
        const response = await fetch(`${this.BASE_URL}/my-room/${userId}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });
        return response.json();
    }

    static async updateRoomAvatar(roomId, userId, avatar) {
        try {
            const response = await fetch(`${this.BASE_URL}/${roomId}/avatar`, {
                method: "PUT",  // ✅ 修改為 PUT
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ userId, avatar })
            })

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`)
            }

            return await response.json()
        } catch (error) {
            console.error('更新頭像 API 錯誤:', error)
            throw error
        }
    }

    static async updateUserState(roomId, userId, status, message, timer) {
        const response = await fetch(`${this.BASE_URL}/${roomId}/userState`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userId, status, message, timer })
        })
        const data = await response.json()
        if (!data.success) throw new Error(data.message)
        return data
    }
}