import type { Directive } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authDirective: Directive<HTMLElement, string> = {
  mounted(el, binding) {
    const authStore = useAuthStore()
    const perm = binding.value
    if (!perm) return
    if (!authStore.hasPermission(perm)) {
      el.style.display = 'none'
    }
  },
  updated(el, binding) {
    const authStore = useAuthStore()
    const perm = binding.value
    if (!perm) return
    if (!authStore.hasPermission(perm)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

export default authDirective
