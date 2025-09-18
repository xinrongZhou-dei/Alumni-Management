<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>关注微信服务号</h3>
        <button class="close-btn" @click="$emit('close')">&times;</button>
      </div>
      
      <div class="modal-body">
        <div class="qr-section">
          <p class="instruction">请使用微信扫描下方二维码关注服务号</p>
          <div class="qr-container">
            <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="微信二维码" class="qr-code" />
            <div v-else class="qr-loading">加载中...</div>
          </div>
          <p class="alumni-info">校友ID: {{ alumniId }}</p>
        </div>
        
        <div class="action-section">
          <button class="verify-btn" @click="handleVerifyFollow" :disabled="verifying">
            {{ verifying ? '验证中...' : '我已关注' }}
          </button>
          <button class="cancel-btn" @click="$emit('close')">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getQRCode, verifyFollow } from '../api/wechat'
import { ElMessage } from 'element-plus'

const props = defineProps({
  alumniId: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['follow-success', 'close'])

const qrCodeUrl = ref('')
const verifying = ref(false)

// 生成二维码
async function generateQRCode() {
  try {
    const response = await getQRCode(props.alumniId)
    if (response.data && response.data.qrCodeUrl) {
      qrCodeUrl.value = response.data.qrCodeUrl
    } else {
      qrCodeUrl.value = ''
      ElMessage.error('获取二维码失败')
    }
  } catch (error) {
    console.error('获取二维码失败:', error)
    ElMessage.error('获取二维码失败，请刷新页面重试')
  }
}

// 验证关注状态
async function handleVerifyFollow() {
  verifying.value = true
  try {
    const response = await verifyFollow(props.alumniId)
    if (response.code === 0 && response.data.followed) {
      ElMessage.success('验证成功！正在为您跳转...')
      emit('follow-success')
    } else {
      ElMessage.warning('请先关注微信服务号，然后点击"我已关注"按钮')
    }
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error('验证失败，请检查网络连接后重试')
  } finally {
    verifying.value = false
  }
}

// 点击遮罩层关闭
function handleOverlayClick() {
  emit('close')
}

onMounted(() => {
  generateQRCode()
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #6b7280;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #374151;
}

.modal-body {
  padding: 24px;
}

.qr-section {
  text-align: center;
  margin-bottom: 24px;
}

.instruction {
  color: #374151;
  margin-bottom: 16px;
  font-size: 0.95rem;
}

.qr-container {
  margin: 16px 0;
  display: flex;
  justify-content: center;
}

.qr-code {
  width: 200px;
  height: 200px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.qr-loading {
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #6b7280;
  background: #f9fafb;
}

.alumni-info {
  color: #6b7280;
  font-size: 0.9rem;
  margin: 8px 0 0;
}

.action-section {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.verify-btn {
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 10px 20px;
  font-size: 0.95rem;
  cursor: pointer;
  min-width: 100px;
}

.verify-btn:hover:not(:disabled) {
  background: #1d4ed8;
}

.verify-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.cancel-btn {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 10px 20px;
  font-size: 0.95rem;
  cursor: pointer;
  min-width: 80px;
}

.cancel-btn:hover {
  background: #e5e7eb;
}
</style> 