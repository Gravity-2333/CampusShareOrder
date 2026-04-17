# CampusShareOrder Git 开发分支规范（后端）

本文档用于约束本项目后端开发中的 Git 使用方式，尤其用于给 AI 协作开发时提供统一分支规范。

目标：

- 保证 `develop` 作为主开发集成分支稳定可控
- 保证后端开发在独立分支内进行，不直接污染 `develop`
- 保证每个具体功能都在独立模块分支开发，便于协作、测试和回滚
- 保证最终发布路径清晰：`backend -> develop -> release/main`

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
- 未经验证的后端模块代码不直接开发在 `develop`
- 当前端或后端整体完成后，才将对应团队分支合并回 `develop`

### 1.4 `backend`

职责：

- 后端团队主开发分支
- 作为后端所有模块功能的集成分支

规则：

- 从最新的 `develop` 创建
- 本地和远程都必须存在同名 `backend`
- 后端开发人员不直接在 `develop` 写功能代码
- 所有后端模块分支都从 `backend` 创建
- 各模块测试通过后，先合并回 `backend`
- 当后端整体功能完成后，再统一从 `backend` 合并到 `develop`

### 1.5 模块分支

职责：

- 承载某一个具体后端功能模块的开发

命名规则：

- 分支名直接使用模块名，要求简洁、可辨认
- 推荐使用英文短名或拼音短名

示例：

- `auth`
- `user`
- `order`
- `complaint`
- `admin`
- `scheduler`
- `security`
- `credit`

规则：

- 每个模块开发前，必须从 `backend` 再创建一个新的模块分支
- 模块开发、调试、测试都在该模块分支完成
- 模块完成后合并回 `backend`
- 模块分支完成合并后可保留，也可视情况删除

## 2. 固定开发流程

### 2.1 初始化后端开发主分支

流程：

1. 先切到 `develop`
2. 同步本地 `develop` 与远程 `origin/develop`
3. 从最新 `develop` 创建 `backend`
4. 将本地 `backend` 推送到远程 `origin/backend`

目的：

- 保证后端主开发分支的起点与 `develop` 完全一致

### 2.2 开发某个后端功能模块

流程：

1. 切到 `backend`
2. 确认 `backend` 已同步到最新状态
3. 从 `backend` 创建新的模块分支
4. 在模块分支上开发、提交、测试
5. 测试通过后，将模块分支合并回 `backend`

规则：

- 模块开发绝不直接写在 `backend`
- 更不允许直接写在 `develop`

### 2.3 后端阶段性集成

流程：

1. 多个模块分支分别开发完成
2. 各模块依次合并回 `backend`
3. 在 `backend` 分支完成后端整体联调和后端侧测试

规则：

- `backend` 是后端集成分支，不是单个功能开发分支

### 2.4 后端完成后回合到 `develop`

流程：

1. 当后端全部模块已完成
2. `backend` 分支整体通过测试
3. 将 `backend` 合并到 `develop`
4. 再与前端在 `develop` 上进行联调

规则：

- 只有后端整体达到可集成状态，才允许 `backend -> develop`

### 2.5 发布流程

流程：

1. 前后端在 `develop` 上联调完成
2. 从 `develop` 创建 `release`
3. 在 `release` 上完成最终测试与发布确认
4. 将 `release` 合并到 `main`
5. 视需要将发布修正回流到 `develop`

## 3. AI 协作时必须遵守的规则

以后无论是谁使用 AI 辅助开发，都必须遵守以下规则：

1. AI 在开始写后端代码前，先确认当前所在分支
2. 如果要开发具体模块，AI 必须先从 `backend` 新建模块分支
3. AI 不允许直接在 `develop` 上写后端功能代码
4. AI 不允许直接在 `main` 或 `release` 上开发日常功能
5. AI 在模块完成后，只能先合并回 `backend`
6. AI 在没有明确要求时，不得擅自把 `backend` 合并到 `develop`
7. AI 在没有明确要求时，不得擅自创建 `release` 或修改 `main`
8. AI 在执行 Git 操作前，应先检查工作区是否干净
9. AI 不得使用破坏性命令，例如 `git reset --hard`
10. AI 在推送远程分支前，应明确分支名称和目标远程分支是否一致

## 4. 推荐操作模板

### 4.1 同步 `develop`

```bash
git checkout develop
git pull origin develop
```

### 4.2 创建并推送 `backend`

```bash
git checkout develop
git checkout -b backend
git push -u origin backend
```

### 4.3 从 `backend` 创建模块分支

```bash
git checkout backend
git pull origin backend
git checkout -b order
```

### 4.4 模块完成后合并回 `backend`

```bash
git checkout backend
git merge order
git push origin backend
```

### 4.5 后端整体完成后合并回 `develop`

```bash
git checkout develop
git pull origin develop
git merge backend
git push origin develop
```

### 4.6 发布时创建 `release`

```bash
git checkout develop
git pull origin develop
git checkout -b release
git push -u origin release
```

### 4.7 发布到 `main`

```bash
git checkout main
git pull origin main
git merge release
git push origin main
```

## 5. 本项目最终分支流向

固定流向如下：

```text
模块分支 -> backend -> develop -> release -> main
```

对应解释：

- 每个具体功能先进入模块分支
- 模块验证后进入 `backend`
- 后端整体完成后进入 `develop`
- 前后端联调完成后进入 `release`
- 发布确认后进入 `main`

## 6. 给 AI 的简化指令

以后给 AI 喂 Git 规范时，可直接使用下面这段：

```text
本项目 Git 规范如下：
1. develop 是主开发集成分支，对应远程 origin/develop。
2. backend 是后端团队主开发分支，从 develop 创建，并同步推送到远程。
3. 每次开发具体后端功能时，必须从 backend 再创建一个模块分支，分支名直接用模块名。
4. 功能开发、调试、测试都在模块分支完成。
5. 模块测试通过后，只能先合并回 backend。
6. 所有后端功能完成后，才允许将 backend 合并到 develop。
7. develop 与前端联调完成后，再从 develop 创建 release。
8. release 验收通过后，再合并到 main。
9. 不允许直接在 develop、release、main 上写日常后端功能代码。
10. 每次执行 Git 操作前，先检查当前分支和工作区状态；不要使用破坏性 Git 命令。
```

## 7. 一句话总结

本项目的后端开发必须严格遵守：

`功能开发在模块分支，模块合并到 backend，后端完成再合并到 develop，联调完成再发布到 release 和 main。`
