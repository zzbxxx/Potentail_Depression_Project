<template>
  <div>
    <h3>信息设置</h3>
    <el-skeleton :rows="3" animated v-if="!user" />
    <el-form v-else :model="user" :rules="formRules" ref="profileForm" label-width="120px">
      <el-form-item label="用户名" prop="nickname">
        <el-input v-model="user.nickname" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="绑定邮箱" prop="email">
        <el-input v-model="user.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="头像">
        <el-upload
          action="#"
          :show-file-list="false"
          :auto-upload="false"
          :on-change="handleAvatarChange"
          :before-upload="beforeUpload"
          :disabled="uploading"
        >
          <el-button type="primary" :loading="uploading">上传头像</el-button>
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
        <el-button type="primary" @click="saveProfile">保存</el-button>
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
    default: null
  }
});

const emit = defineEmits(['update:user', 'save-success']);

const formRules = ref({
  nickname: [
    { required: false, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 50, message: '用户名长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  email: [
    {
      validator: (rule, value, callback) => {
        if (!value) callback();
        else if (/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) callback();
        else callback(new Error('请输入有效的邮箱地址'));
      },
      trigger: ['blur', 'change']
    }
  ],
});

const previewAvatar = ref('');
const avatarFile = ref(null);
const uploading = ref(false);

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error('请上传图片文件');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB');
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
  if (!avatarFile.value) return;
  uploading.value = true;
  try {
    const { url, publicUrl } = await PersonalMessageApi.getPicturePresignedUrl(avatarFile.value.name, avatarFile.value.type);
    const uploadSuccess = await PersonalMessageApi.uploadFile(url, avatarFile.value);
    if (uploadSuccess) {
      emit('update:user', { ...props.user, avatar: publicUrl });
    } else {
      throw new Error('上传到 Backblaze B2 失败');
    }
  } catch (error) {
    ElMessage.error(`上传失败：${error.message || '请稍后重试，或联系支持团队'}`);
    console.error('Upload error:', error);
  } finally {
    uploading.value = false;
  }
};

const saveProfile = async () => {
  try {
    if (avatarFile.value) await handleUpload();
    await PersonalMessageApi.putPersonalInfo(props.user);
    ElMessage.success('保存成功');
    if (previewAvatar.value) {
      URL.revokeObjectURL(previewAvatar.value);
      previewAvatar.value = '';
    }
    avatarFile.value = null;
    emit('save-success');
  } catch (error) {
    ElMessage.error(`保存失败：${error.message || '请稍后重试，或联系支持团队'}`);
  }
};

onUnmounted(() => {
  if (previewAvatar.value) URL.revokeObjectURL(previewAvatar.value);
});
</script>
