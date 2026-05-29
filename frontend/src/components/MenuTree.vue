<script setup lang="ts">
import { ElMenuItem, ElSubMenu, ElIcon } from 'element-plus'
import type { MenuItem } from '../api/permission'
import * as Icons from '@element-plus/icons-vue'

const props = defineProps<{
  menus: MenuItem[]
}>()

const getIconComponent = (name?: string) => {
  if (!name) return null
  return (Icons as Record<string, any>)[name] || null
}
</script>

<template>
  <template v-for="menu in props.menus" :key="menu.name">
    <ElSubMenu v-if="menu.children && menu.children.length > 0" :index="menu.path || menu.name">
      <template #title>
        <ElIcon v-if="getIconComponent(menu.icon)">
          <component :is="getIconComponent(menu.icon)" />
        </ElIcon>
        <span>{{ menu.title }}</span>
      </template>
      <MenuTree :menus="menu.children" />
    </ElSubMenu>
    <ElMenuItem v-else :index="menu.path" :route="{ path: menu.path }">
      <ElIcon v-if="getIconComponent(menu.icon)">
        <component :is="getIconComponent(menu.icon)" />
      </ElIcon>
      <template #title>{{ menu.title }}</template>
    </ElMenuItem>
  </template>
</template>
