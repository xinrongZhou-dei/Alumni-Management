import { ref, onBeforeUnmount } from 'vue'
import { createWS } from '@/utils/ws'

const ws = ref(null)
function initWebSocket() {
  ws.value = createWS({
    onMessage: (event) => {
      const msg = event.data;
      if (["chapter application created", "chapter application reviewed"].includes(msg)) {
        getApplications();
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}
onBeforeUnmount(() => {
  if (ws.value && ws.value._manualClose) ws.value._manualClose();
}) 