<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { auth } from '$lib/stores/auth.svelte';
	import { randevularApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Alert, AlertDescription, AlertTitle } from '$lib/components/ui/alert';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { Calendar, Clock, User, Stethoscope, MapPin, Loader2, XCircle } from 'lucide-svelte';
	import type { Randevu } from '$lib/api';
	import { toast } from 'svelte-sonner';

	let appointments = $state<Randevu[]>([]);
	let isLoading = $state(false);
	let isCancelling = $state<number | null>(null);
	let isCheckingAuth = $state(true);

	onMount(async () => {
		isCheckingAuth = false;

		if (!$auth.isAuthenticated) {
			goto('/login', { replaceState: true });
			return;
		}
		await loadAppointments();
	});

	async function loadAppointments() {
		isLoading = true;
		try {
			appointments = await randevularApi.list();
		} catch (error) {
			console.error('Failed to load appointments:', error);
			toast.error('Randevular yüklenirken hata oluştu');
		}
		isLoading = false;
	}

	async function handleCancel(randevuId: number) {
		if (!confirm('Randevuyu iptal etmek istediğinizden emin misiniz?')) {
			return;
		}

		isCancelling = randevuId;
		try {
			await randevularApi.cancel(randevuId);
			appointments = appointments.filter((r) => r.id !== randevuId);
			toast.success('Randevu iptal edildi');
		} catch (error) {
			console.error('Failed to cancel appointment:', error);
			toast.error('Randevu iptal edilirken hata oluştu');
		}
		isCancelling = null;
	}

	function getStatusVariant(status: string) {
		switch (status) {
			case 'aktif':
				return 'default';
			case 'iptal':
				return 'destructive';
			case 'tamamlandı':
				return 'secondary';
			default:
				return 'outline';
		}
	}

	function getStatusText(status: string) {
		switch (status) {
			case 'aktif':
				return 'Aktif';
			case 'iptal':
				return 'İptal';
			case 'tamamlandı':
				return 'Tamamlandı';
			default:
				return status;
		}
	}

	const activeAppointments = $derived(appointments.filter((r) => r.durum === 'aktif'));
	const pastAppointments = $derived(appointments.filter((r) => r.durum !== 'aktif'));
</script>

<Navbar />

{#if isCheckingAuth || !browser}
	<div class="flex min-h-[60vh] items-center justify-center">
		<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
	</div>
{:else}
	<div class="mx-auto max-w-5xl px-4 py-8 sm:px-6 lg:px-8">
	<!-- Header -->
	<div class="mb-8">
		<h1 class="text-3xl font-bold text-slate-900">Randevularım</h1>
		<p class="mt-2 text-slate-600">Randevularınızı buradan görüntüleyebilir ve yönetebilirsiniz.</p>
	</div>

	{#if isLoading}
		<div class="flex min-h-[400px] items-center justify-center">
			<div class="text-center">
				<Loader2 class="mx-auto h-8 w-8 animate-spin text-blue-600" />
				<p class="mt-2 text-slate-600">Yükleniyor...</p>
			</div>
		</div>
	{:else if appointments.length === 0}
		<div class="text-center py-12">
			<Calendar class="mx-auto h-16 w-16 text-slate-300" />
			<h2 class="mt-4 text-xl font-semibold text-slate-900">Randevu Bulunamadı</h2>
			<p class="mt-2 text-slate-600">Henüz bir randevunuz yok.</p>
			<Button class="mt-4" href="/">Randevu Al</Button>
		</div>
	{:else}
		<!-- Active Appointments -->
		{#if activeAppointments.length > 0}
			<div class="mb-8">
				<h2 class="mb-4 text-xl font-semibold text-slate-900">Aktif Randevular</h2>
				<div class="space-y-4">
					{#each activeAppointments as appointment}
						<Card class="border-l-4 border-l-blue-500">
							<CardHeader>
								<div class="flex items-start justify-between">
									<div>
										<CardTitle class="text-lg">{appointment.doktor?.adi_soyadi ?? 'Doktor'}</CardTitle>
										<CardDescription class="mt-1">
											{appointment.hastane?.adi} - {appointment.brans?.adi}
										</CardDescription>
									</div>
									<Badge variant={getStatusVariant(appointment.durum)}>
										{getStatusText(appointment.durum)}
									</Badge>
								</div>
							</CardHeader>
							<CardContent>
								<div class="grid gap-4 sm:grid-cols-2">
									<div class="flex items-center gap-2 text-sm text-slate-600">
										<Calendar class="h-4 w-4" />
										<span>
											{new Date(appointment.tarih).toLocaleDateString('tr-TR', {
												weekday: 'long',
												year: 'numeric',
												month: 'long',
												day: 'numeric'
											})}
										</span>
									</div>
									<div class="flex items-center gap-2 text-sm text-slate-600">
										<Clock class="h-4 w-4" />
										<span>{appointment.saat}</span>
									</div>
								</div>
								<div class="mt-4 flex justify-end">
									{#if isCancelling === appointment.id}
										<Button variant="destructive" disabled size="sm">
											<Loader2 class="mr-2 h-4 w-4 animate-spin" />
											İptal ediliyor...
										</Button>
									{:else}
										<Button
											variant="outline"
											size="sm"
											onclick={() => handleCancel(appointment.id)}
										>
											<XCircle class="mr-2 h-4 w-4" />
											Randevuyu İptal Et
										</Button>
									{/if}
								</div>
							</CardContent>
						</Card>
					{/each}
				</div>
			</div>
		{/if}

		<!-- Past Appointments -->
		{#if pastAppointments.length > 0}
			<div>
				<h2 class="mb-4 text-xl font-semibold text-slate-900">Geçmiş Randevular</h2>
				<div class="space-y-4">
					{#each pastAppointments as appointment}
						<Card class="opacity-75">
							<CardHeader>
								<div class="flex items-start justify-between">
									<div>
										<CardTitle class="text-lg text-slate-700">
											{appointment.doktor?.adi_soyadi ?? 'Doktor'}
										</CardTitle>
										<CardDescription class="mt-1">
											{appointment.hastane?.adi} - {appointment.brans?.adi}
										</CardDescription>
									</div>
									<Badge variant={getStatusVariant(appointment.durum)}>
										{getStatusText(appointment.durum)}
									</Badge>
								</div>
							</CardHeader>
							<CardContent>
								<div class="grid gap-4 sm:grid-cols-2">
									<div class="flex items-center gap-2 text-sm text-slate-600">
										<Calendar class="h-4 w-4" />
										<span>
											{new Date(appointment.tarih).toLocaleDateString('tr-TR', {
												year: 'numeric',
												month: 'long',
												day: 'numeric'
											})}
										</span>
									</div>
									<div class="flex items-center gap-2 text-sm text-slate-600">
										<Clock class="h-4 w-4" />
										<span>{appointment.saat}</span>
									</div>
								</div>
							</CardContent>
						</Card>
					{/each}
				</div>
			</div>
		{/if}
	{/if}
</div>
{/if}
