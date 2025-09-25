export default class FavoriteService {

    static BASE_URL = "/api/favorites"

    static async addFavorite(favorites) {
        const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');
        console.log(favorites)
        const response = await fetch(`${this.BASE_URL}/add?userId=${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(favorites)
        })

        return response.json()
    }

    static async checkFavorite(favoriteableId, favoriteableType) {
        const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');
        const response = await fetch(`${this.BASE_URL}/check?userId=${userId}&favoriteableId=${favoriteableId}&favoriteableType=${favoriteableType}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })

        return response.json()
    }

    static async removeFavorite(removeFavorite) {
        const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');
        const response = await fetch(`${this.BASE_URL}/remove?userId=${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(removeFavorite)
        })

        return response.json()
    }

    static async getFavoriteList(userId, type) {
        const response = await fetch(`${this.BASE_URL}/list?userId=${userId}&type=${type}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })

        return response.json()
    }

}