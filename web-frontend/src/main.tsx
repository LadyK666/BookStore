import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import App from './pages/App';
import 'antd/dist/reset.css';
import './styles/global.css';

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#722ed1', // Purple base
          colorInfo: '#2f54eb',    // Blue accent
          borderRadius: 8,
          fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif",
          boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)',
        },
        components: {
          Button: {
            boxShadow: '0 2px 0 rgba(0, 0, 0, 0.045)',
            controlHeight: 40,
            borderRadius: 8,
          },
          Card: {
            borderRadiusLG: 16,
            boxShadowTertiary: '0 1px 2px 0 rgba(0, 0, 0, 0.03), 0 1px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px 0 rgba(0, 0, 0, 0.02)',
          },
          Input: {
            controlHeight: 40,
            borderRadius: 8,
          },
          Select: {
            controlHeight: 40,
            borderRadius: 8,
          },
          Layout: {
            bodyBg: '#f5f7fa',
            headerBg: 'transparent',
            siderBg: '#fff',
          },
          Table: {
            borderRadiusLG: 12,
            headerBg: 'transparent',
            headerSplitColor: 'transparent',
          }
        }
      }}
    >
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ConfigProvider>
  </React.StrictMode>
);


