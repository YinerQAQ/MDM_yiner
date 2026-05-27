<script setup lang="ts">
import { provide } from 'vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
provide('authStore', authStore)
</script>

<template>
  <router-view />
</template>
