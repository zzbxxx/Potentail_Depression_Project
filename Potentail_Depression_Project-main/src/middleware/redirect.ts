import type { Connect } from 'vite'

export function redirectMiddleware(): Connect.NextHandleFunction {
    return (req, res, next) => {
        if (req.url === '/') {
            res.writeHead(302, { Location: '/index.html' })
            res.end()
        } else {
            next?.()
        }
    }
}