<script lang="ts">
	import './layout.css';
	import { onMount } from 'svelte';
	import { Toaster } from 'svelte-sonner';
	import { auth } from '$lib/stores/auth.svelte';
	import { page } from '$app/stores';
	import { initKy } from '$lib/utils/ky';
	import favicon from '$lib/assets/favicon.svg';

	let { children } = $props();

	onMount(() => {
		// Initialize ky with auth token getter
		initKy(() => $auth.token);
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
	<title>MHRS - Merkezi Hekim Randevu Sistemi</title>
	<meta name="description" content="Merkezi Hekim Randevu Sistemi" />
</svelte:head>

{#if $page.url.pathname !== '/login' && $page.url.pathname !== '/register'}
	<div class="min-h-screen bg-slate-50">
		{@render children()}
	</div>
{:else}
	<div class="min-h-screen bg-gradient-to-br from-blue-600 to-blue-800">
		{@render children()}
	</div>
{/if}

<Toaster richColors position="top-right" />
