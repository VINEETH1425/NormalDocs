import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';

function Products({ user, onCartUpdated }) {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [stores, setStores] = useState([]);
  const [selectedStore, setSelectedStore] = useState('STORE-GGN-01');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [stockMap, setStockMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [actionMessage, setActionMessage] = useState('');

  // Admin Product Creation State
  const [showAddModal, setShowAddModal] = useState(false);
  const [addFormData, setAddFormData] = useState({
    sku: '',
    name: '',
    description: '',
    categoryId: '',
    price: ''
  });
  const [addLoading, setAddLoading] = useState(false);
  const [addError, setAddError] = useState('');

  const handleAddProductSubmit = async (e) => {
    e.preventDefault();
    setAddError('');
    setAddLoading(true);

    try {
      const res = await api.post('/api/products', {
        sku: addFormData.sku.trim().toUpperCase(),
        name: addFormData.name.trim(),
        description: addFormData.description,
        categoryId: addFormData.categoryId || (categories[0]?.id || null),
        price: parseFloat(addFormData.price),
        isActive: true
      });

      setProducts([res.data, ...products]);
      setShowAddModal(false);
      setAddFormData({ sku: '', name: '', description: '', categoryId: '', price: '' });
      setActionMessage(`✓ Product "${res.data.name}" (${res.data.sku}) successfully created!`);
      setTimeout(() => setActionMessage(''), 4000);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create product. Ensure SKU is unique and you have ADMIN privileges.';
      setAddError(msg);
    } finally {
      setAddLoading(false);
    }
  };

  // 1. Load initial stores, categories, and products
  useEffect(() => {
    async function initData() {
      setLoading(true);
      try {
        const [storesRes, catsRes, prodsRes] = await Promise.all([
          api.get('/api/stores'),
          api.get('/api/categories'),
          api.get('/api/products')
        ]);
        setStores(storesRes.data || []);
        setCategories(catsRes.data || []);
        setProducts(prodsRes.data || []);
        if (storesRes.data && storesRes.data.length > 0) {
          setSelectedStore(storesRes.data[0].id);
        }
      } catch (err) {
        console.error('Error loading catalog data:', err);
      } finally {
        setLoading(false);
      }
    }
    initData();
  }, []);

  // 2. Fetch stock for all visible products whenever selectedStore or products change
  useEffect(() => {
    if (!selectedStore || products.length === 0) return;

    async function loadStock() {
      const newStockMap = {};
      await Promise.all(
        products.map(async (prod) => {
          try {
            const res = await api.get(`/api/inventory/store/${selectedStore}/product/${prod.id}`);
            newStockMap[prod.id] = res.data?.quantityAvailable ?? 0;
          } catch {
            newStockMap[prod.id] = 0;
          }
        })
      );
      setStockMap(newStockMap);
    }

    loadStock();
  }, [selectedStore, products]);

  // 3. Handle Add to Cart
  const handleAddToCart = async (product) => {
    if (!user) {
      navigate('/login');
      return;
    }

    try {
      await api.post('/api/cart/items', {
        productId: product.id,
        quantity: 1,
        storeId: selectedStore
      });

      setActionMessage(`✓ Added "${product.name}" to cart!`);
      if (onCartUpdated) onCartUpdated();
      setTimeout(() => setActionMessage(''), 3000);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to add item to cart.';
      alert(msg);
    }
  };

  const filteredProducts = selectedCategory
    ? products.filter((p) => p.categoryId === selectedCategory)
    : products;

  if (loading) {
    return <div className="loading-spinner">Loading StoreConnect Catalog...</div>;
  }

  return (
    <div className="catalog-container">
      {/* Top Banner & Physical Store Selector */}
      <div className="store-banner">
        <div className="store-selector-box">
          <label htmlFor="store-select">
            <strong>📍 Select Store Pickup Location:</strong>
          </label>
          <select
            id="store-select"
            value={selectedStore}
            onChange={(e) => setSelectedStore(e.target.value)}
            className="store-dropdown"
          >
            {stores.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name} ({s.city}) — {s.address}
              </option>
            ))}
          </select>
        </div>
        <p className="store-hint">
          Real-time stock availability will be displayed for this store. You can collect your order directly via BOPIS (Buy Online, Pick Up in Store).
        </p>
      </div>

      {/* Admin Action Bar */}
      {user?.role === 'ADMIN' && (
        <div className="admin-actions-bar">
          <div className="admin-info-badge">
            <span className="shield-icon">🛡️</span>
            <div>
              <strong>Store Administrator Mode</strong>
              <p>You have catalog privileges to add new products directly to the inventory database.</p>
            </div>
          </div>
          <button className="btn-admin-add" onClick={() => setShowAddModal(true)}>
            + Add New Product
          </button>
        </div>
      )}

      {actionMessage && <div className="toast-success">{actionMessage}</div>}

      {/* Category Pills */}
      <div className="category-bar">
        <button
          className={`category-pill ${selectedCategory === '' ? 'active' : ''}`}
          onClick={() => setSelectedCategory('')}
        >
          All Products ({products.length})
        </button>
        {categories.map((c) => (
          <button
            key={c.id}
            className={`category-pill ${selectedCategory === c.id ? 'active' : ''}`}
            onClick={() => setSelectedCategory(c.id)}
          >
            {c.name}
          </button>
        ))}
      </div>

      {/* Product Grid */}
      <div className="product-grid">
        {filteredProducts.map((p) => {
          const availableStock = stockMap[p.id] ?? 0;
          const inStock = availableStock > 0;

          return (
            <div key={p.id} className="product-card">
              <div className="product-card-body">
                <span className="product-sku">{p.sku}</span>
                <h3 className="product-title">{p.name}</h3>
                <p className="product-desc">{p.description}</p>
                <div className="product-price">₹{Number(p.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>

                <div className="stock-status">
                  {inStock ? (
                    <span className="badge-in-stock">
                      ● {availableStock} in stock at store
                    </span>
                  ) : (
                    <span className="badge-out-stock">
                      ● Out of stock at this store
                    </span>
                  )}
                </div>
              </div>

              <div className="product-card-footer">
                <button
                  className="btn-add-cart"
                  disabled={!inStock}
                  onClick={() => handleAddToCart(p)}
                >
                  {inStock ? 'Add to Cart 🛒' : 'Unavailable'}
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Admin Add Product Modal */}
      {showAddModal && (
        <div className="modal-backdrop" onClick={() => setShowAddModal(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>✨ Add New Product to Catalog</h3>
              <button
                type="button"
                className="btn-modal-close"
                onClick={() => setShowAddModal(false)}
              >
                ✕
              </button>
            </div>

            {addError && <div className="alert-error">{addError}</div>}

            <form onSubmit={handleAddProductSubmit} className="admin-product-form">
              <div className="form-row">
                <div className="form-group">
                  <label>SKU (Stock Keeping Unit) *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. PROD-HONEY-01"
                    value={addFormData.sku}
                    onChange={(e) =>
                      setAddFormData({ ...addFormData, sku: e.target.value })
                    }
                  />
                </div>
                <div className="form-group">
                  <label>Price (₹) *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    placeholder="e.g. 299.00"
                    value={addFormData.price}
                    onChange={(e) =>
                      setAddFormData({ ...addFormData, price: e.target.value })
                    }
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Product Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Raw Organic Wildflower Honey (500g)"
                  value={addFormData.name}
                  onChange={(e) =>
                    setAddFormData({ ...addFormData, name: e.target.value })
                  }
                />
              </div>

              <div className="form-group">
                <label>Category *</label>
                <select
                  value={addFormData.categoryId}
                  onChange={(e) =>
                    setAddFormData({ ...addFormData, categoryId: e.target.value })
                  }
                  required
                >
                  <option value="">Select Category...</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  rows="3"
                  placeholder="Product description, ingredients, or specifications..."
                  value={addFormData.description}
                  onChange={(e) =>
                    setAddFormData({ ...addFormData, description: e.target.value })
                  }
                />
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn-modal-cancel"
                  onClick={() => setShowAddModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-modal-submit"
                  disabled={addLoading}
                >
                  {addLoading ? 'Creating Product...' : 'Create Product ✨'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Products;
