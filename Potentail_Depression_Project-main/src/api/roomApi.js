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
        return response.json()
    }

    static async joinRoom(roomId, userId) {
        console.log("ujid" + userId);
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
}