import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from './LoginPage';
import CustomerLayout from './customer/CustomerLayout';
import AdminLayout from './admin/AdminLayout';

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/customer/*" element={<CustomerLayout />} />
      <Route path="/admin/*" element={<AdminLayout />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default App;


