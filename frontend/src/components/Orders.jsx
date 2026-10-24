import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client';

function Orders({ user }) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchOrders = async () => {
    if (!user) {
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      const res = await api.get('/api/orders');
      setOrders(res.data || []);
    } catch (err) {
      console.error('Error fetching orders:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [user]);

  if (!user) {
    return (
      <div className="orders-empty-box">
        <h2>Please Sign In</h2>
        <p>Sign in to view your omnichannel store pickup orders.</p>
        <Link to="/login" className="btn-primary">Sign In</Link>
      </div>
    );
  }

  if (loading) {
    return <div className="loading-spinner">Loading your orders...</div>;
  }

  if (orders.length === 0) {
    return (
      <div className="orders-empty-box">
        <div className="empty-icon">📦</div>
        <h2>No Orders Found</h2>
        <p>You have not placed any store pickup orders yet.</p>
        <Link to="/" className="btn-primary">Shop Products Now</Link>
      </div>
    );
  }

  return (
    <div className="orders-container">
      <div className="orders-header">
        <h2>My BOPIS Orders ({orders.length})</h2>
        <button onClick={fetchOrders} className="btn-refresh">↻ Refresh</button>
      </div>

      <div className="orders-list">
        {orders.map((order) => {
          const dateStr = order.createdAt
            ? new Date(order.createdAt).toLocaleString('en-IN', {
                dateStyle: 'medium',
                timeStyle: 'short'
              })
            : 'Recent';

          return (
            <div key={order.id} className="order-card">
              <div className="order-card-header">
                <div>
                  <span className="order-number-label">Order Number:</span>
                  <span className="order-number-val">{order.orderNumber || order.id}</span>
                  <span className="order-date">📅 {dateStr}</span>
                </div>
                <div>
                  <span className="badge-status">{order.status || 'CREATED'}</span>
                  <span className="badge-fulfillment">{order.fulfillmentType || 'STORE_PICKUP'}</span>
                </div>
              </div>

              <div className="order-meta-row">
                <span><strong>Pickup Store:</strong> {order.storeId}</span>
                <span>
                  <strong>Total:</strong>{' '}
                  <span className="order-total-price">
                    ₹{Number(order.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </span>
                </span>
              </div>

              {order.items && order.items.length > 0 && (
                <div className="order-items-table-box">
                  <table className="order-mini-table">
                    <thead>
                      <tr>
                        <th>Product ID</th>
                        <th>Qty</th>
                        <th>Unit Price</th>
                        <th>Subtotal</th>
                      </tr>
                    </thead>
                    <tbody>
                      {order.items.map((item, idx) => (
                        <tr key={item.id || idx}>
                          <td>{item.productId}</td>
                          <td>{item.quantity}</td>
                          <td>₹{Number(item.unitPrice).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                          <td>₹{Number(item.subtotal).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default Orders;
