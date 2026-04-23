<script setup>
import { computed } from 'vue'

import { getStatusTone } from '../../utils/format'

const props = defineProps({
  text: {
    required: true,
    type: String,
  },
  value: {
    required: true,
    type: String,
  },
})

const type = computed(() => getStatusTone(props.value))
</script>

<template>
  <el-tag
    :type="type"
    effect="light"
    round
  >
    {{ text }}
  </el-tag>
</template>
