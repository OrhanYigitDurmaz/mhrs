<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { isAdmin } from '$lib/stores/auth.svelte';
	import { illerApi, branlarApi, hastanelerApi, doktorlarApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Tabs, TabsContent, TabsList, TabsTrigger } from '$lib/components/ui/tabs';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { Users, Building, User, Stethoscope, Loader2, Plus, Clock } from 'lucide-svelte';
	import type { Il, Brans, Hastane, Doktor } from '$lib/api';

	let stats = $state({
		cities: 0,
		branches: 0,
		hospitals: 0,
		doctors: 0
	});
	let isLoading = $state(false);
	let isCheckingAuth = $state(true);
	let activeTab = $state('districts');

	onMount(async () => {
		isCheckingAuth = false;

		if (!$isAdmin) {
			goto('/', { replaceState: true });
			return;
		}
		await loadStats();
	});

	async function loadStats() {
		isLoading = true;
		try {
			const [cities, branches, hospitals, doctors] = await Promise.all([
				illerApi.list(),
				branlarApi.list(),
				hastanelerApi.list(),
				doktorlarApi.list()
			]);
			stats = {
				cities: cities.length,
				branches: branches.length,
				hospitals: hospitals.length,
				doctors: doctors.length
			};
		} catch (error) {
			console.error('Failed to load stats:', error);
		}
		isLoading = false;
	}
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
		<h1 class="text-3xl font-bold text-slate-900">Yönetici Paneli</h1>
		<p class="mt-2 text-slate-600">MHRS sistem yönetimi ve istatistikler.</p>
	</div>

	{#if isLoading}
		<div class="flex min-h-[400px] items-center justify-center">
			<div class="text-center">
				<Loader2 class="mx-auto h-8 w-8 animate-spin text-blue-600" />
				<p class="mt-2 text-slate-600">Yükleniyor...</p>
			</div>
		</div>
	{:else}
		<!-- Stats Cards -->
		<div class="mb-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
			<Card>
				<CardHeader class="flex flex-row items-center justify-between pb-2">
					<CardTitle class="text-sm font-medium text-slate-600">İller</CardTitle>
					<Building class="h-4 w-4 text-slate-500" />
				</CardHeader>
				<CardContent>
					<div class="text-3xl font-bold">{stats.cities}</div>
					<p class="mt-1 text-xs text-slate-500">Toplam il sayısı</p>
				</CardContent>
			</Card>

			<Card>
				<CardHeader class="flex flex-row items-center justify-between pb-2">
					<CardTitle class="text-sm font-medium text-slate-600">Branşlar</CardTitle>
					<Stethoscope class="h-4 w-4 text-slate-500" />
				</CardHeader>
				<CardContent>
					<div class="text-3xl font-bold">{stats.branches}</div>
					<p class="mt-1 text-xs text-slate-500">Tıbbi branşlar</p>
				</CardContent>
			</Card>

			<Card>
				<CardHeader class="flex flex-row items-center justify-between pb-2">
					<CardTitle class="text-sm font-medium text-slate-600">Hastaneler</CardTitle>
					<Building class="h-4 w-4 text-slate-500" />
				</CardHeader>
				<CardContent>
					<div class="text-3xl font-bold">{stats.hospitals}</div>
					<p class="mt-1 text-xs text-slate-500">Kayıtlı hastaneler</p>
				</CardContent>
			</Card>

			<Card>
				<CardHeader class="flex flex-row items-center justify-between pb-2">
					<CardTitle class="text-sm font-medium text-slate-600">Doktorlar</CardTitle>
					<User class="h-4 w-4 text-slate-500" />
				</CardHeader>
				<CardContent>
					<div class="text-3xl font-bold">{stats.doctors}</div>
					<p class="mt-1 text-xs text-slate-500">Kayıtlı doktorlar</p>
				</CardContent>
			</Card>
		</div>

		<!-- Quick Actions -->
		<Card>
			<CardHeader>
				<CardTitle>Hızlı İşlemler</CardTitle>
				<CardDescription>Sistemi yönetmek için kullanabileceğiniz işlemler</CardDescription>
			</CardHeader>
			<CardContent>
				<Tabs value={activeTab}>
					<TabsList class="mb-4">
						<TabsTrigger onclick={() => activeTab = 'districts'} value="districts">İlçeler</TabsTrigger>
						<TabsTrigger onclick={() => activeTab = 'cities'} value="cities">İller</TabsTrigger>
						<TabsTrigger onclick={() => activeTab = 'branches'} value="branches">Branşlar</TabsTrigger>
						<TabsTrigger onclick={() => activeTab = 'hospitals'} value="hospitals">Hastaneler</TabsTrigger>
						<TabsTrigger onclick={() => activeTab = 'doctors'} value="doctors">Doktorlar</TabsTrigger>
						<TabsTrigger onclick={() => activeTab = 'hours'} value="hours">Çalışma Saatleri</TabsTrigger>
						<TabsTrigger onclick={() => activeTab = 'users'} value="users">Kullanıcılar</TabsTrigger>
					</TabsList>

					<TabsContent value="districts">
						<div class="space-y-4">
							<div class="flex items-center justify-between">
								<div>
									<h3 class="font-semibold">İlçe Yönetimi</h3>
									<p class="text-sm text-slate-600">Sistemdeki ilçeleri yönetin.</p>
								</div>
								<Button href="/admin/ilceler" size="sm">
									<Plus class="mr-2 h-4 w-4" />
									İlçe Ekle
								</Button>
							</div>
						</div>
					</TabsContent>

					<TabsContent value="cities">
						<div class="space-y-4">
							<div class="flex items-center justify-between">
								<div>
									<h3 class="font-semibold">İl Yönetimi</h3>
									<p class="text-sm text-slate-600">Sistemdeki illeri yönetin.</p>
								</div>
								<Button href="/admin/iller" size="sm">
									<Plus class="mr-2 h-4 w-4" />
									İl Ekle
								</Button>
							</div>
						</div>
					</TabsContent>

					<TabsContent value="branches">
						<div class="space-y-4">
							<div class="flex items-center justify-between">
								<div>
									<h3 class="font-semibold">Branş Yönetimi</h3>
									<p class="text-sm text-slate-600">Tıbbi branşları yönetin.</p>
								</div>
								<Button href="/admin/branlar" size="sm">
									<Plus class="mr-2 h-4 w-4" />
									Branş Ekle
								</Button>
							</div>
						</div>
					</TabsContent>

					<TabsContent value="hospitals">
						<div class="space-y-4">
							<div class="flex items-center justify-between">
								<div>
									<h3 class="font-semibold">Hastane Yönetimi</h3>
									<p class="text-sm text-slate-600">Hastaneleri yönetin.</p>
								</div>
								<Button href="/admin/hastaneler" size="sm">
									<Plus class="mr-2 h-4 w-4" />
									Hastane Ekle
								</Button>
							</div>
						</div>
					</TabsContent>

					<TabsContent value="doctors">
						<div class="space-y-4">
							<div class="flex items-center justify-between">
								<div>
									<h3 class="font-semibold">Doktor Yönetimi</h3>
									<p class="text-sm text-slate-600">Doktorları yönetin.</p>
								</div>
								<Button href="/admin/doktorlar" size="sm">
									<Plus class="mr-2 h-4 w-4" />
									Doktor Ekle
								</Button>
							</div>
						</div>
					</TabsContent>

					<TabsContent value="hours">
						<div class="space-y-4">
							<div class="flex items-center justify-between">
								<div>
									<h3 class="font-semibold">Çalışma Saati Yönetimi</h3>
									<p class="text-sm text-slate-600">Doktorların çalışma saatlerini yönetin.</p>
								</div>
								<Button href="/admin/calisma-saatleri" size="sm">
									<Clock class="mr-2 h-4 w-4" />
									Saatleri Yönet
								</Button>
							</div>
						</div>
					</TabsContent>

					<TabsContent value="users">
						<div class="space-y-4">
							<div class="flex items-center justify-between">
								<div>
									<h3 class="font-semibold">Kullanıcı Yönetimi</h3>
									<p class="text-sm text-slate-600">Sistem kullanıcılarını yönetin.</p>
								</div>
								<Button href="/admin/kullanicilar" size="sm">
									<Plus class="mr-2 h-4 w-4" />
									Kullanıcı Ekle
								</Button>
							</div>
						</div>
					</TabsContent>
				</Tabs>
			</CardContent>
		</Card>
	{/if}
</div>
{/if}
