<template>
  <div>
    <h3>邮箱上传</h3>
    <el-form label-width="120px">
      <el-form-item label="上传邮箱文件">
        <el-upload
          action="#"
          :show-file-list="false"
          :http-request="handleEmailUpload"
          :before-upload="beforeEmailUpload"
          :disabled="emailUploading"
        >
          <el-button type="primary" :loading="emailUploading">上传邮箱文件</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="viewUploadedEmails">查看已上传邮箱</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import PersonalMessageApi from '/src/api/PersonalMessageApi';

const emailFile = ref(null);
const emailUploading = ref(false);

const beforeEmailUpload = (file) => {
  const isCsvOrTxt = file.type === 'text/csv' || file.type === 'text/plain';
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isCsvOrTxt) {
    ElMessage.error('请上传 CSV 或 TXT 文件');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('文件大小不能超过 5MB');
    return false;
  }
  emailFile.value = file;
  return false;
};

const handleEmailUpload = async () => {
  if (!emailFile.value) return;
  emailUploading.value = true;
  try {
    const { url, publicUrl } = await PersonalMessageApi.getEmailPresignedUrl(emailFile.value.name, emailFile.value.type);
    const uploadSuccess = await PersonalMessageApi.uploadFile(url, emailFile.value);
    if (uploadSuccess) ElMessage.success('邮箱文件上传成功！');
    else throw new Error('上传邮箱文件到 Backblaze B2 失败');
  } catch (error) {
    ElMessage.error(`上传失败：${error.message || '请稍后重试，或联系支持团队'}`);
    console.error('Email Upload error:', error);
  } finally {
    emailUploading.value = false;
  }
};

const viewUploadedEmails = async () => {
  try {
    const files = await PersonalMessageApi.listUploadedEmails();
    ElMessage.success(`已上传 ${files.length} 个邮箱文件`);
    console.log('Uploaded email files:', files);
  } catch (error) {
    ElMessage.error(`获取失败：${error.message || '请稍后重试，或联系支持团队'}`);
    console.error('List email files error:', error);
  }
};
</script>
