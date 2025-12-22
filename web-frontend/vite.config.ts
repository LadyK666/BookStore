import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';

// Vite 配置：开发环境将 /api 前缀代理到本地 Spring Boot 后端
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});


