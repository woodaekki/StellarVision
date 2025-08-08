<template>
  <footer class="footer-container">
    <div class="footer-content">
      <div class="footer-left">
        <div class="footer-logo">StellarVision</div>
        <p class="footer-slogan">우주 탐험의 모든 것</p>
        <p class="footer-description">
          StellarVision은 우주 탐험에 대한 이러이러한 서비스를 제공합니다.
        </p>
      </div>
      <div class="footer-right">
        <div class="footer-section">
          <h4>서비스</h4>
          <ul>
            <li><RouterLink to="/streaming">스트리밍목록</RouterLink></li>
            <li> <RouterLink to="/profile/:id">프로필</RouterLink></li>
            <li><RouterLink to="/badge">도감</RouterLink></li>
            <li> <RouterLink to="/calender">캘린더</RouterLink></li>
          </ul>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2025 StellarVision. All Rights Reserved.</p>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css');

.footer-container {
  background-color: #121212;
  color: #e0e0e0;
  font-family: 'Poppins', sans-serif;
  padding: 1rem 2rem;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  max-width: 1200px;
  margin: 2rem auto;
  flex-wrap: wrap;
  gap: 2rem;
}

.footer-left {
  flex: 1;
  min-width: 300px;
}

.footer-logo {
  font-size: 2rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 0.5rem;
}

.footer-slogan {
  font-size: 0.9rem;
  color: #a0a0a0;
  margin-bottom: 1rem;
}

.footer-description {
  font-size: 0.8rem;
  color: #707070;
  line-height: 1.7;
  max-width: 400px;
}

.footer-right {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  margin-left: auto;
  padding-right: 2rem;
}

.footer-section {
  text-align: right;
  margin-top: 1rem;
}

.footer-section h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 1.2rem;
  position: relative;
}

.footer-section h4::after {
  content: '';
  position: absolute;
  right: 0;
  bottom: -6px;
  width: 50px;
  height: 2px;
  background-color: #007bff;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
  text-align: right;
}

.footer-section ul li {
  margin-bottom: 0.6rem;
}

.footer-section ul li a {
  color: #a0a0a0;
  text-decoration: none;
  transition: color 0.3s ease;
  font-size: 1rem;
}

.footer-section ul li a:hover {
  color: #ffffff;
}

.social-links {
  display: flex;
  gap: 1.2rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

.social-links a {
  color: #a0a0a0;
  font-size: 1.5rem;
  transition: color 0.3s ease;
}

.social-links a:hover {
  color: #ffffff;
}

.footer-bottom {
  border-top: 1px solid #333333;
  padding-top: 1rem;
  margin-top: 3rem;
  text-align: center;
}

.footer-bottom p {
  font-size: 0.9rem;
  color: #707070;
  margin: 0;
}

/* Responsive Styles */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    text-align: center;
    margin-top: 1.5rem;
    gap: 1.5rem;
  }
  .footer-left,
  .footer-right {
    min-width: unset;
  }
  .footer-left {
    margin-bottom: 1.5rem;
    text-align: center;
  }
  .footer-right {
    margin-left: 0;
    justify-content: center;
    padding-right: 0;
  }
  .footer-section {
    text-align: center;
    margin-bottom: 1.5rem;
    margin-top: 0;
  }
  .footer-section h4::after {
    left: 50%;
    right: auto;
    transform: translateX(-50%);
  }
  .footer-section ul {
    text-align: center;
  }
  .social-links {
    justify-content: center;
    margin-top: 0.5rem;
  }
}
</style>
