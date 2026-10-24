import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/client';

function Cart({ user, onCartUpdated }) {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [stores, setStores] = useState([]);
  const [selectedStore, setSelectedStore] = useState('STORE-GGN-01');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  // 1. Fetch current cart and stores list
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
      } else if (storesRes.data && storesRes.data.length > 0) {
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

  // 2. Remove single item
  const handleRemoveItem = async (itemId) => {
    try {
      await api.delete(`/api/cart/items/${itemId}`);
      await loadCartData();
      if (onCartUpdated) onCartUpdated();
    } catch (err) {
      alert('Failed to remove item: ' + (err.response?.data?.message || err.message));
    }
  };

  // 3. Clear entire cart
  const handleClearCart = async () => {
    if (!window.confirm('Are you sure you want to clear your cart?')) return;
    try {
      await api.delete('/api/cart');
      await loadCartData();
      if (onCartUpdated) onCartUpdated();
    } catch (err) {
      alert('Failed to clear cart: ' + (err.response?.data?.message || err.message));
    }
  };

  // 4. Place BOPIS Order (Direct Checkout - No Payment Screen)
  const handleCheckout = async () => {
    setError('');
    setSubmitting(true);

    try {
      const res = await api.post('/api/orders/checkout', {
        storeId: selectedStore,
        fulfillmentType: 'STORE_PICKUP'
      });

      const orderNumber = res.data?.orderNumber || res.data?.id;
      alert(`🎉 Order Confirmed! Your pickup order number is: ${orderNumber}\n\nInventory has been reserved at your selected store. Payment is not required for this demo.`);
      
      if (onCartUpdated) onCartUpdated();
      navigate('/orders');
    } catch (err) {
      const msg = err.response?.data?.message || 'Checkout failed. Please verify store stock availability.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (!user) {
    return (
      <div className="cart-empty-container">
        <h2>Please Sign In</h2>
        <p>You need to be logged in to view and manage your shopping cart.</p>
        <Link to="/login" className="btn-primary">Sign In to Continue</Link>
      </div>
    );
  }

  if (loading) {
    return <div className="loading-spinner">Loading your cart...</div>;
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="cart-empty-container">
        <div className="empty-cart-icon">🛒</div>
        <h2>Your Cart is Empty</h2>
        <p>Explore our omnichannel retail catalog and add items for store pickup.</p>
        <Link to="/" className="btn-primary">Browse Products Catalog</Link>
      </div>
    );
  }

  return (
    <div className="cart-page-container">
      <div className="cart-header">
        <h2>Your Shopping Cart</h2>
        <button onClick={handleClearCart} className="btn-clear-cart">Clear Cart</button>
      </div>

      {error && <div className="alert-error">{error}</div>}

      <div className="cart-layout">
        {/* Line Items Table */}
        <div className="cart-table-box">
          <table className="cart-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Unit Price</th>
                <th>Quantity</th>
                <th>Subtotal</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {cart.items.map((item) => (
                <tr key={item.id || item.productId}>
                  <td>
                    <strong>{item.productName || item.productId}</strong>
                  </td>
                  <td>₹{Number(item.unitPrice).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                  <td>{item.quantity}</td>
                  <td>
                    <strong>₹{Number(item.subtotal).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</strong>
                  </td>
                  <td>
                    <button
                      onClick={() => handleRemoveItem(item.id)}
                      className="btn-remove-item"
                    >
                      ✕ Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Order Summary & BOPIS Store Selection */}
        <div className="checkout-summary-card">
          <h3>Order Summary</h3>
          <div className="summary-row">
            <span>Total Items:</span>
            <span>{cart.items.reduce((sum, i) => sum + i.quantity, 0)}</span>
          </div>
          <div className="summary-row total-row">
            <span>Total Payable:</span>
            <span>₹{Number(cart.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
          </div>

          <div className="pickup-store-select-group">
            <label htmlFor="checkout-store">
              <strong>🏬 Physical Store for Pickup:</strong>
            </label>
            <select
              id="checkout-store"
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
            <small className="bopis-note">
              Fulfillment: <strong>BOPIS (Buy Online, Pick Up in Store)</strong>.
            </small>
          </div>

          <button
            onClick={handleCheckout}
            disabled={submitting}
            className="btn-checkout-primary"
          >
            {submitting ? 'Placing Order...' : 'Confirm Store Pickup Order 🛍️'}
          </button>
          
          <p className="no-payment-disclaimer">
            ✓ Stock will be reserved atomically at the selected store. No payment details required.
          </p>
        </div>
      </div>
    </div>
  );
}

export default Cart;
