<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import CounselorHome from './CounselorHome.vue'
import CounselorScan from './CounselorScan.vue'
import CounselorProfile from './CounselorProfile.vue'

const route = useRoute()
const tab = computed(() => (route.query.tab as string) || 'home')

const componentMap: Record<string, any> = {
  home: CounselorHome,
  scan: CounselorScan,
  profile: CounselorProfile,
}

const currentComponent = computed(() => componentMap[tab.value] || CounselorHome)
</script>

<template>
  <div class="counselor-shell">
    <component :is="currentComponent" />
  </div>
</template>

<style scoped>
.counselor-shell {
  min-height: 100%;
}
</style>
