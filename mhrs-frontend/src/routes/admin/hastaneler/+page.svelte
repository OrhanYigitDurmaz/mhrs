<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { isAdmin } from '$lib/stores/auth.svelte';
	import { illerApi, ilcelerApi, hastanelerApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { Loader2, ArrowLeft, Plus, Trash2, Building } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';
	import type { Il, Ilce, Hastane } from '$lib/api';

	let hospitals = $state<Hastane[]>([]);
let cities = $state<Il[]>([]);
	let districts = $state<Ilce[]>([]);
	let isLoadingDistricts = $state(false);
	let isLoading = $state(false);
	let isDeleting = $state<number | null>(null);
	let isAdding = $state(false);
	let showAddForm = $state(false);
	let isCheckingAuth = $state(true);

	// Form state
	let formData = $state({
		il_id: 0,
		ilce_id: 0,
		adi: '',
		tip: 'Devlet',
		adres: ''
	});

	onMount(async () => {
		isCheckingAuth = false;

		if (!$isAdmin) {
			goto('/', { replaceState: true });
			return;
		}
		await loadData();
	});

	async function loadData() {
		isLoading = true;
		try {
			[cities, hospitals] = await Promise.all([
				illerApi.list(),
				hastanelerApi.list()
			]);
		} catch (error) {
			console.error('Failed to load data:', error);
			toast.error('Veriler yüklenirken hata oluştu');
		}
		isLoading = false;
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

	async function handleAddHospital() {
		if (!formData.il_id || !formData.ilce_id || !formData.adi.trim() || !formData.adres.trim()) {
			toast.error('Lütfen tüm alanları doldurun');
			return;
		}

		isAdding = true;
		try {
			await hastanelerApi.create({
				il_id: formData.il_id,
				ilce_id: formData.ilce_id,
				adi: formData.adi,
				tip: formData.tip as 'Devlet' | 'Özel',
				adres: formData.adres
			});
			await loadData();
			resetForm();
			toast.success('Hastane eklendi');
		} catch (error) {
			console.error('Failed to add hospital:', error);
			toast.error('Hastane eklenirken hata oluştu');
		}
		isAdding = false;
	}

	function resetForm() {
		formData = {
			il_id: 0,
			ilce_id: 0,
			adi: '',
			tip: 'Devlet',
			adres: ''
		};
		districts = [];
		showAddForm = false;
	}

	async function handleDeleteHospital(hospitalId: number) {
		if (!confirm('Bu hastaneyi silmek istediğinizden emin misiniz?')) {
			return;
		}

		isDeleting = hospitalId;
		try {
			await hastanelerApi.delete(hospitalId);
			hospitals = hospitals.filter((h) => h.id !== hospitalId);
			toast.success('Hastane silindi');
		} catch (error) {
			console.error('Failed to delete hospital:', error);
			toast.error('Hastane silinirken hata oluştu');
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
	<div class="mx-auto max-w-5xl px-4 py-8 sm:px-6 lg:px-8">
		<div class="mb-8 flex items-center justify-between">
			<div class="flex items-center gap-4">
				<Button variant="ghost" href="/admin">
					<ArrowLeft class="mr-2 h-4 w-4" />
					Admin Panel
				</Button>
				<div>
					<h1 class="text-3xl font-bold text-slate-900">Hastane Yönetimi</h1>
					<p class="mt-2 text-slate-600">Hastaneleri yönetin.</p>
				</div>
			</div>
			<Button onclick={() => showAddForm = !showAddForm}>
				<Plus class="mr-2 h-4 w-4" />
				Hastane Ekle
			</Button>
		</div>

		{#if showAddForm}
			<Card class="mb-8">
				<CardHeader>
					<CardTitle>Yeni Hastane Ekle</CardTitle>
				</CardHeader>
				<CardContent>
					<form onsubmit={(e) => { e.preventDefault(); handleAddHospital(); }} class="space-y-4">
						<div class="grid gap-4 sm:grid-cols-2">
							<div class="space-y-2">
								<Label for="city">İl</Label>
								<select
									bind:value={formData.il_id}
									class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm ring-offset-white file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-950 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
									onchange={() => {
										formData.ilce_id = 0;
										districts = [];
										if (formData.il_id) loadDistricts(formData.il_id);
									}}
								>
									<option value={0}>İl Seçin</option>
									{#each cities as city}
										<option value={city.id}>{city.adi}</option>
									{/each}
								</select>
							</div>
							<div class="space-y-2">
								<Label for="district">İlçe</Label>
								<select
									bind:value={formData.ilce_id}
									disabled={!formData.il_id || isLoadingDistricts}
									class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm ring-offset-white file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-950 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
								>
									<option value={0}>{isLoadingDistricts ? 'Yükleniyor...' : 'İlçe Seçin'}</option>
									{#each districts as district}
										<option value={district.id}>{district.adi}</option>
									{/each}
								</select>
							</div>
						</div>
						<div class="grid gap-4 sm:grid-cols-2">
							<div class="space-y-2">
								<Label for="name">Hastane Adı</Label>
								<Input id="name" bind:value={formData.adi} placeholder="Hastane adı" />
							</div>
							<div class="space-y-2">
								<Label for="type">Hastane Tipi</Label>
								<select
									bind:value={formData.tip}
									class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm ring-offset-white file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-950 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
								>
									<option value="Devlet">Devlet</option>
									<option value="Özel">Özel</option>
								</select>
							</div>
						</div>
						<div class="space-y-2">
							<Label for="address">Adres</Label>
							<Input id="address" bind:value={formData.adres} placeholder="Adres" />
						</div>
						<div class="flex gap-2 justify-end">
							<Button type="button" variant="outline" onclick={resetForm}>İptal</Button>
							<Button type="submit" disabled={isAdding}>
								{#if isAdding}
									<Loader2 class="mr-2 h-4 w-4 animate-spin" />
									Ekleniyor...
								{:else}
									Hastane Ekle
								{/if}
							</Button>
						</div>
					</form>
				</CardContent>
			</Card>
		{/if}

		<Card>
			<CardHeader>
				<CardTitle>Kayıtlı Hastaneler ({hospitals.length})</CardTitle>
			</CardHeader>
			<CardContent>
				{#if isLoading}
					<div class="flex min-h-[200px] items-center justify-center">
						<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
					</div>
				{:else if hospitals.length === 0}
					<div class="py-8 text-center text-slate-500">Henüz hastane eklenmemiş.</div>
				{:else}
					<div class="space-y-3">
						{#each hospitals as hospital}
							<div class="flex items-start justify-between rounded-md border p-4">
								<div class="flex items-start gap-3">
									<div class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100">
										<Building class="h-5 w-5 text-blue-600" />
									</div>
									<div>
										<p class="font-medium">{hospital.adi}</p>
										<p class="text-sm text-slate-600">{hospital.adres}</p>
										<p class="text-xs text-slate-500">{hospital.tip}</p>
									</div>
								</div>
								{#if isDeleting === hospital.id}
									<Loader2 class="h-4 w-4 animate-spin text-slate-400" />
								{:else}
									<Button
										variant="ghost"
										size="sm"
										onclick={() => handleDeleteHospital(hospital.id)}
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
