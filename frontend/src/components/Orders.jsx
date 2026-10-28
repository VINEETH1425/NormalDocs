import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client';
import './Orders.css';

const STORES = [
  { id: 'ALL', name: 'All Stores' },
  { id: 'STORE-GGN-01', name: 'Gurugram Flagship (STORE-GGN-01)' },
  { id: 'STORE-DEL-01', name: 'Delhi Connaught Place (STORE-DEL-01)' },
  { id: 'STORE-BLR-01', name: 'Bangalore Indiranagar (STORE-BLR-01)' }
];

function Orders({ user }) {
  const isOperatorOrAdmin = user?.role === 'ADMIN' || user?.role === 'STORE_OPERATOR';

  // Active View Tab: 'MY_ORDERS' or 'OPERATIONS'
  const [activeTab, setActiveTab] = useState(isOperatorOrAdmin ? 'OPERATIONS' : 'MY_ORDERS');
  const [selectedStore, setSelectedStore] = useState('ALL');

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionAlert, setActionAlert] = useState('');
  const [updatingId, setUpdatingId] = useState(null);

  const fetchOrders = useCallback(async () => {
    if (!user) {
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      let res;
      if (activeTab === 'OPERATIONS' && isOperatorOrAdmin) {
        if (selectedStore === 'ALL') {
          res = await api.get('/api/orders/all');
        } else {
          res = await api.get(`/api/orders/store/${selectedStore}`);
        }
      } else {
        // Customer personal orders
        res = await api.get('/api/orders');
      }
      setOrders(res.data || []);
    } catch (err) {
      console.error('Error fetching orders:', err);
      setActionAlert('Failed to load orders. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [user, activeTab, selectedStore, isOperatorOrAdmin]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleUpdateStatus = async (orderId, newStatus) => {
    setUpdatingId(orderId);
    setActionAlert('');
    try {
      const res = await api.patch(`/api/orders/${orderId}/status`, { status: newStatus });
      // Update local state smoothly
      setOrders((prev) =>
        prev.map((o) => (o.id === orderId ? { ...o, status: res.data.status } : o))
      );
      setActionAlert(`Order #${orderId} status successfully advanced to "${newStatus}"!`);
      setTimeout(() => setActionAlert(''), 4000);
    } catch (err) {
      setActionAlert(err.response?.data?.message || 'Failed to update order status.');
    } finally {
      setUpdatingId(null);
    }
  };

  if (!user) {
    return (
      <div className="orders-empty-box">
        <h2>Please Sign In</h2>
        <p>Sign in to view your store pickup orders and track fulfillment status.</p>
        <Link to="/login" className="btn-primary">Sign In</Link>
      </div>
    );
  }

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return 'badge-status badge-confirmed';
      case 'PROCESSING':
        return 'badge-status badge-processing';
      case 'READY_FOR_PICKUP':
        return 'badge-status badge-ready';
      case 'COMPLETED':
        return 'badge-status badge-completed';
      case 'CANCELLED':
        return 'badge-status badge-cancelled';
      default:
        return 'badge-status';
    }
  };

  const getNextStatusAction = (currentStatus) => {
    switch (currentStatus) {
      case 'CONFIRMED':
        return { label: 'Start Packing (PROCESSING)', next: 'PROCESSING' };
      case 'PROCESSING':
        return { label: 'Stage in Locker (READY FOR PICKUP)', next: 'READY_FOR_PICKUP' };
      case 'READY_FOR_PICKUP':
        return { label: 'Handover to Customer (COMPLETED)', next: 'COMPLETED' };
      default:
        return null;
    }
  };

  return (
    <div className="orders-container">
      {/* Top Banner & Header */}
      <div className="orders-header">
        <div>
          <h2>
            {activeTab === 'OPERATIONS'
              ? 'Store Fulfillment Console (BOPIS Operations)'
              : 'My Store Pickup Orders'}
          </h2>
          <p className="orders-subtitle">
            {activeTab === 'OPERATIONS'
              ? 'Real-time order processing, staging, and customer handoff pipeline.'
              : 'Track the status of your Buy-Online-Pick-up-in-Store (BOPIS) orders.'}
          </p>
        </div>
        <button onClick={fetchOrders} className="btn-refresh" disabled={loading}>
          {loading ? 'Refreshing...' : 'Refresh Orders'}
        </button>
      </div>

      {/* Role Navigation Tabs if Operator / Admin */}
      {isOperatorOrAdmin && (
        <div className="orders-tab-bar">
          <button
            type="button"
            className={`tab-btn ${activeTab === 'OPERATIONS' ? 'active' : ''}`}
            onClick={() => setActiveTab('OPERATIONS')}
          >
            Fulfillment Console ({user.role})
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'MY_ORDERS' ? 'active' : ''}`}
            onClick={() => setActiveTab('MY_ORDERS')}
          >
            My Personal Orders
          </button>
        </div>
      )}

      {/* Store Filter for Store Operator / Admin Console */}
      {activeTab === 'OPERATIONS' && isOperatorOrAdmin && (
        <div className="operations-filter-bar">
          <label htmlFor="store-filter-select">
            <strong>Filter by Fulfillment Store:</strong>
          </label>
          <select
            id="store-filter-select"
            value={selectedStore}
            onChange={(e) => setSelectedStore(e.target.value)}
            className="store-filter-dropdown"
          >
            {STORES.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>
      )}

      {/* Action / Success Alert Banner */}
      {actionAlert && (
        <aside className="orders-action-alert" role="alert" aria-live="polite">
          {actionAlert}
        </aside>
      )}

      {loading ? (
        <div className="loading-spinner">Loading orders...</div>
      ) : orders.length === 0 ? (
        <div className="orders-empty-box">
          <h2>No Orders Found</h2>
          <p>
            {activeTab === 'OPERATIONS'
              ? 'There are no customer orders matching the selected store filter.'
              : 'You have not placed any store pickup orders yet.'}
          </p>
          {activeTab === 'MY_ORDERS' && (
            <Link to="/" className="btn-primary">Browse Catalog</Link>
          )}
        </div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => {
            const dateStr = order.createdAt
              ? new Date(order.createdAt).toLocaleString()
              : 'Recent';
            const action = getNextStatusAction(order.status);
            const canCancel = order.status !== 'COMPLETED' && order.status !== 'CANCELLED';

            return (
              <article key={order.id} className="order-card" aria-label={`Order ${order.orderNumber || order.id}`}>
                {/* Header Row */}
                <div className="order-card-header">
                  <div className="order-identifiers">
                    <span className="order-number-val">
                      {order.orderNumber || order.id}
                    </span>
                    <span className="order-date">{dateStr}</span>
                  </div>
                  <div className="order-badges">
                    <span className={getStatusBadgeClass(order.status)}>
                      {order.status}
                    </span>
                    <span className="badge-fulfillment">
                      {order.fulfillmentType || 'STORE_PICKUP'}
                    </span>
                  </div>
                </div>

                {/* Customer Journey Status Stepper */}
                <div className="order-stepper" aria-label="Order Progress">
                  <div className={`step-item ${['CONFIRMED', 'PROCESSING', 'READY_FOR_PICKUP', 'COMPLETED'].includes(order.status) ? 'step-done' : ''}`}>
                    <div className="step-circle">1</div>
                    <span className="step-label">Confirmed</span>
                  </div>
                  <div className="step-line"></div>
                  <div className={`step-item ${['PROCESSING', 'READY_FOR_PICKUP', 'COMPLETED'].includes(order.status) ? 'step-done' : ''}`}>
                    <div className="step-circle">2</div>
                    <span className="step-label">Packing</span>
                  </div>
                  <div className="step-line"></div>
                  <div className={`step-item ${['READY_FOR_PICKUP', 'COMPLETED'].includes(order.status) ? 'step-done' : ''}`}>
                    <div className="step-circle">3</div>
                    <span className="step-label">Ready at Store</span>
                  </div>
                  <div className="step-line"></div>
                  <div className={`step-item ${order.status === 'COMPLETED' ? 'step-done' : ''}`}>
                    <div className="step-circle">4</div>
                    <span className="step-label">Picked Up</span>
                  </div>
                </div>

                {/* Metadata Row */}
                <div className="order-meta-row">
                  <div>
                    <p className="order-meta-item">
                      <strong>Pickup Store:</strong> {order.storeId}
                    </p>
                    {activeTab === 'OPERATIONS' && order.customerId && (
                      <p className="order-meta-item">
                        <strong>Customer ID:</strong> {order.customerId}
                      </p>
                    )}
                  </div>
                  <div className="order-total-price">
                    ₹{Number(order.totalAmount).toFixed(2)}
                  </div>
                </div>

                {/* Items Breakdown Table */}
                {order.items && order.items.length > 0 && (
                  <div className="order-items-table-box">
                    <table className="order-mini-table">
                      <thead>
                        <tr>
                          <th>Item</th>
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
                            <td>₹{Number(item.unitPrice).toFixed(2)}</td>
                            <td>₹{Number(item.subtotal).toFixed(2)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}

                {/* Operator Fulfillment Action Controls */}
                {activeTab === 'OPERATIONS' && isOperatorOrAdmin && (
                  <footer className="operator-actions-footer">
                    <div className="operator-action-text">
                      <strong>Operator Status Controls:</strong>
                    </div>
                    <div className="operator-btn-group">
                      {action && (
                        <button
                          type="button"
                          className="btn-advance-status"
                          disabled={updatingId === order.id}
                          onClick={() => handleUpdateStatus(order.id, action.next)}
                        >
                          {updatingId === order.id ? 'Updating...' : action.label}
                        </button>
                      )}
                      {canCancel && (
                        <button
                          type="button"
                          className="btn-cancel-order"
                          disabled={updatingId === order.id}
                          onClick={() => {
                            if (window.confirm(`Are you sure you want to cancel order ${order.orderNumber || order.id}?`)) {
                              handleUpdateStatus(order.id, 'CANCELLED');
                            }
                          }}
                        >
                          Cancel Order
                        </button>
                      )}
                      {order.status === 'COMPLETED' && (
                        <span className="text-order-finalized">Order Completed and Collected.</span>
                      )}
                      {order.status === 'CANCELLED' && (
                        <span className="text-order-cancelled">Order Cancelled.</span>
                      )}
                    </div>
                  </footer>
                )}
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default Orders;
