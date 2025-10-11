<template>
  <div class="action-bar" :style="{ gap: gap + 'px' }">
    <el-button
      v-if="showActions.like"
      size="small"
      :type="likeStatus.liked ? 'primary' : 'default'"
      @click="debouncedHandleLike"
    >
      <el-icon :size="16">
        <Star :style="{ color: likeStatus.liked ? '#409EFF' : '#606266' }" />
      </el-icon>
      <span>{{ likeStatus.liked ? '已點讚' : '點讚' }} {{ likeStatus.likes || 0 }}</span>
    </el-button>
    <el-button
      v-if="(showActions.favorite) && (userId != article.userId)"
      size="small"
      text
      @click="debouncedHandleFavorite"
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
    <div v-if="showActions.more" class="more-box">
      <el-button size="small" text @click.stop="toggleMenu">
        <el-icon><MoreFilled /></el-icon>
      </el-button>
      <div v-show="menuVisible" class="more-menu" @click.stop>
        <div class="more-item" @click="handleReport">舉報</div>
        <div class="more-item" @click="handleDislikeArticle">不喜歡文章</div>
        <div class="more-item" @click="handleDislikeAuthor">不喜歡作者</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { MoreFilled, Star } from '@element-plus/icons-vue';
import FavoriteService from '/src/api/favoriteApi';
import LikeService from '/src/api/likeApi';
import { debounce } from 'lodash';
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
  'report',
  'dislikeArticle',
  'dislikeAuthor',
  'toggleMenu'
]);

// 用來儲存點讚狀態和計數
const likeStatus = ref({
  liked: false,
  likes: 0
});
const menuVisible = ref(false);
const isFavorited = ref(false);
const userId = ref(localStorage.getItem('userId') || 1);

const checkFavorite = async () => {
  const favoriteableType = 'Article';
  try {
    const response = await FavoriteService.checkFavorite(props.article.id, favoriteableType);
    isFavorited.value = response;
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
    };

    if (isFavorited.value) {
      const response = await FavoriteService.removeFavorite(removeFavorite);
      if (response.code === 0) {
        ElMessage.success('取消收藏成功');
        isFavorited.value = false;
      } else {
        ElMessage.error('取消收藏失敗');
      }
    } else {
      const response = await FavoriteService.addFavorite(favorite);
      if (response.code === 1) {
        ElMessage.warning('已經收藏該內容');
      } else if (response.code === 0) {
        ElMessage.success('收藏成功');
        isFavorited.value = true;
      }
    }
  } catch (error) {
    ElMessage.error(`操作失敗: ${error.message || '請稍後重試'}`);
    console.error('收藏操作失敗:', error);
  }
  emit('favorite', props.article);
};

const checkLike = async () => {
  try {
    const response = await LikeService.checkLike(userId.value, props.article.id, 'ARTICLE');
    likeStatus.value = {
      liked: response.liked,
      likes: response.likeCount
    };
    emit('like', {
      ...props.article,
      liked: response.liked,
      likes: response.likeCount
    });
  } catch (error) {
    console.error('檢查點讚狀態失敗:', error);
    ElMessage.error('檢查點讚狀態失敗');
  }
};

const handleLike = async () => {
  try {
    const updatedLikeStatus = { ...likeStatus.value };
    if (likeStatus.value.liked) {
      await LikeService.unlike(userId.value, props.article.id, props.article.userId, 'ARTICLE');
      ElMessage.success('取消點讚成功');
      updatedLikeStatus.liked = false;
      updatedLikeStatus.likes -= 1;
    } else {
      await LikeService.like(userId.value, props.article.id, props.article.userId, 'ARTICLE', 'LIKE');
      ElMessage.success('點讚成功');
      updatedLikeStatus.liked = true;
      updatedLikeStatus.likes += 1;
    }
    likeStatus.value = updatedLikeStatus;
    emit('like', {
      ...props.article,
      liked: updatedLikeStatus.liked,
      likes: updatedLikeStatus.likes
    });
  } catch (error) {
    ElMessage.error(`點讚操作失敗: ${error.message || '請稍後重試'}`);
    console.error('點讚操作失敗:', error);
  }
};

const handleShare = () => {
  emit('share', props.article);
  ElMessage.success(`分享文章 ${props.article.id}`);
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

const toggleMenu = () => {
  menuVisible.value = !menuVisible.value;
  emit('toggleMenu', menuVisible.value);
};

const debouncedHandleLike = debounce(handleLike, 300);
const debouncedHandleFavorite = debounce(handleFavorite, 300);

onMounted(() => {
  checkFavorite();
  checkLike();
});
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
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