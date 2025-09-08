// authInterface.js
const API_PREFIX = '/api/auth'; // 顺便把单词拼写修正一下

export async function login(data) {
    const response = await fetch(`${API_PREFIX}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
    });

    return response.json();
}

export async function register(data) {
    console.log(data);

    const response = await fetch(`${API_PREFIX}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
    });
    console.log(response);

    return response.json();
}