<template>
  <div class="profile-container">
    <h3>信息設置</h3>
    <el-skeleton :rows="3" animated v-if="!user || loading" />
    <el-form v-else :model="user" :rules="formRules" ref="profileForm" label-width="120px">
      <el-form-item label="用戶名" prop="nickname">
        <el-input v-model="user.nickname" placeholder="請輸入用戶名" clearable />
      </el-form-item>
      <el-form-item label="綁定郵箱" prop="email">
        <el-input v-model="user.email" placeholder="請輸入郵箱" clearable />
      </el-form-item>
      <el-form-item label="自我介紹" prop="bio">
        <el-input
          v-model="user.bio"
          type="textarea"
          :rows="4"
          placeholder="請輸入自我介紹"
          maxlength="500"
          show-word-limit
          resize="vertical"
        />
      </el-form-item>
      <el-form-item label="IP屬地">
        <el-tooltip content="此為您最近登錄的地理位置，僅用於安全驗證。若使用 VPN，可能顯示不準確" placement="top">
          <span>{{ user.lastIpLocation || '加載中...' }}</span>
        </el-tooltip>
      </el-form-item>
      <el-form-item label="頭像">
        <el-upload
          action="#"
          :show-file-list="false"
          :auto-upload="false"
          :on-change="handleAvatarChange"
          :before-upload="beforeUpload"
          :disabled="uploading"
        >
          <el-button type="primary" :loading="uploading">上傳頭像</el-button>
        </el-upload>
        <el-image
          v-if="previewAvatar || user.avatar"
          :src="previewAvatar || user.avatar"
          style="width: 50px; height: 50px; border-radius: 50%; margin-top: 10px;"
          fit="cover"
          :preview-src-list="[previewAvatar || user.avatar]"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveProfile" :loading="uploading">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import PersonalMessageApi from '/src/api/PersonalMessageApi';

const props = defineProps({
  user: {
    type: Object,
    default: () => ({
      id: null,
      nickname: '',
      email: '',
      bio: '',
      avatar: '',
      lastIpLocation: ''
    })
  }
});

const emit = defineEmits(['update:user', 'save-success']);

const formRules = ref({
  nickname: [
    { required: false, message: '請輸入用戶名', trigger: 'blur' },
    { min: 1, max: 50, message: '用戶名長度在 1 到 50 個字符', trigger: 'blur' }
  ],
  email: [
    {
      validator: (rule, value, callback) => {
        if (!value) callback();
        else if (/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) callback();
        else callback(new Error('請輸入有效的郵箱地址'));
      },
      trigger: ['blur', 'change']
    }
  ],
  bio: [
    { required: false, message: '請輸入自我介紹', trigger: 'blur' },
    { max: 500, message: '自我介紹長度不能超過 500 個字符', trigger: 'blur' }
  ]
});

const previewAvatar = ref('');
const avatarFile = ref(null);
const uploading = ref(false);
const loading = ref(false);
const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error('請上傳圖片文件');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('圖片大小不能超過 2MB');
    return false;
  }
  return false;
};

const handleAvatarChange = (uploadFile) => {
  if (previewAvatar.value) URL.revokeObjectURL(previewAvatar.value);
  avatarFile.value = uploadFile.raw;
  previewAvatar.value = URL.createObjectURL(avatarFile.value);
};

const handleUpload = async () => {
  if (!avatarFile.value) return null;
  uploading.value = true;
  try {
    const { url, publicUrl } = await PersonalMessageApi.getPicturePresignedUrl(
      avatarFile.value.name,
      avatarFile.value.type
    );
    const uploadSuccess = await PersonalMessageApi.uploadFile(url, avatarFile.value);
    if (uploadSuccess) {
      return publicUrl;
    }
    throw new Error('上傳到 Backblaze B2 失敗');
  } catch (error) {
    ElMessage.error(`上傳失敗：${error.message || '請稍後重試，或聯繫支持團隊'}`);
    console.error('Upload error:', error);
    throw error;
  } finally {
    uploading.value = false;
  }
};

const saveProfile = async () => {
  if (!userId) {
    ElMessage.error('用戶ID無效，無法保存資料');
    return;
  }
  try {
    const updatedUser = { ...props.user };
    if (avatarFile.value) {
      const publicUrl = await handleUpload();
      if (publicUrl) updatedUser.avatar = publicUrl;
    }
    console.log('Saving profile:', updatedUser);
    await PersonalMessageApi.putPersonalInfo(updatedUser);
    ElMessage.success('保存成功');
    if (previewAvatar.value) {
      URL.revokeObjectURL(previewAvatar.value);
      previewAvatar.value = '';
    }
    avatarFile.value = null;
    emit('save-success');
    emit('update:user', updatedUser);
  } catch (error) {
    ElMessage.error(`保存失敗：${error.message || '請稍後重試，或聯繫支持團隊'}`);
  }
};

onUnmounted(() => {
  if (previewAvatar.value) URL.revokeObjectURL(previewAvatar.value);
});
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 16px;
}

h3 {
  font-size: 24px;
  color: #2daee6;
  margin-bottom: 20px;
  text-align: center;
}
</style>