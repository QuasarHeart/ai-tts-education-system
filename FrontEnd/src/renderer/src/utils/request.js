// src/renderer/src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  // 这里写队友后端的地址。假设本地 SpringBoot 是 8080 端口
  // 配合 Vite 的代理解决跨域问题，让vue帮我们转发，防止浏览器拦截
  // 暂时先写 '/api'，代表所有请求都以 /api 开头
  baseURL: '/api', 
  timeout: 5000 // 请求超时时间：5秒
})

// 请求拦截器
// 在发送请求前做的事，这里自动带上 Token
service.interceptors.request.use(
  (config) => {
    // 假设登录后我们把 token 存在 localStorage 里
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token // 让每个请求都带上身份证明
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

//响应拦截器 (Response Interceptor)
//返回数据后会进入这里处理数据
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 假设后端返回格式是：{ code: 200, msg: "成功", data: {...} }
    // 如果 code 不是 200，说明业务出错（比如密码错误）
    if (res.code !== 200) {
      ElMessage.error(res.msg || '系统错误')
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      return res.data // 直接把 data 里的东西给前端组件
    }
  },
  (error) => {
    // 处理网络错误（比如断网、404、500）
    console.error('err' + error)
    ElMessage.error(error.message || '网络请求失败')//弹窗提示
    return Promise.reject(error)
  }
)

export default service