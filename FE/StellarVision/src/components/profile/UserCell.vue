<!-- UserCell.vue -->
<template>
  <div class="user-cell" @click="goToUser">
    <img
      :src="user.profileImageUrl"
      :alt="user.name"
      class="profile-image"
      loading="lazy"
    />
    <span class="user-name">{{ user.name }}</span>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps(
  { user:
    {
      type: Object,
      required: true
    }
  }
)

const emit = defineEmits(['select'])

const router = useRouter()

async function goToUser() {
  emit('select')
  try {
    await router.push({
      path: `/profile/${props.user.email}`,
      state: { profilePk: props.user.memberId }
    })
    window.location.reload() // 라우터 이동이 성공하면 페이지 새로고침
  } catch (err) {
  }
}
</script>


<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  color: black;
  transition: background 0.2s;
  cursor: pointer;
  gap: 10px;
}

.user-cell:hover {
  background-color: black;
  color: white;
}

.profile-image {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  object-position: center;
  background: #eee;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.user-name {
  color: #fff
}
</style>
