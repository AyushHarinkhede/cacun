import { motion } from 'framer-motion';

export default function CacunContainer({ 
  children, 
  className = '', 
  hover = false,
  onClick 
}) {
  const containerVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: {
        type: "spring",
        stiffness: 100,
        damping: 15
      }
    }
  };

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      whileHover={hover ? { 
        scale: 1.02, 
        boxShadow: "0 20px 40px rgba(0,0,0,0.3)" 
      } : {}}
      onClick={onClick}
      className={`
        relative bg-card p-6 shadow-xl
        transition-all duration-300 ease-out
        ${hover ? 'cursor-pointer' : ''}
        ${className}
      `}
      style={{
        borderRadius: '24px 24px 24px 8px',
        border: '1px solid rgba(230, 126, 95, 0.1)',
      }}
    >
      {/* Subtle gradient overlay for depth */}
      <div 
        className="absolute inset-0 opacity-10 pointer-events-none"
        style={{
          background: 'linear-gradient(135deg, #E67E5F 0%, transparent 60%)',
          borderRadius: '24px 24px 24px 8px',
        }}
      />
      
      {/* Content */}
      <div className="relative z-10">
        {children}
      </div>
    </motion.div>
  );
}
