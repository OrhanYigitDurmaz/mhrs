<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { isAdmin } from '$lib/stores/auth.svelte';
	import { doktorlarApi, calismaSaatiApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Label } from '$lib/components/ui/label';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { ArrowLeft, Loader2, Plus, Trash2, Clock } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';
	import type { Doktor, CalismaSaati } from '$lib/api';

	let doctors = $state<Doktor[]>([]);
	let workingHours = $state<CalismaSaati[]>([]);
	let selectedDoctorId = $state<number>(0);
	let isLoading = $state(false);
	let isAdding = $state(false);
	let isDeleting = $state<number | null>(null);
	let isCheckingAuth = $state(true);
	let showAddForm = $state(false);

	const DAYS = ['Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma', 'Cumartesi', 'Pazar'] as const;
	const TIME_SLOTS = [
		'08:00', '08:30', '09:00', '09:30', '10:00', '10:30', '11:00', '11:30',
		'12:00', '12:30', '13:00', '13:30', '14:00', '14:30', '15:00', '15:30',
		'16:00', '16:30', '17:00', '17:30', '18:00'
	];

	let formData = $state({
		doktor_id: 0,
		gun: 'Pazartesi',
		saat_bas: '09:00',
		saat_bit: '17:00'
	});

	onMount(async () => {
		isCheckingAuth = false;

		if (!$isAdmin) {
			goto('/', { replaceState: true });
			return;
		}
		await loadDoctors();
	});

	async function loadDoctors() {
		isLoading = true;
		try {
			doctors = await doktorlarApi.list();
			if (doctors.length > 0 && !selectedDoctorId) {
				selectedDoctorId = doctors[0].id;
				await loadWorkingHours(selectedDoctorId);
			}
		} catch (error) {
			console.error('Failed to load doctors:', error);
			toast.error('Doktorlar yüklenirken hata oluştu');
		}
		isLoading = false;
	}

	async function loadWorkingHours(doktorId: number) {
		try {
			workingHours = await calismaSaatiApi.getByDoktor(doktorId);
		} catch (error) {
			console.error('Failed to load working hours:', error);
			toast.error('Çalışma saatleri yüklenirken hata oluştu');
		}
	}

	async function handleAddHour() {
		if (!formData.doktor_id || !formData.gun || !formData.saat_bas || !formData.saat_bit) {
			toast.error('Lütfen tüm alanları doldurun');
			return;
		}

		if (formData.saat_bas >= formData.saat_bit) {
			toast.error('Bitiş saati başlangıç saatinden sonra olmalıdır');
			return;
		}

		isAdding = true;
		try {
			await calismaSaatiApi.create({
				doktor_id: formData.doktor_id,
				gun: formData.gun,
				saat_bas: formData.saat_bas,
				saat_bit: formData.saat_bit
			});
			await loadWorkingHours(formData.doktor_id);
			resetForm();
			toast.success('Çalışma saati eklendi');
		} catch (error) {
			console.error('Failed to add hour:', error);
			toast.error('Çalışma saati eklenirken hata oluştu');
		}
		isAdding = false;
	}

	async function handleDeleteHour(hourId: number) {
		isDeleting = hourId;
		try {
			await calismaSaatiApi.delete(hourId);
			workingHours = workingHours.filter((h) => h.id !== hourId);
			toast.success('Çalışma saati silindi');
		} catch (error) {
			console.error('Failed to delete hour:', error);
			toast.error('Çalışma saati silinirken hata oluştu');
		}
		isDeleting = null;
	}

	function resetForm() {
		formData = {
			doktor_id: selectedDoctorId,
			gun: 'Pazartesi',
			saat_bas: '09:00',
			saat_bit: '17:00'
		};
		showAddForm = false;
	}

	function getDoctorName(id: number) {
		return doctors.find((d) => d.id === id)?.adi_soyadi || '-';
	}

	const hoursByDay = $derived(
		DAYS.map((day) => ({
			day,
			hours: workingHours.filter((h) => h.gun === day)
		}))
	);
</script>

<Navbar />

{#if isCheckingAuth || !browser}
	<div class="flex min-h-[60vh] items-center justify-center">
		<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
	</div>
{:else}
	<div class="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
		<div class="mb-8 flex items-center justify-between">
			<div class="flex items-center gap-4">
				<Button variant="ghost" href="/admin">
					<ArrowLeft class="mr-2 h-4 w-4" />
					Admin Panel
				</Button>
				<div>
					<h1 class="text-3xl font-bold text-slate-900">Çalışma Saati Yönetimi</h1>
					<p class="mt-2 text-slate-600">Doktorların çalışma saatlerini yönetin.</p>
				</div>
			</div>
		</div>

		{#if isLoading}
			<div class="flex min-h-[400px] items-center justify-center">
				<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
			</div>
		{:else}
			<!-- Doctor Selector -->
			<Card class="mb-6">
				<CardHeader>
					<CardTitle>Doktor Seçin</CardTitle>
				</CardHeader>
				<CardContent>
					<select
						bind:value={selectedDoctorId}
						onchange={() => loadWorkingHours(selectedDoctorId)}
						class="flex h-10 w-full max-w-xs rounded-md border border-slate-200 bg-white px-3 py-2 text-sm ring-offset-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-950"
					>
						{#each doctors as doctor}
							<option value={doctor.id}>{doctor.adi_soyadi}</option>
						{/each}
					</select>
				</CardContent>
			</Card>

			{#if selectedDoctorId}
				<div class="grid gap-6 lg:grid-cols-3">
					<!-- Working Hours List -->
					<div class="lg:col-span-2 space-y-4">
						<Card>
							<CardHeader class="flex flex-row items-center justify-between">
								<div>
									<CardTitle>{getDoctorName(selectedDoctorId)}</CardTitle>
									<CardDescription>Haftalık çalışma programı</CardDescription>
								</div>
								<Button onclick={() => {
									formData.doktor_id = selectedDoctorId;
									showAddForm = !showAddForm;
								}} size="sm">
									<Plus class="mr-2 h-4 w-4" />
									Saat Ekle
								</Button>
							</CardHeader>
							<CardContent>
								{#if workingHours.length === 0}
									<div class="py-8 text-center text-slate-500">
										<Clock class="mx-auto h-12 w-12 text-slate-300 mb-3" />
										<p>Henüz çalışma saati tanımlanmamış.</p>
									</div>
								{:else}
									<div class="space-y-4">
										{#each hoursByDay as { day, hours }}
											{#if hours.length > 0}
												<div>
													<h4 class="mb-2 font-medium text-slate-700">{day}</h4>
													<div class="space-y-2">
														{#each hours as hour}
															<div class="flex items-center justify-between rounded-md border border-slate-200 p-3">
																<div class="flex items-center gap-3">
																	<Clock class="h-4 w-4 text-slate-500" />
																	<span class="text-sm">
																		<span class="font-medium">{hour.saat_bas}</span>
																		<span class="text-slate-500 mx-1">-</span>
																		<span class="font-medium">{hour.saat_bit}</span>
																	</span>
																</div>
																{#if isDeleting === hour.id}
																	<Loader2 class="h-4 w-4 animate-spin text-slate-400" />
																{:else}
																	<Button
																		variant="ghost"
																		size="sm"
																		onclick={() => handleDeleteHour(hour.id)}
																		class="text-red-600 hover:text-red-700 hover:bg-red-50"
																	>
																		<Trash2 class="h-4 w-4" />
																	</Button>
																{/if}
															</div>
														{/each}
													</div>
												</div>
											{/if}
										{/each}
									</div>
								{/if}
							</CardContent>
						</Card>
					</div>

					<!-- Add Form -->
					{#if showAddForm}
						<Card>
							<CardHeader>
								<CardTitle>Çalışma Saati Ekle</CardTitle>
							</CardHeader>
							<CardContent>
								<form onsubmit={(e) => { e.preventDefault(); handleAddHour(); }} class="space-y-4">
									<div class="space-y-2">
										<Label for="day">Gün</Label>
										<select
											id="day"
											bind:value={formData.gun}
											class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm"
										>
											{#each DAYS as day}
												<option value={day}>{day}</option>
											{/each}
										</select>
									</div>

									<div class="space-y-2">
										<Label for="start">Başlangıç</Label>
										<select
											id="start"
											bind:value={formData.saat_bas}
											class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm"
										>
											{#each TIME_SLOTS as time}
												<option value={time}>{time}</option>
											{/each}
										</select>
									</div>

									<div class="space-y-2">
										<Label for="end">Bitiş</Label>
										<select
											id="end"
											bind:value={formData.saat_bit}
											class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm"
										>
											{#each TIME_SLOTS as time}
												<option value={time}>{time}</option>
											{/each}
										</select>
									</div>

									<div class="flex gap-2">
										<Button type="button" variant="outline" onclick={resetForm} class="flex-1">
											İptal
										</Button>
										<Button type="submit" disabled={isAdding} class="flex-1">
											{#if isAdding}
												<Loader2 class="mr-2 h-4 w-4 animate-spin" />
											{/if}
											Ekle
										</Button>
									</div>
								</form>
							</CardContent>
						</Card>
					{/if}
				</div>
			{/if}
		{/if}
	</div>
{/if}
