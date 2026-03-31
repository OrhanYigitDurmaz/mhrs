FROM oven/bun:alpine
WORKDIR /app

# Copy package files
COPY package.json bun.lock ./

# Install ALL dependencies (dev server needs devDependencies like Vite and Svelte)
RUN bun install --frozen-lockfile

# Copy all source code
COPY . .

# Run SvelteKit sync to generate necessary files
RUN bun run prepare

# Expose the default Vite development port
EXPOSE 5173

# Run the dev server and bind to all network interfaces so Docker can route traffic to it
CMD ["bun", "run", "dev", "--host", "0.0.0.0"]
