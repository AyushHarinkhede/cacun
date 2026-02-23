import React, { useState } from 'react';
import { Bell, X, Check } from 'lucide-react';

const NotificationBell = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState([
    { id: 1, text: 'New trip request from Sarah', read: false, time: '2 min ago' },
    { id: 2, text: 'Your Bali trip is confirmed!', read: false, time: '1 hour ago' },
    { id: 3, text: 'Weather update for Iceland trip', read: true, time: '3 hours ago' },
  ]);

  const unreadCount = notifications.filter(n => !n.read).length;

  const markAsRead = (id) => {
    setNotifications(prev => 
      prev.map(notification => 
        notification.id === id ? { ...notification, read: true } : notification
      )
    );
  };

  const markAllAsRead = () => {
    setNotifications(prev => 
      prev.map(notification => ({ ...notification, read: true }))
    );
  };

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 rounded-full bg-gradient-to-r from-purple-500 to-violet-500 text-white shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-110"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 rounded-lg shadow-xl border z-50" style={{
          background: 'var(--bg-card)',
          borderColor: 'var(--border)'
        }}>
          <div className="p-4 border-b" style={{ borderColor: 'var(--border)' }}>
            <div className="flex items-center justify-between">
              <h3 className="font-semibold" style={{ color: 'var(--text-primary)' }}>Notifications</h3>
              <button
                onClick={markAllAsRead}
                className="text-sm transition"
                style={{ color: 'var(--primary)' }}
              >
                Mark all as read
              </button>
            </div>
          </div>
          
          <div className="max-h-96 overflow-y-auto">
            {notifications.map(notification => (
              <div
                key={notification.id}
                className="p-4 border-b cursor-pointer transition"
                style={{
                  borderColor: 'var(--border)',
                  backgroundColor: !notification.read ? 'var(--bg-accent)' : 'transparent'
                }}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <p className="text-sm" style={{ color: 'var(--text-primary)' }}>{notification.text}</p>
                    <p className="text-xs mt-1" style={{ color: 'var(--text-muted)' }}>{notification.time}</p>
                  </div>
                  {!notification.read && (
                    <button
                      onClick={() => markAsRead(notification.id)}
                      className="ml-2 transition"
                      style={{ color: 'var(--primary)' }}
                    >
                      <Check className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationBell;
