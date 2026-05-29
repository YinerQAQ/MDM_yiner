# Git 提交流程文档

本文档面向 MDM 项目团队，描述从零开始使用 Git 进行版本控制的完整流程。

---

## 一、前置准备

### 1.1 安装 Git

- Windows：从 [git-scm.com](https://git-scm.com/download/win) 下载安装包，按默认选项安装
- 安装后在终端执行 `git --version` 验证

### 1.2 配置用户信息

```powershell
# 全局配置（所有仓库生效）
git config --global user.name "你的用户名"
git config --global user.email "你的邮箱"

# 或仅对当前仓库配置
git config user.name "YinerQAQ"
git config user.email "yinerqianbei@outlook.com"
```

### 1.3 配置远程仓库

```powershell
# 查看当前远程仓库
git remote -v

# 添加远程仓库（首次）
git remote add origin https://github.com/YinerQAQ/MDM_yiner.git

# 修改远程仓库地址
git remote set-url origin https://github.com/YinerQAQ/MDM_yiner.git

# 使用 Token 认证推送（将用户名和 Token 嵌入 URL）
git remote set-url origin https://用户名:Token@github.com/YinerQAQ/MDM_yiner.git
```

---

## 二、日常提交流程

### 2.1 标准流程（四步法）

```powershell
# 第 1 步：查看当前状态
git status

# 第 2 步：添加文件到暂存区
git add -A                    # 添加所有变更
git add src/main/java/        # 仅添加指定目录
git add README.md             # 仅添加指定文件

# 第 3 步：提交到本地仓库
git commit -m "feat: 简要描述本次变更"

# 第 4 步：推送到远程仓库
git push origin master        # 推送到 master 分支
```

### 2.2 拉取远程更新

在推送之前，建议先拉取远程最新代码：

```powershell
# 拉取并合并远程更新
git pull origin master

# 如果本地和远程都有新提交，可能需要合并
git pull origin master --allow-unrelated-histories   # 允许不相关历史合并
```

---

## 三、常用命令速查表

| 命令 | 说明 |
|------|------|
| `git status` | 查看工作区和暂存区状态 |
| `git status --short` | 精简模式查看状态 |
| `git add -A` | 添加所有变更到暂存区 |
| `git add <file>` | 添加指定文件到暂存区 |
| `git commit -m "消息"` | 提交暂存区到本地仓库 |
| `git push origin <branch>` | 推送到远程分支 |
| `git pull origin <branch>` | 拉取远程分支并合并 |
| `git fetch origin` | 仅拉取远程信息（不合并） |
| `git log --oneline` | 查看精简提交历史 |
| `git diff` | 查看未暂存的修改内容 |
| `git diff --cached` | 查看已暂存的修改内容 |
| `git stash` | 暂存未提交的修改 |
| `git stash pop` | 恢复暂存的修改 |
| `git rm --cached <file>` | 从 Git 跟踪中移除文件（保留本地） |
| `git branch` | 查看本地分支列表 |
| `git branch -a` | 查看所有分支（含远程） |
| `git remote -v` | 查看远程仓库地址 |

---

## 四、提交信息规范

### 4.1 前缀说明

采用 [Conventional Commits](https://www.conventionalcommits.org/) 规范，提交信息格式为：

```
<类型>: <简要描述>

[可选的详细说明]
```

| 前缀 | 用途 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat: 新增数据导入导出功能` |
| `fix` | 修复 Bug | `fix: 修复非管理员用户 Access Denied 问题` |
| `docs` | 文档变更 | `docs: 更新 README 技术栈说明` |
| `style` | 代码格式（不影响逻辑） | `style: 统一缩进为 4 空格` |
| `refactor` | 重构（非新功能、非修复） | `refactor: 拆分 MainDataService 逻辑` |
| `perf` | 性能优化 | `perf: 优化主数据分页查询性能` |
| `test` | 测试相关 | `test: 添加 UserService 单元测试` |
| `chore` | 构建/工具变更 | `chore: 升级 Spring Boot 至 3.2.x` |
| `ci` | CI/CD 配置 | `ci: 配置 GitHub Actions 自动构建` |

### 4.2 示例

```powershell
git commit -m "feat: 新增模型审核页面与密级配置功能"
git commit -m "fix: 修复登录态 Token 过期后未跳转登录页"
git commit -m "docs: 创建 CHANGELOG 版本更新记录"
git commit -m "refactor: 抽取数据权限切面为独立模块"
```

---

## 五、常见问题解决

### 5.1 推送被拒绝（远程有新提交）

```powershell
# 错误信息：! [rejected] master -> master (fetch first)

# 解决方案：先拉取远程更新再推送
git pull origin master
git push origin master
```

### 5.2 合并冲突

```powershell
# 拉取时提示冲突
git pull origin master
# ... 提示 CONFLICT ...

# 1. 打开冲突文件，找到类似标记：
#    <<<<<<< HEAD
#    本地修改内容
#    =======
#    远程修改内容
#    >>>>>>> origin/master

# 2. 手动编辑保留正确的内容，删除冲突标记

# 3. 标记冲突已解决并提交
git add <冲突文件>
git commit -m "fix: 解决合并冲突"
git push origin master
```

### 5.3 误提交了不需要的文件（如 node_modules）

```powershell
# 1. 先添加到 .gitignore
echo "node_modules/" >> .gitignore

# 2. 从 Git 跟踪中移除（保留本地文件）
git rm -r --cached node_modules/

# 3. 提交
git add .gitignore
git commit -m "chore: 移除误提交的 node_modules 并更新 .gitignore"
git push origin master
```

### 5.4 暂存当前修改（切换分支前）

```powershell
# 暂存当前修改
git stash

# 切换分支处理其他事务
git checkout other-branch
# ... 处理完毕 ...

# 切回原分支并恢复暂存
git checkout master
git stash pop
```

### 5.5 远程仓库是空的，本地也没有初始化过

```powershell
cd your-project
git init
git add -A
git commit -m "feat: 项目初始化"
git branch -M main
git remote add origin https://github.com/user/repo.git
git push -u origin main
```

### 5.6 认证失败（403 错误）

```powershell
# 检查远程 URL 是否包含正确的认证信息
git remote -v

# 更新远程 URL（嵌入 Token）
git remote set-url origin https://用户名:Token@github.com/user/repo.git

# 注意：GitHub Fine-grained token 需要 contents:write 权限
# 推荐使用 Classic token 并勾选 repo 权限
```

---

## 六、分支管理基础

### 6.1 分支操作

```powershell
# 查看本地分支
git branch

# 查看所有分支（含远程）
git branch -a

# 创建新分支
git branch feature/new-page

# 切换分支
git checkout feature/new-page

# 创建并切换到新分支（简写）
git checkout -b feature/new-page

# 重命名当前分支
git branch -M main

# 删除本地分支（已合并）
git branch -d feature/new-page

# 删除远程分支
git push origin --delete feature/new-page
```

### 6.2 分支策略建议

| 分支 | 用途 | 命名示例 |
|------|------|----------|
| `master` / `main` | 生产稳定版本 | — |
| `develop` | 开发集成分支 | — |
| `feature/*` | 功能开发 | `feature/data-import` |
| `fix/*` | Bug 修复 | `fix/token-expired` |
| `release/*` | 发布准备 | `release/v1.3.0` |

### 6.3 典型功能开发流程

```powershell
# 1. 从 master 创建功能分支
git checkout master
git pull origin master
git checkout -b feature/data-export

# 2. 在功能分支上开发并提交
git add -A
git commit -m "feat: 新增数据导出功能"

# 3. 推送功能分支到远程
git push -u origin feature/data-export

# 4. 在 GitHub 上创建 Pull Request 合并到 master

# 5. 合并后删除功能分支
git checkout master
git pull origin master
git branch -d feature/data-export
git push origin --delete feature/data-export
```

---

## 七、.gitignore 配置参考

MDM 项目推荐配置：

```gitignore
# Java
*.class
*.jar
*.war
*.ear
target/
*.log
log.txt
log_err.txt

# Node
node_modules/
dist/
*.local

# IDE
.idea/
*.iml
.vscode/
.trae/

# OS
.DS_Store
Thumbs.db

# Env
*.env
.env.*

# Agent skills
.agents/
skills-lock.json
```

> **注意**：已被 Git 跟踪的文件不受 `.gitignore` 影响，需用 `git rm --cached <file>` 移除跟踪后才会生效。
