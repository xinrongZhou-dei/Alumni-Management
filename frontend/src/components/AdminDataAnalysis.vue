<template>
  <div class="admin-data-analysis">
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" :lg="6" v-for="(item, index) in statItems" :key="index" class="stat-item">
        <el-card class="stat-card">
          <div class="stat-title">{{ item.title }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" class="chart-col">
        <el-card>
          <div class="chart-title">毕业年份分布（Top5）</div>
          <v-chart :option="yearChartOption" autoresize class="chart" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12" class="chart-col">
        <el-card>
          <div class="chart-title">校友地理分布</div>
          <v-chart :option="locationChartOption" autoresize class="chart" />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" class="chart-col">
        <el-card>
          <div class="chart-title">YCYW Schools分布</div>
          <div class="chart-container">
            <div id="schoolChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12" class="chart-col">
        <el-card>
          <div class="chart-title">职业领域分布</div>
          <div class="chart-container">
            <div id="industryChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-button type="text" @click="goBack" class="back-btn" :style="isMobile ? 'position: static; margin-bottom: 16px;' : ''">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import VChart from 'vue-echarts'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import * as echarts from 'echarts'
import router from '@/router'
import { ArrowLeft } from '@element-plus/icons-vue'

// 判断是否为移动设备
const isMobile = ref(window.innerWidth <= 768)

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
})

function goBack  (){
  router.push("../")
}

use([
  CanvasRenderer,
  BarChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
])

// 动态数据
const stats = ref({
  totalAlumni: 0,
  currentYearGraduates: 0,
  activeAlumni: 0,
  currentYearActivity: 0
})

// 统计卡片数据
const statItems = computed(() => [
  { title: '总校友数', value: stats.value.totalAlumni },
  { title: '本年度毕业生', value: stats.value.currentYearGraduates },
  { title: '活跃校友', value: stats.value.activeAlumni },
  { title: '活动参与', value: stats.value.currentYearActivity }
])

const graduationYears = ref([])
const schools = ref([])
const industries = ref([])
const locationDistribution = ref({ mainland: 0, hongKong: 0, overseas: 0 })

// 权限相关
const dataAnalysisPermission = ref(0)
const hasPermission = ref(false)

// 获取权限
const fetchPermission = async () => {
  try {
    const res = await axios.get('/api/admin/permissions')
    if (res.data.code === 0) {
      const permissions = res.data.data
      dataAnalysisPermission.value = permissions.data_analysis_permission || 0
      hasPermission.value = dataAnalysisPermission.value > 0
    } else {
      dataAnalysisPermission.value = 0
      hasPermission.value = false
    }
  } catch (e) {
    dataAnalysisPermission.value = 0
    hasPermission.value = false
  }
}

// 图表option
const yearChartOption = ref({
  title: { left: 'center', text: '' },
  tooltip: {},
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [
    { data: [], type: 'bar', itemStyle: { color: '#409EFF' } }
  ]
})
const locationChartOption = ref({
  tooltip: { trigger: 'item' },
  legend: { top: 'bottom' },
  series: [
    {
      name: '地理分布',
      type: 'pie',
      radius: '60%',
      data: [],
      label: { formatter: '{b}: {c} ({d}%)' }
    }
  ]
})

function renderSchoolChart() {
  const chartDom = document.getElementById('schoolChart')
  if (!chartDom) return
  
  // 销毁已存在的实例
  const existingChart = echarts.getInstanceByDom(chartDom)
  if (existingChart) {
    existingChart.dispose()
  }
  
  const myChart = echarts.init(chartDom)
  myChart.setOption({
    tooltip: {},
    legend: { data: ['校友人数'], top: 30 },
    xAxis: {
      type: 'category',
      data: schools.value.map(item => item.name),
      axisLabel: {
        fontSize: 12,
        interval: 0,
        formatter: function(value) {
          return value.replace(/\s+/g, '\n')
        }
      },
      axisLine: { lineStyle: { color: '#aaa', width: 2 } },
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { fontSize: 14, color: '#333' },
      axisLine: { lineStyle: { color: '#aaa', width: 2 } },
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } }
    },
    series: [
      {
        name: '校友人数',
        data: schools.value.map(item => item.count),
        type: 'bar',
        itemStyle: { color: '#67C23A', opacity: 0.6 },
        barWidth: '40%'
      }
    ]
  })
  myChart.resize()
}

function renderIndustryChart() {
  const chartDom = document.getElementById('industryChart')
  if (!chartDom) return
  // 销毁已存在的实例
  const existingChart = echarts.getInstanceByDom(chartDom)
  if (existingChart) {
    existingChart.dispose()
  }
  const myChart = echarts.init(chartDom)
  myChart.setOption({
    tooltip: { trigger: 'item' },
    legend: {
      top: 'bottom',
    },
    series: [
      {
        name: '职业领域',
        type: 'pie',
        radius: ['40%', '60%'],
        avoidLabelOverlap: false,
        data: industries.value.map(item => ({
          name: item.name,
          value: item.value
        })),
        label: { show: false },
        labelLine: { show: false }
      }
    ]
  })
  myChart.resize()
}

onMounted(async () => {
  // 先获取权限
  await fetchPermission()
  
  if (!hasPermission.value) {
    ElMessage.error('权限不足，无法访问数据分析页面')
    router.push('/')
    return
  }
  
  try {
    const res = await axios.get('/api/admin/data-analysis')
    const data = res.data
    
    // 统计卡
    stats.value = {
      totalAlumni: data.totalAlumni || 0,
      currentYearGraduates: data.currentYearGraduates || 0,
      activeAlumni: data.activeAlumni || 0,
      currentYearActivity: data.currentYearActivity || 0
    }
    
    // 毕业年份分布
    graduationYears.value = data.graduationYears || []
    yearChartOption.value = {
      ...yearChartOption.value,
      xAxis: { ...yearChartOption.value.xAxis, data: graduationYears.value.map(item => item.year) },
      series: [{ ...yearChartOption.value.series[0], data: graduationYears.value.map(item => item.count) }]
    }
    
    // 学校分布
    schools.value = data.schools || []
    renderSchoolChart()
    
    // 地理分布
    locationDistribution.value = data.locationDistribution || { mainland: 0, hongKong: 0, overseas: 0 }
    locationChartOption.value = {
      ...locationChartOption.value,
      series: [{
        ...locationChartOption.value.series[0],
        data: [
          { name: '中国大陆', value: locationDistribution.value.mainland },
          { name: '香港', value: locationDistribution.value.hongKong },
          { name: '海外', value: locationDistribution.value.overseas }
        ],
        label: { show: false }
      }]
    }
    
    // 职业领域分布
    industries.value = data.industries || []
    renderIndustryChart()
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
})

// 监听窗口大小变化
const handleResize = () => {
  const schoolChart = echarts.getInstanceByDom(document.getElementById('schoolChart'))
  schoolChart?.resize()
  const industryChart = echarts.getInstanceByDom(document.getElementById('industryChart'))
  industryChart?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  const schoolChart = echarts.getInstanceByDom(document.getElementById('schoolChart'))
  schoolChart?.dispose()
  const industryChart = echarts.getInstanceByDom(document.getElementById('industryChart'))
  industryChart?.dispose()
})

watch(schools, () => {
  renderSchoolChart()
})
</script>

<style scoped>
.admin-data-analysis {
  padding: 32px;
  background: #f5f7fa;
  min-height: 100vh;
}

.stat-cards {
  margin-bottom: 32px;
}

.stat-item {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  background: linear-gradient(135deg, #e0eaff 0%, #f8fafd 100%);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
}

.stat-title {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-col {
  margin-bottom: 20px;
}

.chart-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 16px;
  color: #333;
}

.chart {
  height: 350px;
  width: 100%;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.back-btn {
  position: absolute;
  left: 40px;
  top: 40px;
  font-size: 18px;
  z-index: 1000;
  color: #409eff;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0 8px;
}

@media (max-width: 768px) {
  .admin-data-analysis {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 28px;
  }
  
  .chart-title {
    font-size: 16px;
    text-align: center;
  }
  
  .chart {
    height: 300px;
  }
  
  .chart-container {
    height: 300px;
  }
  
  .back-btn {
    position: static;
    margin-bottom: 16px;
    display: block;
    width: 100%;
    text-align: left;
  }
}
</style> 