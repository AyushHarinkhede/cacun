import { useEffect } from 'react';

const SecurityProtection = () => {
  useEffect(() => {
    // Block copy, cut, and paste events (but allow in specific input areas)
    const blockCopyPaste = (e) => {
      // Allow in input fields, textareas, and contenteditable elements
      const target = e.target;
      const isInputArea = target.tagName === 'INPUT' || 
                         target.tagName === 'TEXTAREA' || 
                         target.contentEditable === 'true' ||
                         target.closest('.search-input') ||
                         target.closest('.message-input') ||
                         target.closest('[contenteditable="true"]') ||
                         target.classList.contains('search-input') ||
                         target.classList.contains('message-input');
      
      if (!isInputArea) {
        e.preventDefault();
        e.stopPropagation();
        return false;
      }
    };

    // Block right-click context menu (but allow in specific input areas)
    const blockContextMenu = (e) => {
      const target = e.target;
      const isInputArea = target.tagName === 'INPUT' || 
                         target.tagName === 'TEXTAREA' || 
                         target.contentEditable === 'true' ||
                         target.closest('.search-input') ||
                         target.closest('.message-input') ||
                         target.closest('[contenteditable="true"]') ||
                         target.classList.contains('search-input') ||
                         target.classList.contains('message-input');
      
      if (!isInputArea) {
        e.preventDefault();
        e.stopPropagation();
        return false;
      }
    };

    // Block developer tools and other keyboard shortcuts (but allow text editing shortcuts in input areas)
    const blockKeyboardShortcuts = (e) => {
      const target = e.target;
      const isInputArea = target.tagName === 'INPUT' || 
                         target.tagName === 'TEXTAREA' || 
                         target.contentEditable === 'true' ||
                         target.closest('.search-input') ||
                         target.closest('.message-input') ||
                         target.closest('[contenteditable="true"]') ||
                         target.classList.contains('search-input') ||
                         target.classList.contains('message-input');

      // Allow text editing shortcuts in input areas
      if (isInputArea) {
        // Allow Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X, Ctrl+Z in input areas
        if (e.ctrlKey && [65, 67, 86, 88, 90].includes(e.keyCode)) {
          return true; // Allow these shortcuts in input areas
        }
      }

      // Block developer tools shortcuts regardless of context
      if (
        e.keyCode === 123 || // F12
        (e.ctrlKey && e.shiftKey && e.keyCode === 73) || // Ctrl+Shift+I
        (e.ctrlKey && e.shiftKey && e.keyCode === 74) || // Ctrl+Shift+J
        (e.ctrlKey && e.keyCode === 85) // Ctrl+U
      ) {
        e.preventDefault();
        e.stopPropagation();
        return false;
      }

      // Block text editing shortcuts outside input areas
      if (!isInputArea && e.ctrlKey && [65, 67, 86, 88, 90].includes(e.keyCode)) {
        e.preventDefault();
        e.stopPropagation();
        return false;
      }
    };

    // Block drag and drop events
    const blockDragDrop = (e) => {
      e.preventDefault();
      e.stopPropagation();
      return false;
    };

    // Block text selection events (but allow in input areas)
    const blockTextSelection = (e) => {
      const target = e.target;
      const isInputArea = target.tagName === 'INPUT' || 
                         target.tagName === 'TEXTAREA' || 
                         target.contentEditable === 'true' ||
                         target.closest('.search-input') ||
                         target.closest('.message-input') ||
                         target.closest('[contenteditable="true"]') ||
                         target.classList.contains('search-input') ||
                         target.classList.contains('message-input');
      
      if (!isInputArea) {
        e.preventDefault();
        e.stopPropagation();
        return false;
      }
    };

    // Add event listeners
    document.addEventListener('copy', blockCopyPaste);
    document.addEventListener('cut', blockCopyPaste);
    document.addEventListener('paste', blockCopyPaste);
    document.addEventListener('contextmenu', blockContextMenu);
    document.addEventListener('keydown', blockKeyboardShortcuts);
    document.addEventListener('dragstart', blockDragDrop);
    document.addEventListener('drop', blockDragDrop);
    document.addEventListener('dragover', blockDragDrop);
    document.addEventListener('selectstart', blockTextSelection);

    // Additional protection for images
    const blockImageEvents = (e) => {
      e.preventDefault();
      e.stopPropagation();
      return false;
    };

    // Apply to all images
    const images = document.querySelectorAll('img');
    images.forEach(img => {
      img.addEventListener('dragstart', blockImageEvents);
      img.addEventListener('contextmenu', blockImageEvents);
      img.addEventListener('mousedown', blockImageEvents);
    });

    // Monitor for dynamically added images
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
          if (node.nodeName === 'IMG') {
            node.addEventListener('dragstart', blockImageEvents);
            node.addEventListener('contextmenu', blockImageEvents);
            node.addEventListener('mousedown', blockImageEvents);
          }
          // Check for images within added nodes
          if (node.querySelectorAll) {
            const nestedImages = node.querySelectorAll('img');
            nestedImages.forEach(img => {
              img.addEventListener('dragstart', blockImageEvents);
              img.addEventListener('contextmenu', blockImageEvents);
              img.addEventListener('mousedown', blockImageEvents);
            });
          }
        });
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });

    // Block dev tools detection
    const devtools = {
      open: false,
      orientation: null
    };

    const threshold = 160;

    setInterval(() => {
      if (window.outerHeight - window.innerHeight > threshold || 
          window.outerWidth - window.innerWidth > threshold) {
        if (!devtools.open) {
          devtools.open = true;
          // Optionally redirect or close window
          // window.location.href = 'about:blank';
        }
      } else {
        devtools.open = false;
      }
    }, 500);

    // Cleanup function
    return () => {
      document.removeEventListener('copy', blockCopyPaste);
      document.removeEventListener('cut', blockCopyPaste);
      document.removeEventListener('paste', blockCopyPaste);
      document.removeEventListener('contextmenu', blockContextMenu);
      document.removeEventListener('keydown', blockKeyboardShortcuts);
      document.removeEventListener('dragstart', blockDragDrop);
      document.removeEventListener('drop', blockDragDrop);
      document.removeEventListener('dragover', blockDragDrop);
      document.removeEventListener('selectstart', blockTextSelection);
      
      // Remove image event listeners
      images.forEach(img => {
        img.removeEventListener('dragstart', blockImageEvents);
        img.removeEventListener('contextmenu', blockImageEvents);
        img.removeEventListener('mousedown', blockImageEvents);
      });
      
      observer.disconnect();
    };
  }, []);

  return null; // This component doesn't render anything
};

export default SecurityProtection;
