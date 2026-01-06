import { useState } from 'react'
import './EnhancedFeatures.css'

import {
  IconLeaf,
  IconRecycle,
  IconHeart,
  IconStar,
  IconTrendingUp,
  IconAward,
  IconShoppingBag,
  IconFilter,
  IconSearch,
  IconGrid,
  IconList,
} from '../icons.jsx'
import QuickActions from '../QuickActions/QuickActions.jsx'

export default function EnhancedFeatures() {
  const [viewMode, setViewMode] = useState('grid')
  const [selectedCategory, setSelectedCategory] = useState('all')
  const [searchQuery, setSearchQuery] = useState('')

  const categories = [
    { id: 'all', name: 'All Products', icon: IconGrid },
    { id: 'plastic-free', name: 'Plastic Free', icon: IconLeaf },
    { id: 'non-toxic', name: 'Non Toxic', icon: IconHeart },
    { id: 'recycled', name: 'Recycled', icon: IconRecycle },
    { id: 'nature', name: 'Nature Products', icon: IconLeaf },
    { id: 'reuse', name: 'Reuse', icon: IconRecycle },
  ]

  const ecoStats = [
    { label: 'Plastic Saved', value: '2.5T', icon: IconRecycle },
    { label: 'Trees Planted', value: '1,234', icon: IconLeaf },
    { label: 'CO2 Reduced', value: '850kg', icon: IconTrendingUp },
    { label: 'Eco Score', value: 'A+', icon: IconAward },
  ]

  const handleSearch = (query) => {
    setSearchQuery(query)
    // Implement search functionality
  }

  const handleCategoryChange = (categoryId) => {
    setSelectedCategory(categoryId)
    // Implement category filtering
  }

  return (
    <div className="enhancedFeatures">
      {/* Eco Stats Dashboard */}
      <section className="ecoStats">
        <div className="statsContainer">
          <h2 className="statsTitle">Your Environmental Impact</h2>
          <div className="statsGrid">
            {ecoStats.map((stat, index) => (
              <div key={index} className="statCard">
                <div className="statIcon">
                  <stat.icon />
                </div>
                <div className="statContent">
                  <div className="statValue">{stat.value}</div>
                  <div className="statLabel">{stat.label}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Enhanced Product Filters */}
      <section className="enhancedFilters">
        <div className="filtersContainer">
          <div className="filterHeader">
            <h3 className="filterTitle">Discover Eco Products</h3>
            <div className="viewToggle">
              <button
                className={`viewBtn ${viewMode === 'grid' ? 'active' : ''}`}
                onClick={() => setViewMode('grid')}
                aria-label="Grid view"
              >
                <IconGrid />
              </button>
              <button
                className={`viewBtn ${viewMode === 'list' ? 'active' : ''}`}
                onClick={() => setViewMode('list')}
                aria-label="List view"
              >
                <IconList />
              </button>
            </div>
          </div>

          <div className="searchBar">
            <div className="searchInput">
              <IconSearch />
              <input
                type="text"
                placeholder="Search eco-friendly products..."
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
                className="searchField"
              />
            </div>
            <button className="filterBtn" aria-label="Advanced filters">
              <IconFilter />
            </button>
          </div>

          <div className="categoryTabs">
            {categories.map((category) => (
              <button
                key={category.id}
                className={`categoryTab ${selectedCategory === category.id ? 'active' : ''}`}
                onClick={() => handleCategoryChange(category.id)}
              >
                <category.icon />
                <span>{category.name}</span>
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* Quick Actions */}
      <section className="quickActions">
        <QuickActions />
      </section>
    </div>
  )
}
