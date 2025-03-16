FROM node:22 AS builder

WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN corepack enable && pnpm install --frozen-lockfile

COPY . ./

EXPOSE 3000
CMD ["pnpm", "run", "dev", "--host"]
