<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import StudentHomeWeb from './StudentHomeWeb.vue'
import StudentWork from './StudentWork.vue'
import StudentInfoWeb from './StudentInfoWeb.vue'
import StudentFeeWeb from './StudentFeeWeb.vue'
import StudentDormWeb from './StudentDormWeb.vue'
import StudentHandbookWeb from './StudentHandbookWeb.vue'
import StudentCampusMapWeb from './StudentCampusMapWeb.vue'
import StudentGroupWeb from './StudentGroupWeb.vue'
import StudentFaqWeb from './StudentFaqWeb.vue'
import StudentCommonFaqWeb from './StudentCommonFaqWeb.vue'
import StudentProfileWeb from './StudentProfileWeb.vue'

const route = useRoute()
const tab = computed(() => (route.query.tab as string) || 'home')

const componentMap: Record<string, any> = {
  home: StudentHomeWeb,
  work: StudentWork,
  info: StudentInfoWeb,
  fee: StudentFeeWeb,
  dorm: StudentDormWeb,
  'discover-handbook': StudentHandbookWeb,
  'discover-map': StudentCampusMapWeb,
  'discover-group': StudentGroupWeb,
  'discover-faq': StudentFaqWeb,
  'discover-common-faq': StudentCommonFaqWeb,
  profile: StudentProfileWeb,
}

const currentComponent = computed(() => componentMap[tab.value] || StudentHomeWeb)
</script>

<template>
  <div class="web-shell">
    <component :is="currentComponent" />
  </div>
</template>

<style scoped>
.web-shell { max-width: 1600px; margin: 0 auto; }
</style>
