export default class PersonalMessageApi {
    static BASE_URL = 'http://localhost:8080/api/picture';
    static EMAIL_BASE_URL = 'http://localhost:8080/api/email';

    static async getPersonalInfo(userId) {

        if (!userId) throw new Error('缺少 userId');

        const res = await fetch(
            `http://localhost:8080/api/userMessage/personal?userId=${userId}`,
            {
                method: 'GET',
                headers: { Accept: 'application/json' },
            }
        );

        const json = await res.json();
        console.log(json);


        return json;
    }

    static async getPicturePresignedUrl(fileName, fileType) {
        try {
            const url = new URL(`${this.BASE_URL}/get-presigned-url`);
            url.searchParams.append('fileName', fileName);
            console.log('Requesting presigned URL for file:', fileName);
            console.log('File type:', fileType);

            const res = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                },
            });

            if (!res.ok) {
                throw new Error(`获取预签名 URL 失败: ${res.status} ${res.statusText}`);
            }

            const data = await res.json();
            if (!data.success) {
                throw new Error('获取上传链接失败');
            }

            return { url: data.url, publicUrl: data.publicUrl };
        } catch (error) {
            throw new Error(`获取预签名 URL 失败: ${error.message}`);
        }
    }

    static async getEmailPresignedUrl(fileName, fileType) {
        try {
            const url = new URL(`${this.EMAIL_BASE_URL}/get-presigned-url`);
            url.searchParams.append('fileName', fileName);
            console.log('Requesting presigned URL for email file:', fileName);
            console.log('File type:', fileType);

            const res = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                },
            });

            if (!res.ok) {
                throw new Error(`获取预签名 URL 失败: ${res.status} ${res.statusText}`);
            }

            const data = await res.json();
            if (!data.success) {
                throw new Error('获取邮箱上传链接失败');
            }

            return { url: data.url, publicUrl: data.publicUrl };
        } catch (error) {
            throw new Error(`获取邮箱预签名 URL 失败: ${error.message}`);
        }
    }

    static async uploadFile(url, file) {
        try {
            const uploadRes = await fetch(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': file.type,
                },
                body: file,
            });

            console.log('Upload response status:', uploadRes.status);
            console.log('Upload response headers:', [...uploadRes.headers]);

            if (!uploadRes.ok) {
                const errorText = await uploadRes.text();
                console.error('Upload failed:', uploadRes.status, errorText);
                return false;
            }
            return true;
        } catch (error) {
            console.error('Upload error:', error);
            return false;
        }
    }

    static async listUploadedEmails() {
        try {
            const res = await fetch(`${this.EMAIL_BASE_URL}/list-uploaded`, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                },
            });

            if (!res.ok) {
                throw new Error(`获取已上传邮箱文件失败: ${res.status} ${res.statusText}`);
            }

            const data = await res.json();
            if (!data.success) {
                throw new Error('获取已上传邮箱文件列表失败');
            }

            return data.files;
        } catch (error) {
            throw new Error(`获取已上传邮箱文件失败: ${error.message}`);
        }
    }

    static async getPicturePresignedDownloadUrl(fileKey) {
        try {
            const url = new URL(`${this.BASE_URL}/get-presigned-download-url`);
            url.searchParams.append('key', fileKey);

            const res = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                },
            });

            const data = await res.json();
            if (!data.success) {
                throw new Error('刷新下载链接失败');
            }

            return data.url;
        } catch (error) {
            throw new Error(`刷新下载链接失败: ${error.message}`);
        }
    }

    static async putPersonalInfo(data) {
        let userId = localStorage.getItem("userId") || localStorage.getItem("user_id")

        try {
            const res = await fetch(`http://localhost:8080/api/userMessage/profile?userId=${userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                },
                body: JSON.stringify(data),
            });

            if (!res.ok) {
                throw new Error(`保存用户信息失败: ${res.status} ${res.statusText}`);
            }

            const result = await res.json();
            if (!result.success) {
                throw new Error('保存用户信息失败');
            }

            return result;
        } catch (error) {
            throw new Error(`保存用户信息失败: ${error.message}`);
        }
    }
}
