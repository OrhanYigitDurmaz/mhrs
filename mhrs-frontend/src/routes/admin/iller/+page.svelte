<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { isAdmin } from '$lib/stores/auth.svelte';
	import { illerApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { Loader2, ArrowLeft, Plus, Trash2 } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';
	import type { Il } from '$lib/api';

	let cities = $state<Il[]>([]);
	let isLoading = $state(false);
	let isDeleting = $state<number | null>(null);
	let isAdding = $state(false);
	let newCityName = $state('');
	let isCheckingAuth = $state(true);

	onMount(async () => {
		isCheckingAuth = false;

		if (!$isAdmin) {
			goto('/', { replaceState: true });
			return;
		}
		await loadCities();
	});

	async function loadCities() {
		isLoading = true;
		try {
			cities = await illerApi.list();
		} catch (error) {
			console.error('Failed to load cities:', error);
			toast.error('İller yüklenirken hata oluştu');
		}
		isLoading = false;
	}

	async function handleAddCity() {
		if (!newCityName.trim()) {
			toast.error('İl adı boş olamaz');
			return;
		}

		isAdding = true;
		try {
			await illerApi.create({ adi: newCityName });
			await loadCities();
			newCityName = '';
			toast.success('İl eklendi');
		} catch (error) {
			console.error('Failed to add city:', error);
			toast.error('İl eklenirken hata oluştu');
		}
		isAdding = false;
	}

	async function handleDeleteCity(cityId: number) {
		if (!confirm('Bu ili silmek istediğinizden emin misiniz?')) {
			return;
		}

		isDeleting = cityId;
		try {
			await illerApi.delete(cityId);
			cities = cities.filter((c) => c.id !== cityId);
			toast.success('İl silindi');
		} catch (error) {
			console.error('Failed to delete city:', error);
			toast.error('İl silinirken hata oluştu');
		}
		isDeleting = null;
	}
</script>

<Navbar />

{#if isCheckingAuth || !browser}
	<div class="flex min-h-[60vh] items-center justify-center">
		<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
	</div>
{:else}
	<div class="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Header -->
		<div class="mb-8 flex items-center gap-4">
			<Button variant="ghost" href="/admin">
				<ArrowLeft class="mr-2 h-4 w-4" />
				Admin Panel
			</Button>
			<div>
				<h1 class="text-3xl font-bold text-slate-900">İl Yönetimi</h1>
				<p class="mt-2 text-slate-600">Sistemdeki illeri yönetin.</p>
			</div>
		</div>

		<!-- Add City Form -->
		<Card class="mb-8">
			<CardHeader>
				<CardTitle>Yeni İl Ekle</CardTitle>
			</CardHeader>
			<CardContent>
				<form onsubmit={(e) => { e.preventDefault(); handleAddCity(); }} class="flex gap-4">
					<div class="flex-1">
						<Label for="cityName">İl Adı</Label>
						<Input
							id="cityName"
							bind:value={newCityName}
							placeholder="Örn: İstanbul"
							disabled={isAdding}
						/>
					</div>
					<div class="flex items-end">
						<Button type="submit" disabled={isAdding}>
							{#if isAdding}
								<Loader2 class="mr-2 h-4 w-4 animate-spin" />
								Ekleniyor...
							{:else}
								<Plus class="mr-2 h-4 w-4" />
								İl Ekle
							{/if}
						</Button>
					</div>
				</form>
			</CardContent>
		</Card>

		<!-- Cities List -->
		<Card>
			<CardHeader>
				<CardTitle>Kayıtlı İller ({cities.length})</CardTitle>
			</CardHeader>
			<CardContent>
				{#if isLoading}
					<div class="flex min-h-[200px] items-center justify-center">
						<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
					</div>
				{:else if cities.length === 0}
					<div class="py-8 text-center text-slate-500">
						Henüz il eklenmemiş.
					</div>
				{:else}
					<div class="space-y-2">
						{#each cities as city}
							<div class="flex items-center justify-between rounded-md border p-4">
								<span class="font-medium">{city.adi}</span>
								{#if isDeleting === city.id}
									<Loader2 class="h-4 w-4 animate-spin text-slate-400" />
								{:else}
									<Button
										variant="ghost"
										size="sm"
										onclick={() => handleDeleteCity(city.id)}
										class="text-red-600 hover:text-red-700 hover:bg-red-50"
									>
										<Trash2 class="h-4 w-4" />
									</Button>
								{/if}
							</div>
						{/each}
					</div>
				{/if}
			</CardContent>
		</Card>
	</div>
{/if}
