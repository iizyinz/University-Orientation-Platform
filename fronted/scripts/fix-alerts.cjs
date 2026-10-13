const fs = require('fs')
const path = require('path')

const files = [
  'D:/新生一站式/fronted/src/views/student/StudentInfo.vue',
  'D:/新生一站式/fronted/src/views/student/StudentDorm.vue',
]

for (const file of files) {
  let content = fs.readFileSync(file, 'utf-8')

  // 替换 alert(err.message || 'xxx') 为 showModal(err.message || 'xxx', 'error')
  content = content.replace(/alert\(err\.message \|\| '([^']+)'\)/g, "showModal(err.message || '$1', 'error')")

  // 替换 alert('xxx') 为 showModal('xxx')
  content = content.replace(/alert\('([^']+)'\)/g, "showModal('$1')")

  fs.writeFileSync(file, content, 'utf-8')
  console.log(`Fixed: ${path.basename(file)}`)
}
