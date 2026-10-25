import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import './Login.css';

function Login({ onLoginSuccess }) {
  const navigate = useNavigate();
  const [isRegister, setIsRegister] = useState(false);
  const [formData, setFormData] = useState({ firstName: '', lastName: '', email: '', password: '', phone: '', address: '' });
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
      const endpoint = isRegister ? '/api/auth/register' : '/api/auth/login';
      const payload = isRegister ? formData : { email: formData.email, password: formData.password };
      const res = await api.post(endpoint, payload);
      const userData = res.data.user || res.data.customer;

      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(userData));
      onLoginSuccess(userData);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed. Please check credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2 className="auth-title">{isRegister ? 'Create Account' : 'Welcome Back'}</h2>
        <p className="auth-subtitle">
          {isRegister ? 'Sign up to shop with store pickup' : 'Sign in to access your cart and orders'}
        </p>

        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          {isRegister && (
            <div className="form-row">
              <div className="form-group">
                <label>First Name</label>
                <input type="text" name="firstName" value={formData.firstName} onChange={handleChange} required placeholder="First name" />
              </div>
              <div className="form-group">
                <label>Last Name</label>
                <input type="text" name="lastName" value={formData.lastName} onChange={handleChange} required placeholder="Last name" />
              </div>
            </div>
          )}

          <div className="form-group">
            <label>Email Address</label>
            <input type="email" name="email" value={formData.email} onChange={handleChange} required placeholder="name@example.com" />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" value={formData.password} onChange={handleChange} required placeholder="••••••••" />
          </div>

          {isRegister && (
            <>
              <div className="form-group">
                <label>Phone Number</label>
                <input type="tel" name="phone" value={formData.phone} onChange={handleChange} placeholder="+91 9876543210" />
              </div>
              <div className="form-group">
                <label>Delivery Address</label>
                <input type="text" name="address" value={formData.address} onChange={handleChange} placeholder="City, State" />
              </div>
            </>
          )}

          <button type="submit" className="btn-primary-full" disabled={loading}>
            {loading ? 'Please wait...' : (isRegister ? 'Register Account' : 'Sign In')}
          </button>
        </form>

        <div className="auth-footer">
          <button type="button" className="btn-link" onClick={() => { setIsRegister(!isRegister); setError(''); }}>
            {isRegister ? 'Already have an account? Sign In' : "Don't have an account? Create One"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Login;
