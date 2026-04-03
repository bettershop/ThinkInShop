<template>
    <div class="color-picker-container">
      <!-- 引入 Font Awesome 图标库（确保图标正常显示） -->
      <link
        rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css"
      >
  
      <!-- 颜色预览区 -->
      <div class="preview-section">
        <h3 class="section-title">
          <i class="fa fa-eye mr-2"></i>颜色预览
        </h3>
        <div 
          class="preview-box" 
          :style="{ background: currentColor, transition: 'background 0.3s ease' }"
        ></div>
      </div>
  
      <!-- 模式切换 -->
      <div class="mode-switch">
        <button 
          class="mode-btn"
          :class="{ active: isSolidMode }"
          @click="switchMode(true)"
        >
          <i class="fa fa-square mr-1"></i>纯色
        </button>
        <button 
          class="mode-btn"
          :class="{ active: !isSolidMode }"
          @click="switchMode(false)"
        >
          <i class="fa fa-random mr-1"></i>渐变
        </button>
      </div>
  
      <!-- 纯色选择面板 -->
      <div class="solid-panel" v-if="isSolidMode">
        <h3 class="section-title">
          <i class="fa fa-paint-brush mr-2"></i>纯色配置
        </h3>
        
        <!-- 颜色选择器画布 -->
        <div class="color-canvas-container">
          <div 
            class="color-canvas"
            @mousedown="startPickColor"
            @touchstart="startPickColor"
          >
            <div class="hue-overlay" :style="{ backgroundColor: `hsl(${currentHue}, 100%, 50%)` }"></div>
            <div class="saturation-overlay"></div>
            <div 
              class="picker-marker"
              :style="{ 
                left: `${currentSaturation}%`, 
                top: `${100 - currentValue}%`,
                borderColor: currentValue > 50 ? '#000' : '#fff'
              }"
            ></div>
          </div>
  
          <!-- 色相条 -->
          <div class="hue-bar-container">
            <div class="hue-bar" @mousedown="startPickHue" @touchstart="startPickHue">
              <div class="hue-gradient"></div>
              <div 
                class="hue-marker"
                :style="{ left: `${currentHue / 3.6}%` }"
              ></div>
            </div>
          </div>
        </div>
  
        <!-- 颜色值输入 -->
        <div class="color-value-inputs">
          <div class="input-group">
            <label>HEX</label>
            <div class="input-wrapper">
              <input 
                type="text" 
                v-model="hexValue" 
                @input="handleHexInput"
                placeholder="#RRGGBB"
              >
              <button @click="copyColorValue('hex')" class="copy-btn">
                <i class="fa fa-copy"></i>
              </button>
            </div>
          </div>
  
          <div class="input-group rgb-group">
            <label>RGB</label>
            <div class="rgb-inputs">
              <input 
                type="number" 
                v-model.number="rgbValue.r" 
                @input="handleRgbInput"
                min="0" 
                max="255"
                placeholder="R"
              >
              <input 
                type="number" 
                v-model.number="rgbValue.g" 
                @input="handleRgbInput"
                min="0" 
                max="255"
                placeholder="G"
              >
              <input 
                type="number" 
                v-model.number="rgbValue.b" 
                @input="handleRgbInput"
                min="0" 
                max="255"
                placeholder="B"
              >
            </div>
          </div>
        </div>
      </div>
  
      <!-- 渐变选择面板 -->
      <div class="gradient-panel" v-else>
        <h3 class="section-title">
          <i class="fa fa-sliders mr-2"></i>渐变配置
        </h3>
  
        <!-- 渐变颜色点 -->
        <div class="gradient-colors">
          <div v-for="(color, index) in gradientColors" :key="index" class="gradient-color-item">
            <input 
              type="color" 
              v-model="color.value"
              @input="updateGradient"
              class="color-picker-input"
            >
            <input 
              type="text" 
              v-model="color.value"
              @input="validateGradientHex(index)"
              class="color-hex-input"
            >
            <div class="position-control">
              <input 
                type="number" 
                v-model.number="color.position"
                @input="updateGradient"
                min="0" 
                max="100"
              >
              <span class="percent">%</span>
            </div>
            <button 
              class="remove-color-btn" 
              @click="removeGradientColor(index)"
              :disabled="gradientColors.length <= 2"
            >
              <i class="fa fa-times"></i>
            </button>
          </div>
  
          <button 
            class="add-color-btn" 
            @click="addGradientColor"
            :disabled="gradientColors.length >= 5"
          >
            <i class="fa fa-plus"></i> 添加颜色
          </button>
        </div>
  
        <!-- 渐变预览条 -->
        <div class="gradient-preview-bar">
          <div 
            class="gradient-bar"
            :style="{ background: gradientValue }"
          >
            <div 
              v-for="(color, index) in sortedGradientColors" 
              :key="index"
              class="gradient-marker"
              :style="{ 
                left: `${color.position}%`,
                backgroundColor: color.value
              }"
              @mousedown="startDragMarker(index, $event)"
              @touchstart="startDragMarker(index, $event)"
            ></div>
          </div>
        </div>
  
        <!-- 渐变角度控制 -->
        <div class="gradient-angle-control"  >
          <h4 class="control-title">
            渐变角度: <span class="angle-value">{{ gradientAngle }}°</span>
          </h4>
          <input 
            type="range" 
            min="0" 
            max="360" 
            v-model.number="gradientAngle"
            @input="updateGradient"
            class="angle-slider"
          >
  
          <!-- 角度可视化 -->
          <div class="angle-visualizer">
            <svg viewBox="0 0 100 100" class="angle-svg">
              <circle cx="50" cy="50" r="45" fill="none" stroke="#e5e7eb" stroke-width="2"></circle>
              <line 
                x1="50" 
                y1="50" 
                :x2="50 + 45 * Math.cos((gradientAngle - 90) * Math.PI / 180)" 
                :y2="50 + 45 * Math.sin((gradientAngle - 90) * Math.PI / 180)" 
                stroke="#3B82F6" 
                stroke-width="2" 
                stroke-linecap="round"
              ></line>
            </svg>
          </div>
        </div>
  
        <!-- 渐变代码 -->
        <div class="gradient-code">
          <h4 class="control-title">渐变代码</h4>
          <div class="code-wrapper">
            <input 
              type="text" 
              v-model="gradientValue" 
              readonly
              class="code-input"
            >
            <button @click="copyColorValue('gradient')" class="copy-btn">
              <i class="fa fa-copy"></i>
            </button>
          </div>
        </div>
      </div>
  
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button @click="resetColor" class="reset-btn">
          <i class="fa fa-refresh mr-1"></i> 重置
        </button>
        <button @click="confirmColor" class="confirm-btn">
          <i class="fa fa-check mr-1"></i> 确认选择
        </button>
      </div>
  
      <!-- 通知提示 -->
      <div 
        class="notification"
        :class="{ 
          show: showNotification,
          success: notificationType === 'success',
          error: notificationType === 'error'
        }"
      >
        <i :class="['fa', notificationType === 'success' ? 'fa-check-circle' : 'fa-times-circle', 'mr-2']"></i>
        {{ notificationText }}
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'ColorPicker',
    data() {
      return {
        // 基础状态
        isSolidMode: true, // 是否为纯色模式
        showNotification: false,
        notificationText: '',
        notificationType: 'success',
        
        // 纯色状态（HSV 模式）
        currentHue: 210,    // 色相 0-360
        currentSaturation: 100, // 饱和度 0-100%
        currentValue: 50,   // 明度 0-100%
        hexValue: '#3B82F6',
        rgbValue: { r: 59, g: 130, b: 246 },
        
        // 渐变状态
        gradientColors: [
          { value: '#3B82F6', position: 0 },
          { value: '#6366F1', position: 100 }
        ],
        gradientAngle: 0,
        draggingMarkerIndex: -1 // 正在拖拽的渐变标记索引
      }
    },
    computed: {
      // 当前显示的颜色（纯色/渐变）
      currentColor() {
        return this.isSolidMode ? this.hexValue : this.gradientValue
      },
      // 排序后的渐变颜色（按位置排序）
      sortedGradientColors() {
        return [...this.gradientColors].sort((a, b) => a.position - b.position)
      },
      // 渐变 CSS 值
      gradientValue() {
        const colorStops = this.sortedGradientColors
          .map(color => `${color.value} ${color.position}%`)
          .join(', ')
        return `linear-gradient(${this.gradientAngle}deg, ${colorStops})`
      }
    },
    methods: {
      // 切换纯色/渐变模式
      switchMode(isSolid) {
        this.isSolidMode = isSolid
        this.updateCurrentColor() // 切换后更新预览
      },
  
      // 开始选择颜色（颜色画布）
      startPickColor(e) {
        e.preventDefault()
        this.handleColorPick(e)
        // 绑定移动和松开事件
        document.addEventListener('mousemove', this.handleColorPick)
        document.addEventListener('mouseup', this.stopPickColor)
        document.addEventListener('touchmove', this.handleColorPick)
        document.addEventListener('touchend', this.stopPickColor)
      },
  
      // 处理颜色选择
      handleColorPick(e) {
        const canvas = document.querySelector('.color-canvas')
        const rect = canvas.getBoundingClientRect()
        // 获取触摸/鼠标位置
        const clientX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX
        const clientY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY
        
        // 计算相对百分比
        let saturation = ((clientX - rect.left) / rect.width) * 100
        let value = 100 - ((clientY - rect.top) / rect.height) * 100
        
        // 限制在 0-100 范围内
        saturation = Math.max(0, Math.min(100, Math.round(saturation)))
        value = Math.max(0, Math.min(100, Math.round(value)))
        
        // 更新颜色值
        this.currentSaturation = saturation
        this.currentValue = value
        this.updateCurrentColor()
      },
  
      // 停止选择颜色
      stopPickColor() {
        document.removeEventListener('mousemove', this.handleColorPick)
        document.removeEventListener('mouseup', this.stopPickColor)
        document.removeEventListener('touchmove', this.handleColorPick)
        document.removeEventListener('touchend', this.stopPickColor)
      },
  
      // 开始选择色相
      startPickHue(e) {
        e.preventDefault()
        this.handleHuePick(e)
        document.addEventListener('mousemove', this.handleHuePick)
        document.addEventListener('mouseup', this.stopPickHue)
        document.addEventListener('touchmove', this.handleHuePick)
        document.addEventListener('touchend', this.stopPickHue)
      },
  
      // 处理色相选择
      handleHuePick(e) {
        const hueBar = document.querySelector('.hue-bar')
        const rect = hueBar.getBoundingClientRect()
        const clientX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX
        
        // 计算色相值（0-360）
        let hue = ((clientX - rect.left) / rect.width) * 360
        hue = Math.max(0, Math.min(360, Math.round(hue)))
        
        this.currentHue = hue
        this.updateCurrentColor()
      },
  
      // 停止选择色相
      stopPickHue() {
        document.removeEventListener('mousemove', this.handleHuePick)
        document.removeEventListener('mouseup', this.stopPickHue)
        document.removeEventListener('touchmove', this.handleHuePick)
        document.removeEventListener('touchend', this.stopPickHue)
      },
  
      // 更新当前颜色（HSV 转 RGB/HEX）
      updateCurrentColor() {
        const rgb = this.hsvToRgb(this.currentHue, this.currentSaturation, this.currentValue)
        this.rgbValue = {
          r: Math.round(rgb.r),
          g: Math.round(rgb.g),
          b: Math.round(rgb.b)
        }
        this.hexValue = this.rgbToHex(this.rgbValue.r, this.rgbValue.g, this.rgbValue.b)
      },
  
      // 处理 HEX 输入
      handleHexInput() {
        const hex = this.hexValue.trim().replace(/^#/, '')
        if (/^[0-9A-Fa-f]{6}$/.test(hex)) {
          const rgb = this.hexToRgb(`#${hex}`)
          this.rgbValue = rgb
          const hsv = this.rgbToHsv(rgb.r, rgb.g, rgb.b)
          this.currentHue = hsv.h
          this.currentSaturation = hsv.s
          this.currentValue = hsv.v
        }
      },
  
      // 处理 RGB 输入
      handleRgbInput() {
        // 限制 RGB 值在 0-255
        const r = Math.max(0, Math.min(255, this.rgbValue.r || 0))
        const g = Math.max(0, Math.min(255, this.rgbValue.g || 0))
        const b = Math.max(0, Math.min(255, this.rgbValue.b || 0))
        
        this.rgbValue = { r, g, b }
        this.hexValue = this.rgbToHex(r, g, b)
        
        const hsv = this.rgbToHsv(r, g, b)
        this.currentHue = hsv.h
        this.currentSaturation = hsv.s
        this.currentValue = hsv.v
      },
  
      // 添加渐变颜色
      addGradientColor() {
        if (this.gradientColors.length >= 5) {
          this.showNotify('最多支持5种渐变颜色', 'error')
          return
        }
        
        // 生成随机颜色和中间位置
        const randomColor = `#${Math.floor(Math.random() * 16777215)
          .toString(16)
          .padStart(6, '0')}`
        let position = 50
        const positions = this.gradientColors.map(c => c.position)
        
        // 避免位置重复
        if (positions.includes(position)) {
          for (let i = 1; i < 100; i++) {
            if (!positions.includes(i)) {
              position = i
              break
            }
          }
        }
        
        this.gradientColors.push({ value: randomColor, position })
        this.updateGradient()
      },
  
     // 接前面的代码继续
     removeGradientColor(index) {
        if (this.gradientColors.length <= 2) {
          this.showNotify('至少需要保留两种渐变颜色', 'error')
          return
        }
        
        this.gradientColors.splice(index, 1)
        this.updateGradient()
      },
  
      // 验证渐变颜色的HEX格式
      validateGradientHex(index) {
        const color = this.gradientColors[index]
        if (!color) return
        
        // 清除非十六进制字符
        let hex = color.value.replace(/[^0-9A-Fa-f]/g, '')
        
        // 验证并格式化
        if (hex.length === 6) {
          this.$set(this.gradientColors, index, {
            ...color,
            value: `#${hex.toUpperCase()}`
          })
          this.updateGradient()
        } else if (hex.length < 6) {
          // 格式不正确时恢复之前的值
          this.$set(this.gradientColors, index, {
            ...color
          })
        }
      },
  
      // 开始拖拽渐变标记
      startDragMarker(index, e) {
        e.preventDefault()
        this.draggingMarkerIndex = index
        
        document.addEventListener('mousemove', this.handleDragMarker)
        document.addEventListener('mouseup', this.stopDragMarker)
        document.addEventListener('touchmove', this.handleDragMarker)
        document.addEventListener('touchend', this.stopDragMarker)
      },
  
      // 处理标记拖拽
      handleDragMarker(e) {
        if (this.draggingMarkerIndex === -1) return
        
        const bar = document.querySelector('.gradient-bar')
        const rect = bar.getBoundingClientRect()
        const clientX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX
        
        // 计算位置百分比
        let position = ((clientX - rect.left) / rect.width) * 100
        position = Math.max(0, Math.min(100, Math.round(position)))
        
        // 更新位置（使用$set确保响应式更新）
        this.$set(this.gradientColors, this.draggingMarkerIndex, {
          ...this.gradientColors[this.draggingMarkerIndex],
          position
        })
        
        this.updateGradient()
      },
  
      // 停止拖拽标记
      stopDragMarker() {
        this.draggingMarkerIndex = -1
        
        document.removeEventListener('mousemove', this.handleDragMarker)
        document.removeEventListener('mouseup', this.stopDragMarker)
        document.removeEventListener('touchmove', this.handleDragMarker)
        document.removeEventListener('touchend', this.stopDragMarker)
      },
  
      // 更新渐变显示
      updateGradient() {
        // 确保颜色点按位置排序
        this.gradientColors = [...this.gradientColors].sort((a, b) => a.position - b.position)
      },
  
      // 复制颜色值
      copyColorValue(type) {
        let text = type === 'hex' ? this.hexValue : this.gradientValue
        
        // 创建临时输入框用于复制
        const tempInput = document.createElement('input')
        document.body.appendChild(tempInput)
        tempInput.value = text
        tempInput.select()
        document.execCommand('copy')
        document.body.removeChild(tempInput)
        
        this.showNotify(`${type === 'hex' ? 'HEX颜色' : '渐变代码'}已复制`, 'success')
      },
  
      // 重置颜色
      resetColor() {
        // 重置纯色状态
        this.currentHue = 210
        this.currentSaturation = 100
        this.currentValue = 50
        this.hexValue = '#3B82F6'
        this.rgbValue = { r: 59, g: 130, b: 246 }
        
        // 重置渐变状态
        this.gradientColors = [
          { value: '#3B82F6', position: 0 },
          { value: '#6366F1', position: 100 }
        ]
        this.gradientAngle = 0
        
        this.showNotify('已重置为默认颜色', 'success')
      },
  
      // 确认选择
      confirmColor() {
        const colorInfo = this.isSolidMode 
          ? { 
              type: 'solid', 
              value: this.hexValue,
              rgb: this.rgbValue
            } 
          : { 
              type: 'gradient', 
              value: this.gradientValue,
              angle: this.gradientAngle,
              colors: this.gradientColors
            }
        
        // 可以通过事件将选择的颜色信息传递给父组件
        this.$emit('colorSelected', colorInfo)
        this.showNotify('颜色选择已确认', 'success')
      },
  
      // 显示通知
      showNotify(text, type = 'success') {
        this.notificationText = text
        this.notificationType = type
        this.showNotification = true
        
        // 3秒后自动隐藏
        setTimeout(() => {
          this.showNotification = false
        }, 3000)
      },
  
      // 颜色转换工具: HSV转RGB
      hsvToRgb(h, s, v) {
        h = Math.max(0, Math.min(360, h))
        s = Math.max(0, Math.min(100, s)) / 100
        v = Math.max(0, Math.min(100, v)) / 100
        
        let r, g, b
        const i = Math.floor(h / 60)
        const f = h / 60 - i
        const p = v * (1 - s)
        const q = v * (1 - f * s)
        const t = v * (1 - (1 - f) * s)
        
        switch (i % 6) {
          case 0: r = v; g = t; b = p; break
          case 1: r = q; g = v; b = p; break
          case 2: r = p; g = v; b = t; break
          case 3: r = p; g = q; b = v; break
          case 4: r = t; g = p; b = v; break
          case 5: r = v; g = p; b = q; break
        }
        
        return {
          r: r * 255,
          g: g * 255,
          b: b * 255
        }
      },
  
      // 颜色转换工具: RGB转HEX
      rgbToHex(r, g, b) {
        const toHex = (x) => {
          const hex = Math.round(x).toString(16)
          return hex.length === 1 ? '0' + hex : hex
        }
        return `#${toHex(r)}${toHex(g)}${toHex(b)}`.toUpperCase()
      },
  
      // 颜色转换工具: HEX转RGB
      hexToRgb(hex) {
        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
        return result ? {
          r: parseInt(result[1], 16),
          g: parseInt(result[2], 16),
          b: parseInt(result[3], 16)
        } : null
      },
  
      // 颜色转换工具: RGB转HSV
      rgbToHsv(r, g, b) {
        r /= 255
        g /= 255
        b /= 255
        
        const max = Math.max(r, g, b)
        const min = Math.min(r, g, b)
        let h, s, v = max
        
        const d = max - min
        s = max === 0 ? 0 : d / max
        
        if (max === min) {
          h = 0 // 灰色
        } else {
          switch (max) {
            case r: h = (g - b) / d + (g < b ? 6 : 0); break
            case g: h = (b - r) / d + 2; break
            case b: h = (r - g) / d + 4; break
          }
          h *= 60
        }
        
        return {
          h: Math.round(h),
          s: Math.round(s * 100),
          v: Math.round(v * 100)
        }
      }
    },
    // 清理事件监听
    beforeDestroy() {
      document.removeEventListener('mousemove', this.handleColorPick)
      document.removeEventListener('mouseup', this.stopPickColor)
      document.removeEventListener('touchmove', this.handleColorPick)
      document.removeEventListener('touchend', this.stopPickColor)
      
      document.removeEventListener('mousemove', this.handleHuePick)
      document.removeEventListener('mouseup', this.stopPickHue)
      document.removeEventListener('touchmove', this.handleHuePick)
      document.removeEventListener('touchend', this.stopPickHue)
      
      document.removeEventListener('mousemove', this.handleDragMarker)
      document.removeEventListener('mouseup', this.stopDragMarker)
      document.removeEventListener('touchmove', this.handleDragMarker)
      document.removeEventListener('touchend', this.stopDragMarker)
    }
  }
  </script>
  
  <style scoped>
  .color-picker-container {
    font-family: 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
    max-width: 500px;
    /* margin: 20px auto; */
    padding: 20px;
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
  
  /* 通用样式 */
  .section-title {
    color: #333;
    font-size: 16px;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
  }
  
  /* 预览区域 */
  .preview-section {
    margin-bottom: 25px;
  }
  
  .preview-box {
    width: 100%;
    height: 80px;
    border-radius: 6px;
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.1);
  }
  
  /* 模式切换 */
  .mode-switch {
    display: flex;
    gap: 10px;
    margin-bottom: 25px;
  }
  
  .mode-btn {
    flex: 1;
    padding: 10px;
    background: #f5f7fa;
    border: none;
    border-radius: 6px;
    color: #4e5969;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .mode-btn.active {
    background: #3b82f6;
    color: white;
  }
  
  .mode-btn:hover:not(.active) {
    background: #eef2f7;
  }
  
  /* 纯色面板样式 */
  .color-canvas-container {
    margin-bottom: 25px;
  }
  
  .color-canvas {
    width: 100%;
    height: 200px;
    position: relative;
    border-radius: 6px;
    overflow: hidden;
    cursor: pointer;
    margin-bottom: 10px;
  }
  
  .hue-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(to right, #fff, currentColor);
  }
  
  .saturation-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(to top, #000, transparent);
  }
  
  .picker-marker {
    position: absolute;
    width: 20px;
    height: 20px;
    border: 2px solid;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.1);
  }
  
  .hue-bar-container {
    width: 100%;
  }
  
  .hue-bar {
    width: 100%;
    height: 10px;
    border-radius: 5px;
    position: relative;
    cursor: pointer;
  }
  
  .hue-gradient {
    width: 100%;
    height: 100%;
    background: linear-gradient(to right, 
      hsl(0, 100%, 50%), 
      hsl(60, 100%, 50%), 
      hsl(120, 100%, 50%), 
      hsl(180, 100%, 50%), 
      hsl(240, 100%, 50%), 
      hsl(300, 100%, 50%), 
      hsl(360, 100%, 50%)
    );
    border-radius: 5px;
  }
  
  .hue-marker {
    position: absolute;
    top: 50%;
    width: 14px;
    height: 14px;
    background: currentColor;
    border: 2px solid white;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.2);
  }
  
  /* 颜色值输入区域 */
  .color-value-inputs {
    display: flex;
    flex-direction: column;
    gap: 15px;
    margin-bottom: 25px;
  }
  
  .input-group {
    display: flex;
    flex-direction: column;
    gap: 5px;
  }
  
  .input-group label {
    font-size: 14px;
    color: #6b7280;
  }
  
  .input-wrapper {
    display: flex;
    gap: 8px;
  }
  
  .input-group input {
    flex: 1;
    padding: 10px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 14px;
  }
  
  .input-group input:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
  }
  
  .rgb-group .rgb-inputs {
    display: flex;
    gap: 8px;
  }
  
  .rgb-group .rgb-inputs input {
    flex: 1;
  }
  
  .copy-btn {
    padding: 0 12px;
    background: #f5f7fa;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .copy-btn:hover {
    background: #eef2f7;
  }
  
  /* 渐变面板样式 */
  .gradient-colors {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-bottom: 25px;
  }
  
  .gradient-color-item {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .color-picker-input {
    width: 40px;
    height: 40px;
    padding: 0;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    cursor: pointer;
  }
  
  .color-hex-input {
    flex: 1;
    padding: 10px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 14px;
  }
  
  .position-control {
    display: flex;
    align-items: center;
    gap: 5px;
    width: 80px;
  }
  
  .position-control input {
    width: 100%;
    padding: 10px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 14px;
  }
  
  .percent {
    color: #6b7280;
  }
  
  .remove-color-btn {
    width: 36px;
    height: 36px;
    background: #fee2e2;
    border: 1px solid #fecaca;
    border-radius: 6px;
    color: #dc2626;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
  }
  
  .remove-color-btn:disabled {
    background: #f3f4f6;
    border-color: #e5e7eb;
    color: #9ca3af;
    cursor: not-allowed;
  }
  
  .add-color-btn {
    padding: 10px;
    background: #eff6ff;
    border: 1px dashed #93c5fd;
    border-radius: 6px;
    color: #2563eb;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    transition: all 0.2s ease;
  }
  
  .add-color-btn:disabled {
    background: #f3f4f6;
    border-color: #e5e7eb;
    color: #9ca3af;
    cursor: not-allowed;
  }
  
  /* 渐变预览条 */
  .gradient-preview-bar {
    margin-bottom: 25px;
  }
  
  .gradient-bar {
    height: 40px;
    border-radius: 6px;
    position: relative;
    box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.1);
  }
  
  .gradient-marker {
    position: absolute;
    top: 50%;
    width: 14px;
    height: 14px;
    border: 2px solid white;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.2);
    cursor: ew-resize;
  }
  
  /* 渐变角度控制 */
  .gradient-angle-control {
    margin-bottom: 25px;
  }
  
  .control-title {
    color: #333;
    font-size: 14px;
    margin-bottom: 10px;
  }
  
  .angle-value {
    color: #3b82f6;
    font-weight: 500;
  }
  
  .angle-slider {
    width: 100%;
    height: 6px;
    -webkit-appearance: none;
    appearance: none;
    background: #e5e7eb;
    border-radius: 3px;
    outline: none;
    margin-bottom: 15px;
  }
  
  .angle-slider::-webkit-slider-thumb {
    -webkit-appearance: none;
    appearance: none;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #3b82f6;
    cursor: pointer;
    border: 2px solid white;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  }
  
  .angle-visualizer {
    display: flex;
    justify-content: center;
  }
  
  .angle-svg {
    width: 120px;
    height: 120px;
  }
  
  /* 渐变代码区域 */
  .gradient-code {
    margin-bottom: 25px;
  }
  
  .code-wrapper {
    display: flex;
    gap: 8px;
  }
  
  .code-input {
    flex: 1;
    padding: 10px;
    background: #f9fafb;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-family: monospace;
    font-size: 14px;
    color: #111827;
  }
  
  /* 操作按钮 */
  .action-buttons {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }
  
  .reset-btn, .confirm-btn {
    padding: 10px 16px;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    transition: all 0.2s ease;
  }
  
  .reset-btn {
    flex: 1;
    background: #f3f4f6;
    border: 1px solid #e5e7eb;
    color: #4b5563;
  }
  
  .reset-btn:hover {
    background: #e5e7eb;
  }
  
  .confirm-btn {
    flex: 1;
    background: #3b82f6;
    border: 1px solid #3b82f6;
    color: white;
  }
  
  .confirm-btn:hover {
    background: #2563eb;
  }
  
  /* 通知提示 */
  .notification {
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 12px 20px;
    border-radius: 6px;
    color: white;
    font-weight: 500;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateX(120%);
    transition: transform 0.3s ease;
    z-index: 1000;
    display: flex;
    align-items: center;
  }
  
  .notification.show {
    transform: translateX(0);
  }
  
  .notification.success {
    background: #10b981;
  }
  
  .notification.error {
    background: #ef4444;
  }
  
  /* 响应式调整 */
  @media (max-width: 480px) {
    .color-picker-container {
      padding: 15px;
    }
    
    .gradient-color-item {
      flex-wrap: wrap;
    }
    
    .color-hex-input {
      flex-basis: 100%;
      order: 1;
    }
    
    .action-buttons {
      flex-direction: column;
    }
  }
  </style>