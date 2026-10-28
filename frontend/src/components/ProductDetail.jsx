import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../api/client';
import './ProductDetail.css';

function ProductDetail({ user, onCartUpdated }) {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [stores, setStores] = useState([]);
  const [selectedStore, setSelectedStore] = useState('STORE-GGN-01');
  const [stockInfo, setStockInfo] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionAlert, setActionAlert] = useState('');

  // Admin Edit Modal State
  const [showEditModal, setShowEditModal] = useState(false);
  const [editFormData, setEditFormData] = useState({ name: '', price: '', description: '' });
  const [editLoading, setEditLoading] = useState(false);

  // Store Operator Restock State
  const [restockQty, setRestockQty] = useState(25);
  const [restockLoading, setRestockLoading] = useState(false);

  const isAdmin = user?.role === 'ADMIN';
  const isOperatorOrAdmin = user?.role === 'STORE_OPERATOR' || user?.role === 'ADMIN';

  // 1. Fetch product details by ID and list of stores
  useEffect(() => {
    async function fetchProductAndStores() {
      setLoading(true);
      setError('');
      try {
        const [prodRes, storesRes] = await Promise.all([
          api.get(`/api/products/${id}`),
          api.get('/api/stores')
        ]);
        setProduct(prodRes.data);
        setEditFormData({
          name: prodRes.data.name || '',
          price: prodRes.data.price || '',
          description: prodRes.data.description || ''
        });
        setStores(storesRes.data || []);
        if (storesRes.data && storesRes.data.length > 0) {
          setSelectedStore(storesRes.data[0].id);
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Product not found or unavailable.');
      } finally {
        setLoading(false);
      }
    }
    fetchProductAndStores();
  }, [id]);

  // 2. Fetch stock for the selected store
  const fetchStock = async () => {
    if (!selectedStore || !id) return;
    try {
      const res = await api.get(`/api/inventory/store/${selectedStore}/product/${id}`);
      setStockInfo(res.data);
    } catch {
      setStockInfo({ quantityAvailable: 0, quantityReserved: 0 });
    }
  };

  useEffect(() => {
    fetchStock();
  }, [selectedStore, id]);

  const handleAddToCart = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    if (availableStock < quantity) {
      setActionAlert('Requested quantity exceeds available stock at this store.');
      return;
    }

    try {
      await api.post('/api/cart/items', {
        productId: product.id,
        quantity: quantity,
        storeId: selectedStore
      });
      setActionAlert(`Added ${quantity} unit(s) of "${product.name}" to cart!`);
      if (onCartUpdated) onCartUpdated();
      setTimeout(() => setActionAlert(''), 3500);
    } catch (err) {
      setActionAlert(err.response?.data?.message || 'Failed to add item to cart.');
    }
  };

  // Admin Edit Submit
  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditLoading(true);
    try {
      const res = await api.put(`/api/products/${product.id}`, {
        name: editFormData.name.trim(),
        price: parseFloat(editFormData.price),
        description: editFormData.description.trim(),
        categoryId: product.categoryId
      });
      setProduct(res.data);
      setShowEditModal(false);
      setActionAlert(`Product "${res.data.name}" successfully updated!`);
      setTimeout(() => setActionAlert(''), 4000);
    } catch (err) {
      setActionAlert(err.response?.data?.message || 'Failed to update product details.');
    } finally {
      setEditLoading(false);
    }
  };

  // Store Operator Restock Submit
  const handleRestockSubmit = async (qtyToAdd) => {
    const qty = qtyToAdd || restockQty;
    if (qty <= 0) return;
    setRestockLoading(true);
    try {
      const res = await api.post(`/api/inventory/store/${selectedStore}/product/${product.id}/restock`, {
        quantity: qty
      });
      setStockInfo(res.data);
      setActionAlert(`Restocked +${qty} units of "${product.name}" at store ${selectedStore}!`);
      setTimeout(() => setActionAlert(''), 4000);
    } catch (err) {
      setActionAlert(err.response?.data?.message || 'Failed to restock inventory.');
    } finally {
      setRestockLoading(false);
    }
  };

  if (loading) {
    return <div className="detail-loading">Loading product details...</div>;
  }

  if (error || !product) {
    return (
      <div className="detail-error-box">
        <h2>Product Not Found</h2>
        <p>{error || 'The requested product could not be located in our catalog.'}</p>
        <Link to="/" className="btn-primary">Back to Catalog</Link>
      </div>
    );
  }

  const availableStock = stockInfo?.quantityAvailable ?? 0;
  const isOutOfStock = availableStock <= 0;

  return (
    <article className="product-detail-container" aria-label="Product Details Page">
      <nav className="detail-breadcrumbs" aria-label="Breadcrumb navigation">
        <Link to="/" className="breadcrumb-link">Products</Link>
        <span className="breadcrumb-separator">/</span>
        <span className="breadcrumb-current">{product.categoryName || 'General'}</span>
        <span className="breadcrumb-separator">/</span>
        <span className="breadcrumb-current">{product.name}</span>
      </nav>

      {actionAlert && (
        <aside className="detail-action-alert" role="alert" aria-live="polite">
          {actionAlert}
        </aside>
      )}

      <div className="product-detail-card">
        <header className="product-detail-header">
          <div className="header-top-row">
            <div className="badge-row">
              <span className="detail-category-badge">{product.categoryName || 'Product'}</span>
              <span className="detail-sku-badge">SKU: {product.sku}</span>
            </div>
            {isAdmin && (
              <button
                type="button"
                className="btn-admin-edit"
                onClick={() => setShowEditModal(true)}
              >
                ✏️ Edit Product Info (Admin)
              </button>
            )}
          </div>
          <h1 className="detail-title">{product.name}</h1>
          <p className="detail-price">₹{Number(product.price).toFixed(2)}</p>
        </header>

        <section className="detail-description-section" aria-label="Description">
          <h3>Description</h3>
          <p className="detail-description-text">
            {product.description || 'No additional description provided for this product.'}
          </p>
        </section>

        {/* Omnichannel Store Selection & Stock Availability */}
        <section className="detail-store-section" aria-label="Store Availability">
          <div className="store-select-group">
            <label htmlFor="detail-store-select">
              <strong>Check Stock at Physical Store (BOPIS):</strong>
            </label>
            <select
              id="detail-store-select"
              value={selectedStore}
              onChange={(e) => {
                setSelectedStore(e.target.value);
                setQuantity(1);
              }}
              className="store-select-dropdown"
            >
              {stores.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} ({s.city})
                </option>
              ))}
            </select>
          </div>

          <div className="stock-status-box">
            <span className="stock-label">Store Stock Status:</span>
            {isOutOfStock ? (
              <span className="stock-val out-of-stock">Out of Stock at this Store</span>
            ) : (
              <span className="stock-val in-stock">
                {availableStock} units available for pickup
              </span>
            )}
          </div>

          {/* Store Operator Restock Controls */}
          {isOperatorOrAdmin && (
            <div className="operator-restock-panel">
              <div className="operator-restock-title">
                <strong>Store Operator Inventory Restock:</strong>
              </div>
              <div className="operator-restock-actions">
                <button
                  type="button"
                  className="btn-quick-restock"
                  disabled={restockLoading}
                  onClick={() => handleRestockSubmit(15)}
                >
                  +15 Units
                </button>
                <button
                  type="button"
                  className="btn-quick-restock"
                  disabled={restockLoading}
                  onClick={() => handleRestockSubmit(30)}
                >
                  +30 Units
                </button>
                <div className="custom-restock-input-row">
                  <input
                    type="number"
                    min="1"
                    max="500"
                    value={restockQty}
                    onChange={(e) => setRestockQty(parseInt(e.target.value, 10) || 1)}
                    className="restock-input"
                  />
                  <button
                    type="button"
                    className="btn-submit-restock"
                    disabled={restockLoading}
                    onClick={() => handleRestockSubmit(restockQty)}
                  >
                    {restockLoading ? 'Restocking...' : 'Restock'}
                  </button>
                </div>
              </div>
            </div>
          )}
        </section>

        {/* Purchase & Action Controls */}
        <footer className="detail-actions-footer">
          <div className="qty-control-row">
            <label htmlFor="quantity-input">Quantity:</label>
            <div className="qty-stepper">
              <button
                type="button"
                className="btn-step"
                onClick={() => setQuantity((prev) => Math.max(1, prev - 1))}
                disabled={quantity <= 1 || isOutOfStock}
                aria-label="Decrease quantity"
              >
                -
              </button>
              <input
                id="quantity-input"
                type="number"
                min="1"
                max={availableStock || 1}
                value={quantity}
                onChange={(e) => {
                  const val = parseInt(e.target.value, 10);
                  if (!isNaN(val)) {
                    setQuantity(Math.max(1, Math.min(val, availableStock)));
                  }
                }}
                disabled={isOutOfStock}
                className="qty-number-input"
              />
              <button
                type="button"
                className="btn-step"
                onClick={() => setQuantity((prev) => Math.min(availableStock, prev + 1))}
                disabled={quantity >= availableStock || isOutOfStock}
                aria-label="Increase quantity"
              >
                +
              </button>
            </div>
          </div>

          <div className="detail-btn-row">
            <button
              type="button"
              className="btn-add-detail"
              onClick={handleAddToCart}
              disabled={isOutOfStock}
              aria-label={`Add ${product.name} to Cart`}
            >
              {isOutOfStock ? 'Currently Out of Stock' : `Add ${quantity} to Cart • ₹${(product.price * quantity).toFixed(2)}`}
            </button>
            <button
              type="button"
              className="btn-secondary-detail"
              onClick={() => navigate('/')}
            >
              Back to Catalog
            </button>
          </div>
        </footer>
      </div>

      {/* Admin Edit Product Modal */}
      {showEditModal && (
        <div className="modal-backdrop" onClick={() => setShowEditModal(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Edit Product Details (Admin)</h3>
              <button type="button" className="btn-modal-close" onClick={() => setShowEditModal(false)}>✕</button>
            </div>

            <form onSubmit={handleEditSubmit} className="admin-product-form">
              <label>Product Name *</label>
              <input
                type="text"
                required
                value={editFormData.name}
                onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })}
              />

              <label>Price (₹) *</label>
              <input
                type="number"
                step="0.01"
                min="0"
                required
                value={editFormData.price}
                onChange={(e) => setEditFormData({ ...editFormData, price: e.target.value })}
              />

              <label>Description</label>
              <textarea
                rows="3"
                value={editFormData.description}
                onChange={(e) => setEditFormData({ ...editFormData, description: e.target.value })}
              />

              <div className="modal-footer">
                <button type="button" className="btn-modal-cancel" onClick={() => setShowEditModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-modal-submit" disabled={editLoading}>
                  {editLoading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </article>
  );
}

export default ProductDetail;
