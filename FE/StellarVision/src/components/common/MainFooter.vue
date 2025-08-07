<script setup lang="ts">
import { RouterLink } from 'vue-router';

</script>

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

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css');

.footer-container {
  background-color: #121212;
  color: #e0e0e0;
  font-family: 'Poppins', sans-serif;
  padding: 4rem 2rem;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  max-width: 1200px;
  margin: 0 auto;
  flex-wrap: wrap;
  gap: 2rem;
}

.footer-left {
  flex: 1;
  min-width: 300px;
}

.footer-logo {
  font-size: 2.5rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 0.5rem;
}

.footer-slogan {
  font-size: 1rem;
  color: #a0a0a0;
  margin-bottom: 1rem;
}

.footer-description {
  font-size: 0.9rem;
  color: #707070;
  line-height: 1.6;
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
}

.footer-section h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 1.5rem;
  position: relative;
}

.footer-section h4::after {
  content: '';
  position: absolute;
  right: 0;
  bottom: -5px;
  width: 40px;
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
  margin-bottom: 0.8rem;
}

.footer-section ul li a {
  color: #a0a0a0;
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-section ul li a:hover {
  color: #ffffff;
}

.social-links {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
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
  padding-top: 2rem;
  margin-top: 4rem;
  text-align: center;
}

.footer-bottom p {
  font-size: 0.8rem;
  color: #707070;
  margin: 0;
}

/* Responsive Styles */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    text-align: center;
  }
  .footer-left,
  .footer-right {
    min-width: unset;
  }
  .footer-left {
    margin-bottom: 2rem;
  }
  .footer-right {
    margin-left: 0;
    justify-content: center;
  }
  .footer-section {
    text-align: center;
    margin-bottom: 1.5rem;
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
  }
}
</style>
