<template>
  <div class="image-block">
    <el-upload
      :show-file-list="false"
      :auto-upload="false"
      @change="handleChange"
      accept="image/*"
    >
      <el-button class="toolbar-btn">上傳圖片</el-button>
    </el-upload>
    <img v-if="modelValue" :src="modelValue" alt="圖片" class="w-full mt-2 rounded-lg" />
  </div>
</template>

<script setup>
import { ElUpload, ElButton, ElMessage } from 'element-plus';

const props = defineProps({ modelValue: String });
const emit = defineEmits(['update:modelValue', 'upload']);

const handleChange = (uploadFile) => {
  const isImage = uploadFile.raw.type.startsWith('image/');
  const isLt2M = uploadFile.raw.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error('請上傳圖片文件');
    return;
  }
  if (!isLt2M) {
    ElMessage.error('圖片大小不能超過 2MB');
    return;
  }
  const url = URL.createObjectURL(uploadFile.raw);
  emit('update:modelValue', url); 
  emit('upload', uploadFile.raw); 
};
</script>

<style scoped>
.image-block {
  margin-top: 8px;
}

.w-full {
  width: 100%;
  max-width: 120px;
}

.toolbar-btn {
  width: 100px;
}

.mt-2 {
  margin-top: 8px;
}

.rounded-lg {
  border-radius: 8px;
}
</style>