<template>
  <article class="card card-back">
    <section class="card-content">
      <h3 class="back-title">今天感觉如何？</h3>

      <el-tabs v-model="activeMain" class="main-tabs" type="border-card">
        <el-tab-pane
          v-for="group in moodGroups"
          :key="group.key"
          :label="group.label"
          :name="group.key"
        >
          <el-tabs
            v-model="activeSub[group.key]"
            class="sub-tabs"
            tab-position="top"
            type="card"
          >
            <el-tab-pane
              v-for="sub in group.children"
              :key="sub.key"
              :label="sub.label"
              :name="sub.key"
            >
              <div class="mood-brief">
                <el-tag
                  v-for="tag in sub.tags"
                  :key="tag"
                  size="small"
                  class="mood-tag"
                  type="info"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
      </el-tabs>

      <el-form class="mood-form" :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="想用一句话描述当下吗？" prop="text">
          <el-input
            v-model="form.text"
            type="textarea"
            :rows="4"
            placeholder="记录当下：比如“有点焦虑，但看书让我放松”"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

      </el-form>
    </section>

    <footer class="card-footer">
      <el-button size="small" class="ghost-btn" @click="$emit('close')">
        取消
      </el-button>
      <el-button size="small" type="primary" class="calm-btn" :loading="submitting" @click="onSubmit">
        提交
      </el-button>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { reactive, ref, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

interface MoodGroup {
  key: string
  label: string
  children: { key: string; label: string; tags: string[] }[]
}

const props = withDefaults(defineProps<{
  modelValue?: {
    main?: string
    sub?: string
    text?: string
  }
  groups?: MoodGroup[]
  submitting?: boolean
}>(), {
  modelValue: () => ({ main: 'positive', sub: 'calm', text: '' }),
  groups: () => ([
    {
      key: 'positive',
      label: '积极',
      children: [
        { key: 'calm', label: '平静', tags: ['安宁', '稳定', '踏实'] },
        { key: 'joy', label: '喜悦', tags: ['开心', '满足', '庆祝'] },
        { key: 'motivated', label: '动力', tags: ['专注', '进步', '期待'] }
      ]
    },
    {
      key: 'neutral',
      label: '中性',
      children: [
        { key: 'ordinary', label: '平常', tags: ['如常', '温和', '缓慢'] },
        { key: 'tired', label: '疲惫', tags: ['需要休息', '能量低'] }
      ]
    },
    {
      key: 'negative',
      label: '消极',
      children: [
        { key: 'anxious', label: '焦虑', tags: ['紧张', '担心'] },
        { key: 'sad', label: '难过', tags: ['失落', '低落'] },
        { key: 'angry', label: '生气', tags: ['烦躁', '不满'] }
      ]
    }
  ]),
  submitting: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: { main?: string; sub?: string; text?: string }): void
  (e: 'submit', payload: { main: string; sub: string; text: string }): void
  (e: 'close'): void
}>()

const moodGroups = computed<MoodGroup[]>(() => props.groups)

const activeMain = ref<string>(props.modelValue?.main || 'positive')
const activeSub = reactive<Record<string, string>>({})
// 初始化各主类的默认子类
moodGroups.value.forEach(group => {
  activeSub[group.key] = group.children[0]?.key || ''
})
// 如果有传入默认子类
if (props.modelValue?.sub) {
  activeSub[activeMain.value] = props.modelValue.sub
}

watch(activeMain, (v) => {
  emit('update:modelValue', { ...props.modelValue, main: v, sub: activeSub[v] })
})
watch(activeSub, () => {
  emit('update:modelValue', { ...props.modelValue, main: activeMain.value, sub: activeSub[activeMain.value] })
}, { deep: true })

const form = reactive({
  text: props.modelValue?.text || ''
})
watch(() => form.text, (v) => {
  emit('update:modelValue', { ...props.modelValue, text: v })
})

const formRef = ref<FormInstance>()
const rules: FormRules = {
  text: [
    { required: false, message: '随时可留白', trigger: 'blur' },
    { min: 0, max: 200, message: '最多 200 字', trigger: 'change' }
  ]
}

const submitting = computed(() => props.submitting)

function onSubmit() {
  formRef.value?.validate((ok) => {
    if (!ok) return
    const payload = {
      main: activeMain.value,
      sub: activeSub[activeMain.value],
      text: form.text.trim()
    }
    emit('submit', payload)
  })
}
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
  position: relative;
}

.card-content {
  padding: 14px 16px 10px;
}

.back-title {
  margin: 0 0 8px;
  font-size: 16px;
  color: #4a3c2f;
}

.main-tabs :deep(.el-tabs__content) {
  padding: 8px 0 0 0;
}
.sub-tabs {
  margin-top: 8px;
}

.mood-brief {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 4px 2px 8px;
}
.mood-tag {
  background: rgba(255,255,255,0.6);
  border-color: #d9c2a5;
  color: #4a3c2f;
}

.mood-form {
  margin-top: 8px;
}
:deep(.el-form-item__label) {
  color: #4a3c2f;
}
:deep(.el-textarea__inner) {
  background: rgba(255,255,255,0.7);
  border-color: #d9c2a5;
  color: #4a3c2f;
}

.card-footer {
  display: flex;
  gap: 10px;
  padding: 10px 16px 16px;
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
</style>