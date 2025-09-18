// WebSocket统一管理模块
// 使用方法：import { createWS } from '@/utils/ws'
// const ws = createWS({ onMessage, onClose, onError })

// 可通过环境变量或直接修改此处统一配置
const WS_HOST = 'ustoopg.cn'; // 使用自定义域名
const WS_PATH = '/ws/database-updates';

/**
 * 创建WebSocket连接
 * @param {Object} options
 *   - onMessage: 消息回调 (event)
 *   - onClose: 关闭回调 (event)
 *   - onError: 错误回调 (event)
 *   - autoReconnect: 是否自动重连（默认true）
 *   - reconnectInterval: 重连间隔ms（默认3000）
 * @returns {WebSocket} ws实例
 */
export function createWS(options = {}) {
  const {
    onMessage,
    onClose,
    onError,
    autoReconnect = true,
    reconnectInterval = 3000
  } = options;
  // 强制使用wss协议，适配cloudflare和https站点
  const wsProtocol = 'wss:';
  const wsUrl = `${wsProtocol}//${WS_HOST}${WS_PATH}`;
  let ws;
  let reconnectTimer = null;
  let shouldReconnect = autoReconnect; // 新增变量

  function connect() {
    ws = new window.WebSocket(wsUrl);
    ws.onmessage = onMessage;
    ws.onerror = (e) => {
      if (onError) onError(e);
    };
    ws.onclose = (e) => {
      if (onClose) onClose(e);
      if (shouldReconnect) {
        reconnectTimer = setTimeout(connect, reconnectInterval);
      }
    };
  }

  connect();

  ws._manualClose = () => {
    shouldReconnect = false; // 只改局部变量
    if (reconnectTimer) clearTimeout(reconnectTimer);
    ws.close();
  };

  return ws;
} 