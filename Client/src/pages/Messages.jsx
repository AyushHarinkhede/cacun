import { motion } from 'framer-motion';
import CacunContainer from '../components/CacunContainer';
import { MessageCircle, Send, Search } from 'lucide-react';

export default function Messages() {
  const conversations = [
    {
      id: 1,
      name: 'Sarah Chen',
      avatar: 'SC',
      lastMessage: 'Hey! Are you still planning the Bali trip?',
      time: '2h ago',
      unread: 2,
    },
    {
      id: 2,
      name: 'Mike Johnson',
      avatar: 'MJ',
      lastMessage: 'The hotel looks amazing! Should we book it?',
      time: '5h ago',
      unread: 0,
    },
    {
      id: 3,
      name: 'Yuki Tanaka',
      avatar: 'YT',
      lastMessage: 'Thanks for the restaurant recommendations!',
      time: '1d ago',
      unread: 0,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Search */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <CacunContainer>
          <h2 className="text-xl font-bold text-text mb-4">Messages</h2>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-text-muted" />
            <input
              type="text"
              placeholder="Search conversations..."
              className="w-full pl-10 pr-4 py-3 bg-background border border-primary/20 rounded-lg text-text placeholder-text-muted"
            />
          </div>
        </CacunContainer>
      </motion.div>

      {/* Conversations */}
      {conversations.map((conversation, index) => (
        <motion.div
          key={conversation.id}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 * (index + 1) }}
        >
          <CacunContainer hover>
            <div className="flex items-center space-x-4">
              <div className="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center flex-shrink-0">
                <span className="text-primary font-semibold">{conversation.avatar}</span>
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-text truncate">{conversation.name}</h3>
                  <span className="text-xs text-text-muted">{conversation.time}</span>
                </div>
                <p className="text-text-muted text-sm truncate">{conversation.lastMessage}</p>
              </div>
              {conversation.unread > 0 && (
                <div className="w-6 h-6 rounded-full bg-primary flex items-center justify-center">
                  <span className="text-white text-xs font-semibold">{conversation.unread}</span>
                </div>
              )}
            </div>
          </CacunContainer>
        </motion.div>
      ))}

      {/* Empty State for Chat View */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.5 }}
      >
        <CacunContainer>
          <div className="text-center py-8">
            <MessageCircle className="w-16 h-16 text-primary/20 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-text mb-2">Select a conversation</h3>
            <p className="text-text-muted">
              Choose a conversation from the list to start messaging
            </p>
          </div>
        </CacunContainer>
      </motion.div>
    </div>
  );
}
