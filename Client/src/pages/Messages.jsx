import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import CacunContainer from '../components/CacunContainer';
import { 
  MessageCircle, Send, Search, ArrowLeft, MoreVertical, Phone, Video, 
  Paperclip, Mic, Smile, Check, CheckCheck, Ban, Trash2, Download,
  Circle, UserCircle, X, Upload, Play, Pause
} from 'lucide-react';

export default function Messages() {
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [message, setMessage] = useState('');
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const [showOptions, setShowOptions] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const [recordingTime, setRecordingTime] = useState(0);
  const [typingUsers, setTypingUsers] = useState(new Set());
  const [showBlockDialog, setShowBlockDialog] = useState(false);
  const [showClearDialog, setShowClearDialog] = useState(false);
  const [isMobileView, setIsMobileView] = useState(false);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);
  const recordingInterval = useRef(null);

  const conversations = [
    {
      id: 1,
      name: 'Sarah Chen',
      avatar: 'SC',
      lastMessage: 'Hey! Are you still planning the Bali trip?',
      time: '2h ago',
      unread: 2,
      online: true,
      typing: false,
      blocked: false,
      messages: [
        { id: 1, text: 'Hey! How are you doing?', sender: 'other', time: '10:30 AM', read: true },
        { id: 2, text: 'I\'m doing great! Just planning my next trip', sender: 'me', time: '10:32 AM', read: true },
        { id: 3, text: 'That sounds exciting! Where are you thinking of going?', sender: 'other', time: '10:33 AM', read: true },
        { id: 4, text: 'Bali! I\'ve always wanted to visit', sender: 'me', time: '10:35 AM', read: true },
        { id: 5, text: 'Hey! Are you still planning the Bali trip?', sender: 'other', time: '10:40 AM', read: false },
      ]
    },
    {
      id: 2,
      name: 'Mike Johnson',
      avatar: 'MJ',
      lastMessage: 'The hotel looks amazing! Should we book it?',
      time: '5h ago',
      unread: 0,
      online: true,
      typing: true,
      blocked: false,
      messages: [
        { id: 1, text: 'Found this great hotel in Bali', sender: 'other', time: '8:00 AM', read: true },
        { id: 2, text: 'Let me see!', sender: 'me', time: '8:15 AM', read: true },
        { id: 3, text: 'The hotel looks amazing! Should we book it?', sender: 'other', time: '8:20 AM', read: true },
      ]
    },
    {
      id: 3,
      name: 'Yuki Tanaka',
      avatar: 'YT',
      lastMessage: 'Thanks for the restaurant recommendations!',
      time: '1d ago',
      unread: 0,
      online: false,
      typing: false,
      blocked: false,
      messages: [
        { id: 1, text: 'Any good restaurants in Bali?', sender: 'other', time: 'Yesterday', read: true },
        { id: 2, text: 'Yes! You should try Bebek Bengil', sender: 'me', time: 'Yesterday', read: true },
        { id: 3, text: 'Thanks for the restaurant recommendations!', sender: 'other', time: 'Yesterday', read: true },
      ]
    },
  ];

  const emojis = [
    '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩',
    '😘', '😗', '😚', '😙', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨',
    '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕',
    '🤢', '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓', '🧐', '😕', '😟', '🙁',
    '☹️', '😮', '😯', '😲', '😳', '🥺', '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣',
    '😞', '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈', '👿', '💀', '☠️', '💩', '🤡', '👹',
    '👺', '🎃', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾', '👋', '🤚', '🖐️', '✋', '🖖',
    '👌', '🤌', '🤏', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '🖕', '👇', '☝️', '👍', '👎',
    '✊', '👊', '🤛', '🤜', '👏', '🙌', '👐', '🤲', '🤝', '🙏', '❤️', '🧡', '💛', '💚', '💙', '💜',
    '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '🌹', '🥀', '🌸', '💐'
  ];

  const filteredConversations = conversations.filter(conv =>
    conv.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    conv.lastMessage.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const currentConversation = conversations.find(conv => conv.id === selectedConversation);

  useEffect(() => {
    const handleResize = () => {
      setIsMobileView(window.innerWidth < 768);
    };
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [currentConversation?.messages]);

  useEffect(() => {
    if (isRecording) {
      recordingInterval.current = setInterval(() => {
        setRecordingTime(prev => prev + 1);
      }, 1000);
    } else {
      clearInterval(recordingInterval.current);
      setRecordingTime(0);
    }
    return () => clearInterval(recordingInterval.current);
  }, [isRecording]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const sendMessage = () => {
    if (message.trim() && currentConversation) {
      // In a real app, this would send to backend
      setMessage('');
      setShowEmojiPicker(false);
    }
  };

  const handleEmojiSelect = (emoji) => {
    setMessage(prev => prev + emoji);
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file && currentConversation) {
      // In a real app, this would upload the file
      console.log('Uploading file:', file);
    }
  };

  const toggleRecording = () => {
    setIsRecording(!isRecording);
    if (!isRecording) {
      // Start recording
      console.log('Starting voice recording...');
    } else {
      // Stop recording
      console.log('Stopping voice recording...');
    }
  };

  const blockUser = () => {
    if (currentConversation) {
      // In a real app, this would call the backend
      console.log('Blocking user:', currentConversation.name);
      setShowBlockDialog(false);
      setShowOptions(false);
    }
  };

  const clearChat = () => {
    if (currentConversation) {
      // In a real app, this would call the backend
      console.log('Clearing chat with:', currentConversation.name);
      setShowClearDialog(false);
      setShowOptions(false);
    }
  };

  const exportChat = () => {
    if (currentConversation) {
      const chatText = currentConversation.messages.map(msg =>
        `${msg.sender === 'me' ? 'You' : currentConversation.name} (${msg.time}): ${msg.text}`
      ).join('\n\n');
      
      const blob = new Blob([chatText], { type: 'text/plain' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `chat_${currentConversation.name.replace(' ', '_')}.txt`;
      a.click();
      URL.revokeObjectURL(url);
      setShowOptions(false);
    }
  };

  const markAsRead = () => {
    if (currentConversation) {
      // In a real app, this would call the backend
      console.log('Marking conversation as read');
    }
  };

  return (
    <div className="h-screen flex flex-col bg-background">
      {/* Search Header */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="p-4 border-b border-primary/20"
      >
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-text-muted" />
          <input
            type="text"
            placeholder="Search conversations..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-3 bg-card border border-primary/20 rounded-lg text-text placeholder-text-muted focus:outline-none focus:border-primary"
          />
        </div>
      </motion.div>

      <div className="flex-1 flex overflow-hidden">
        {/* Conversation List */}
        <div className={`${isMobileView && selectedConversation ? 'hidden' : 'flex'} flex-col w-full md:w-96 border-r border-primary/20`}>
          <div className="flex-1 overflow-y-auto">
            {filteredConversations.map((conversation, index) => (
              <motion.div
                key={conversation.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.05 * index }}
                onClick={() => {
                  setSelectedConversation(conversation.id);
                  markAsRead();
                }}
                className={`p-4 border-b border-primary/10 cursor-pointer transition-colors ${
                  selectedConversation === conversation.id ? 'bg-primary/10' : 'hover:bg-primary/5'
                }`}
              >
                <div className="flex items-center space-x-3">
                  <div className="relative">
                    <div className="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center">
                      <span className="text-primary font-semibold">{conversation.avatar}</span>
                    </div>
                    {conversation.online && (
                      <div className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full border-2 border-background"></div>
                    )}
                    {conversation.typing && (
                      <div className="absolute bottom-0 right-0 w-3 h-3 bg-blue-500 rounded-full border-2 border-background animate-pulse"></div>
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <h3 className="font-semibold text-text truncate">{conversation.name}</h3>
                      <span className="text-xs text-text-muted">{conversation.time}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <p className="text-text-muted text-sm truncate">
                        {conversation.typing ? 'typing...' : conversation.lastMessage}
                      </p>
                      {conversation.unread > 0 && (
                        <div className="w-5 h-5 rounded-full bg-primary flex items-center justify-center ml-2 flex-shrink-0">
                          <span className="text-white text-xs font-semibold">{conversation.unread}</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>

        {/* Chat Area */}
        {currentConversation ? (
          <div className="flex-1 flex flex-col">
            {/* Chat Header */}
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              className="p-4 border-b border-primary/20 bg-card"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  {isMobileView && (
                    <button
                      onClick={() => setSelectedConversation(null)}
                      className="p-2 hover:bg-primary/10 rounded-lg transition-colors"
                    >
                      <ArrowLeft className="w-5 h-5 text-text" />
                    </button>
                  )}
                  <div className="relative">
                    <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                      <span className="text-primary font-semibold">{currentConversation.avatar}</span>
                    </div>
                    {currentConversation.online && (
                      <div className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 rounded-full border-2 border-card"></div>
                    )}
                  </div>
                  <div>
                    <h3 className="font-semibold text-text">{currentConversation.name}</h3>
                    <p className="text-xs text-text-muted">
                      {currentConversation.online ? 'Online' : 'Offline'}
                      {currentConversation.typing && ' • typing...'}
                    </p>
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <button className="p-2 hover:bg-primary/10 rounded-lg transition-colors">
                    <Phone className="w-5 h-5 text-text" />
                  </button>
                  <button className="p-2 hover:bg-primary/10 rounded-lg transition-colors">
                    <Video className="w-5 h-5 text-text" />
                  </button>
                  <div className="relative">
                    <button
                      onClick={() => setShowOptions(!showOptions)}
                      className="p-2 hover:bg-primary/10 rounded-lg transition-colors"
                    >
                      <MoreVertical className="w-5 h-5 text-text" />
                    </button>
                    <AnimatePresence>
                      {showOptions && (
                        <motion.div
                          initial={{ opacity: 0, scale: 0.95 }}
                          animate={{ opacity: 1, scale: 1 }}
                          exit={{ opacity: 0, scale: 0.95 }}
                          className="absolute right-0 top-12 w-48 bg-card rounded-lg shadow-lg border border-primary/20 z-50"
                        >
                          <button
                            onClick={() => setShowClearDialog(true)}
                            className="w-full text-left px-4 py-3 hover:bg-primary/10 transition-colors flex items-center space-x-2"
                          >
                            <Trash2 className="w-4 h-4 text-text" />
                            <span className="text-sm text-text">Clear Chat</span>
                          </button>
                          <button
                            onClick={exportChat}
                            className="w-full text-left px-4 py-3 hover:bg-primary/10 transition-colors flex items-center space-x-2"
                          >
                            <Download className="w-4 h-4 text-text" />
                            <span className="text-sm text-text">Export Chat</span>
                          </button>
                          <button
                            onClick={() => setShowBlockDialog(true)}
                            className="w-full text-left px-4 py-3 hover:bg-primary/10 transition-colors flex items-center space-x-2 text-red-500"
                          >
                            <Ban className="w-4 h-4" />
                            <span className="text-sm">Block User</span>
                          </button>
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </div>
                </div>
              </div>
            </motion.div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {currentConversation.messages.map((msg) => (
                <motion.div
                  key={msg.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className={`flex ${msg.sender === 'me' ? 'justify-end' : 'justify-start'}`}
                >
                  <div className={`max-w-xs lg:max-w-md px-4 py-2 rounded-2xl ${
                    msg.sender === 'me'
                      ? 'bg-primary text-white'
                      : 'bg-card text-text border border-primary/20'
                  }`}>
                    <p className="text-sm">{msg.text}</p>
                    <div className={`flex items-center justify-end space-x-1 mt-1 ${
                      msg.sender === 'me' ? 'text-white/70' : 'text-text-muted'
                    }`}>
                      <span className="text-xs">{msg.time}</span>
                      {msg.sender === 'me' && (
                        msg.read ? <CheckCheck className="w-3 h-3" /> : <Check className="w-3 h-3" />
                      )}
                    </div>
                  </div>
                </motion.div>
              ))}
              <div ref={messagesEndRef} />
            </div>

            {/* Message Input */}
            <div className="p-4 border-t border-primary/20 bg-card">
              <div className="flex items-center space-x-2">
                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={handleFileUpload}
                  className="hidden"
                  accept="image/*,video/*,.pdf,.doc,.docx"
                />
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="p-2 hover:bg-primary/10 rounded-lg transition-colors"
                >
                  <Paperclip className="w-5 h-5 text-text" />
                </button>
                <div className="relative">
                  <button
                    onClick={() => setShowEmojiPicker(!showEmojiPicker)}
                    className="p-2 hover:bg-primary/10 rounded-lg transition-colors"
                  >
                    <Smile className="w-5 h-5 text-text" />
                  </button>
                  <AnimatePresence>
                    {showEmojiPicker && (
                      <motion.div
                        initial={{ opacity: 0, scale: 0.95 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.95 }}
                        className="absolute bottom-12 left-0 w-80 h-64 bg-card rounded-lg shadow-lg border border-primary/20 p-2 overflow-y-auto z-50"
                      >
                        <div className="grid grid-cols-8 gap-1">
                          {emojis.map((emoji, index) => (
                            <button
                              key={index}
                              onClick={() => handleEmojiSelect(emoji)}
                              className="p-2 hover:bg-primary/10 rounded transition-colors text-lg"
                            >
                              {emoji}
                            </button>
                          ))}
                        </div>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </div>
                <input
                  type="text"
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                  placeholder="Type a message..."
                  className="flex-1 px-4 py-2 bg-background border border-primary/20 rounded-lg text-text placeholder-text-muted focus:outline-none focus:border-primary"
                />
                {isRecording ? (
                  <button
                    onClick={toggleRecording}
                    className="p-2 bg-red-500 text-white rounded-lg transition-colors flex items-center space-x-1"
                  >
                    <Pause className="w-5 h-5" />
                    <span className="text-sm">{formatTime(recordingTime)}</span>
                  </button>
                ) : (
                  <button
                    onClick={toggleRecording}
                    className="p-2 hover:bg-primary/10 rounded-lg transition-colors"
                  >
                    <Mic className="w-5 h-5 text-text" />
                  </button>
                )}
                <button
                  onClick={sendMessage}
                  className="p-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors"
                >
                  <Send className="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>
        ) : (
          <div className="hidden md:flex flex-1 items-center justify-center">
            <div className="text-center">
              <MessageCircle className="w-16 h-16 text-primary/20 mx-auto mb-4" />
              <h3 className="text-lg font-semibold text-text mb-2">Select a conversation</h3>
              <p className="text-text-muted">
                Choose a conversation from the list to start messaging
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Confirmation Dialogs */}
      <AnimatePresence>
        {showBlockDialog && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-card rounded-lg p-6 max-w-sm w-full"
            >
              <h3 className="text-lg font-semibold text-text mb-2">Block User</h3>
              <p className="text-text-muted mb-6">
                Are you sure you want to block {currentConversation?.name}? You won't receive messages from them anymore.
              </p>
              <div className="flex space-x-3">
                <button
                  onClick={() => setShowBlockDialog(false)}
                  className="flex-1 px-4 py-2 border border-primary/20 rounded-lg text-text hover:bg-primary/10 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={blockUser}
                  className="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
                >
                  Block
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {showClearDialog && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-card rounded-lg p-6 max-w-sm w-full"
            >
              <h3 className="text-lg font-semibold text-text mb-2">Clear Chat</h3>
              <p className="text-text-muted mb-6">
                Are you sure you want to clear all messages with {currentConversation?.name}? This action cannot be undone.
              </p>
              <div className="flex space-x-3">
                <button
                  onClick={() => setShowClearDialog(false)}
                  className="flex-1 px-4 py-2 border border-primary/20 rounded-lg text-text hover:bg-primary/10 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={clearChat}
                  className="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
                >
                  Clear
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
