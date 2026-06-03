FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npx prisma generate
RUN npm run build
RUN npm prune --production

FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=5193
ENV WOKWI_PORT=5195

RUN apk add --no-cache postgresql-client

COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/prisma ./prisma
COPY --from=builder /app/entrypoint.sh ./entrypoint.sh

RUN chmod +x ./entrypoint.sh
RUN mkdir -p /app/exports /app/backups/db /app/backups/filesystem && chown -R node:node /app
USER node

EXPOSE 5193
EXPOSE 5195

ENTRYPOINT ["./entrypoint.sh"]
CMD ["node", "dist/src/main.js"]