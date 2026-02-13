<template>
  <div class="login-container">
    <!-- 动态背景层：通过 iframe 加载 html 文件 -->
    <iframe
      class="bg-iframe"
      src="/anim_bg/background.html"
      frameborder="0"
      scrolling="no"
    ></iframe>

    <el-card class="box-card">
      <h2 class="title">AI 教学语音合成系统</h2>

      <el-tabs v-model="activeTab" stretch>

        <!-- === 登录面板 === -->
        <el-tab-pane label="用户登录" name="login" >
          <!-- ref="loginFormRef" 用于脚本中获取表单实例 -->
          <!-- :rules="rules" 绑定验证规则 -->
          <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-position="top" size="large">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="loginForm.email" placeholder="请输入邮箱" :prefix-icon="Message" />
            </el-form-item>

            <el-form-item label="验证码" prop="code">
              <div class="email-row">
                <el-input v-model="loginForm.code" placeholder="请输入邮箱验证码" :prefix-icon="Key" />
                <el-button type="success" plain :disabled="isCodeSent" @click="handleSendCode">
                  {{ isCodeSent ? `${countdown}s后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password :prefix-icon="Lock" />
            </el-form-item>

            <el-button   class="submit-btn" @click="handleLogin" :loading="isLoading">
              立即登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <!-- === 注册面板 === -->
        <el-tab-pane label="新用户注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-position="top" size="large">

            <el-form-item label="电子邮箱" prop="email">
              <div class="email-row">
                <el-input v-model="registerForm.email" placeholder="请输入邮箱" :prefix-icon="Message" />
                <el-button type="success" plain :disabled="isCodeSent" @click="handleSendCode">
                  {{ isCodeSent ? `${countdown}s后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="验证码" prop="code">
              <el-input v-model="registerForm.code" placeholder="请输入邮箱验证码" :prefix-icon="Key" />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="设置密码" show-password :prefix-icon="Lock" />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password :prefix-icon="Lock" />
            </el-form-item>

            <el-button class="submit-btn" @click="handleRegister" :loading="isLoading">
              注册并登录
            </el-button>
          </el-form>
        </el-tab-pane>

      </el-tabs>

      <!-- 这里新增一个调试按钮，用来直接进入主界面-->
      <div class="debug-area">
        <el-divider>调试通道</el-divider>
        <el-button type="warning" plain style="width: 100%" @click="handleDebugLogin">
          [调试] 跳过登录直接进主页
        </el-button>
      </div>
      <!--新增结束-->

    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, Message, Key, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
// 引入定义的 API
import { login, register, sendCode } from '../api/user'

const router = useRouter()
const activeTab = ref('login')
const isLoading = ref(false)

// 表单引用（用于调用 validate 方法）
const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 数据
const loginForm = reactive({ email: '', code: '', password: '' })
const registerForm = reactive({ email: '', code: '', password: '', confirmPassword: '' })

// 验证码倒计时状态
const isCodeSent = ref(false)
const countdown = ref(60)
let timer = null

// === 表单验证规则 ===
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = reactive({
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码长度在 6 到 16 之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '密码只能包含数字、字母和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位数字', trigger: 'blur' },
    { pattern: /^\d+$/, message: '验证码只能包含数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
})

// === 发送验证码 ===
const handleSendCode = async () => {
  // 单独验证邮箱字段
  let email = ''
  if (activeTab.value === 'login') {
    // 验证登录表单的邮箱
    const valid = await new Promise((resolve) => {
      loginFormRef.value.validateField('email', (error) => {
        resolve(!error)
      })
    })
    if (valid) {

      email = loginForm.email
    } else {
      ElMessage.warning('请先填写正确的邮箱')
      return
    }
  } else {
    // 验证注册表单的邮箱
    const valid = await new Promise((resolve) => {
      registerFormRef.value.validateField('email', (error) => {
        resolve(!error)
      })
    })
    if (valid) {
      email = registerForm.email
    } else {
      ElMessage.warning('请先填写正确的邮箱')
      return
    }
  }

  try {
    await sendCode(email) // 调用真实API
    ElMessage.success('验证码已发送')

    // 倒计时逻辑
    isCodeSent.value = true
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        isCodeSent.value = false
      }
    }, 1000)
  } catch (error) {
    // 错误已经在 request.js 处理过了，这里不用写
  }
}

// === 登录逻辑 ===
const handleLogin = async () => {
  if (!loginFormRef.value) return

  // 1. 校验表单
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      isLoading.value = true
      try {
        // 2. 调用后端 API
        const data = await login(loginForm)

        // 3. 登录成功处理
        ElMessage.success('登录成功')
        // 假设后端返回了 token，存起来
        if(data.token) localStorage.setItem('token', data.token)

        router.push('/home')

      } catch (error) {
        console.error('登录失败', error)
      } finally {
        isLoading.value = false
      }
    }
  })
}

// === 注册逻辑 ===
const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      isLoading.value = true
      try {
        // 只传递后端需要的字段
        const registerData = {
          email: registerForm.email,
          code: registerForm.code,
          password: registerForm.password
        }
        const data = await register(registerData)
        ElMessage.success('注册成功')
        if(data.token) localStorage.setItem('token', data.token)
        router.push('/home')
      } catch (error) {
        console.error('注册失败', error)
      } finally {
        isLoading.value = false
      }
    }
  })
}

//新增一个调试跳转函数
const handleDebugLogin = () => {
  ElMessage.warning('进入调试模式')
  router.push('/home')
}
</script>

<style scoped>
.login-container { position: relative; width: 100%; height: 100vh; display: flex; justify-content: center; align-items: center; overflow: hidden; background-color: #000; }

.bg-iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

/*新增样式*/
.debug-area {
  margin-top: 20px;
}

.box-card { width: 400px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); z-index: 1; }
.title { text-align: center; color: #303133; margin-bottom: 20px; font-weight: 600; }
.submit-btn {
  width: 100%;
  margin-top: 10px;
  font-weight: bold;
  background-color: lightskyblue;
  border: none;
  color: white;
  border-radius: 4px;
  padding: 12px;
  font-size: 16px;
  transition: all 0.3s ease;
}

.submit-btn:hover {

  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}
.email-row { display: flex; gap: 10px; }
</style>
