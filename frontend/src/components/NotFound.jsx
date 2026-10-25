import React from 'react';
import { Link } from 'react-router-dom';

function NotFound() {
  return (
    <div style={{ textAlign: 'center', padding: '60px 20px' }}>
      <h2>404 - Page Not Found</h2>
      <p style={{ color: '#64748b', margin: '10px 0 20px' }}>
        The page you are looking for does not exist.
      </p>
      <Link to="/" className="btn-primary">Back to Home</Link>
    </div>
  );
}

export default NotFound;
