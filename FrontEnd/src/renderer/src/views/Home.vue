<template>
  <div class="home-container">
    <!-- 1. 动态背景层 -->
    <iframe class="bg-iframe" src="/anim_bg/background.html" frameborder="0" scrolling="no"></iframe>

    <!-- 2. 核心大框 -->
    <div class="glass-container main-box">
      
      <!-- === 左侧区域：资源管理 === -->
      <div class="left-panel">
        
        <!-- 1. 用户信息区 -->
        <div class="user-profile-card glass-inner">
          <el-popover placement="bottom-start" :width="250" trigger="click" popper-class="glass-popover">
            <template #default>
              <!-- 状态 A：已设置 -->
              <div v-if="currentUser.username" class="popover-content">
                <div class="pop-avatar">
                  <img v-if="currentUser.avatarUrl" :src="currentUser.avatarUrl" />
                  <div v-else class="default-avatar">{{ currentUser.username ? currentUser.username.substring(0,1) : 'U' }}</div>
                </div>
                <h3 class="pop-name">{{ currentUser.username }}</h3>
                <p class="pop-email">{{ currentUser.email || '未绑定邮箱' }}</p>
                <el-button size="small" text bg @click="openProfileDialog">修改资料</el-button>
              </div>
              <!-- 状态 B：未设置 -->
              <div v-else class="popover-content">
                <p style="margin-bottom: 15px; color: #666;">您尚未设置个人信息</p>
                <el-button type="primary" @click="openProfileDialog">设置头像和名字</el-button>
              </div>
            </template>
            <template #reference>
              <div class="profile-trigger">
                <div class="mini-avatar">
                  <img v-if="currentUser.avatarUrl" :src="currentUser.avatarUrl" />
                  <el-icon v-else :size="24" color="#999"><UserFilled /></el-icon>
                </div>
                <div class="mini-info">
                  <span class="mini-name">{{ currentUser.username || '请设置' }}</span>
                  <span class="mini-status">在线</span>
                </div>
              </div>
            </template>
          </el-popover>
        </div>

        <!-- 2. 文本文件区 -->
        <div class="resource-box top-half glass-inner">
          <div class="box-header">
            <div class="header-title"><el-icon><Document /></el-icon><span>我的文本文件</span></div>
            <div class="header-actions">
              <el-button circle :icon="Plus" @click="handleAddText" />
              <el-button circle :icon="Edit" type="warning" plain @click="handleRenameText" />
              <el-button circle :icon="Delete" type="danger" plain @click="handleDeleteText" />
            </div>
            <!-- 隐藏的 input -->
            <input type="file" ref="textFileInputRef" accept=".txt,.docx,.doc" style="display: none" @change="handleTextFileUpload" />
          </div>
          <div class="file-grid" @click="clearAllSelection">
            <div 
              v-for="item in textFiles" :key="item.id" 
              class="file-item" 
              :class="{ active: selectedTextId === item.id }" 
              @click.stop="selectText(item.id)"
            >
              <el-icon class="file-icon" :size="40"><Document /></el-icon>
              <span class="file-name">{{ item.name }}</span>
            </div>
          </div>
        </div>

        <!-- 3. PPT文件区 -->
        <div class="resource-box bottom-half glass-inner">
          <div class="box-header">
            <div class="header-title"><el-icon><Monitor /></el-icon><span>我的 PPT 文件</span></div>
            <div class="header-actions">
              <el-button circle :icon="Plus" @click="handleAddPPT" />
              <el-button circle :icon="Edit" type="warning" plain @click="handleRenamePPT" />
              <el-button circle :icon="Delete" type="danger" plain @click="handleDeletePPT" />
            </div>
            <input type="file" ref="pptFileInputRef" accept=".ppt,.pptx" style="display: none" @change="handlePPTFileUpload" />
          </div>
          <div class="file-grid" @click="clearAllSelection">
            <div 
              v-for="item in pptFiles" :key="item.id" 
              class="file-item" 
              :class="{ active: selectedPPTId === item.id }" 
              @click.stop="selectPPT(item.id)"
            >
              <el-icon class="file-icon ppt-icon" :size="40"><DataBoard /></el-icon>
              <span class="file-name">{{ item.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- === 右侧区域：声音库 === -->
      <div class="right-panel">
        <div class="voice-box glass-inner">
          <div class="box-header">
            <div class="header-title"><el-icon><Microphone /></el-icon><span>我的声音库</span></div>
            <div class="header-actions-group">
              <el-button-group class="action-group">
                <el-button :icon="Plus" @click="openAddVoiceDialog">新增</el-button>
                <el-button :icon="Edit" @click="openRenameVoiceDialog">重命名</el-button>
                <el-button :icon="Delete" type="danger" plain @click="openDeleteVoiceDialog">删除</el-button>
              </el-button-group>
              <el-button class="export-btn-large" type="primary" size="large" @click="openExportDialog">
                <el-icon :size="20" style="margin-right: 6px"><Download /></el-icon>导出合成
              </el-button>
            </div>
            <input type="file" ref="voiceFileInputRef" accept=".mp3,.wav,.m4a" style="display: none" @change="handleVoiceFileChange" />
          </div>
          <div class="voice-grid-container" @click="clearAllSelection">
            <div 
              v-for="voice in voiceList" :key="voice.id" 
              class="voice-square" 
              :class="{ active: selectedVoiceId === voice.id }" 
              @click.stop="selectVoice(voice.id)"
            >
              <div class="voice-avatar"><span>{{ voice.name ? voice.name.substring(0, 1) : '?' }}</span></div>
              <span class="voice-name">{{ voice.name }}</span>
              <span v-if="voice.isDefault" class="tag">默认</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ================= 弹窗区域 ================= -->

    <!-- A. 设置个人资料弹窗 -->
    <el-dialog v-model="profileDialogVisible" title="设置个人资料" width="400px" align-center class="glass-dialog">
      <div class="profile-form">
        <div class="avatar-upload-area" @click="triggerAvatarInput">
          <img v-if="tempAvatarUrl" :src="tempAvatarUrl" class="avatar-preview" />
          <div v-else class="avatar-placeholder"><el-icon :size="30"><Plus /></el-icon><span>点击上传</span></div>
          <input type="file" ref="avatarInputRef" accept="image/*" style="display: none" @change="handleAvatarChange" />
        </div>
        <el-input v-model="tempUsername" placeholder="请输入您的昵称" :prefix-icon="User" size="large" style="margin-top: 20px" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="profileDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="isSavingProfile" @click="handleSaveProfile">保存设置</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- B. 导出设置弹窗 -->
    <el-dialog v-model="exportDialogVisible" title="导出合成" width="500px" align-center class="glass-dialog">
      <el-form label-position="top">
        <el-form-item label="1. 选择音色">
          <el-select v-model="exportForm.voiceId" placeholder="请选择音色" style="width: 100%">
            <el-option v-for="item in voiceList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="2. 选择内容">
          <el-tabs v-model="exportForm.fileType">
            <el-tab-pane label="文本文件" name="text">
              <el-select v-model="exportForm.fileId" placeholder="请选择文本" style="width: 100%">
                <el-option v-for="item in textFiles" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-tab-pane>
            <el-tab-pane label="PPT文件" name="ppt">
              <el-select v-model="exportForm.fileId" placeholder="请选择PPT" style="width: 100%">
                <el-option v-for="item in pptFiles" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-tab-pane>
          </el-tabs>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="isExporting" @click="handleConfirmExport">确认导出</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- C. 新增声音弹窗 -->
    <el-dialog v-model="addVoiceDialogVisible" title="新增声音" width="400px" align-center class="glass-dialog">
      <el-input v-model="newVoiceName" placeholder="请输入声音名称" :prefix-icon="Microphone" size="large" />
      <div class="add-voice-options">
        <el-button type="primary" class="option-btn" size="large" @click="startRecordingFlow"><el-icon style="margin-right:5px"><Mic /></el-icon> 直接录制声音</el-button>
        <el-button class="option-btn" size="large" @click="triggerVoiceFileUpload"><el-icon style="margin-right:5px"><FolderOpened /></el-icon> 选择声音文件</el-button>
      </div>
    </el-dialog>

    <!-- D. 录音中弹窗 -->
    <el-dialog v-model="recordDialogVisible" title="正在录音..." width="350px" align-center class="glass-dialog record-dialog" :close-on-click-modal="false" :show-close="false">
      <div class="record-animation">
        <div class="bars-container"><div class="wave-bar"></div><div class="wave-bar"></div><div class="wave-bar"></div><div class="wave-bar"></div><div class="wave-bar"></div></div>
        <p>正在收录麦克风声音...</p>
      </div>
      <template #footer>
        <span class="dialog-footer center-footer"><el-button @click="cancelRecording">取消</el-button><el-button type="primary" :loading="isSavingRecord" @click="confirmRecording">确定完成</el-button></span>
      </template>
    </el-dialog>

    <!-- E. 重命名弹窗 -->
    <el-dialog v-model="renameDialogVisible" title="重命名" width="350px" align-center class="glass-dialog">
      <el-input v-model="renameInput" placeholder="请输入新名称" />
      <template #footer><span class="dialog-footer"><el-button @click="renameDialogVisible = false">取消</el-button><el-button type="primary" @click="confirmRename">确定</el-button></span></template>
    </el-dialog>

    <!-- F. 删除确认弹窗 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="300px" align-center class="glass-dialog">
      <span>确定要删除选中的文件吗？此操作无法撤回。</span>
      <template #footer><span class="dialog-footer"><el-button @click="deleteDialogVisible = false">取消</el-button><el-button type="danger" @click="confirmDelete">确定删除</el-button></span></template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Delete, Edit, Document, Monitor, Download, Microphone, DataBoard, Mic, FolderOpened, UserFilled, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request' // 确保你的 request.js 配置正确

// =================== 0. 核心状态与排他性选择 ===================
const textFiles = ref([])
const pptFiles = ref([])
const voiceList = ref([])

const selectedTextId = ref(null)
const selectedPPTId = ref(null)
const selectedVoiceId = ref(null)

const clearAllSelection = () => {
  selectedTextId.value = null
  selectedPPTId.value = null
  selectedVoiceId.value = null
}

const selectText = (id) => { clearAllSelection(); selectedTextId.value = id }
const selectPPT = (id) => { clearAllSelection(); selectedPPTId.value = id }
const selectVoice = (id) => { clearAllSelection(); selectedVoiceId.value = id }

// =================== 1. 初始化加载 ===================
onMounted(() => {
  loadUserInfo()
  loadTextFiles()
  loadPPTFiles()
  loadVoiceList()
})

// =================== 2. 用户信息逻辑 (真实接口对接) ===================
const currentUser = reactive({ username: '', avatarUrl: '', email: '' })
const profileDialogVisible = ref(false)
const isSavingProfile = ref(false)
const tempUsername = ref('')
const tempAvatarUrl = ref('')
const tempAvatarFile = ref(null)
const avatarInputRef = ref(null)

const loadUserInfo = async () => {
  try {
    // 假设后端接口: GET /user/info
    const res = await request.get('/user/info')
    if (res) {
      currentUser.username = res.username
      currentUser.avatarUrl = res.avatar
      currentUser.email = res.email
    }
  } catch (error) {
    console.warn('获取用户信息失败(可能是后端未实现):', error)
  }
}

const openProfileDialog = () => {
  tempUsername.value = currentUser.username
  tempAvatarUrl.value = currentUser.avatarUrl
  profileDialogVisible.value = true
}

const triggerAvatarInput = () => { avatarInputRef.value.click() }

const handleAvatarChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    tempAvatarFile.value = file
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (ev) => { tempAvatarUrl.value = ev.target.result }
  }
}

const handleSaveProfile = async () => {
  if (!tempUsername.value) return ElMessage.warning('请输入名字')
  isSavingProfile.value = true
  
  try {
    const formData = new FormData()
    formData.append('username', tempUsername.value)
    if (tempAvatarFile.value) {
      formData.append('avatar', tempAvatarFile.value)
    }
    // 假设后端接口: POST /user/update-profile
    const res = await request.post('/user/update-profile', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    // 更新本地
    currentUser.username = res.username || tempUsername.value
    currentUser.avatarUrl = res.avatar || tempAvatarUrl.value
    profileDialogVisible.value = false
    ElMessage.success('设置成功')
  } catch (error) {
    ElMessage.error('保存失败，请检查后端接口')
  } finally {
    isSavingProfile.value = false
  }
}

// =================== 3. 文本文件逻辑 (假设后端实现了 /texts) ===================
const textFileInputRef = ref(null)

const loadTextFiles = async () => {
  try {
    // 假设接口: GET /texts
    const res = await request.get('/texts')
    textFiles.value = res.map(item => ({ id: item.id, name: item.name }))
  } catch (error) {
    console.warn('获取文本列表失败(后端可能未实现)')
  }
}

const handleAddText = () => textFileInputRef.value.click()

const handleTextFileUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    // 假设接口: POST /texts
    const res = await request.post('/texts', formData)
    textFiles.value.push({ id: res.id, name: res.name })
    ElMessage.success('文本上传成功')
  } catch (error) {
    ElMessage.error('上传失败，请检查后端')
  } finally {
    e.target.value = ''
  }
}

const handleRenameText = () => {
  if (!selectedTextId.value) return ElMessage.warning('请选择文本文件')
  const file = textFiles.value.find(f => f.id === selectedTextId.value)
  renameInput.value = file.name
  renameDialogVisible.value = true
}

const handleDeleteText = () => {
  if (!selectedTextId.value) return ElMessage.warning('请选择文本文件')
  deleteDialogVisible.value = true
}

// =================== 4. PPT文件逻辑 (真实接口) ===================
const pptFileInputRef = ref(null)

const loadPPTFiles = async () => {
  try {
    // 接口: GET /api/ppts
    const res = await request.get('/ppts')
    pptFiles.value = res.map(item => ({
      id: item.id,
      name: item.displayName || item.name
    }))
  } catch (error) {
    console.error('加载PPT失败', error)
  }
}

const handleAddPPT = () => pptFileInputRef.value.click()

const handlePPTFileUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (!/\.(ppt|pptx)$/i.test(file.name)) return ElMessage.error('只支持 .ppt 或 .pptx')

  const formData = new FormData()
  formData.append('file', file)
  
  try {
    // 接口: POST /api/ppts
    const res = await request.post('/ppts', formData)
    pptFiles.value.push({ id: res.id, name: res.displayName || res.name })
    ElMessage.success('PPT上传成功')
  } catch (error) {
    ElMessage.error('上传PPT失败')
  } finally {
    e.target.value = ''
  }
}

const handleRenamePPT = () => {
  if (!selectedPPTId.value) return ElMessage.warning('请选择PPT文件')
  const file = pptFiles.value.find(f => f.id === selectedPPTId.value)
  renameInput.value = file.name
  renameDialogVisible.value = true
}

const handleDeletePPT = () => {
  if (!selectedPPTId.value) return ElMessage.warning('请选择PPT文件')
  deleteDialogVisible.value = true
}

// =================== 5. 声音库逻辑 (真实接口) ===================
const addVoiceDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const newVoiceName = ref('')
const voiceFileInputRef = ref(null)
const mediaRecorder = ref(null)
const isSavingRecord = ref(false)

const loadVoiceList = async () => {
  try {
    const list = []
    // 1. 基础声音: GET /api/base-voices
    const baseRes = await request.get('/base-voices')
    if (baseRes) baseRes.forEach(v => list.push({ id: v.code, name: v.name, isDefault: true }))

    // 2. 用户声音: GET /api/audios
    const userRes = await request.get('/audios')
    if (userRes) userRes.forEach(v => list.push({ id: v.id, name: v.displayName || v.name, isDefault: false }))
    
    voiceList.value = list
  } catch (error) {
    console.error('加载声音失败', error)
  }
}

const openAddVoiceDialog = () => { newVoiceName.value = ''; addVoiceDialogVisible.value = true }

const startRecordingFlow = async () => {
  if (!newVoiceName.value) return ElMessage.warning('请先输入声音名称')
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    const recorder = new MediaRecorder(stream)
    const chunks = []
    recorder.ondataavailable = e => chunks.push(e.data)
    recorder.start()
    mediaRecorder.value = { stream, recorder, chunks }
    recordDialogVisible.value = true
  } catch (err) {
    ElMessage.error('无法调用麦克风')
  }
}

const confirmRecording = async () => {
  if (!mediaRecorder.value) return
  isSavingRecord.value = true
  mediaRecorder.value.recorder.stop()
  
  // 等待录音停止事件
  await new Promise(r => setTimeout(r, 500)) 

  const blob = new Blob(mediaRecorder.value.chunks, { type: 'audio/wav' })
  const file = new File([blob], 'recording.wav', { type: 'audio/wav' })
  
  await uploadVoiceFile(file)
  
  mediaRecorder.value.stream.getTracks().forEach(t => t.stop())
  recordDialogVisible.value = false
  addVoiceDialogVisible.value = false
  isSavingRecord.value = false
}

const cancelRecording = () => {
  if (mediaRecorder.value) mediaRecorder.value.stream.getTracks().forEach(t => t.stop())
  recordDialogVisible.value = false
}

const triggerVoiceFileUpload = () => {
  if (!newVoiceName.value) return ElMessage.warning('请先输入声音名称')
  voiceFileInputRef.value.click()
}

const handleVoiceFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    uploadVoiceFile(file)
    e.target.value = ''
  }
}

const uploadVoiceFile = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('displayName', newVoiceName.value)
  
  try {
    // 接口: POST /api/audios
    const res = await request.post('/audios', formData)
    voiceList.value.push({ id: res.id, name: res.displayName || res.name, isDefault: false })
    addVoiceDialogVisible.value = false
    ElMessage.success('声音添加成功')
  } catch (error) {
    ElMessage.error('上传声音失败')
  }
}

const openRenameVoiceDialog = () => {
  if (!selectedVoiceId.value) return ElMessage.warning('请选择声音')
  const voice = voiceList.value.find(v => v.id === selectedVoiceId.value)
  if (voice.isDefault) return ElMessage.warning('系统声音无法重命名')
  renameInput.value = voice.name
  renameDialogVisible.value = true
}

const openDeleteVoiceDialog = () => {
  if (!selectedVoiceId.value) return ElMessage.warning('请选择声音')
  const voice = voiceList.value.find(v => v.id === selectedVoiceId.value)
  if (voice.isDefault) return ElMessage.warning('系统声音无法删除')
  deleteDialogVisible.value = true
}

// =================== 6. 公共操作：重命名与删除 (真实逻辑) ===================
const renameDialogVisible = ref(false)
const renameInput = ref('')
const deleteDialogVisible = ref(false)

const confirmRename = async () => {
  if (!renameInput.value) return ElMessage.warning('名称不能为空')
  
  try {
    // 1. 重命名文本 (假设 PATCH /texts/{id}/name)
    if (selectedTextId.value) {
      await request.patch(`/texts/${selectedTextId.value}/name`, { name: renameInput.value })
      const file = textFiles.value.find(f => f.id === selectedTextId.value)
      file.name = renameInput.value
      ElMessage.success('文本重命名成功')
    }
    // 2. 重命名 PPT (PATCH /api/ppts/{id}/name)
    else if (selectedPPTId.value) {
      await request.patch(`/ppts/${selectedPPTId.value}/name`, { name: renameInput.value })
      const file = pptFiles.value.find(f => f.id === selectedPPTId.value)
      file.name = renameInput.value
      ElMessage.success('PPT重命名成功')
    }
    // 3. 重命名声音 (PATCH /api/audios/{id}/name)
    else if (selectedVoiceId.value) {
      await request.patch(`/audios/${selectedVoiceId.value}/name`, { name: renameInput.value })
      const voice = voiceList.value.find(v => v.id === selectedVoiceId.value)
      voice.name = renameInput.value
      ElMessage.success('声音重命名成功')
    }
    renameDialogVisible.value = false
  } catch (error) {
    ElMessage.error('重命名操作失败')
  }
}

const confirmDelete = async () => {
  try {
    // 1. 删除文本 (假设 DELETE /texts/{id})
    if (selectedTextId.value) {
      await request.delete(`/texts/${selectedTextId.value}`)
      textFiles.value = textFiles.value.filter(f => f.id !== selectedTextId.value)
      selectedTextId.value = null
      ElMessage.success('文本删除成功')
    }
    // 2. 删除 PPT (DELETE /api/ppts/{id})
    else if (selectedPPTId.value) {
      await request.delete(`/ppts/${selectedPPTId.value}`)
      pptFiles.value = pptFiles.value.filter(f => f.id !== selectedPPTId.value)
      selectedPPTId.value = null
      ElMessage.success('PPT删除成功')
    }
    // 3. 删除声音 (DELETE /api/audios/{id})
    else if (selectedVoiceId.value) {
      await request.delete(`/audios/${selectedVoiceId.value}`)
      voiceList.value = voiceList.value.filter(v => v.id !== selectedVoiceId.value)
      selectedVoiceId.value = null
      ElMessage.success('声音删除成功')
    }
    deleteDialogVisible.value = false
  } catch (error) {
    ElMessage.error('删除操作失败')
  }
}

// =================== 7. 导出合成 (真实逻辑) ===================
const exportDialogVisible = ref(false)
const isExporting = ref(false)
const exportForm = reactive({ voiceId: '', fileType: 'text', fileId: '' })

const openExportDialog = () => {
  if (selectedVoiceId.value) exportForm.voiceId = selectedVoiceId.value
  if (selectedTextId.value) {
    exportForm.fileType = 'text'
    exportForm.fileId = selectedTextId.value
  } else if (selectedPPTId.value) {
    exportForm.fileType = 'ppt'
    exportForm.fileId = selectedPPTId.value
  }
  exportDialogVisible.value = true
}

const handleConfirmExport = async () => {
  if (!exportForm.voiceId || !exportForm.fileId) return ElMessage.warning('请选择音色和文件')
  
  isExporting.value = true
  try {
    // 假设接口: POST /tts/generate
    // 返回值 res 包含 { downloadUrl: "..." }
    const res = await request.post('/tts/generate', {
      voiceId: exportForm.voiceId,
      fileId: exportForm.fileId,
      type: exportForm.fileType // 'text' 或 'ppt'
    })
    
    // 触发下载 ( Electron 环境中最好调用 ipcRenderer 触发主进程下载)
    // 这里先用简单的浏览器下载方式模拟
    if(res.downloadUrl) {
      window.open(res.downloadUrl, '_blank')
      ElMessage.success('合成成功，开始下载')
    } else {
      ElMessage.success('任务已提交，请等待')
    }
    
    exportDialogVisible.value = false
  } catch (error) {
    ElMessage.error('合成请求失败')
  } finally {
    isExporting.value = false
  }
}
</script>

<style scoped>
/* 样式基础 */
.home-container { position: relative; width: 100%; height: 100vh; overflow: hidden; background-color: #000; }
.bg-iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none; pointer-events: none; }
.main-box { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 90%; height: 90%; display: flex; padding: 30px; gap: 40px; box-sizing: border-box; }
.glass-container { background: rgba(255, 255, 255, 0.02); backdrop-filter: blur(2px); border-radius: 20px; border: 1px solid rgba(255, 255, 255, 0.3); box-shadow: 0 0 15px rgba(255, 255, 255, 0.1); }
.glass-inner { background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(3px); border-radius: 16px; border: 1px solid rgba(255, 255, 255, 0.4); transition: all 0.3s; box-shadow: inset 0 0 20px rgba(255, 255, 255, 0.05); }

/* 布局 */
.left-panel { flex: 1; display: flex; flex-direction: column; gap: 20px; }
.right-panel { flex: 1; display: flex; flex-direction: column; }
.resource-box { flex: 1; display: flex; flex-direction: column; padding: 15px; overflow: hidden; }
.voice-box { flex: 1; display: flex; flex-direction: column; padding: 20px; }

/* 用户资料卡片样式 */
.user-profile-card { height: 80px; display: flex; align-items: center; padding: 0 20px; cursor: pointer; }
.user-profile-card:hover { background: rgba(255, 255, 255, 0.1); }
.profile-trigger { display: flex; align-items: center; gap: 15px; width: 100%; }
.mini-avatar { width: 50px; height: 50px; border-radius: 50%; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1px solid rgba(255,255,255,0.5); }
.mini-avatar img { width: 100%; height: 100%; object-fit: cover; }
.mini-info { display: flex; flex-direction: column; }
.mini-name { color: white; font-weight: bold; font-size: 16px; text-shadow: 0 1px 2px rgba(0,0,0,0.5); }
.mini-status { font-size: 12px; color: #a4fa7a; margin-top: 4px; display: flex; align-items: center; }
.mini-status::before { content: ''; width: 6px; height: 6px; background: #a4fa7a; border-radius: 50%; margin-right: 4px; }

/* Popover 内容样式 */
.popover-content { text-align: center; padding: 10px; }
.pop-avatar { width: 80px; height: 80px; margin: 0 auto 10px; border-radius: 50%; overflow: hidden; border: 1px solid #eee; background: #f0f0f0; display: flex; align-items: center; justify-content: center; }
.pop-avatar img { width: 100%; height: 100%; object-fit: cover; }
.default-avatar { font-size: 30px; font-weight: bold; color: #999; }
.pop-name { margin: 5px 0; font-size: 18px; color: #333; }
.pop-email { color: #999; font-size: 13px; margin-bottom: 15px; }

/* 弹窗上传样式 */
.profile-form { display: flex; flex-direction: column; align-items: center; }
.avatar-upload-area { width: 100px; height: 100px; border-radius: 50%; border: 2px dashed #d9d9d9; cursor: pointer; overflow: hidden; display: flex; align-items: center; justify-content: center; transition: .3s; background: #fafafa; }
.avatar-upload-area:hover { border-color: #409EFF; }
.avatar-preview { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder { display: flex; flex-direction: column; align-items: center; color: #8c939d; font-size: 12px; }

/* 头部 Header */
.box-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; color: #fff; text-shadow: 0 2px 4px rgba(0,0,0,0.3); border-bottom: 1px solid rgba(255,255,255,0.2); padding-bottom: 10px; min-height: 40px; }
.header-title { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: bold; }
.header-actions { display: flex; gap: 8px; }
.header-actions-group { display: flex; align-items: center; gap: 20px; }
.export-btn-large { font-weight: bold; font-size: 16px; padding: 20px 25px; background-color: #50a7fd !important; border: none; box-shadow: 0 4px 15px rgba(64, 158, 255, 0.5); transition: 0.3s; }
.export-btn-large:hover { transform: translateY(-2px) scale(1.05); box-shadow: 0 6px 20px rgba(64, 158, 255, 0.7); }

/* 文件列表 */
.file-grid { display: flex; flex-wrap: wrap; gap: 15px; overflow-y: auto; padding: 5px; height: 100%; align-content: flex-start; }
.file-item { width: 100px; min-height: 110px; display: flex; flex-direction: column; align-items: center; justify-content: flex-start; padding-top: 10px; cursor: pointer; border-radius: 8px; border: 1px solid transparent; transition: 0.2s; color: #eee; position: relative; }
.file-item:hover { background: rgba(255,255,255,0.15); border-color: rgba(255,255,255,0.5); }
.file-item.active { background: rgba(64, 158, 255, 0.9); border: 1px solid #409EFF; color: white; z-index: 10; height: auto; box-shadow: 0 4px 12px rgba(0,0,0,0.3); }
.file-name { font-size: 13px; margin-top: 8px; text-align: center; width: 100%; text-shadow: 0 1px 2px rgba(0,0,0,0.5); padding: 0 4px; line-height: 1.4; word-break: break-all; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.file-item.active .file-name { -webkit-line-clamp: unset; line-clamp: unset; overflow: visible; padding-bottom: 10px; }

/* 声音库 */
.voice-grid-container { flex: 1; display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 20px; overflow-y: auto; padding: 10px; align-content: flex-start; }
.voice-square { min-height: 130px; display: flex; flex-direction: column; align-items: center; justify-content: flex-start; padding-top: 15px; cursor: pointer; position: relative; border-radius: 12px; transition: 0.2s; border: 1px solid transparent; }
.voice-square:hover { background: rgba(255,255,255,0.1); }
.voice-square.active { background: rgba(64, 158, 255, 0.9); border-color: #409EFF; z-index: 10; height: auto; box-shadow: 0 4px 15px rgba(64, 158, 255, 0.6); }
.voice-avatar { width: 65px; height: 65px; background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.4); backdrop-filter: blur(5px); border-radius: 12px; display: flex; align-items: center; justify-content: center; font-weight: bold; color: #fff; font-size: 26px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); flex-shrink: 0; }
.voice-square.active .voice-avatar { background: rgba(255,255,255,0.2); transform: scale(1.05); }
.voice-name { margin-top: 10px; font-size: 14px; font-weight: 500; color: #fff; text-shadow: 0 1px 3px rgba(0,0,0,0.6); text-align: center; line-height: 1.4; width: 100%; padding: 0 5px; word-break: break-all; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.voice-square.active .voice-name { -webkit-line-clamp: unset; line-clamp: unset; overflow: visible; padding-bottom: 10px; }
.tag { position: absolute; top: 5px; right: 5px; background: rgba(103, 194, 58, 0.8); backdrop-filter: blur(2px); color: white; font-size: 10px; padding: 2px 6px; border-radius: 4px; }

/* 弹窗样式 */
.add-voice-options { margin-top: 20px; display: flex; flex-direction: column; gap: 15px; align-items: center; }
.option-btn { width: 100%; height: 50px; font-size: 16px; margin-left: 0 !important; }
.record-animation { height: 140px; display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center; }
.bars-container { height: 50px; display: flex; align-items: center; margin-bottom: 20px; }
.wave-bar { display: inline-block; width: 6px; height: 20px; background-color: #409EFF; margin: 0 3px; border-radius: 3px; animation: wave 1s infinite ease-in-out; }
.wave-bar:nth-child(1) { animation-delay: 0s; }
.wave-bar:nth-child(2) { animation-delay: 0.2s; }
.wave-bar:nth-child(3) { animation-delay: 0.4s; }
.wave-bar:nth-child(4) { animation-delay: 0.6s; }
.wave-bar:nth-child(5) { animation-delay: 0.8s; }
@keyframes wave { 0%, 100% { height: 20px; } 50% { height: 50px; } }
.center-footer { display: flex; justify-content: center; gap: 20px; }
.file-grid::-webkit-scrollbar, .voice-grid-container::-webkit-scrollbar { width: 6px; }
.file-grid::-webkit-scrollbar-thumb, .voice-grid-container::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.3); border-radius: 3px; }
</style>