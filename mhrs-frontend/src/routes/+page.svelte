<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { auth } from '$lib/stores/auth.svelte';
	import { illerApi, ilcelerApi, branlarApi, hastanelerApi, doktorlarApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { Stethoscope, MapPin, Calendar, Clock, User, Loader2, ArrowLeft, ChevronRight } from 'lucide-svelte';
	import type { Il, Ilce, Brans, Hastane, Doktor } from '$lib/api';

	// Step state
	type Step = 'city' | 'district' | 'hospital' | 'branch' | 'doctor' | 'datetime';
	let currentStep = $state<Step>('city');

	// Selection state
	let selectedCity = $state<Il | null>(null);
	let selectedDistrict = $state<Ilce | null>(null);
	let selectedHospital = $state<Hastane | null>(null);
	let selectedBranch = $state<Brans | null>(null);
	let selectedDoctor = $state<Doktor | null>(null);
	let selectedDate = $state<string | null>(null);
	let selectedTime = $state<string | null>(null);

	// Data state
	let cities = $state<Il[]>([]);
	let districts = $state<Ilce[]>([]);
	let branches = $state<Brans[]>([]);
	let hospitals = $state<Hastane[]>([]);
	let doctors = $state<Doktor[]>([]);
	let availableSlots = $state<Array<{ tarih: string; saat: string; musait: boolean }>>([]);

	let isLoading = $state(false);
	let isCheckingAuth = $state(true);

	onMount(async () => {
		isCheckingAuth = false;

		if (!$auth.isAuthenticated) {
			goto('/login', { replaceState: true });
			return;
		}

		// Load initial data
		isLoading = true;
		try {
			cities = await illerApi.list();
			branches = await branlarApi.list();
		} catch (error) {
			console.error('Failed to load initial data:', error);
		}
		isLoading = false;
	});

	async function loadDistricts(cityId: number) {
		isLoading = true;
		try {
			districts = await ilcelerApi.listByIl(cityId);
		} catch (error) {
			console.error('Failed to load districts:', error);
		}
		isLoading = false;
	}

	async function loadHospitals() {
		isLoading = true;
		try {
			hospitals = await hastanelerApi.list({
				il: selectedCity?.id,
				ilce: selectedDistrict?.id
			});
		} catch (error) {
			console.error('Failed to load hospitals:', error);
		}
		isLoading = false;
	}

	async function loadDoctors() {
		isLoading = true;
		try {
			doctors = await doktorlarApi.list({
				hastane: selectedHospital?.id,
				brans: selectedBranch?.id
			});
		} catch (error) {
			console.error('Failed to load doctors:', error);
		}
		isLoading = false;
	}

	async function loadAvailability() {
		if (!selectedDoctor) return;

		isLoading = true;
		const today = new Date();
		const nextWeek = new Date(today);
		nextWeek.setDate(today.getDate() + 7);

		try {
			availableSlots = await doktorlarApi.getAvailability(
				selectedDoctor.id,
				today.toISOString().split('T')[0],
				nextWeek.toISOString().split('T')[0]
			);
		} catch (error) {
			console.error('Failed to load availability:', error);
		}
		isLoading = false;
	}

	function handleCitySelect(value: string) {
		const city = cities.find((c) => c.id === parseInt(value));
		if (city) {
			selectedCity = city;
			selectedDistrict = null;
			selectedHospital = null;
			selectedBranch = null;
			selectedDoctor = null;
			loadDistricts(city.id);
			currentStep = 'district';
		}
	}

	function handleDistrictSelect(value: string) {
		const district = districts.find((d) => d.id === parseInt(value));
		if (district) {
			selectedDistrict = district;
			selectedHospital = null;
			selectedBranch = null;
			selectedDoctor = null;
			loadHospitals();
			currentStep = 'hospital';
		}
	}

	function handleHospitalSelect(hospital: Hastane) {
		selectedHospital = hospital;
		selectedBranch = null;
		selectedDoctor = null;
		currentStep = 'branch';
	}

	function handleBranchSelect(branch: Brans) {
		selectedBranch = branch;
		selectedDoctor = null;
		loadDoctors();
		currentStep = 'doctor';
	}

	function handleDoctorSelect(doctor: Doktor) {
		selectedDoctor = doctor;
		loadAvailability();
		currentStep = 'datetime';
	}

	function handleDateTimeSelect(date: string, time: string) {
		selectedDate = date;
		selectedTime = time;
		// TODO: Book appointment
	}

	function resetSelection() {
		currentStep = 'city';
		selectedCity = null;
		selectedDistrict = null;
		selectedHospital = null;
		selectedBranch = null;
		selectedDoctor = null;
		selectedDate = null;
		selectedTime = null;
	}

	const groupedSlots = $derived(() => {
		const groups: Record<string, string[]> = {};
		availableSlots.forEach((slot) => {
			if (slot.musait) {
				if (!groups[slot.tarih]) {
					groups[slot.tarih] = [];
				}
				groups[slot.tarih].push(slot.saat);
			}
		});
		return groups;
	});
</script>

<Navbar />

{#if isCheckingAuth || !browser}
	<div class="flex min-h-[60vh] items-center justify-center">
		<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
	</div>
{:else}
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
	<!-- Header -->
	<div class="mb-8">
		<h1 class="text-3xl font-bold text-slate-900">Randevu Al</h1>
		<p class="mt-2 text-slate-600">Merkezi Hekim Randevu Sistemi üzerinden randevunuzu oluşturun.</p>
	</div>

	<!-- Progress Steps -->
	<div class="mb-8 overflow-x-auto">
		<div class="flex min-w-max items-center justify-between">
			<div class="flex items-center">
				<div class="flex h-10 w-10 items-center justify-center rounded-full {currentStep === 'city' ? 'bg-blue-600 text-white' : 'bg-slate-200 text-slate-500'}">
					<MapPin class="h-5 w-5" />
				</div>
				<span class="ml-2 font-medium {currentStep === 'city' ? 'text-blue-600' : 'text-slate-500'}">İl</span>
			</div>
			<div class="mx-4 h-0.5 w-16 bg-slate-200 {selectedCity && '!bg-blue-600'}"></div>
			<div class="flex items-center">
				<div class="flex h-10 w-10 items-center justify-center rounded-full {selectedDistrict ? 'bg-blue-600 text-white' : 'bg-slate-200 text-slate-500'}">
					<MapPin class="h-5 w-5" />
				</div>
				<span class="ml-2 font-medium {selectedDistrict ? 'text-blue-600' : 'text-slate-500'}">İlçe</span>
			</div>
			<div class="mx-4 h-0.5 w-16 bg-slate-200 {selectedHospital && '!bg-blue-600'}"></div>
			<div class="flex items-center">
				<div class="flex h-10 w-10 items-center justify-center rounded-full {selectedHospital ? 'bg-blue-600 text-white' : 'bg-slate-200 text-slate-500'}">
					<Stethoscope class="h-5 w-5" />
				</div>
				<span class="ml-2 font-medium {selectedHospital ? 'text-blue-600' : 'text-slate-500'}">Hastane</span>
			</div>
			<div class="mx-4 h-0.5 w-16 bg-slate-200 {selectedBranch && '!bg-blue-600'}"></div>
			<div class="flex items-center">
				<div class="flex h-10 w-10 items-center justify-center rounded-full {selectedBranch ? 'bg-blue-600 text-white' : 'bg-slate-200 text-slate-500'}">
					<User class="h-5 w-5" />
				</div>
				<span class="ml-2 font-medium {selectedBranch ? 'text-blue-600' : 'text-slate-500'}">Branş</span>
			</div>
			<div class="mx-4 h-0.5 w-16 bg-slate-200 {selectedDoctor && '!bg-blue-600'}"></div>
			<div class="flex items-center">
				<div class="flex h-10 w-10 items-center justify-center rounded-full {selectedDoctor ? 'bg-blue-600 text-white' : 'bg-slate-200 text-slate-500'}">
					<Clock class="h-5 w-5" />
				</div>
				<span class="ml-2 font-medium {selectedDoctor ? 'text-blue-600' : 'text-slate-500'}">Tarih/Saat</span>
			</div>
		</div>
	</div>

	<!-- Selection Cards -->
	<div class="grid gap-6 lg:grid-cols-3">
		<!-- Selection Panel -->
		<div class="lg:col-span-2">
			{#if isLoading}
				<div class="flex min-h-[400px] items-center justify-center">
					<div class="text-center">
						<Loader2 class="mx-auto h-8 w-8 animate-spin text-blue-600" />
						<p class="mt-2 text-slate-600">Yükleniyor...</p>
					</div>
				</div>
			{:else if currentStep === 'city'}
				<div class="space-y-4">
					<h2 class="text-xl font-semibold text-slate-900">İl Seçin</h2>
					<div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
						{#each cities as city}
							<Card
								class="cursor-pointer transition-colors hover:bg-blue-50 hover:border-blue-300 {selectedCity?.id === city.id
									? 'border-blue-500 bg-blue-50'
									: ''}"
								onclick={() => handleCitySelect(city.id.toString())}
							>
								<CardHeader>
									<CardTitle class="text-lg">{city.adi}</CardTitle>
								</CardHeader>
							</Card>
						{/each}
					</div>
				</div>

			{:else if currentStep === 'district'}
				<div class="space-y-4">
					<div class="flex items-center gap-2">
						<Button variant="ghost" size="sm" onclick={() => currentStep = 'city'}>
							<ArrowLeft class="mr-1 h-4 w-4" />
						</Button>
						<h2 class="text-xl font-semibold text-slate-900">{selectedCity?.adi} - İlçe Seçin</h2>
					</div>
					<div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
						{#each districts as district}
							<Card
								class="cursor-pointer transition-colors hover:bg-blue-50 hover:border-blue-300 {selectedDistrict?.id === district.id
									? 'border-blue-500 bg-blue-50'
									: ''}"
								onclick={() => handleDistrictSelect(district.id.toString())}
							>
								<CardHeader>
									<CardTitle class="text-lg">{district.adi}</CardTitle>
								</CardHeader>
							</Card>
						{/each}
					</div>
				</div>

			{:else if currentStep === 'hospital'}
				<div class="space-y-4">
					<div class="flex items-center gap-2">
						<Button variant="ghost" size="sm" onclick={() => currentStep = 'district'}>
							<ArrowLeft class="mr-1 h-4 w-4" />
						</Button>
						<h2 class="text-xl font-semibold text-slate-900">Hastane Seçin</h2>
					</div>
					<div class="grid gap-4">
						{#each hospitals as hospital}
							<Card
								class="cursor-pointer transition-colors hover:bg-blue-50 hover:border-blue-300 {selectedHospital?.id === hospital.id
									? 'border-blue-500 bg-blue-50'
									: ''}"
								onclick={() => handleHospitalSelect(hospital)}
							>
								<CardHeader>
									<div class="flex items-start justify-between">
										<div>
											<CardTitle class="text-lg">{hospital.adi}</CardTitle>
											<CardDescription class="mt-1">
												<div class="flex items-center gap-1 text-sm">
													<MapPin class="h-3 w-3" />
													{hospital.adres}
												</div>
											</CardDescription>
										</div>
										<Badge variant={hospital.tip === 'Devlet' ? 'default' : 'secondary'}>
											{hospital.tip}
										</Badge>
									</div>
								</CardHeader>
							</Card>
						{/each}
					</div>
				</div>

			{:else if currentStep === 'branch'}
				<div class="space-y-4">
					<div class="flex items-center gap-2">
						<Button variant="ghost" size="sm" onclick={() => currentStep = 'hospital'}>
							<ArrowLeft class="mr-1 h-4 w-4" />
						</Button>
						<h2 class="text-xl font-semibold text-slate-900">Branş Seçin</h2>
					</div>
					<div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
						{#each branches as branch}
							<Card
								class="cursor-pointer transition-colors hover:bg-blue-50 hover:border-blue-300 {selectedBranch?.id === branch.id
									? 'border-blue-500 bg-blue-50'
									: ''}"
								onclick={() => handleBranchSelect(branch)}
							>
								<CardHeader>
									<CardTitle class="text-lg">{branch.adi}</CardTitle>
								</CardHeader>
							</Card>
						{/each}
					</div>
				</div>

			{:else if currentStep === 'doctor'}
				<div class="space-y-4">
					<div class="flex items-center gap-2">
						<Button variant="ghost" size="sm" onclick={() => currentStep = 'branch'}>
							<ArrowLeft class="mr-1 h-4 w-4" />
						</Button>
						<h2 class="text-xl font-semibold text-slate-900">Doktor Seçin</h2>
					</div>
					<div class="grid gap-4 sm:grid-cols-2">
						{#each doctors as doctor}
							<Card
								class="cursor-pointer transition-colors hover:bg-blue-50 hover:border-blue-300 {selectedDoctor?.id === doctor.id
									? 'border-blue-500 bg-blue-50'
									: ''}"
								onclick={() => handleDoctorSelect(doctor)}
							>
								<CardHeader>
									<div class="flex items-center gap-3">
										<div class="flex h-12 w-12 items-center justify-center rounded-full bg-blue-100">
											<User class="h-6 w-6 text-blue-600" />
										</div>
										<CardTitle class="text-lg">{doctor.adi_soyadi}</CardTitle>
									</div>
								</CardHeader>
							</Card>
						{/each}
					</div>
				</div>

			{:else if currentStep === 'datetime'}
				<div class="space-y-4">
					<div class="flex items-center gap-2">
						<Button variant="ghost" size="sm" onclick={() => currentStep = 'doctor'}>
							<ArrowLeft class="mr-1 h-4 w-4" />
						</Button>
						<h2 class="text-xl font-semibold text-slate-900">Randevu Saati Seçin</h2>
					</div>

					{#if Object.keys(groupedSlots()).length === 0}
						<Card>
							<CardContent class="py-8 text-center">
								<Calendar class="mx-auto h-12 w-12 text-slate-400" />
								<p class="mt-4 text-slate-600">Uygun randevu saati bulunamadı.</p>
								<Button variant="outline" class="mt-4" onclick={resetSelection}>
									Yeni Arama
								</Button>
							</CardContent>
						</Card>
					{:else}
						<div class="space-y-6">
							{#each Object.entries(groupedSlots()) as [date, times]}
								<Card>
									<CardHeader>
										<CardTitle class="text-lg">
											{new Date(date).toLocaleDateString('tr-TR', {
												weekday: 'long',
												year: 'numeric',
												month: 'long',
												day: 'numeric'
											})}
										</CardTitle>
									</CardHeader>
									<CardContent>
										<div class="flex flex-wrap gap-2">
											{#each times.sort() as time}
												<Button
													variant={selectedDate === date && selectedTime === time ? 'default' : 'outline'}
													size="sm"
													onclick={() => handleDateTimeSelect(date, time)}
												>
													{time}
												</Button>
											{/each}
										</div>
									</CardContent>
								</Card>
							{/each}
						</div>
					{/if}
				</div>
			{/if}
		</div>

		<!-- Summary Panel -->
		<div class="lg:col-span-1">
			<Card class="sticky top-20">
				<CardHeader>
					<CardTitle class="text-lg">Seçim Özeti</CardTitle>
				</CardHeader>
				<CardContent class="space-y-4">
					<div class="space-y-3">
						<div class="flex items-center justify-between text-sm">
							<span class="text-slate-600">İl:</span>
							<span class="font-medium">{selectedCity?.adi ?? '-'}</span>
						</div>
						<div class="flex items-center justify-between text-sm">
							<span class="text-slate-600">İlçe:</span>
							<span class="font-medium">{selectedDistrict?.adi ?? '-'}</span>
						</div>
						<div class="flex items-center justify-between text-sm">
							<span class="text-slate-600">Hastane:</span>
							<span class="font-medium text-right">{selectedHospital?.adi ?? '-'}</span>
						</div>
						<div class="flex items-center justify-between text-sm">
							<span class="text-slate-600">Branş:</span>
							<span class="font-medium">{selectedBranch?.adi ?? '-'}</span>
						</div>
						<div class="flex items-center justify-between text-sm">
							<span class="text-slate-600">Doktor:</span>
							<span class="font-medium text-right">{selectedDoctor?.adi_soyadi ?? '-'}</span>
						</div>
						<hr class="border-slate-200" />
						<div class="flex items-center justify-between text-sm">
							<span class="text-slate-600">Tarih:</span>
							<span class="font-medium">
								{selectedDate
									? new Date(selectedDate).toLocaleDateString('tr-TR', {
											day: 'numeric',
											month: 'long',
											year: 'numeric'
										})
									: '-'}
							</span>
						</div>
						<div class="flex items-center justify-between text-sm">
							<span class="text-slate-600">Saat:</span>
							<span class="font-medium">{selectedTime ?? '-'}</span>
						</div>
					</div>

					{#if selectedTime}
						<Button class="w-full" size="lg">
							Randevuyu Onayla
						</Button>
					{/if}
				</CardContent>
			</Card>
		</div>
	</div>
</div>
{/if}
