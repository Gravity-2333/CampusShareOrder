<script setup>
defineProps({
  description: {
    default: '当前暂无数据',
    type: String,
  },
  kicker: {
    default: '空状态',
    type: String,
  },
  title: {
    default: '空状态',
    type: String,
  },
})
</script>

<template>
  <div class="empty-state surface-card">
    <p class="section-kicker">{{ kicker }}</p>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <slot />
  </div>
</template>
