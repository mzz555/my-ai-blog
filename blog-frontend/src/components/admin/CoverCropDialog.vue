<template>
  <el-dialog
    :model-value="visible"
    title="裁剪封面（16:9）"
    width="600px"
    :close-on-click-modal="false"
    @closed="onClosed"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="crop-wrap">
      <img ref="imgRef" :src="previewUrl" alt="封面预览" />
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="uploading" @click="handleConfirm">
        确认裁剪
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick, onUnmounted } from 'vue'
import Cropper from 'cropperjs'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean,
  file: { type: File, default: null },
})
const emit = defineEmits(['update:visible', 'done'])

const imgRef = ref(null)
const previewUrl = ref('')
const uploading = ref(false)
let cropper = null

watch(
  () => props.visible,
  async (val) => {
    if (val && props.file) {
      previewUrl.value = URL.createObjectURL(props.file)
      await nextTick()
      if (!imgRef.value) return
      cropper = new Cropper(imgRef.value, {
        aspectRatio: 16 / 9,
        viewMode: 1,
      })
    } else {
      destroyCropper()
    }
  }
)

function destroyCropper() {
  if (cropper) { cropper.destroy(); cropper = null }
  if (previewUrl.value) { URL.revokeObjectURL(previewUrl.value); previewUrl.value = '' }
}

function onClosed() {
  destroyCropper()
}

async function handleConfirm() {
  if (!cropper) return
  uploading.value = true
  try {
    const blob = await new Promise((resolve, reject) =>
      cropper.getCroppedCanvas().toBlob((b) => {
        if (b) resolve(b)
        else reject(new Error('Canvas toBlob returned null'))
      }, 'image/jpeg', 0.9)
    )
    const res = await uploadImage(blob)
    emit('done', res.data)
    emit('update:visible', false)
  } catch {
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

onUnmounted(destroyCropper)
</script>

<style scoped>
.crop-wrap {
  height: 338px;
  background: #0C0C10;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.crop-wrap img { max-height: 338px; display: block; }
</style>
