export default class MoodApiService {

    static BASE_URL = "api/card";

    static async getToadyCard() {
        const userId = localStorage.getItem("userId")
        const response = await fetch(`${this.BASE_URL}/today?userId=${userId}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
        })


        return response.json();
    }
}
