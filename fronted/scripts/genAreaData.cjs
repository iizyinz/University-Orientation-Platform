const fs = require('fs')
const path = require('path')

const csvPath = path.join(__dirname, '../../参考文档/ok_data_level3.csv')
const outPath = path.join(__dirname, '../src/utils/areaData.ts')

const lines = fs.readFileSync(csvPath, 'utf-8').split('\n').filter(l => l.trim())

// Parse CSV
const records = []
for (let i = 1; i < lines.length; i++) {
  const line = lines[i].trim()
  if (!line) continue
  // Match: id,pid,deep,"name",pinyin_prefix,pinyin,ext_id,"ext_name"
  const m = line.match(/^(\d+),(\d+),(\d+),"([^"]*)",[^,]*,"[^"]*","[^"]*","([^"]*)"$/)
  if (!m) continue
  records.push({
    id: parseInt(m[1]),
    pid: parseInt(m[2]),
    deep: parseInt(m[3]),
    name: m[4],
    extName: m[5],
  })
}

// Build tree
const provinces = []
const cityMap = {} // pid -> city array

for (const r of records) {
  if (r.deep === 0) {
    // province
    provinces.push({ id: r.id, name: r.extName, children: [] })
    cityMap[r.id] = []
  } else if (r.deep === 1) {
    // city
    const p = provinces.find(p => p.id === r.pid)
    if (p) {
      const city = { id: r.id, name: r.extName, children: [] }
      p.children.push(city)
    }
  } else if (r.deep === 2) {
    // district
    const p = provinces.find(p => p.id === Math.floor(r.pid / 100) * 100 || p.children.some(c => c.id === r.pid))
    if (p) {
      const city = p.children.find(c => c.id === r.pid)
      if (city) {
        city.children.push({ name: r.extName })
      }
    }
  }
}

// Generate TypeScript
let ts = `export const provinceData = [\n`
for (const p of provinces) {
  ts += `  {\n    name: '${p.name}', children: [\n`
  for (const c of p.children) {
    ts += `      { name: '${c.name}', children: [`
    const districts = c.children.map(d => `{ name: '${d.name}' }`).join(', ')
    ts += districts
    ts += `] },\n`
  }
  ts += `    ]\n  },\n`
}
ts += `]\n`

fs.writeFileSync(outPath, ts, 'utf-8')
console.log(`Generated ${outPath} with ${provinces.length} provinces`)
