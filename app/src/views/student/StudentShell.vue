<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import StudentHome from './StudentHome.vue'
import StudentWork from './StudentWork.vue'
import StudentCheckin from './StudentCheckin.vue'
import StudentInfo from './StudentInfo.vue'
import StudentFee from './StudentFee.vue'
import StudentDorm from './StudentDorm.vue'
import StudentDiscover from './StudentDiscover.vue'
import StudentFaq from './StudentFaq.vue'
import StudentCommonFaq from './StudentCommonFaq.vue'
import StudentProfile from './StudentProfile.vue'

const route = useRoute()
const tab = computed(() => (route.query.tab as string) || 'home')

const componentMap: Record<string, any> = {
  home: StudentHome,
  work: StudentWork,
  checkin: StudentCheckin,
  info: StudentInfo,
  fee: StudentFee,
  dorm: StudentDorm,
  discover: StudentDiscover,
  'discover-handbook': StudentDiscover,
  'discover-map': StudentDiscover,
  'discover-group': StudentDiscover,
  'discover-faq': StudentDiscover,
  'discover-common-faq': StudentDiscover,
  faq: StudentFaq,
  'common-faq': StudentCommonFaq,
  profile: StudentProfile,
}

const currentComponent = computed(() => componentMap[tab.value] || StudentHome)
</script>

<template>
  <div class="student-shell">
    <component :is="currentComponent" />
  </div>
</template>

<style scoped>
.student-shell {
  min-height: 100%;
}
</style>
