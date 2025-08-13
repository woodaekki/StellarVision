<template>
  <button @click="goToMain" class="start-button">시작하기</button>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

function goToMain() {
  router.push("/main");
}
</script>

<style scoped>
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

  &:hover {
    background-color: #f2f2f2;
    color: #0b0c10;
  }
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
</style>
