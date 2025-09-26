export default class MoodApiService {

    static BASE_URL = "api/card";
    static MOOD_BASE_URL = "api/mood";
    static async saveMood(data) {
        const response = await fetch(`${this.MOOD_BASE_URL}/saveMood`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        console.log(response);
        return response.json();
    }

    static async getTodayCard() {

        let userId = localStorage.getItem("userId");
        let isGuest = false;

        if (!userId) {
            userId = localStorage.getItem("user_id");
            isGuest = true;
        }

        const params = new URLSearchParams();
        if (userId) params.set("userId", userId);

        const response = await fetch(`${this.BASE_URL}/getCardToday?${params}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        return response.json();
    }

    static async getCardHistoryInfo() {
        let userId = localStorage.getItem("userId") || localStorage.getItem("user_id")

        const res = await fetch(`${this.BASE_URL}/CardHistory?userId=${userId}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        })

        return res.json();

    }

    static async getBriefCardHistoryInfo() {
        let userId = localStorage.getItem("userId") || localStorage.getItem("user_id")

        const res = await fetch(`${this.BASE_URL}/BriefCardHistory?userId=${userId}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        })

        return res.json();

    }

    static async getMoodHistoryInfo() {
        let userId = localStorage.getItem("userId") || localStorage.getItem("user_id")

        const res = await fetch(`${this.MOOD_BASE_URL}/getMoodHistory?userId=${userId}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        })

        return res.json();
    }

    static async getCardByCardId(userId, contentId) {
        const response = await fetch(`${this.BASE_URL}/getCardByCardId?userId=${userId}&contentId=${contentId}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });
        return response.json();
    }
}
