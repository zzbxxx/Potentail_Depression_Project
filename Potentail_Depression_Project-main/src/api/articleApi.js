export default class ArticleService {
    static BASE_URL = "api/articles";
    static REPLY_BASE_URL = "api/reply";

    static async putArticleReplyInfo(data) {
        const { userId, ...replyData } = data;
        const response = await fetch(`/${this.REPLY_BASE_URL}/putReply?userId=${userId}`, {
            method: 'POST',
            body: JSON.stringify(replyData),
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response.json();
    }

    static async deleteArticleReply(replyId, userId) {
        const response = await fetch(`/${this.REPLY_BASE_URL}/deleteReply?replyId=${replyId}&userId=${userId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response.json();
    }

    static async getArticleReplyInfo(articleId, userId) {
        const response = await fetch(`/${this.REPLY_BASE_URL}/getArticleReply?articleId=${articleId}&userId=${userId}`);
        return response.json();
    }

    static async getAllArticles() {
        const response = await fetch(`${this.BASE_URL}/getAllArticles`);
        return response.json();
    }


    static async putArticleData(formData) {
        const response = await fetch(`${this.BASE_URL}/putArticleData`, {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            let errorMessage;
            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.error || '未知错误';
            } catch {
                errorMessage = await response.text() || '网络错误';
            }
            throw new Error(errorMessage);
        }
        return response.json();
    }

    static async getArticleData(articleId) {
        const response = await fetch(`/${this.BASE_URL}/getDataInfoByArticleId?articleId=${articleId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response.json();
    }

    static async updateArticleStatus(articleId, status) {
        const response = await fetch(`/${this.BASE_URL}/updateStatus?articleId=${articleId}&status=${status}`, {
            method: 'POST',
        })
        return response.json();
    }

    static async getApprovedArticles() {
        const response = await fetch(`/${this.BASE_URL}/getApprovedArticles`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response.json();
    }
    static async getPendingArticles() {
        const response = await fetch(`/${this.BASE_URL}/getPendingArticles`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response.json();
    }
    static async getRejectedArticles() {
        const response = await fetch(`/${this.BASE_URL}/getRejectedArticles`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response.json();
    }
}