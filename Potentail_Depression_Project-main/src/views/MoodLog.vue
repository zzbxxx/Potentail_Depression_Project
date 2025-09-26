<template>
  <div class="MoodLog">
    <h1>时间日志</h1>
    <div class="content">
      <div class="left-panel">
        <CardLogCard
          v-if="currentView === 'card'"
          :date="selectedDate.value"
          :data="detailCard"
          @primary="fetchDetailedCardData"
          @removed="handleCardRemoved"
        />
        <p v-if="currentView === 'card' && !detailCard" class="no-data">這一天沒有卡片數據喔</p>
        <MoodLogCard v-if="currentView === 'mood' && moodHistoryData" :date="selectedDate" :mood-data="moodHistoryData" />
        <p v-if="shouldShowNoData" class="no-data">{{ noDataMessage }}</p>
        <div class="button-group">
          <el-button type="primary" @click="handleCardClick">卡片</el-button>
          <el-button type="success" @click="handleMessageClick">留言</el-button>
          <el-button type="info" @click="handleOtherClick" disabled>其他</el-button>
        </div>
      </div>
      <div class="right-panel">
        <MoodCalendar
          :mood-data-store="moodDataStore"
          :mood-history-store="moodHistoryStore"
          :marked-dates="markedDates"
          @date-change="handleDateChange"
        />
      </div>
    </div>
    <el-button type="primary" @click="goBack">返回首页</el-button>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { debounce } from 'lodash';
import CardLogCard from '/src/components/DateComponent/CardLogCard.vue';
import MoodLogCard from '/src/components/DateComponent/MoodLogCard.vue';
import MoodCalendar from '/src/components/DateComponent/MoodCalendar.vue';
import MoodApiService from '/src/api/moodApi.js';
import FavoriteService from '/src/api/favoriteApi';

const router = useRouter();
const selectedDate = ref(new Date().toISOString().split('T')[0]);
const moodData = ref(null);
const moodDataStore = ref({});
const moodHistoryStore = ref({});
const markedDates = ref([]);
const currentView = ref('card');
const moodHistoryData = ref(null);
const detailCard = ref(null);

const fetchCardData = async () => {
  try {
    const info = await MoodApiService.getBriefCardHistoryInfo();
    moodDataStore.value = info.reduce((acc, item) => {
      acc[item.date] = {
        id: item.id,
        quoteText: item.quoteText,
        author: item.author,
        bookTitle: item.bookTitle,
        tags: item.tags,
      };
      return acc;
    }, {});
    markedDates.value = info.map((item) => item.date);
    moodData.value = moodDataStore.value[selectedDate.value] || null;
  } catch (error) {
    console.error('API failed:', error);
    ElMessage.error('獲取卡片日志數據失敗，請稍後再試');
  }
};

const fetchMoodData = async () => {
  try {
    const data = await MoodApiService.getMoodHistoryInfo();
    moodHistoryStore.value = data.reduce((acc, item) => {
      acc[item.date] = {
        text: item.text,
        primaryMood: item.primaryMood,
        events: item.events,
      };
      return acc;
    }, {});
    moodHistoryData.value = moodHistoryStore.value[selectedDate.value] || null;
  } catch (error) {
    console.error('API failed (mood):', error);
    ElMessage.error('獲取情緒日志數據失敗，請稍後再試');
  }
};

const fetchDetailedCardData = debounce(async (date) => {
  try {
    if (!date) {
      console.error('Invalid date provided, using selectedDate.value:', selectedDate.value);
      date = selectedDate.value || new Date().toISOString().split('T')[0];
    }

    const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');
    const cardId = moodDataStore.value[date]?.id;
    console.log('Fetching card for date:', date, 'cardId:', cardId, 'userId:', userId);

    if (!userId || !cardId) {
      console.warn('Missing userId or cardId:', { userId, cardId });
      ElMessage.warning(`日期 ${date} 沒有對應的卡片數據`);
      detailCard.value = null;
      return;
    }

    const res = await MoodApiService.getCardByCardId(userId, cardId);
    console.log('API response from getCardByCardId:', res);

    if (!res || !res.id) {
      console.warn('Invalid or empty response from API:', res);
      const backupData = moodDataStore.value[date];
      if (backupData) {
        console.log('Using backup data from moodDataStore:', backupData);
        detailCard.value = {
          id: backupData.id,
          quoteText: backupData.quoteText,
          author: backupData.author,
          bookTitle: backupData.bookTitle,
          tags: backupData.tags,
          date,
          isFavorited: false,
        };
        try {
          const isFavorited = await FavoriteService.checkFavorite(cardId, 'CARD');
          detailCard.value.isFavorited = isFavorited;
        } catch (error) {
          console.error('Failed to check favorite status:', error);
          detailCard.value.isFavorited = false;
        }
      } else {
        console.warn('No backup data available for date:', date);
        detailCard.value = null;
        ElMessage.error('卡片數據無效或不存在');
      }
      return;
    }

    detailCard.value = { ...res, date };
    console.log('Updated detailCard:', detailCard.value);
  } catch (error) {
    console.error('Failed to fetch detailed card data:', error);
    ElMessage.error('獲取卡片詳情失敗，請稍後再試');
    detailCard.value = null;
  }
}, 300);

const handleCardRemoved = async (date) => {
  await fetchDetailedCardData(date);
};

onMounted(async () => {
  console.log('onMounted: selectedDate.value:', selectedDate.value);
  await fetchCardData();
  await fetchMoodData();
  await fetchDetailedCardData(selectedDate.value);
});

watch([selectedDate, moodHistoryStore], async ([newDate]) => {
  console.log('watch: selectedDate.value:', newDate);
  if (!newDate) {
    console.error('selectedDate is invalid:', newDate);
    selectedDate.value = new Date().toISOString().split('T')[0];
  }
  moodHistoryData.value = moodHistoryStore.value[newDate] || null;
  moodData.value = moodDataStore.value[newDate] || null;
  await fetchDetailedCardData(newDate);
});

const handleDateChange = async (formattedDate, cardId) => {
  selectedDate.value = formattedDate;
  moodData.value = moodDataStore.value[formattedDate] || null;
  moodHistoryData.value = moodHistoryStore.value[formattedDate] || null;
  await fetchDetailedCardData(formattedDate);
};

const goBack = () => {
  router.push('/main');
};

const handleCardClick = () => {
  currentView.value = 'card';
};

const handleMessageClick = () => {
  currentView.value = 'mood';
};

const handleOtherClick = () => {
  console.log('點擊了其他按鈕');
};

const shouldShowNoData = computed(() => {
  if (currentView.value === 'card' && !moodData.value && !detailCard.value) {
    return true;
  } else if (currentView.value === 'mood' && !moodHistoryData.value) {
    return true;
  }
  return false;
});

const noDataMessage = computed(() => {
  if (currentView.value === 'card' && !moodData.value && !detailCard.value) {
    return '這一天沒有卡片數據喔';
  } else if (currentView.value === 'mood' && !moodHistoryData.value) {
    return '這一天沒有心情數據喔';
  }
  return '';
});
</script>

<style scoped>

.MoodLog {
  background: linear-gradient(135deg, #e6f0fa 0%, #f5e6e8 100%);
  padding: 1rem;
  text-align: center;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 1200px;
  margin: 0 auto;
}

.left-panel {
  background-color: #ffffff;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  order: 2;
}

.right-panel {
  order: 1;
}

.no-data {
  color: #909399;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.button-group {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  margin-top: auto;
  flex-wrap: wrap;
}

.button-group .el-button {
  padding: 8px 12px;
  font-size: 0.9rem;
  min-width: 80px;
}

h1 {
  color: #303133;
  font-size: 1.5rem;
  margin-bottom: 1rem;
  font-family: 'PingFang SC', sans-serif;
}

.el-button {
  margin-top: 1rem;
  background-color: #67c23a;
  border-color: #67c23a;
  font-size: 0.9rem;
}

@media (min-width: 769px) {
  .content {
    flex-direction: row;
    justify-content: space-between;
  }
  
  .left-panel {
    flex: 1;
    min-height: 400px;
    order: 1;
  }
  
  .right-panel {
    flex: 1;
    order: 2;
  }
}

@media (max-width: 480px) {
  .MoodLog {
    padding: 0.5rem;
  }
  
  .left-panel {
    padding: 0.75rem;
  }
  
  .button-group {
    gap: 0.3rem;
  }
  
  .button-group .el-button {
    padding: 6px 10px;
    font-size: 0.8rem;
    min-width: 70px;
  }
  
  h1 {
    font-size: 1.2rem;
  }
  
  .no-data {
    font-size: 0.8rem;
  }
}
</style>