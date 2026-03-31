<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { isAdmin } from '$lib/stores/auth.svelte';
	import { illerApi, ilcelerApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { Loader2, ArrowLeft, Plus, Trash2, MapPin } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';
	import type { Il, Ilce } from '$lib/api';

	let cities = $state<Il[]>([]);
	let districts = $state<Ilce[]>([]);
	let selectedCityId = $state<number>(0);
	let isLoadingCities = $state(false);
	let isLoadingDistricts = $state(false);
	let isDeleting = $state<number | null>(null);
	let isAdding = $state(false);
	let newDistrictName = $state('');
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
		isLoadingCities = true;
		try {
			cities = await illerApi.list();
		} catch (error) {
			console.error('Failed to load cities:', error);
			toast.error('İller yüklenirken hata oluştu');
		}
		isLoadingCities = false;
	}

	async function loadDistricts(ilId: number) {
		isLoadingDistricts = true;
		try {
			districts = await ilcelerApi.listByIl(ilId);
		} catch (error) {
			console.error('Failed to load districts:', error);
			toast.error('İlçeler yüklenirken hata oluştu');
		}
		isLoadingDistricts = false;
	}

	async function handleAddDistrict() {
		if (!selectedCityId) {
			toast.error('Lütfen bir il seçin');
			return;
		}

		if (!newDistrictName.trim()) {
			toast.error('İlçe adı boş olamaz');
			return;
		}

		isAdding = true;
		try {
			await ilcelerApi.create(selectedCityId, { adi: newDistrictName });
			await loadDistricts(selectedCityId);
			newDistrictName = '';
			toast.success('İlçe eklendi');
		} catch (error) {
			console.error('Failed to add district:', error);
			toast.error('İlçe eklenirken hata oluştu');
		}
		isAdding = false;
	}

	async function handleDeleteDistrict(districtId: number) {
		if (!selectedCityId) {
			return;
		}

		if (!confirm('Bu ilçeyi silmek istediğinizden emin misiniz?')) {
			return;
		}

		isDeleting = districtId;
		try {
			await ilcelerApi.delete(selectedCityId, districtId);
			districts = districts.filter((d) => d.id !== districtId);
			toast.success('İlçe silindi');
		} catch (error) {
			console.error('Failed to delete district:', error);
			toast.error('İlçe silinirken hata oluştu');
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
				<h1 class="text-3xl font-bold text-slate-900">İlçe Yönetimi</h1>
				<p class="mt-2 text-slate-600">Sistemdeki ilçeleri yönetin.</p>
			</div>
		</div>

		<!-- City Selection -->
		<Card class="mb-8">
			<CardHeader>
				<CardTitle class="flex items-center gap-2">
					<MapPin class="h-5 w-5" />
					İl Seçin
				</CardTitle>
			</CardHeader>
			<CardContent>
				{#if isLoadingCities}
					<div class="flex min-h-[100px] items-center justify-center">
						<Loader2 class="h-6 w-6 animate-spin text-blue-600" />
					</div>
				{:else}
					<div class="max-w-sm space-y-2">
						<Label for="citySelect">İl</Label>
						<select
							bind:value={selectedCityId}
							class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm ring-offset-white file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-950 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
							onchange={() => {
								districts = [];
								newDistrictName = '';
								if (selectedCityId) loadDistricts(selectedCityId);
							}}
						>
							<option value={0}>İl Seçin</option>
							{#each cities as city}
								<option value={city.id}>{city.adi}</option>
							{/each}
						</select>
					</div>
				{/if}
			</CardContent>
		</Card>

		{#if selectedCityId}
			<!-- Add District Form -->
			<Card class="mb-8">
				<CardHeader>
					<CardTitle>Yeni İlçe Ekle</CardTitle>
				</CardHeader>
				<CardContent>
					<form onsubmit={(e) => { e.preventDefault(); handleAddDistrict(); }} class="flex gap-4">
						<div class="flex-1">
							<Label for="districtName">İlçe Adı</Label>
							<Input
								id="districtName"
								bind:value={newDistrictName}
								placeholder="Örn: Kadıköy"
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
									İlçe Ekle
								{/if}
							</Button>
						</div>
					</form>
				</CardContent>
			</Card>

			<!-- Districts List -->
			<Card>
				<CardHeader>
					<CardTitle>
						Kayıtlı İlçeler ({districts.length})
					</CardTitle>
				</CardHeader>
				<CardContent>
					{#if isLoadingDistricts}
						<div class="flex min-h-[200px] items-center justify-center">
							<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
						</div>
					{:else if districts.length === 0}
						<div class="py-8 text-center text-slate-500">
							Henüz ilçe eklenmemiş.
						</div>
					{:else}
						<div class="space-y-2">
							{#each districts as district}
								<div class="flex items-center justify-between rounded-md border p-4">
									<span class="font-medium">{district.adi}</span>
									{#if isDeleting === district.id}
										<Loader2 class="h-4 w-4 animate-spin text-slate-400" />
									{:else}
										<Button
											variant="ghost"
											size="sm"
											onclick={() => handleDeleteDistrict(district.id)}
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
		{/if}
	</div>
{/if}
