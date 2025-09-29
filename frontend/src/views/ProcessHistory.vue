<template>
  <div class="process-history">
    <el-card shadow="hover">
      <h2>处理历史</h2>
      <el-table :data="appStore.history" style="width: 100%">
        <el-table-column prop="source_path" label="原图路径" />
        <el-table-column prop="output_path" label="输出路径" />
        <el-table-column prop="template_id" label="模板ID" />
        <el-table-column prop="processed_at" label="处理时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { useAppStore } from '../stores/app'
const appStore = useAppStore()
</script>

<style scoped>
.process-history {
  max-width: 900px;
  margin: 0 auto;
  margin-top: 20px;
}
</style>