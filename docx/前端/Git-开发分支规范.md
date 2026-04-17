# CampusShareOrder Git 开发分支规范

本文档用于约束本项目后续开发中的 Git 使用方式，尤其用于给 AI 协作开发时提供统一分支规范。

目标：

- 保证 `develop` 作为主开发集成分支稳定可控
- 保证前端开发在独立分支内进行，不直接污染 `develop`
- 保证每个具体功能都在独立模块分支开发，便于协作、测试和回滚
- 保证模块分支的创建、提交、合并过程都能在 GitHub 上被清晰看到
- 保证最终发布路径清晰：`frontend -> develop -> release/main`

## 1. 分支职责

### 1.1 `main`

职责：

- 线上稳定分支
- 只接收已经完成联调、测试、确认可发布的代码

规则：

- 不直接在 `main` 上开发
- 不直接向 `main` 提交零散功能
- 只能从 `release` 或经过确认的发布流程合并进入

### 1.2 `release`

职责：

- 发布准备分支
- 用于发布前最终验收、修复和确认

规则：

- 当 `develop` 完成前后端联调并满足发布条件后，从 `develop` 创建 `release`
- 发布确认后，再将 `release` 合并到 `main`

### 1.3 `develop`

职责：

- 项目主开发分支
- 汇总前端、后端已经完成并通过测试的阶段性成果

规则：

- 本地 `develop` 必须始终跟踪远程 `origin/develop`
- 每次开始工作前，先同步 `develop`
- 未经验证的前端模块代码不直接开发在 `develop`
- 当前端整体完成后，才将 `frontend` 分支合并回 `develop`

### 1.4 `frontend`

职责：

- 前端团队主开发分支
- 作为前端所有模块功能的集成分支

规则：

- 从最新的 `develop` 创建
- 本地和远程都必须存在同名 `frontend`
- 前端开发人员不直接在 `develop` 写功能代码
- 所有前端模块分支都从 `frontend` 创建
- 各模块测试通过后，先合并回 `frontend`
- 当前端整体功能完成后，再统一从 `frontend` 合并到 `develop`

### 1.5 模块分支

职责：

- 承载某一个具体前端功能模块的开发

命名规则：

- 分支名直接使用模块名，要求简洁、可辨认
- 推荐使用英文短名或拼音短名

示例：

- `login`
- `order-list`
- `order-detail`
- `create-order`
- `my-orders`
- `complaint`
- `profile`
- `credit`

规则：

- 每个模块开发前，必须从 `frontend` 再创建一个新的模块分支
- 模块分支创建后，必须立即推送到远程，并建立对应的远程同名分支，不能只在本地存在
- 之所以必须推送到远程，是因为老师需要在 GitHub 上看到按模块开发、提交、合并的完整过程
- 模块开发、调试、测试都在该模块分支完成
- 模块分支按功能目标划分，不要求改动文件绝对隔离
- 为完成一个模块，可以联动修改相关页面、路由、`api`、`store`、`types`、公共组件以及必要的工具文件
- 模块完成后合并回 `frontend`
- 模块分支完成合并后可保留，也可视情况删除

## 2. 固定开发流程

### 2.1 初始化前端开发主分支

流程：

1. 先切到 `develop`
2. 同步本地 `develop` 与远程 `origin/develop`
3. 从最新 `develop` 创建 `frontend`
4. 将本地 `frontend` 推送到远程 `origin/frontend`

目的：

- 保证前端主开发分支的起点与 `develop` 完全一致

### 2.2 开发某个前端功能模块

流程：

1. 切到 `frontend`
2. 确认 `frontend` 已同步到最新状态
3. 从 `frontend` 创建新的模块分支
4. 立即将该模块分支推送到远程，建立远程同名分支
5. 在模块分支上开发、提交、测试
6. 测试通过后，将模块分支合并回 `frontend`

规则：

- 模块开发绝不直接写在 `frontend`
- 更不允许直接写在 `develop`
- 模块分支推送到远程属于正式流程的一部分，不可省略

### 2.3 前端阶段性集成

流程：

1. 多个模块分支分别开发完成
2. 各模块依次合并回 `frontend`
3. 在 `frontend` 分支完成前端整体联调和前端侧测试

规则：

- `frontend` 是前端集成分支，不是单个功能开发分支
- `frontend` 上应能清晰看到各模块分支合并回来的记录

### 2.4 前端完成后回合到 `develop`

流程：

1. 当前端全部模块已完成
2. `frontend` 分支整体通过测试
3. 将 `frontend` 合并到 `develop`
4. 再与后端在 `develop` 上进行联调

规则：

- 只有前端整体达到可集成状态，才允许 `frontend -> develop`

### 2.5 发布流程

流程：

1. 前后端在 `develop` 上联调完成
2. 从 `develop` 创建 `release`
3. 在 `release` 上完成最终测试与发布确认
4. 将 `release` 合并到 `main`
5. 视需要将发布修正回流到 `develop`

## 3. AI 协作时必须遵守的规则

以后无论是谁使用 AI 辅助开发，都必须遵守以下规则：

1. AI 在开始写前端代码前，先确认当前所在分支
2. 如果要开发具体模块，AI 必须先从 `frontend` 新建模块分支
3. 模块分支创建后，AI 必须立即推送到远程，并确认远程同名分支已存在
4. AI 不允许直接在 `develop` 上写前端功能代码
5. AI 不允许直接在 `main` 或 `release` 上开发日常功能
6. AI 在模块完成后，只能先合并回 `frontend`
7. AI 在没有明确要求时，不得擅自把 `frontend` 合并到 `develop`
8. AI 在没有明确要求时，不得擅自创建 `release` 或修改 `main`
9. AI 在执行 Git 操作前，应先检查工作区是否干净，或明确当前未提交改动是否属于本次任务
10. AI 不得使用破坏性命令，例如 `git reset --hard`
11. AI 在推送远程分支前，应明确分支名称和目标远程分支是否一致
12. AI 在开发模块时，应以功能目标为边界，而不是强行要求只修改单一目录
13. 如果某个模块需要联动修改页面、路由、`api`、`store`、`types`、公共组件，AI 可以在同一模块分支内一并完成
14. AI 在同步文档或流程规则到多个分支时，应确保各分支对应文档内容一致

## 4. 推荐操作模板

### 4.1 同步 `develop`

```bash
git checkout develop
git pull origin develop
```

### 4.2 创建并推送 `frontend`

```bash
git checkout develop
git checkout -b frontend
git push -u origin frontend
```

### 4.3 从 `frontend` 创建并推送模块分支

```bash
git checkout frontend
git pull origin frontend
git checkout -b order-detail
git push -u origin order-detail
```

### 4.4 在模块分支上提交开发结果

```bash
git checkout order-detail
git add .
git commit -m "feat: complete order detail module"
git push origin order-detail
```

### 4.5 模块完成后合并回 `frontend`

```bash
git checkout frontend
git pull origin frontend
git merge order-detail
git push origin frontend
```

### 4.6 前端整体完成后合并回 `develop`

```bash
git checkout develop
git pull origin develop
git merge frontend
git push origin develop
```

### 4.7 发布时创建 `release`

```bash
git checkout develop
git pull origin develop
git checkout -b release
git push -u origin release
```

### 4.8 发布到 `main`

```bash
git checkout main
git pull origin main
git merge release
git push origin main
```

## 5. 本项目最终分支流向

固定流向如下：

```text
模块分支 -> frontend -> develop -> release -> main
```

对应解释：

- 每个具体功能先进入模块分支
- 模块验证后进入 `frontend`
- 前端整体完成后进入 `develop`
- 前后端联调完成后进入 `release`
- 发布确认后进入 `main`

## 6. 给 AI 的简化指令

以后给 AI 喂 Git 规范时，可直接使用下面这段：

```text
本项目 Git 规范如下：
1. develop 是主开发集成分支，对应远程 origin/develop。
2. frontend 是前端团队主开发分支，从 develop 创建，并同步推送到远程。
3. 每次开发具体前端功能时，必须从 frontend 再创建一个模块分支，分支名直接用模块名。
4. 模块分支创建后，必须立即 push 到远程，不能只在本地存在，因为老师需要在 GitHub 上看到按模块开发和合并的过程。
5. 功能开发、调试、测试都在模块分支完成。
6. 模块分支按功能目标划分，不要求改动文件绝对隔离；为完成一个模块，可以联动修改相关页面、路由、api、store、types、公共组件。
7. 模块测试通过后，只能先合并回 frontend。
8. 所有前端功能完成后，才允许将 frontend 合并到 develop。
9. develop 与后端联调完成后，再从 develop 创建 release。
10. release 验收通过后，再合并到 main。
11. 不允许直接在 develop、release、main 上写日常前端功能代码。
12. 每次执行 Git 操作前，先检查当前分支和工作区状态；不要使用破坏性 Git 命令。
```

## 7. 一句话总结

本项目的前端开发必须严格遵守：

`功能开发在模块分支，模块分支必须同步到远程，模块按功能目标组织，模块合并到 frontend，前端完成再合并到 develop，联调完成再发布到 release 和 main。`
