<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { isAdmin } from '$lib/stores/auth.svelte';
	import { hastanelerApi, branlarApi, doktorlarApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { Loader2, ArrowLeft, Plus, Trash2, User } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';
	import type { Hastane, Brans, Doktor } from '$lib/api';

	let doctors = $state<Doktor[]>([]);
	let hospitals = $state<Hastane[]>([]);
	let branches = $state<Brans[]>([]);
	let isLoading = $state(false);
	let isDeleting = $state<number | null>(null);
	let isAdding = $state(false);
	let showAddForm = $state(false);
	let isCheckingAuth = $state(true);

	let formData = $state({
		hastane_id: 0,
		brans_id: 0,
		kullanici_id: 0,
		adi_soyadi: ''
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
			[hospitals, branches, doctors] = await Promise.all([
				hastanelerApi.list(),
				branlarApi.list(),
				doktorlarApi.list()
			]);
		} catch (error) {
			console.error('Failed to load data:', error);
			toast.error('Veriler yüklenirken hata oluştu');
		}
		isLoading = false;
	}

	async function handleAddDoctor() {
		if (!formData.hastane_id || !formData.brans_id || !formData.kullanici_id || !formData.adi_soyadi.trim()) {
			toast.error('Lütfen tüm alanları doldurun');
			return;
		}

		isAdding = true;
		try {
			await doktorlarApi.create({
				hastane_id: formData.hastane_id,
				brans_id: formData.brans_id,
				kullanici_id: formData.kullanici_id,
				adi_soyadi: formData.adi_soyadi
			});
			await loadData();
			resetForm();
			toast.success('Doktor eklendi');
		} catch (error) {
			console.error('Failed to add doctor:', error);
			toast.error('Doktor eklenirken hata oluştu');
		}
		isAdding = false;
	}

	function resetForm() {
		formData = {
			hastane_id: 0,
			brans_id: 0,
			kullanici_id: 0,
			adi_soyadi: ''
		};
		showAddForm = false;
	}

	async function handleDeleteDoctor(doctorId: number) {
		if (!confirm('Bu doktoru silmek istediğinizden emin misiniz?')) {
			return;
		}

		isDeleting = doctorId;
		try {
			await doktorlarApi.delete(doctorId);
			doctors = doctors.filter((d) => d.id !== doctorId);
			toast.success('Doktor silindi');
		} catch (error) {
			console.error('Failed to delete doctor:', error);
			toast.error('Doktor silinirken hata oluştu');
		}
		isDeleting = null;
	}

	function getHospitalName(id: number) {
		return hospitals.find((h) => h.id === id)?.adi || '-';
	}

	function getBranchName(id: number) {
		return branches.find((b) => b.id === id)?.adi || '-';
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
					<h1 class="text-3xl font-bold text-slate-900">Doktor Yönetimi</h1>
					<p class="mt-2 text-slate-600">Doktorları yönetin.</p>
				</div>
			</div>
			<Button onclick={() => showAddForm = !showAddForm}>
				<Plus class="mr-2 h-4 w-4" />
				Doktor Ekle
			</Button>
		</div>

		{#if showAddForm}
			<Card class="mb-8">
				<CardHeader>
					<CardTitle>Yeni Doktor Ekle</CardTitle>
				</CardHeader>
				<CardContent>
					<form onsubmit={(e) => { e.preventDefault(); handleAddDoctor(); }} class="space-y-4">
						<div class="grid gap-4 sm:grid-cols-2">
							<div class="space-y-2">
								<Label for="hospital">Hastane</Label>
								<select
									bind:value={formData.hastane_id}
									class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm ring-offset-white file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-950 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
								>
									<option value={0}>Hastane Seçin</option>
									{#each hospitals as hospital}
										<option value={hospital.id}>{hospital.adi}</option>
									{/each}
								</select>
							</div>
							<div class="space-y-2">
								<Label for="branch">Branş</Label>
								<select
									bind:value={formData.brans_id}
									class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm ring-offset-white file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-950 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
								>
									<option value={0}>Branş Seçin</option>
									{#each branches as branch}
										<option value={branch.id}>{branch.adi}</option>
									{/each}
								</select>
							</div>
						</div>
						<div class="space-y-2">
							<Label for="userId">Kullanıcı ID</Label>
							<Input
								id="userId"
								type="number"
								bind:value={formData.kullanici_id}
								placeholder="Önce kullanıcı oluşturun, ID'yi buraya girin"
								min="1"
							/>
							<p class="text-xs text-slate-500">
								Önce "Kullanıcı Yönetimi" sayfasından doktor hesabı oluşturun, sonra buraya ID'sini girin.
							</p>
						</div>
						<div class="space-y-2">
							<Label for="name">Doktor Adı</Label>
							<Input id="name" bind:value={formData.adi_soyadi} placeholder="Dr. Adı Soyadı" />
						</div>
						<div class="flex gap-2 justify-end">
							<Button type="button" variant="outline" onclick={resetForm}>İptal</Button>
							<Button type="submit" disabled={isAdding}>
								{#if isAdding}
									<Loader2 class="mr-2 h-4 w-4 animate-spin" />
									Ekleniyor...
								{:else}
									Doktor Ekle
								{/if}
							</Button>
						</div>
					</form>
				</CardContent>
			</Card>
		{/if}

		<Card>
			<CardHeader>
				<CardTitle>Kayıtlı Doktorlar ({doctors.length})</CardTitle>
			</CardHeader>
			<CardContent>
				{#if isLoading}
					<div class="flex min-h-[200px] items-center justify-center">
						<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
					</div>
				{:else if doctors.length === 0}
					<div class="py-8 text-center text-slate-500">Henüz doktor eklenmemiş.</div>
				{:else}
					<div class="space-y-3">
						{#each doctors as doctor}
							<div class="flex items-center justify-between rounded-md border p-4">
								<div class="flex items-center gap-3">
									<div class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100">
										<User class="h-5 w-5 text-blue-600" />
									</div>
									<div>
										<p class="font-medium">{doctor.adi_soyadi}</p>
										<p class="text-sm text-slate-600">{getHospitalName(doctor.hastane_id)} - {getBranchName(doctor.brans_id)}</p>
									</div>
								</div>
								{#if isDeleting === doctor.id}
									<Loader2 class="h-4 w-4 animate-spin text-slate-400" />
								{:else}
									<Button
										variant="ghost"
										size="sm"
										onclick={() => handleDeleteDoctor(doctor.id)}
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
