import React, { useState, useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Products from './components/Products';
import Cart from './components/Cart';
import Orders from './components/Orders';
import Login from './components/Login';
import api from './api/client';
import './App.css';

function App() {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  const [cartCount, setCartCount] = useState(0);

  const refreshCartCount = async () => {
    if (!localStorage.getItem('token')) {
      setCartCount(0);
      return;
    }
    try {
      const res = await api.get('/api/cart');
      const count = res.data?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;
      setCartCount(count);
    } catch {
      setCartCount(0);
    }
  };

  useEffect(() => {
    if (user) {
      refreshCartCount();
    } else {
      setCartCount(0);
    }
  }, [user]);

  const handleLoginSuccess = (userData) => {
    setUser(userData);
    refreshCartCount();
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setCartCount(0);
  };

  return (
    <div className="app-layout">
      <Navbar user={user} onLogout={handleLogout} cartCount={cartCount} />

      <main className="main-content">
        <Routes>
          <Route
            path="/"
            element={<Products user={user} onCartUpdated={refreshCartCount} />}
          />
          <Route
            path="/cart"
            element={<Cart user={user} onCartUpdated={refreshCartCount} />}
          />
          <Route path="/orders" element={<Orders user={user} />} />
          <Route path="/login" element={<Login onLoginSuccess={handleLoginSuccess} />} />
          <Route path="*" element={<Products user={user} onCartUpdated={refreshCartCount} />} />
        </Routes>
      </main>

      <footer className="footer">
        <p>StoreConnect Omnichannel Retail Platform &copy; 2026 | Native Spring Boot 4.1.1 Microservices</p>
      </footer>
    </div>
  );
}

export default App;
