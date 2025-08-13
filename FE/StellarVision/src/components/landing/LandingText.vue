<template>
  <div class="text-layer">
    <p class="text-above fade-in-up">{{ currentText }}</p>
    <button @click="goToMain" class="start-button">시작하기</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
const router = useRouter();

defineProps({
    currentText: String
});

function goToMain() {
    router.push("/main");
}
</script>

<style scoped>
.text-layer {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  z-index: 2; /* Globe 위에 위치 */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 2.1rem;
  text-align: center;
  pointer-events: none; /* 하위 요소에만 이벤트 전달 */
}

.text-above {
  color: #f2f2f2;
  font-weight: 700;
  font-size: 34px;
  animation: fade-in-up 2s ease-out forwards;
  animation-delay: 0.5s;
}

@keyframes fade-in-up {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.start-button {
  position: absolute;
  bottom: 55px;
  z-index: 3;
  padding: 0.5rem 1rem;
  color: #fff;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  text-decoration: none;
  pointer-events: auto;
  max-width: max-content;
  font-size: .0.95rem;
  background-color: transparent;
  border: 1px solid #f2f2f2;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.start-button:hover {
  background-color: #f2f2f2;
  color: #0b0c10;
}
</style>