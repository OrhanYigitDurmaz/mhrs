<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { isAdmin } from '$lib/stores/auth.svelte';
	import { authApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Select, SelectContent, SelectItem, SelectTrigger } from '$lib/components/ui/select';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { Loader2, ArrowLeft, Plus, Trash2, Users } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';
	import type { User } from '$lib/api';

	let users = $state<User[]>([]);
	let isLoading = $state(false);
	let isAdding = $state(false);
	let showAddForm = $state(false);
	let isCheckingAuth = $state(true);

	let formData = $state({
		tc_no: '',
		adi_soyadi: '',
		telefon: '',
		sifre: '',
		rol: 'user'
	});

	onMount(async () => {
		isCheckingAuth = false;

		if (!$isAdmin) {
			goto('/', { replaceState: true });
			return;
		}
		await loadUsers();
	});

	async function loadUsers() {
		isLoading = true;
		try {
			// Since there's no direct API to list users, we'll use a placeholder
			// You may need to add this endpoint to your backend
			users = [];
		} catch (error) {
			console.error('Failed to load users:', error);
			toast.error('Kullanıcılar yüklenirken hata oluştu');
		}
		isLoading = false;
	}

	async function handleAddUser() {
		if (!formData.tc_no || !formData.adi_soyadi.trim() || !formData.telefon || !formData.sifre) {
			toast.error('Lütfen tüm alanları doldurun');
			return;
		}

		isAdding = true;
		try {
			await authApi.createUser({
				tc_no: formData.tc_no,
				adi_soyadi: formData.adi_soyadi,
				telefon: formData.telefon,
				sifre: formData.sifre,
				rol: formData.rol
			});
			resetForm();
			toast.success('Kullanıcı eklendi');
		} catch (error) {
			console.error('Failed to add user:', error);
			toast.error('Kullanıcı eklenirken hata oluştu');
		}
		isAdding = false;
	}

	function resetForm() {
		formData = {
			tc_no: '',
			adi_soyadi: '',
			telefon: '',
			sifre: '',
			rol: 'user'
		};
		showAddForm = false;
	}
</script>

<Navbar />

{#if isCheckingAuth || !browser}
	<div class="flex min-h-[60vh] items-center justify-center">
		<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
	</div>
{:else}
	<div class="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
		<div class="mb-8 flex items-center justify-between">
			<div class="flex items-center gap-4">
				<Button variant="ghost" href="/admin">
					<ArrowLeft class="mr-2 h-4 w-4" />
					Admin Panel
				</Button>
				<div>
					<h1 class="text-3xl font-bold text-slate-900">Kullanıcı Yönetimi</h1>
					<p class="mt-2 text-slate-600">Sistem kullanıcılarını yönetin.</p>
				</div>
			</div>
			<Button onclick={() => showAddForm = !showAddForm}>
				<Plus class="mr-2 h-4 w-4" />
				Kullanıcı Ekle
			</Button>
		</div>

		{#if showAddForm}
			<Card class="mb-8">
				<CardHeader>
					<CardTitle>Yeni Kullanıcı Ekle</CardTitle>
				</CardHeader>
				<CardContent>
					<form onsubmit={(e) => { e.preventDefault(); handleAddUser(); }} class="space-y-4">
						<div class="grid gap-4 sm:grid-cols-2">
							<div class="space-y-2">
								<Label for="tcNo">TC Kimlik No</Label>
								<Input id="tcNo" bind:value={formData.tc_no} placeholder="11 haneli TC no" maxlength={11} />
							</div>
							<div class="space-y-2">
								<Label for="role">Rol</Label>
								<select
									bind:value={formData.rol}
									class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm ring-offset-white file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-950 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
								>
									<option value="user">Kullanıcı</option>
									<option value="doktor">Doktor</option>
									<option value="admin">Admin</option>
								</select>
							</div>
						</div>
						<div class="space-y-2">
							<Label for="fullName">Adı Soyadı</Label>
							<Input id="fullName" bind:value={formData.adi_soyadi} placeholder="Ad Soyad" />
						</div>
						<div class="space-y-2">
							<Label for="phone">Telefon</Label>
							<Input id="phone" bind:value={formData.telefon} placeholder="5xxxxxxxxx" />
						</div>
						<div class="space-y-2">
							<Label for="password">Şifre</Label>
							<Input id="password" type="password" bind:value={formData.sifre} placeholder="En az 6 karakter" />
						</div>
						<div class="flex gap-2 justify-end">
							<Button type="button" variant="outline" onclick={resetForm}>İptal</Button>
							<Button type="submit" disabled={isAdding}>
								{#if isAdding}
									<Loader2 class="mr-2 h-4 w-4 animate-spin" />
									Ekleniyor...
								{:else}
									Kullanıcı Ekle
								{/if}
							</Button>
						</div>
					</form>
				</CardContent>
			</Card>
		{/if}

		<Card>
			<CardHeader>
				<CardTitle>Kullanıcı Oluştur</CardTitle>
			</CardHeader>
			<CardContent>
				<p class="text-slate-600 mb-4">
					Yeni sistem kullanıcıları oluşturun. Kullanıcılar oluşturulduktan sonra giriş yapabilirler.
				</p>
				<div class="rounded-md bg-blue-50 p-4 text-sm text-blue-800">
					<p class="font-semibold">Rol Açıklamaları:</p>
					<ul class="mt-2 list-inside list-disc space-y-1">
						<li><strong>Admin:</strong> Tüm sistem yönetim hakları</li>
						<li><strong>Doktor:</strong> Randevu ve çalışma saati yönetimi</li>
						<li><strong>Kullanıcı:</strong> Randevu alma ve yönetimi</li>
					</ul>
				</div>
			</CardContent>
		</Card>
	</div>
{/if}
