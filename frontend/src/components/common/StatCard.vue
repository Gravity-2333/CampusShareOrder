<script setup>
defineProps({
  hint: {
    default: '',
    type: String,
  },
  label: {
    required: true,
    type: String,
  },
  value: {
    required: true,
    type: [String, Number],
  },
})
</script>

<template>
  <article class="stat-card surface-card">
    <p class="stat-label">
      {{ label }}
    </p>
    <strong class="stat-value">{{ value }}</strong>
    <span class="stat-hint">{{ hint }}</span>
  </article>
</template>
