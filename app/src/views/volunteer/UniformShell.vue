<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import UniformHome from './UniformHome.vue'
import UniformDistribute from './UniformDistribute.vue'
import UniformProfile from './UniformProfile.vue'

const route = useRoute()
const tab = computed(() => (route.query.tab as string) || 'home')

const componentMap: Record<string, any> = {
  home: UniformHome,
  distribute: UniformDistribute,
  profile: UniformProfile,
}

const currentComponent = computed(() => componentMap[tab.value] || UniformHome)
</script>

<template>
  <div class="uniform-shell">
    <component :is="currentComponent" />
  </div>
</template>

<style scoped>
.uniform-shell { min-height: 100%; }
</style>
