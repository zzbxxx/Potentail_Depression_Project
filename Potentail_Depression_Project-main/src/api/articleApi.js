import { log } from "three/src/nodes/TSL.js";

export default class ArticleService {

    static BASE_URL = "api/articles"

    static REPLY_BASE_URL = "api/reply"

    static async putArticleReplyInfo(data) {
        const response = fetch(`${this.REPLY_BASE_URL}/putReply`, {
            method: 'PUT',
            body: data
        })
        console.log("response:" + response);

        return response.data;
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
                errorMessage = errorData.message || errorData.error || '未知錯誤';
            } catch {
                errorMessage = await response.text() || '網絡錯誤';
            }
            throw new Error(errorMessage);
        }
        return response.json();
    }

    static async getArticleData(articleId) {
        const response = await fetch(`/${this.BASE_URL}/getDataInfoByArticleId?ArticleId=${articleId}`);
        return response.json();
    }
}

