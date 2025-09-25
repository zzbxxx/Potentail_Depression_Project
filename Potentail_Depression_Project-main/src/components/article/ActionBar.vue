<template>
  <div class="action-bar" :style="{ gap: gap + 'px' }">
    <el-button
      v-if="showActions.like"
      size="small"
      :type="article.liked ? 'primary' : 'default'"
      @click="handleLike"
    >
      赞同 {{ article.likes || 0 }}
    </el-button>
    <el-button
      v-if="showActions.favorite"
      size="small"
      text
      @click="handleFavorite"
    >
      <el-icon :size="16">
        <Star :style="{ color: isFavorited ? '#fadb14' : '#606266' }" />
      </el-icon>
      <span v-if="isFavorited">已收藏</span>
      <span v-else>收藏</span>
    </el-button>
    <el-button
      v-if="showActions.share"
      size="small"
      text
      @click="handleShare"
    >
      分享
    </el-button>
    <el-button
      v-if="showActions.love"
      size="small"
      text
      @click="handleLove"
    >
      喜欢 {{ article.loves || 0 }}
    </el-button>
    <div v-if="showActions.more" class="more-box">
      <el-button size="small" text @click.stop="toggleMenu">
        <el-icon><MoreFilled /></el-icon>
      </el-button>
      <div v-show="menuVisible" class="more-menu" @click.stop>
        <div class="more-item" @click="handleReport">举报</div>
        <div class="more-item" @click="handleDislikeArticle">不喜欢文章</div>
        <div class="more-item" @click="handleDislikeAuthor">不喜欢作者</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { MoreFilled, Star } from '@element-plus/icons-vue';
import FavoriteService from '/src/api/favoriteApi';

const props = defineProps({
  article: {
    type: Object,
    required: true
  },
  showActions: {
    type: Object,
    default: () => ({
      like: true,
      favorite: true,
      share: true,
      love: true,
      more: true
    })
  },
  gap: {
    type: Number,
    default: 8
  }
});

const emit = defineEmits([
  'like',
  'favorite',
  'share',
  'love',
  'report',
  'dislikeArticle',
  'dislikeAuthor',
  'toggleMenu'
]);

const menuVisible = ref(false);
const isFavorited = ref(false);

const checkFavorite = async () => {
  const favoriteableType = 'Article';
  try {
    const response = await FavoriteService.checkFavorite(props.article.id,favoriteableType);
    isFavorited.value = response; // 假設接口返回 true 或 false
  } catch (error) {
    console.error('檢查收藏狀態失敗:', error);
    ElMessage.error('檢查收藏狀態失敗');
  }
};

const handleFavorite = async () => {
  try {
    const favorite = {
      favoriteableId: props.article.id,
      favoriteableType: 'Article',
      category: '文章收藏'
    };
    const removeFavorite = {
      favoriteableId: props.article.id,
      favoriteableType: 'Article',
    }
    
    if (isFavorited.value) {
      // 已收藏，執行取消收藏
      const response = await FavoriteService.removeFavorite(removeFavorite);
      if (response.code === 0) {
        ElMessage.success('取消收藏成功');
        isFavorited.value = false; // 更新為未收藏狀態
      } else {
        ElMessage.error('取消收藏失敗');
      }
    } else {
      // 未收藏，執行添加收藏
      const response = await FavoriteService.addFavorite(favorite);
      if (response.code === 1) {
        ElMessage.warning('已經收藏該內容');
      } else if (response.code === 0) {
        ElMessage.success('收藏成功');
        isFavorited.value = true; // 更新為已收藏狀態
      }
    }
  } catch (error) {
    ElMessage.error(`操作失敗: ${error.message || '請稍後重試'}`);
    console.error('收藏操作失敗:', error);
  }
  emit('favorite', props.article);
};

const toggleMenu = () => {
  menuVisible.value = !menuVisible.value;
  emit('toggleMenu', menuVisible.value);
};

const handleLike = () => {
  emit('like', props.article);
};

const handleShare = () => {
  emit('share', props.article);
  ElMessage.success(`分享文章 ${props.article.id}`);
};

const handleLove = () => {
  emit('love', props.article);
  ElMessage.success('已喜欢~');
};

const handleReport = () => {
  emit('report', props.article);
  menuVisible.value = false;
};

const handleDislikeArticle = () => {
  emit('dislikeArticle', props.article);
  menuVisible.value = false;
};

const handleDislikeAuthor = () => {
  emit('dislikeAuthor', props.article);
  menuVisible.value = false;
};

// 初始化檢查收藏狀態
onMounted(() => {
  checkFavorite();
})

</script>

<style scoped>
.action-bar {
  display: flex;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
}
.more-box {
  position: relative;
  display: inline-block;
}
.more-menu {
  position: absolute;
  right: 0;
  bottom: 100%;
  margin-bottom: 4px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,.15);
  padding: 4px 0;
  z-index: 10;
  white-space: nowrap;
}
.more-item {
  padding: 6px 16px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
}
.more-item:hover {
  background: #f5f7fa;
}
</style>