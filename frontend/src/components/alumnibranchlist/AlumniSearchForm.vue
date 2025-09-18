<template>
  
  <el-form :model="form" label-width="120px" @submit.native.prevent class="search-form">
    <el-row :gutter="12">
      <el-col :span="6">
        <el-input v-model="form.name" placeholder="按中文名或英文名搜索" clearable @input="onSearchAuto" />
      </el-col>
      <el-col :span="6">
        <el-select v-model="form.ycywSchoolsAttended" placeholder="按YCYW Schools Attended搜索" clearable filterable @change="onSearchAuto">
          <el-option v-for="s in schools" :key="s" :label="s" :value="s" />
        </el-select>
      </el-col>
      <el-col :span="6">
        <el-input v-model="form.university" placeholder="按University搜索" clearable @input="onSearchAuto" />
      </el-col>
      <el-col :span="4" class="btn-col">
        <el-button @click="onReset">重置</el-button>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup>
import { reactive, watch } from 'vue';

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['update:modelValue', 'search', 'reset']);

const form = reactive({ ...props.modelValue });

const schools = [
  'YCIS Hong Kong', 'YCIS Shanghai', 'YCIS Beijing', 'YCIS Chongqing', 'YCIS Qingdao',
  'YWIES Beijing', 'YWIES Guangzhou', 'YWIES Shanghai Gubei', 'YWIES Shanghai Lingang',
  'YWIES Tongxiang', 'YWIES Yantai'
];

watch(() => props.modelValue, (newVal) => {
  Object.assign(form, newVal);
}, { deep: true });

function onSearchAuto() {
  emit('search', { ...form });
}

function onReset() {
  Object.keys(form).forEach(key => form[key] = '');
  emit('reset');
}
</script>

<style scoped>
.search-form {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px #e0e7ef22;
  padding: 32px 24px 16px 24px;
  margin-bottom: 0;
}
.el-form-item__label {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}
.el-input, .el-select, .el-select .el-input, .el-select .el-input__wrapper {
  width: 100% !important;
  box-sizing: border-box;
  min-width: 0;
}
.text-right {
  text-align: right;
}
.el-button + .el-button {
  margin-left: 8px;
}
@media (max-width: 900px) {
  .el-col {
    flex: 0 0 100%;
    max-width: 100%;
  }
  .btn-col {
    flex-direction: column;
    align-items: stretch;
    justify-content: flex-start;
    gap: 8px;
    margin-top: 8px;
  }
  .btn-col .el-button {
    width: 100%;
    box-sizing: border-box;
  }
  .text-right {
    text-align: left;
    margin-top: 8px;
  }
}
@media (max-width: 768px) {
  .search-form {
    padding: 8px 4px 8px 4px;
    border-radius: 0;
    box-shadow: none;
  }
  .el-row {
    flex-direction: column !important;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  .el-col {
    width: 100% !important;
    max-width: 100% !important;
    padding-left: 0 !important;
    padding-right: 0 !important;
    margin-bottom: 8px;
  }
  .el-form-item__label {
    font-size: 14px;
    min-width: 8em;
    width: 8em;
    display: inline-block;
    text-align: right;
  }
  .el-input, .el-select {
    min-height: 36px;
    font-size: 14px;
  }
  .text-right {
    text-align: left;
    margin-top: 8px;
  }
  .el-button + .el-button {
    margin-left: 8px;
  }
}
.btn-col {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}
</style> 