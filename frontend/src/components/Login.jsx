import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';

function Login({ onLoginSuccess }) {
  const navigate = useNavigate();
  const [isRegister, setIsRegister] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phone: '',
    address: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isRegister) {
        const res = await api.post('/api/auth/register', formData);
        const userData = res.data.user || res.data.customer;
        localStorage.setItem('token', res.data.token);
        localStorage.setItem('user', JSON.stringify(userData));
        onLoginSuccess(userData);
        navigate('/');
      } else {
        const res = await api.post('/api/auth/login', {
          email: formData.email,
          password: formData.password
        });
        const userData = res.data.user || res.data.customer;
        localStorage.setItem('token', res.data.token);
        localStorage.setItem('user', JSON.stringify(userData));
        onLoginSuccess(userData);
        navigate('/');
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Authentication failed. Please check credentials.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const fillDemoCustomer = () => {
    setFormData({
      ...formData,
      email: 'rahul.verma@example.com',
      password: 'Customer@123'
    });
    setIsRegister(false);
  };

  const fillDemoAdmin = () => {
    setFormData({
      ...formData,
      email: 'admin@storeconnect.com',
      password: 'Admin@123'
    });
    setIsRegister(false);
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2 className="auth-title">{isRegister ? 'Create Account' : 'Welcome Back'}</h2>
        <p className="auth-subtitle">
          {isRegister ? 'Sign up to shop omnichannel retail with store pickup' : 'Sign in to access your cart and orders'}
        </p>

        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          {isRegister && (
            <div className="form-row">
              <div className="form-group">
                <label>First Name</label>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  placeholder="e.g. Vineeth"
                />
              </div>
              <div className="form-group">
                <label>Last Name</label>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                  placeholder="e.g. Kumar"
                />
              </div>
            </div>
          )}

          <div className="form-group">
            <label>Email Address</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              placeholder="e.g. user@storeconnect.com"
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              placeholder="At least 8 characters"
            />
          </div>

          {isRegister && (
            <>
              <div className="form-group">
                <label>Phone Number</label>
                <input
                  type="tel"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="+91 9876543210"
                />
              </div>
              <div className="form-group">
                <label>Delivery Address</label>
                <input
                  type="text"
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  placeholder="Sector 24, CyberHub, Gurugram"
                />
              </div>
            </>
          )}

          <button type="submit" className="btn-primary-full" disabled={loading}>
            {loading ? 'Please wait...' : (isRegister ? 'Register Account' : 'Sign In')}
          </button>
        </form>

        <div className="auth-footer">
          <button
            type="button"
            className="btn-link"
            onClick={() => { setIsRegister(!isRegister); setError(''); }}
          >
            {isRegister ? 'Already have an account? Sign In' : "Don't have an account? Create One"}
          </button>
        </div>

        {!isRegister && (
          <div className="demo-credentials-box">
            <span>Demo Quick Fill:</span>
            <div className="demo-pills-row">
              <button type="button" className="btn-demo-pill" onClick={fillDemoCustomer}>
                Customer (rahul.verma@example.com)
              </button>
              <button type="button" className="btn-demo-pill btn-demo-admin" onClick={fillDemoAdmin}>
                Admin (admin@storeconnect.com)
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Login;
