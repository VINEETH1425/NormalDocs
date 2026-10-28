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
  const [actionAlert, setActionAlert] = useState('');
  const [checkingOut, setCheckingOut] = useState(false);

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

  const handleRemoveItem = async (itemId, productName) => {
    try {
      await api.delete(`/api/cart/items/${itemId}`);
      setActionAlert(`Removed "${productName || 'Item'}" from cart.`);
      await loadCartData();
      if (onCartUpdated) onCartUpdated();
      setTimeout(() => setActionAlert(''), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove item.');
    }
  };

  const handleClearCart = async () => {
    if (!window.confirm('Are you sure you want to clear your cart?')) return;
    try {
      await api.delete('/api/cart');
      setActionAlert('Cart successfully cleared.');
      await loadCartData();
      if (onCartUpdated) onCartUpdated();
      setTimeout(() => setActionAlert(''), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to clear cart.');
    }
  };

  const handleCheckout = async () => {
    setError('');
    setCheckingOut(true);
    try {
      const res = await api.post('/api/orders/checkout', {
        storeId: selectedStore,
        fulfillmentType: 'STORE_PICKUP'
      });
      const orderNum = res.data?.orderNumber || res.data?.id;
      setActionAlert(`Order placed successfully! Order #${orderNum}`);
      if (onCartUpdated) onCartUpdated();
      setTimeout(() => {
        navigate('/orders');
      }, 1200);
    } catch (err) {
      setError(err.response?.data?.message || 'Checkout failed. Please verify item stock.');
      setCheckingOut(false);
    }
  };

  if (!user) {
    return (
      <div className="cart-page-container">
        <div className="cart-empty-box">
          <h2>Please Sign In</h2>
          <p>You must be signed in to view your cart and proceed with store pickup checkout.</p>
          <Link to="/login" className="btn-primary">Sign In</Link>
        </div>
      </div>
    );
  }

  if (loading) {
    return <div className="loading-spinner">Loading cart...</div>;
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="cart-page-container">
        <div className="cart-empty-box">
          <h2>Your Cart is Empty</h2>
          <p>No products in your cart. Explore our catalog of fresh grocery and daily essentials.</p>
          <Link to="/" className="btn-primary">Browse Products</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="cart-page-container">
      <div className="cart-header">
        <div>
          <h2>Shopping Cart ({cart.items.length} items)</h2>
          <p className="cart-subtitle">Review your selected items and choose your pickup store.</p>
        </div>
        <button onClick={handleClearCart} className="btn-clear-cart">Clear Cart</button>
      </div>

      {actionAlert && (
        <aside className="cart-action-alert" role="alert" aria-live="polite">
          {actionAlert}
        </aside>
      )}

      <div className="cart-layout">
        <div className="cart-items-wrapper">
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
                  <td>
                    <Link to={`/products/${item.productId}`} className="cart-item-link">
                      <strong>{item.productName || item.productId}</strong>
                    </Link>
                  </td>
                  <td>₹{Number(item.unitPrice).toFixed(2)}</td>
                  <td>{item.quantity}</td>
                  <td>₹{Number(item.subtotal).toFixed(2)}</td>
                  <td>
                    <button
                      onClick={() => handleRemoveItem(item.id, item.productName)}
                      className="btn-remove-item"
                      aria-label={`Remove ${item.productName || 'item'}`}
                    >
                      Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="checkout-summary-card">
          <h3>Order Summary</h3>
          <div className="summary-row">
            <span>Total Units:</span>
            <span>{cart.items.reduce((sum, i) => sum + i.quantity, 0)}</span>
          </div>
          <div className="summary-row total-row">
            <span>Total Payable:</span>
            <span className="summary-total-val">₹{Number(cart.totalAmount).toFixed(2)}</span>
          </div>

          <div className="pickup-select-group">
            <label htmlFor="cart-store-select">
              <strong>Pickup Store Location:</strong>
            </label>
            <select
              id="cart-store-select"
              value={selectedStore}
              onChange={(e) => setSelectedStore(e.target.value)}
              className="store-select-input"
            >
              {stores.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} ({s.city})
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={handleCheckout}
            className="btn-checkout-primary"
            disabled={checkingOut}
          >
            {checkingOut ? 'Placing Order...' : 'Confirm Store Pickup Order'}
          </button>

          {/* Bottom Error Notification */}
          {error && (
            <div className="alert-error alert-error-bottom" role="alert">
              {error}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Cart;
