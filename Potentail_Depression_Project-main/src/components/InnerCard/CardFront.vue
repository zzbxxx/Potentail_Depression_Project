<template>
  <article class="card">
    <header class="card-header">
      <img
        v-if="cover"
        class="card-cover"
        :src="cover"
        :alt="coverAlt || title || 'cover image'"
        loading="lazy"
      />
    </header>

    <section class="card-content">
      <p v-if="description" class="desc">
        {{ description }}
      </p>
      <slot name="content"></slot>

      <div v-if="title || subtitle" class="meta">
        <h3 v-if="title" class="book-title">《{{ title }}》</h3>
        <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
      </div>
    </section>

    <footer class="card-footer">
      <el-button
        v-if="primaryActionText"
        type="primary"
        size="small"
        class="calm-btn"
        @click="$emit('primary')"
      >
        {{ primaryActionText }}
      </el-button>
      <el-button size="small" class="ghost-btn" @click="$emit('close')">
        我先休息一下
      </el-button>
    </footer>
  </article>
</template>

<script setup lang="ts">
interface Props {
  cover?: string
  coverAlt?: string
  title?: string
  subtitle?: string
  description?: string
  primaryActionText?: string
}
withDefaults(defineProps<Props>(), {
  cover: '',
  coverAlt: '',
  title: '',
  subtitle: '',
  description: '',
  primaryActionText: ''
})
defineEmits<{
  (e: 'primary'): void
  (e: 'close'): void
}>()
</script>

<style scoped>
.card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f5e8c7 0%, #e8d5b9 48%);
  color: #4a3c2f;
  border: 1px solid #d9c2a5;
  position: absolute;
}

.card-header {
  display: grid;
  grid-template-columns: 100%;
  padding: 28px 24px 8px;
  align-items: center;
}
.card-cover {
  margin: 0 auto 4px;
  width: 78%;
  aspect-ratio: 3/4;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.card-content {
  padding: 8px 20px 16px;
}
.desc {
  margin: 0;
  line-height: 1.8;
  font-size: 15.5px;
  color: #4a3c2f;
}
.meta {
  margin-top: 14px;
}
.book-title {
  margin: 0 0 4px;
  font-size: 16.5px;
  letter-spacing: 0.2px;
}
.subtitle {
  margin: 0;
  color: #7a6650;
  font-size: 13.5px;
}

.card-footer {
  display: flex;
  gap: 10px;
  padding: 10px 16px 18px;
  justify-content: flex-end;
}

.calm-btn.el-button {
  --el-color-primary: #8a6d3b;
  --el-button-bg-color: #f0e6d2;
  --el-button-text-color: #4a3c2f;
  --el-button-hover-bg-color: #e8d5b9;
  --el-button-active-bg-color: #d9c2a5;
  border: 1px solid #c4a87a;
}
.ghost-btn.el-button {
  background: transparent;
  color: #7a6650;
  border: 1px solid #d9c2a5;
}
.ghost-btn.el-button:hover {
  border-color: #c4a87a;
  color: #4a3c2f;
}

@media (max-width: 360px) {
  .card-header { padding: 20px 16px 6px; }
  .card-cover { width: 84%; border-radius: 10px; }
  .desc { font-size: 15px; line-height: 1.75; }
}
</style>