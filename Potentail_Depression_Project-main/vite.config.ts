import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import type { ViteDevServer } from 'vite'
import { redirectMiddleware } from './src/middleware/redirect'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
    plugins: [
        vue(),
        {
            name: 'redirect-plugin',
            configureServer(server: ViteDevServer) {
                server.middlewares.use(redirectMiddleware())
            }
        },
        AutoImport({
            resolvers: [ElementPlusResolver()],
        }),
        Components({
            resolvers: [ElementPlusResolver()],
        }),
    ],
    base: './',
    build: {
        outDir: 'dist',
        rollupOptions: {
            input: {
                main: path.resolve(__dirname, 'index.html'),
                mainPage: path.resolve(__dirname, 'public/main-page.html')
            }
        }
    },
    server: {
        port: 5173,
        open: '/index.html',
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                rewrite: (path) => path
            }
        }
    }
})