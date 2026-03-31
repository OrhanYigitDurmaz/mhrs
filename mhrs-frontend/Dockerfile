# Build stage
FROM oven/bun:alpine AS builder
WORKDIR /app

# Copy package files
COPY package.json bun.lock ./

# Install dependencies
RUN bun install --frozen-lockfile

# Copy source code
COPY . .

# Build the application
RUN bun run build

# Production stage
FROM oven/bun:alpine AS runner
WORKDIR /app

# Create non-root user
RUN addgroup --system --gid 1001 bun && \
    adduser --system --uid 1001 bun

# Copy package files
COPY package.json bun.lock ./

# Install production dependencies only
RUN bun install --frozen-lockfile --production

# Copy built files from builder
COPY --from=builder /app/build ./build
COPY --from=builder /app/.svelte-kit/output ./.svelte-kit/output
COPY --from=builder /app/node_modules/.prisma ./node_modules/.prisma 2>/dev/null || true

# Set ownership
RUN chown -R bun:bun /app

USER bun

# Expose port (SvelteKit default)
EXPOSE 3000

# Run the production server with Bun
CMD ["bun", "run", "start"]
