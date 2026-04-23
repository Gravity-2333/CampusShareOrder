import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    proxy: {
      '/api': {
        changeOrigin: true,
        target: 'http://localhost:8080',
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('element-plus')) {
              return 'element-plus'
            }

            if (id.includes('vue-router')) {
              return 'vue-router'
            }

            if (id.includes('/vue/') || id.includes('\\vue\\')) {
              return 'vue-core'
            }

            return 'vendor'
          }

          if (id.includes('/src/mock/') || id.includes('\\src\\mock\\')) {
            if (id.endsWith('database.js') || id.endsWith('shared.js')) {
              return 'mock-core'
            }

            return 'mock-provider'
          }

          if (id.includes('/src/api/providers/live/') || id.includes('\\src\\api\\providers\\live\\')) {
            return 'live-provider'
          }
        },
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
