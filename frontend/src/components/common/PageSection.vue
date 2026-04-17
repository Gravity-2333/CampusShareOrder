<script setup>
defineProps({
  description: {
    default: '',
    type: String,
  },
  kicker: {
    default: '',
    type: String,
  },
  title: {
    required: true,
    type: String,
  },
})
</script>

<template>
  <section class="page-section surface-card">
    <div class="section-header">
      <div>
        <p v-if="kicker" class="section-kicker">{{ kicker }}</p>
        <h3>{{ title }}</h3>
      </div>
      <p v-if="description" class="section-description">{{ description }}</p>
    </div>
    <div class="section-body">
      <slot />
    </div>
  </section>
</template>
