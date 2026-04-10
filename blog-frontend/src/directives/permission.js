import { useUserStore } from '@/stores/user'

export const permissionDirective = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const code = binding.value
    if (code && !userStore.hasPermission(code)) {
      el.parentNode?.removeChild(el)
    }
  }
}
