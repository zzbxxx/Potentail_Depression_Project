export default class LikeService {
    static BASE_URL = '/api/likes';

    static async like(userId, likeableId, authorId, likeableType, category = 'LIKE') {
        try {
            const response = await fetch(this.BASE_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: Number(userId),
                    likeableId: Number(likeableId),
                    authorId: Number(authorId),
                    likeableType,
                    category
                })
            });
            // ... 其他邏輯
        } catch (error) {
            throw new Error(error.message || '點讚失敗，請稍後重試');
        }
    }

    static async unlike(userId, likeableId, authorId, likeableType) {
        console.log("aid" + authorId);
        try {
            const response = await fetch(`${this.BASE_URL}/unlike`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: Number(userId), // 確保 userId 是數字
                    likeableId: Number(likeableId), // 確保 likeableId 是數字
                    authorId: Number(authorId), // 確保 authorId 是數字
                    likeableType
                })
            });
            // ... 其他邏輯
        } catch (error) {
            throw new Error(error.message || '取消點讚失敗，請稍後重試');
        }
    }

    static async checkLike(userId, likeableId, likeableType) {
        try {
            const response = await fetch(`${this.BASE_URL}/isLiked?userId=${userId}&likeableId=${likeableId}&likeableType=${likeableType}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            const contentType = response.headers.get('content-type');
            let data;
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                data = { error: await response.text() };
            }

            if (!response.ok) {
                throw new Error(data.error || '檢查點讚狀態失敗');
            }

            return data; // 返回 { isLiked: boolean, likeCount: number }
        } catch (error) {
            throw new Error(error.message || '檢查點讚狀態失敗，請稍後重試');
        }
    }
}