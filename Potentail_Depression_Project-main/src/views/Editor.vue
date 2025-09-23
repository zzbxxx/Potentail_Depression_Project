<template>
  <div class="editor-background">
    <div class="editor-container">
      <div class="flex flex-row gap-4 mb-4">
        <el-select v-model="articleType" placeholder="選擇文章類型" class="w-1/2">
          <el-option label="新聞" value="news" />
          <el-option label="博客" value="blog" />
          <el-option label="教程" value="tutorial" />
          <el-option label="評論" value="review" />
        </el-select>

        <el-select
          v-model="topics"
          multiple
          filterable
          allow-create
          default-first-option
          :max="3"
          placeholder="選擇或輸入話題（最多3個）"
          class="w-1/2"
          @change="handleTopicChange"
        >
          <el-option v-for="item in topicOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </div>

      <!-- Title Input -->
      <el-input v-model="title" placeholder="輸入標題" class="mb-4" />

      <!-- Toolbar -->
      <Toolbar 
        @add-text="addTextBlock" 
        @add-image="addImageBlock" 
        @add-link="addLinkBlock" 
        @save="saveContent" 
        @preview="togglePreview" 
      />
      
      <!-- Editor Content -->
      <div class="editor-content">
        <draggable v-model="blocks" item-key="id" handle=".drag-handle">
          <template #item="{ element, index }">
            <div class="block">
              <el-icon class="drag-handle" :size="20"><Rank /></el-icon>
              <el-button type="text" @click="removeBlock(index)" class="float-right">刪除</el-button>
              <TextBlock 
                v-if="element.type === 'text'" 
                :model-value="element.content" 
                @update:model-value="value => updateBlockContent(index, value)" 
              />
              <ImageBlock 
                v-if="element.type === 'image'" 
                :model-value="element.url" 
                @update:model-value="value => updateBlockContent(index, { url: value })" 
                @upload="file => handleImageUpload(index, file)"
              />
            </div>
          </template>
        </draggable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import draggable from 'vuedraggable';
import Toolbar from '/src/components/Editor/Toolbar.vue';
import TextBlock from '/src/components/Editor/TextBlock.vue';
import ImageBlock from '/src/components/Editor/ImageBlock.vue';
import { ElInput, ElIcon, ElButton, ElSelect, ElOption, ElMessage, ElLoading } from 'element-plus';
import { Rank } from '@element-plus/icons-vue';
import ArticleService from '/src/api/articleApi';
const title = ref('');
const articleType = ref('');
const blocks = ref([]);
const topics = ref([]);
const imageFiles = ref([]); // 儲存圖片文件
let blockId = 0;

const topicOptions = ref(["美食", "旅遊", "學習", "生活", "科技"]);

const handleTopicChange = (selectedTopics) => {
  if (selectedTopics.length > 3) {
    topics.value = selectedTopics.slice(0, 3);
    ElMessage.warning('最多只能選擇或輸入3個話題哦！');
  }
};

const addTextBlock = () => {
  blocks.value.push({ id: blockId++, type: 'text', content: '' });
};

const addImageBlock = () => {
  blocks.value.push({ id: blockId++, type: 'image', url: '' });
};

const addLinkBlock = () => {
  blocks.value.push({ id: blockId++, type: 'link', text: '', url: '' });
};

const removeBlock = (index) => {
  blocks.value.splice(index, 1);
  imageFiles.value = imageFiles.value.filter((_, i) => i !== index);
};

const updateBlockContent = (index, value) => {
  blocks.value[index] = { ...blocks.value[index], ...(typeof value === 'string' ? { content: value, url: value } : value) };
};

const handleImageUpload = (index, file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error('請上傳圖片文件');
    return;
  }
  if (!isLt2M) {
    ElMessage.error('圖片大小不能超過 2MB');
    return;
  }
  const url = URL.createObjectURL(file);
  blocks.value[index].url = url;
  imageFiles.value[index] = file;
};

const saveContent = async () => {
  const userId = localStorage.getItem("userId") || localStorage.getItem("user_id");
  if (!userId) {
    ElMessage.error('請先登錄');
    return;
  }

  const loading = ElLoading.service({
    lock: true,
    text: '正在保存文章...',
    background: 'rgba(0, 0, 0, 0.7)',
  });

  try {
    const data = {
      userId: parseInt(userId),
      title: title.value,
      articleType: articleType.value,
      topics: topics.value,
      blocks: blocks.value.map(block => {
        if (block.type === 'link') {
          return { type: 'link', text: block.text, url: block.url };
        }
        return {
          type: block.type,
          content: block.type === 'text' ? block.content : block.url
        };
      })
    };

    const formData = new FormData();
    formData.append('article', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    imageFiles.value.forEach((file, index) => {
      if (file) {
        formData.append('images', file);
      }
    });

    // 直接调用 API 服务，不需要再处理 response
    const result = await ArticleService.putArticleData(formData);
    ElMessage.success('文章保存成功！');
    console.log('保存成功:', result);
  } catch (error) {
    console.error('保存失敗:', error.message);
    ElMessage.error('文章保存失敗：' + error.message);
  } finally {
    loading.close();
  }
};
</script>

<style scoped>
.editor-background{
  background: linear-gradient(135deg, #e6f0fa 0%, #f5e6e8 100%);
  height: 100vh;
  padding-top: 40px;
}
.editor-container {
  max-width: 800px;
  margin: auto;
  margin-top: 40px;
  padding: 20px;
  background-color: #f0f9ff;
  border-radius: 8px;
  color: #606266;
  height: calc(70vh - 40px); 
  overflow-y: auto;
  border: 1px solid black;
}

.block {
  position: relative;
  margin-bottom: 16px;
  padding: 16px;
  background-color: #fff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  margin-top: 12px;
}

.drag-handle {
  cursor: move;
  margin-right: 8px;
  color: #409EFF;
}

.flex-row {
  display: flex;
  flex-direction: row;
  gap: 16px;
  margin-bottom: 16px;
}
</style>