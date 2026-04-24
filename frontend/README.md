# frontend

The frontend uses the page/component -> store -> api service -> live provider structure.
The mock provider has been removed, so local development now requires the backend service.

## Setup

```sh
npm install
cp .env.example .env
npm run dev
```

## Environment

- `VITE_API_BASE_URL=http://localhost:18080`
  Backend service address. If omitted, Vite proxies `/api` to `http://localhost:18080`.

## Test Accounts

- User: `13800000001 / 123456`
- User: `13800000002 / 123456`
- User: `13800000003 / 123456`
- Admin: `admin / 123456`

## Scripts

```sh
npm run dev
npm run build
npm run lint
```
