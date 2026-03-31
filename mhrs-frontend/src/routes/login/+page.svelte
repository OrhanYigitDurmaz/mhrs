<script lang="ts">
	import { goto } from '$app/navigation';
	import { auth, isAdmin, isDoctor } from '$lib/stores/auth.svelte';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Loader2, Stethoscope } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let tcNo = $state('');
	let password = $state('');
	let isLoading = $state(false);

	async function handleLogin() {
		if (!tcNo || !password) {
			toast.error('Lütfen tüm alanları doldurun');
			return;
		}

		isLoading = true;
		const success = await auth.login(tcNo, password);
		isLoading = false;

		if (success) {
			toast.success('Giriş başarılı');
			// Redirect based on role
			if ($isAdmin) {
				goto('/admin');
			} else if ($isDoctor) {
				goto('/doktor');
			} else {
				goto('/');
			}
		} else {
			toast.error('TC kimlik numarası veya şifre hatalı');
		}
	}
</script>

<div class="flex min-h-screen items-center justify-center px-4">
	<Card class="w-full max-w-md">
		<CardHeader class="space-y-1 text-center">
			<div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-blue-100">
				<Stethoscope class="h-8 w-8 text-blue-600" />
			</div>
			<CardTitle class="text-2xl font-bold">MHRS Giriş</CardTitle>
			<CardDescription>Merkezi Hekim Randevu Sistemi</CardDescription>
		</CardHeader>
		<CardContent>
			<form onsubmit={(e) => { e.preventDefault(); handleLogin(); }} class="space-y-4">
				<div class="space-y-2">
					<Label for="tcNo">TC Kimlik No</Label>
					<Input
						id="tcNo"
						type="text"
						placeholder="11 haneli TC kimlik numarası"
						bind:value={tcNo}
						maxlength={11}
						pattern="[0-9]*"
						inputmode="numeric"
						required
						disabled={isLoading}
					/>
				</div>
				<div class="space-y-2">
					<Label for="password">Şifre</Label>
					<Input
						id="password"
						type="password"
						placeholder="Şifreniz"
						bind:value={password}
						required
						disabled={isLoading}
					/>
				</div>
				<Button type="submit" class="w-full" disabled={isLoading}>
					{#if isLoading}
						<Loader2 class="mr-2 h-4 w-4 animate-spin" />
						Giriş yapılıyor...
					{:else}
						Giriş Yap
					{/if}
				</Button>
			</form>

			<div class="mt-4 text-center text-sm text-slate-600">
				Hesabınız yok mu?
				<a href="/register" class="ml-1 font-medium text-blue-600 hover:underline">Kayıt olun</a>
			</div>

			<div class="mt-6 rounded-md bg-blue-50 p-4 text-sm text-blue-800">
				<p class="font-semibold">Test Hesapları:</p>
				<p class="mt-1">• Kullanıcı: 10000000146 / password123</p>
				<p>• Doktor: 10000000147 / password123</p>
				<p>• Admin: 10000000148 / password123</p>
			</div>
		</CardContent>
	</Card>
</div>
