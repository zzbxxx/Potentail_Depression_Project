export default class MoodApiService {

    static BASE_URL = "api/card";

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
}
