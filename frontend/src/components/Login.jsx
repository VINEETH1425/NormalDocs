import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import './Login.css';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PASSWORD_REGEX = /^.{6,}$/;

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
  const [showPassword, setShowPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    if (fieldErrors[name]) {
      setFieldErrors({ ...fieldErrors, [name]: '' });
    }
    if (error) setError('');
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.email || !EMAIL_REGEX.test(formData.email.trim())) {
      errors.email = 'Please provide a valid email address (e.g. user@store.com)';
    }
    if (!formData.password || !PASSWORD_REGEX.test(formData.password)) {
      errors.password = 'Password must be at least 6 characters long';
    }
    if (isRegister) {
      if (!formData.firstName || formData.firstName.trim().length < 2) {
        errors.firstName = 'First name must be at least 2 characters';
      }
      if (!formData.lastName || formData.lastName.trim().length < 2) {
        errors.lastName = 'Last name must be at least 2 characters';
      }
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const endpoint = isRegister ? '/api/auth/register' : '/api/auth/login';
      const payload = isRegister
        ? {
            firstName: formData.firstName.trim(),
            lastName: formData.lastName.trim(),
            email: formData.email.trim().toLowerCase(),
            password: formData.password,
            phone: formData.phone.trim(),
            address: formData.address.trim()
          }
        : {
            email: formData.email.trim().toLowerCase(),
            password: formData.password
          };

      const res = await api.post(endpoint, payload);
      const userData = res.data.user || res.data.customer;

      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(userData));
      onLoginSuccess(userData);
      navigate('/');
    } catch (err) {
      // Bottom error banner receives API or network message
      setError(err.response?.data?.message || 'Authentication failed. Please verify your credentials and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2 className="auth-title">{isRegister ? 'Create Account' : 'Welcome Back'}</h2>
        <p className="auth-subtitle">
          {isRegister ? 'Sign up to shop with store pickup (BOPIS)' : 'Sign in to access your cart and manage orders'}
        </p>

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          {isRegister && (
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="auth-firstname">First Name *</label>
                <input
                  id="auth-firstname"
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  placeholder="e.g. Rahul"
                  className={fieldErrors.firstName ? 'input-error' : ''}
                />
                {fieldErrors.firstName && <span className="field-error-text">{fieldErrors.firstName}</span>}
              </div>
              <div className="form-group">
                <label htmlFor="auth-lastname">Last Name *</label>
                <input
                  id="auth-lastname"
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                  placeholder="e.g. Sharma"
                  className={fieldErrors.lastName ? 'input-error' : ''}
                />
                {fieldErrors.lastName && <span className="field-error-text">{fieldErrors.lastName}</span>}
              </div>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="auth-email">Email Address *</label>
            <input
              id="auth-email"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              placeholder="e.g. customer@store.com"
              className={fieldErrors.email ? 'input-error' : ''}
            />
            {fieldErrors.email && <span className="field-error-text">{fieldErrors.email}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="auth-password">Password *</label>
            <div className="password-input-wrapper">
              <input
                id="auth-password"
                type={showPassword ? 'text' : 'password'}
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
                placeholder="At least 6 characters"
                className={fieldErrors.password ? 'input-error' : ''}
              />
              <button
                type="button"
                className="btn-toggle-password"
                onClick={() => setShowPassword(!showPassword)}
                title={showPassword ? 'Hide password' : 'Show password'}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                    <line x1="1" y1="1" x2="23" y2="23" />
                  </svg>
                ) : (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                )}
              </button>
            </div>
            {fieldErrors.password && <span className="field-error-text">{fieldErrors.password}</span>}
          </div>

          {isRegister && (
            <>
              <div className="form-group">
                <label htmlFor="auth-phone">Phone Number</label>
                <input
                  id="auth-phone"
                  type="tel"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="+91 9876543210"
                />
              </div>
              <div className="form-group">
                <label htmlFor="auth-address">Delivery Address</label>
                <input
                  id="auth-address"
                  type="text"
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  placeholder="Sector 29, Gurugram, Haryana"
                />
              </div>
            </>
          )}

          <button type="submit" className="btn-primary-full" disabled={loading}>
            {loading ? 'Please wait...' : (isRegister ? 'Register Account' : 'Sign In')}
          </button>

          {/* Overall Error placed at the bottom of the form as requested */}
          {error && (
            <div className="alert-error alert-error-bottom" role="alert">
              {error}
            </div>
          )}
        </form>

        <div className="auth-footer">
          <button
            type="button"
            className="btn-link"
            onClick={() => {
              setIsRegister(!isRegister);
              setError('');
              setFieldErrors({});
            }}
          >
            {isRegister ? 'Already have an account? Sign In' : "Don't have an account? Create One"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Login;
