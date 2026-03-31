# Build stage
FROM oven/bun:alpine AS builder
WORKDIR /app

# Copy package files
COPY package.json bun.lock ./

# Install dependencies
RUN bun install --frozen-lockfile

# Copy source code
COPY . .

# Run SvelteKit sync to generate necessary files
RUN bun run prepare

# Build the application
RUN bun run build

# Production stage
FROM oven/bun:alpine AS runner
WORKDIR /app



# Copy package files
COPY package.json bun.lock ./

# Install production dependencies only
RUN bun install --frozen-lockfile --production

# Copy built files from builder
COPY --from=builder /app/build ./build
COPY --from=builder /app/.svelte-kit/output ./.svelte-kit/output



# Expose port (SvelteKit default)
EXPOSE 3000

# Run the production server with Bun
CMD ["bun", "run", "start"]
