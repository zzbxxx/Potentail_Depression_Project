<template>
  <div ref="editorRef" class="quill-editor"></div>
  <div class="voice-input-container">
    <el-button
      :type="isRecording ? 'danger' : 'primary'"
      @click="toggleVoiceInput"
      :disabled="!isSpeechSupported"
      class="voice-btn"
    >
      {{ isRecording ? '停止錄音' : '開始語音輸入' }}
    </el-button>
    <el-alert
      v-if="!isSpeechSupported"
      title="您的瀏覽器不支持語音輸入功能，請使用 Chrome 或 Edge 等支持 Web Speech API 的瀏覽器"
      type="warning"
      :closable="false"
      class="mt-2"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';
import { ElButton, ElAlert } from 'element-plus';

const props = defineProps({ modelValue: String });
const emit = defineEmits(['update:modelValue']);
const editorRef = ref(null);
const isRecording = ref(false);
const isSpeechSupported = ref(!!(window.SpeechRecognition || window.webkitSpeechRecognition));
let quill;
let recognition;

// Initialize Quill editor
onMounted(() => {
  quill = new Quill(editorRef.value, {
    theme: 'snow',
    modules: { toolbar: [['bold', 'italic', 'underline'], ['link']] }
  });
  quill.root.innerHTML = props.modelValue;
  quill.on('text-change', () => emit('update:modelValue', quill.root.innerHTML));

  // Initialize SpeechRecognition if supported
  if (isSpeechSupported.value) {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    recognition = new SpeechRecognition();
    recognition.lang = 'zh-TW';
    recognition.continuous = true;
    recognition.interimResults = true; 

    recognition.onresult = (event) => {
      let interimTranscript = '';
      let finalTranscript = '';

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;
        if (event.results[i].isFinal) {
          finalTranscript += transcript;
        } else {
          interimTranscript += transcript;
        }
      }

      // Append final transcript to Quill editor
      if (finalTranscript) {
        const currentContent = quill.root.innerHTML;
        quill.root.innerHTML = currentContent === '<p><br></p>' ? finalTranscript : currentContent + finalTranscript;
        emit('update:modelValue', quill.root.innerHTML);
      }
    };

    recognition.onerror = (event) => {
      ElMessage.error('語音輸入錯誤: ' + event.error);
      isRecording.value = false;
    };

    recognition.onend = () => {
      isRecording.value = false;
    };
  }
});

// Watch for external modelValue changes
watch(() => props.modelValue, (val) => {
  if (quill && quill.root.innerHTML !== val) quill.root.innerHTML = val;
});

// Toggle voice input
const toggleVoiceInput = () => {
  if (!isSpeechSupported.value) return;

  if (isRecording.value) {
    recognition.stop();
    isRecording.value = false;
  } else {
    try {
      recognition.start();
      isRecording.value = true;
    } catch (error) {
      ElMessage.error('無法啟動語音輸入: ' + error.message);
    }
  }
};
</script>

<style scoped>
.quill-editor {
  background-color: #fff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  min-height: 100px;
}
.voice-input-container {
  margin-top: 8px;
}
.voice-btn {
  width: 100%;
}
.mt-2 {
  margin-top: 8px;
}
</style>