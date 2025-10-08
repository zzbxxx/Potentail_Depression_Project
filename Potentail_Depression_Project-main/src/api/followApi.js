export default class FollowService {

    static BASE_URL = '/api/follow'

    static async checkFollow(followerId, followedId) {
        const response = await fetch(`${this.BASE_URL}/isFollowing?followerId=${followerId}&followedId=${followedId}`)
        const data = response.json()
        return data
    }

    static async follow(userId, followedId) {
        const response = await fetch(`${this.BASE_URL}/follow?userId=${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                followedId: followedId
            })
        })
        const data = response.json()
        return data;
    }

    static async unfollow(userId, followedId) {
        const response = await fetch(`${this.BASE_URL}/unfollow?userId=${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                followedId: followedId
            })
        })
        const data = response.json()
        return data;
    }

    static async getFollowers(userId) {
        const response = await fetch(`${this.BASE_URL}/follower?userId=${userId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        const data = response.json()
        return data;
    }

    static async getFollowed(userId) {
        const response = await fetch(`${this.BASE_URL}/following?userId=${userId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        const data = response.json()
        return data;
    }


}