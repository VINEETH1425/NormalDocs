import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

function Navbar({ user, onLogout, cartCount }) {
  const navigate = useNavigate();

  const handleLogoutClick = () => {
    onLogout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="nav-container">
        <Link to="/" className="nav-brand">
          StoreConnect
        </Link>

        <div className="nav-links">
          <Link to="/" className="nav-link">Products</Link>
          <Link to="/cart" className="nav-link">
            Cart {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
          </Link>
          {user && <Link to="/orders" className="nav-link">My Orders</Link>}
        </div>

        <div className="nav-user">
          {user ? (
            <div className="user-info">
              <span className="user-greeting">
                {user.firstName || user.email}
                {user.role && user.role !== 'CUSTOMER' && (
                  <span className={`role-badge role-${user.role.toLowerCase()}`}>{user.role}</span>
                )}
              </span>
              <button className="btn-logout" onClick={handleLogoutClick}>Logout</button>
            </div>
          ) : (
            <Link to="/login" className="btn-login-link">Sign In</Link>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
