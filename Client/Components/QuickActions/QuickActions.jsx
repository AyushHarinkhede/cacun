import { useState } from 'react'
import './QuickActions.css'

import { useSettings } from '../../contexts/useSettings.js'
import {
  IconShoppingBag,
  IconHeart,
  IconStar,
  IconSettings,
  IconMapPin,
  IconPhone,
  IconMail,
} from '../icons.jsx'

export default function QuickActions() {
  const { setSettingsOpen, user } = useSettings()
  const [activeTab, setActiveTab] = useState('actions')

  const quickActions = [
    {
      id: 'orders',
      title: 'Your Orders',
      description: 'Track your eco-purchases',
      icon: IconShoppingBag,
      color: 'purple',
      onClick: () => console.log('Orders clicked')
    },
    {
      id: 'wishlist',
      title: 'Wishlist',
      description: 'Save favorite products',
      icon: IconHeart,
      color: 'pink',
      onClick: () => console.log('Wishlist clicked')
    },
    {
      id: 'reviews',
      title: 'Reviews',
      description: 'Share your feedback',
      icon: IconStar,
      color: 'yellow',
      onClick: () => console.log('Reviews clicked')
    },
    {
      id: 'settings',
      title: 'Settings',
      description: 'Customize your experience',
      icon: IconSettings,
      color: 'blue',
      onClick: () => setSettingsOpen(true)
    }
  ]

  const userInfo = [
    {
      id: 'profile',
      title: 'Profile Information',
      description: 'Manage your personal details',
      icon: IconMapPin,
      value: user?.name || 'Guest User'
    },
    {
      id: 'address',
      title: 'Delivery Address',
      description: 'Set your delivery location',
      icon: IconMapPin,
      value: 'Add address'
    },
    {
      id: 'phone',
      title: 'Contact Number',
      description: 'Your phone for delivery updates',
      icon: IconPhone,
      value: user?.phone || 'Add number'
    },
    {
      id: 'email',
      title: 'Email Address',
      description: 'For order confirmations',
      icon: IconMail,
      value: user?.email || 'Add email'
    }
  ]

  const tabs = [
    { id: 'actions', label: 'Quick Actions' },
    { id: 'profile', label: 'My Profile' }
  ]

  return (
    <div className="quickActions">
      <div className="actionsContainer">
        <div className="actionsHeader">
          <h3 className="actionsTitle">Quick Access</h3>
          <div className="tabNavigation">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                className={`tabBtn ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {activeTab === 'actions' && (
          <div className="actionsGrid">
            {quickActions.map((action) => (
              <button
                key={action.id}
                className="actionCard"
                onClick={action.onClick}
              >
                <div className={`actionIcon actionIcon--${action.color}`}>
                  <action.icon />
                </div>
                <div className="actionContent">
                  <div className="actionTitle">{action.title}</div>
                  <div className="actionDesc">{action.description}</div>
                </div>
              </button>
            ))}
          </div>
        )}

        {activeTab === 'profile' && (
          <div className="profileGrid">
            {userInfo.map((info) => (
              <div key={info.id} className="profileCard">
                <div className="profileIcon">
                  <info.icon />
                </div>
                <div className="profileContent">
                  <div className="profileTitle">{info.title}</div>
                  <div className="profileDesc">{info.description}</div>
                  <div className="profileValue">{info.value}</div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
