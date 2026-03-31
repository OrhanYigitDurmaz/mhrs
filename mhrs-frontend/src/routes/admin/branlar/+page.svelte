<script lang="ts">
	import { onMount } from 'svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { isAdmin } from '$lib/stores/auth.svelte';
	import { branlarApi } from '$lib/api';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import Navbar from '$lib/components/layout/navbar.svelte';
	import { Loader2, ArrowLeft, Plus, Trash2 } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';
	import type { Brans } from '$lib/api';

	let branches = $state<Brans[]>([]);
	let isLoading = $state(false);
	let isDeleting = $state<number | null>(null);
	let isAdding = $state(false);
	let newBranchName = $state('');
	let isCheckingAuth = $state(true);

	onMount(async () => {
		isCheckingAuth = false;

		if (!$isAdmin) {
			goto('/', { replaceState: true });
			return;
		}
		await loadBranches();
	});

	async function loadBranches() {
		isLoading = true;
		try {
			branches = await branlarApi.list();
		} catch (error) {
			console.error('Failed to load branches:', error);
			toast.error('Branşlar yüklenirken hata oluştu');
		}
		isLoading = false;
	}

	async function handleAddBranch() {
		if (!newBranchName.trim()) {
			toast.error('Branş adı boş olamaz');
			return;
		}

		isAdding = true;
		try {
			await branlarApi.create({ adi: newBranchName });
			await loadBranches();
			newBranchName = '';
			toast.success('Branş eklendi');
		} catch (error) {
			console.error('Failed to add branch:', error);
			toast.error('Branş eklenirken hata oluştu');
		}
		isAdding = false;
	}

	async function handleDeleteBranch(branchId: number) {
		if (!confirm('Bu branşı silmek istediğinizden emin misiniz?')) {
			return;
		}

		isDeleting = branchId;
		try {
			await branlarApi.delete(branchId);
			branches = branches.filter((b) => b.id !== branchId);
			toast.success('Branş silindi');
		} catch (error) {
			console.error('Failed to delete branch:', error);
			toast.error('Branş silinirken hata oluştu');
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
		<div class="mb-8 flex items-center gap-4">
			<Button variant="ghost" href="/admin">
				<ArrowLeft class="mr-2 h-4 w-4" />
				Admin Panel
			</Button>
			<div>
				<h1 class="text-3xl font-bold text-slate-900">Branş Yönetimi</h1>
				<p class="mt-2 text-slate-600">Tıbbi branşları yönetin.</p>
			</div>
		</div>

		<Card class="mb-8">
			<CardHeader>
				<CardTitle>Yeni Branş Ekle</CardTitle>
			</CardHeader>
			<CardContent>
				<form onsubmit={(e) => { e.preventDefault(); handleAddBranch(); }} class="flex gap-4">
					<div class="flex-1">
						<Label for="branchName">Branş Adı</Label>
						<Input
							id="branchName"
							bind:value={newBranchName}
							placeholder="Örn: Kardiyoloji"
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
								Branş Ekle
							{/if}
						</Button>
					</div>
				</form>
			</CardContent>
		</Card>

		<Card>
			<CardHeader>
				<CardTitle>Kayıtlı Branşlar ({branches.length})</CardTitle>
			</CardHeader>
			<CardContent>
				{#if isLoading}
					<div class="flex min-h-[200px] items-center justify-center">
						<Loader2 class="h-8 w-8 animate-spin text-blue-600" />
					</div>
				{:else if branches.length === 0}
					<div class="py-8 text-center text-slate-500">Henüz branş eklenmemiş.</div>
				{:else}
					<div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
						{#each branches as branch}
							<div class="flex items-center justify-between rounded-md border p-4">
								<span class="font-medium">{branch.adi}</span>
								{#if isDeleting === branch.id}
									<Loader2 class="h-4 w-4 animate-spin text-slate-400" />
								{:else}
									<Button
										variant="ghost"
										size="sm"
										onclick={() => handleDeleteBranch(branch.id)}
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
