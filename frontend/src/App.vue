<script setup lang="ts">
import { provide } from 'vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
provide('authStore', authStore)
</script>

<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>