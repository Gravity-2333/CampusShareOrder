# frontend

当前前端已采用“页面/组件 -> store -> api 服务 -> 数据提供者”的分层结构，默认连接真实后端接口。

## 安装与启动

```sh
npm install
```

复制环境变量模板：

```sh
cp .env.example .env
```

开发模式启动：

```sh
npm run dev
```

## 环境变量

- `VITE_API_BASE_URL=http://localhost:8080`
  后端服务地址；未配置时开发环境会通过 Vite 代理访问本地后端。

## 常用账号

- 用户：`13800000001 / 123456`
- 用户：`13800000002 / 123456`
- 用户：`13800000003 / 123456`
- 管理员：`admin / admin123`

## 常用脚本

```sh
npm run dev
npm run build
npm run lint
```
