<template>
  <article class="card" v-if="moodData">
    <header class="card-header">
      <div class="mood-icon">😊</div> 
    </header>

    <section class="card-content">
      <p v-if="moodData.text" class="desc">
        {{ moodData.text }}
      </p>
      <div class="meta">
        <p v-if="moodData.primaryMood" class="mood">情绪: {{ moodData.primaryMood }}</p>
        <p v-if="moodData.events" class="events">事件: {{ moodData.events }}</p>
      </div>
    </section>

    <footer class="card-footer">
      <el-button
        type="primary"
        size="small"
        class="calm-btn"
        @click="$emit('primary')"
      >
        保存心情
      </el-button>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

interface Props {
  date: string
  moodData: {
    text?: string
    primaryMood?: string
    events?: string
  } | null
}

const props = defineProps<Props>()

defineEmits<{
  (e: 'primary'): void
  (e: 'close'): void
}>()

console.log(props.date, props.moodData);

</script>

<style scoped>
.card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f9f1e7 0%, #ece1d8 48%);
  color: #5a4a3f;
  border: 1px solid #e0d2c7;
  max-width: 320px;
  margin: 0 auto;
  padding-bottom: 1rem;
}

.card-header {
  display: flex;
  justify-content: flex-start;
  padding: 16px 20px 8px;
  align-items: center;
}

.mood-icon {
  font-size: 24px;
  margin-left: 0;
}

.card-content {
  padding: 16px 20px;
  text-align: left;
}

.desc {
  margin: 0 0 16px 0;
  line-height: 1.6;
  font-size: 16px;
  color: #5a4a3f;
  max-height: 200px;
  overflow-y: auto;
  padding-right: 10px;
}

.meta {
  margin-top: 12px;
  text-align: left;
}

.mood {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #7a6650;
}

.events {
  margin: 0;
  font-size: 14px;
  color: #7a6650;
}

.card-footer {
  display: flex;
  gap: 12px;
  padding: 12px 20px 16px;
  justify-content: flex-start;
}

.calm-btn.el-button {
  --el-color-primary: #8a6d3b;
  --el-button-bg-color: #f0e6d2;
  --el-button-text-color: #4a3c2f;
  --el-button-hover-bg-color: #e8d5b9;
  --el-button-active-bg-color: #d9c2a5;
  border: 1px solid #c4a87a;
  padding: 6px 12px;
}

@media (max-width: 360px) {
  .card-header {
    padding: 12px 16px 6px;
  }
  .desc {
    font-size: 14px;
    line-height: 1.5;
  }
  .mood, .events {
    font-size: 12px;
  }
  .card-footer {
    padding: 8px 16px 12px;
  }
}
</style>