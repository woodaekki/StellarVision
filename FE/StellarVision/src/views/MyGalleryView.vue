<template>
  <p class="title">My Gallery</p>

  <div class="photo-frames">
    <div class="photo-frame" v-for="n in 3" :key="n">
      <div class="placeholder">+</div>
    </div>
  </div>
</template>

<style scoped>
.title {
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  margin-top: 60px;
  margin-bottom: 32px;
  color: #fff;
}

.photo-frames {
  display: flex;
  gap: 32px;
  justify-content: center;
  padding: 60px 20px;
  flex-wrap: wrap;
}

.photo-frame {
  width: 480px;
  height: 300px;
  background: #fff;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

.photo-frame:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

.placeholder {
  font-size: 96px;
  color: #ccc;
  user-select: none;
  font-weight: 300;
  transition: color 0.3s ease;
}

.photo-frame:hover .placeholder {
  color: #999;
}
</style>
