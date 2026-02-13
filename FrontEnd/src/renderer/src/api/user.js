// src/renderer/src/api/user.js
import request from '../utils/request'

// 1. 登录接口
export function login(data) {
  return request({
    url: '/api/v1/auth/login',
    method: 'post',
    data // 包含 { email, code, password }
  })
}

// 2. 注册接口
export function register(data) {
  return request({
    url: '/api/v1/auth/register',
    method: 'post',
    data // 包含 { email, code, password }
  })
}

// 3. 发送验证码接口
export function sendCode(email) {
  return request({
    url: '/api/v1/auth/code',
    method: 'post',
    data: { email } // 包含 { email }
  })
}
