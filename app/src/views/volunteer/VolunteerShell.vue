<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import VolunteerHome from './VolunteerHome.vue'
import KeyDistribute from './KeyDistribute.vue'
import VolunteerProfile from './VolunteerProfile.vue'

const route = useRoute()
const tab = computed(() => (route.query.tab as string) || 'home')

const componentMap: Record<string, any> = {
  home: VolunteerHome,
  distribute: KeyDistribute,
  profile: VolunteerProfile,
}

const currentComponent = computed(() => componentMap[tab.value] || VolunteerHome)
</script>

<template>
  <div class="volunteer-shell">
    <component :is="currentComponent" />
  </div>
</template>

<style scoped>
.volunteer-shell {
  min-height: 100%;
}
</style>
