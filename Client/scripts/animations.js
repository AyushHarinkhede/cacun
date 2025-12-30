// Scroll animations for Cacun website
document.addEventListener('DOMContentLoaded', () => {
  // Intersection Observer for scroll animations
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
      }
    });
  }, observerOptions);

  // Observe all elements with animation classes
  const animatedElements = document.querySelectorAll('.animate-on-scroll');
  animatedElements.forEach(el => observer.observe(el));

  // Add bounce effects on scroll
  let lastScrollY = window.scrollY;
  const bounceElements = document.querySelectorAll('.prodCard, .heroCard, .shopCtaCard');

  window.addEventListener('scroll', () => {
    const scrollY = window.scrollY;
    const deltaY = scrollY - lastScrollY;
    
    bounceElements.forEach((el, index) => {
      const rect = el.getBoundingClientRect();
      const isVisible = rect.top < window.innerHeight && rect.bottom > 0;
      
      if (isVisible) {
        const speed = 0.5 + (index * 0.1);
        const translateY = deltaY * speed * 0.1;
        const rotate = deltaY * 0.02;
        
        el.style.transform = `translateY(${translateY}px) rotate(${rotate}deg)`;
        
        setTimeout(() => {
          el.style.transform = '';
        }, 100);
      }
    });
    
    lastScrollY = scrollY;
  });

  // Parallax effect for footer wave
  const footerWave = document.querySelector('.footerWave');
  if (footerWave) {
    window.addEventListener('scroll', () => {
      const scrolled = window.pageYOffset;
      const rect = footerWave.getBoundingClientRect();
      const speed = 0.5;
      
      if (rect.top < window.innerHeight && rect.bottom > 0) {
        const yPos = -(scrolled * speed);
        footerWave.style.transform = `translateY(${yPos}px)`;
      }
    });
  }

  // Smooth reveal animation for navbar
  const navbar = document.querySelector('.navWrap');
  if (navbar) {
    navbar.style.animation = 'fadeInDown 0.8s ease-out';
  }

  // Add floating animation to logos
  const logos = document.querySelectorAll('.brandLogo, .footerLogo');
  logos.forEach((logo, index) => {
    logo.style.animation = `float 3s ease-in-out ${index * 0.5}s infinite`;
  });
});

// Add floating keyframe
const style = document.createElement('style');
style.textContent = `
  @keyframes float {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-10px); }
  }
  
  @keyframes fadeInDown {
    from {
      opacity: 0;
      transform: translateY(-30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
`;
document.head.appendChild(style);
