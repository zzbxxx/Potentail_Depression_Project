export default class MoodApiService {

    static BASE_URL = "api/card";
    static MOOD_BASE_URL = "api/mood";
    static async saveMood(data) {
        const response = await fetch(`${this.MOOD_BASE_URL}/save`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        console.log(response);
        return response.json();
    }

    static async getToadyCard() {

        let userId = localStorage.getItem("userId");
        let isGuest = false;

        if (!userId) {
            userId = localStorage.getItem("user_id");
            isGuest = true;
        }

        const params = new URLSearchParams();
        if (userId) params.set("userId", userId);
        if (isGuest) params.set("guest", "1");

        const response = await fetch(`${this.BASE_URL}/today?${params}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        return response.json();
    }

    static async getMoodHistoryInfo() {
        let userId = localStorage.getItem("userId") || localStorage.getItem("user_id")

        const res = await fetch(`${this.BASE_URL}/history?userId=${userId}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        })

        return res.json();

    }
}
