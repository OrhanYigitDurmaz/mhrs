<script lang="ts">
	import { goto } from '$app/navigation';
	import { auth } from '$lib/stores/auth.svelte';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Loader2, Stethoscope, ArrowLeft } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let tcNo = $state('');
	let fullName = $state('');
	let phone = $state('');
	let password = $state('');
	let confirmPassword = $state('');
	let isLoading = $state(false);

	function validateTCNo(tc: string): boolean {
		if (tc.length !== 11 || !/^\d+$/.test(tc)) return false;
		if (tc[0] === '0') return false;

		// Turkish ID validation algorithm
		const digits = tc.split('').map(Number);
		const tenth = (digits[0] + digits[2] + digits[4] + digits[6] + digits[8]) * 7
			- (digits[1] + digits[3] + digits[5] + digits[7]);
		if (tenth % 10 !== digits[9]) return false;

		const sum = digits.slice(0, 10).reduce((a, b) => a + b, 0);
		if (sum % 10 !== digits[10]) return false;

		return true;
	}

	async function handleRegister() {
		if (!tcNo || !fullName || !phone || !password || !confirmPassword) {
			toast.error('Lütfen tüm alanları doldurun');
			return;
		}

		if (!validateTCNo(tcNo)) {
			toast.error('Geçersiz TC kimlik numarası');
			return;
		}

		if (password !== confirmPassword) {
			toast.error('Şifreler eşleşmiyor');
			return;
		}

		if (password.length < 6) {
			toast.error('Şifre en az 6 karakter olmalıdır');
			return;
		}

		isLoading = true;
		const success = await auth.register({
			tc_no: tcNo,
			adi_soyadi: fullName,
			telefon: phone,
			sifre: password
		});
		isLoading = false;

		if (success) {
			toast.success('Kayıt başarılı');
			goto('/');
		} else {
			toast.error('Kayıt başarısız. Bu TC kimlik numarası zaten kayıtlı olabilir.');
		}
	}
</script>

<div class="flex min-h-screen items-center justify-center px-4 py-8">
	<Card class="w-full max-w-md">
		<CardHeader class="space-y-1 text-center">
			<div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-blue-100">
				<Stethoscope class="h-8 w-8 text-blue-600" />
			</div>
			<CardTitle class="text-2xl font-bold">MHRS Kayıt</CardTitle>
			<CardDescription>Merkezi Hekim Randevu Sistemi</CardDescription>
		</CardHeader>
		<CardContent>
			<form onsubmit={(e) => { e.preventDefault(); handleRegister(); }} class="space-y-4">
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
					<Label for="fullName">Adı Soyadı</Label>
					<Input
						id="fullName"
						type="text"
						placeholder="Adınız Soyadınız"
						bind:value={fullName}
						minlength={3}
						required
						disabled={isLoading}
					/>
				</div>
				<div class="space-y-2">
					<Label for="phone">Telefon</Label>
					<Input
						id="phone"
						type="tel"
						placeholder="5xxxxxxxxx"
						bind:value={phone}
						pattern="[0-9]*"
						inputmode="tel"
						required
						disabled={isLoading}
					/>
				</div>
				<div class="space-y-2">
					<Label for="password">Şifre</Label>
					<Input
						id="password"
						type="password"
						placeholder="En az 6 karakter"
						bind:value={password}
						minlength={6}
						required
						disabled={isLoading}
					/>
				</div>
				<div class="space-y-2">
					<Label for="confirmPassword">Şifre Tekrar</Label>
					<Input
						id="confirmPassword"
						type="password"
						placeholder="Şifrenizi tekrar girin"
						bind:value={confirmPassword}
						required
						disabled={isLoading}
					/>
				</div>
				<Button type="submit" class="w-full" disabled={isLoading}>
					{#if isLoading}
						<Loader2 class="mr-2 h-4 w-4 animate-spin" />
						Kaydediliyor...
					{:else}
						Kayıt Ol
					{/if}
				</Button>
			</form>

			<div class="mt-4 text-center text-sm text-slate-600">
				Zaten hesabınız var mı?
				<a href="/login" class="ml-1 font-medium text-blue-600 hover:underline">Giriş yapın</a>
			</div>

			<div class="mt-4">
				<Button variant="ghost" class="w-full" onclick={() => goto('/login')}>
					<ArrowLeft class="mr-2 h-4 w-4" />
					Giriş sayfasına dön
				</Button>
			</div>
		</CardContent>
	</Card>
</div>
