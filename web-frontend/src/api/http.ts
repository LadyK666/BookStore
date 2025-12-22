import axios from 'axios';

// 基础 Axios 实例：所有请求走 /api 前缀，开发时由 Vite 代理到后端
export const http = axios.create({
  baseURL: '/api',
  timeout: 15000
});

http.interceptors.response.use(
  (resp) => resp,
  (error) => {
    // 这里可以统一错误提示，前期先简单抛出
    return Promise.reject(error);
  }
);


