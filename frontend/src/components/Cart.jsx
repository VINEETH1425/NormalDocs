import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/client';
import './Cart.css';

function Cart({ user, onCartUpdated }) {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [stores, setStores] = useState([]);
  const [selectedStore, setSelectedStore] = useState('STORE-GGN-01');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadCartData = async () => {
    if (!user) {
      setLoading(false);
      return;
    }
    try {
      setLoading(true);
      const [cartRes, storesRes] = await Promise.all([
        api.get('/api/cart'),
        api.get('/api/stores')
      ]);
      setCart(cartRes.data);
      setStores(storesRes.data || []);
      if (cartRes.data?.storeId) {
        setSelectedStore(cartRes.data.storeId);
      } else if (storesRes.data?.length > 0) {
        setSelectedStore(storesRes.data[0].id);
      }
    } catch (err) {
      console.error('Error fetching cart:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCartData();
  }, [user]);

  const handleRemoveItem = async (itemId) => {
    try {
      await api.delete(`/api/cart/items/${itemId}`);
      await loadCartData();
      if (onCartUpdated) onCartUpdated();
    } catch {
      alert('Failed to remove item.');
    }
  };

  const handleClearCart = async () => {
    if (!window.confirm('Are you sure you want to clear your cart?')) return;
    try {
      await api.delete('/api/cart');
      await loadCartData();
      if (onCartUpdated) onCartUpdated();
    } catch {
      alert('Failed to clear cart.');
    }
  };

  const handleCheckout = async () => {
    setError('');
    try {
      const res = await api.post('/api/orders/checkout', {
        storeId: selectedStore,
        fulfillmentType: 'STORE_PICKUP'
      });
      alert('Order placed successfully! Order ID: ' + (res.data?.orderNumber || res.data?.id));
      if (onCartUpdated) onCartUpdated();
      navigate('/orders');
    } catch (err) {
      setError(err.response?.data?.message || 'Checkout failed.');
    }
  };

  if (!user) {
    return (
      <div className="cart-page-container">
        <h2>Please Sign In</h2>
        <p>You must be signed in to view your cart.</p>
        <Link to="/login" className="btn-primary">Sign In</Link>
      </div>
    );
  }

  if (loading) {
    return <div className="loading-spinner">Loading cart...</div>;
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="cart-page-container">
        <h2>Your Cart is Empty</h2>
        <p>No products in your cart.</p>
        <Link to="/" className="btn-primary">Browse Products</Link>
      </div>
    );
  }

  return (
    <div className="cart-page-container">
      <div className="cart-header">
        <h2>Shopping Cart</h2>
        <button onClick={handleClearCart} className="btn-clear-cart">Clear Cart</button>
      </div>

      {error && <div className="alert-error">{error}</div>}

      <div className="cart-layout">
        <table className="cart-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Price</th>
              <th>Qty</th>
              <th>Subtotal</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {cart.items.map((item) => (
              <tr key={item.id || item.productId}>
                <td>{item.productName || item.productId}</td>
                <td>₹{Number(item.unitPrice).toFixed(2)}</td>
                <td>{item.quantity}</td>
                <td>₹{Number(item.subtotal).toFixed(2)}</td>
                <td>
                  <button onClick={() => handleRemoveItem(item.id)} className="btn-remove-item">Remove</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="checkout-summary-card">
          <h3>Order Summary</h3>
          <p>Total Items: {cart.items.reduce((sum, i) => sum + i.quantity, 0)}</p>
          <p><strong>Total Payable: ₹{Number(cart.totalAmount).toFixed(2)}</strong></p>

          <label>Pickup Store:</label>
          <select value={selectedStore} onChange={(e) => setSelectedStore(e.target.value)} className="store-select-input">
            {stores.map((s) => (
              <option key={s.id} value={s.id}>{s.name} ({s.city})</option>
            ))}
          </select>

          <button onClick={handleCheckout} className="btn-checkout-primary">
            Place Order
          </button>
        </div>
      </div>
    </div>
  );
}

export default Cart;
