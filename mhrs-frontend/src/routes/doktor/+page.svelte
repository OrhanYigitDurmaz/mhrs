<script lang="ts">
    import { onMount } from "svelte";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { isDoctor, auth } from "$lib/stores/auth.svelte";
    import { calismaSaatiApi, randevuSlotApi } from "$lib/api";
    import { Button } from "$lib/components/ui/button";
    import {
        Card,
        CardContent,
        CardDescription,
        CardHeader,
        CardTitle,
    } from "$lib/components/ui/card";
    import {
        Tabs,
        TabsContent,
        TabsList,
        TabsTrigger,
    } from "$lib/components/ui/tabs";
    import { Badge } from "$lib/components/ui/badge";
    import Navbar from "$lib/components/layout/navbar.svelte";
    import { Calendar, Clock, Loader2, Plus, Trash2 } from "lucide-svelte";
    import type { CalismaSaati, RandevuSlot } from "$lib/api";
    import { toast } from "svelte-sonner";

    let workingHours = $state<CalismaSaati[]>([]);
    let slots = $state<RandevuSlot[]>([]);
    let isLoading = $state(false);
    let isGenerating = $state(false);
    let isDeleting = $state<number | null>(null);
    let isCheckingAuth = $state(true);
    let activeTab = $state("slots");
    let doktorId = $state<number | null>(null);
    let showAddHourForm = $state(false);
    let isAddingHour = $state(false);

    const DAYS = [
        "Pazartesi",
        "Salı",
        "Çarşamba",
        "Perşembe",
        "Cuma",
        "Cumartesi",
        "Pazar",
    ] as const;
    const TIME_SLOTS = [
        "08:00",
        "08:30",
        "09:00",
        "09:30",
        "10:00",
        "10:30",
        "11:00",
        "11:30",
        "12:00",
        "12:30",
        "13:00",
        "13:30",
        "14:00",
        "14:30",
        "15:00",
        "15:30",
        "16:00",
        "16:30",
        "17:00",
        "17:30",
        "18:00",
    ];

    let hourForm = $state({
        gun: "Pazartesi",
        saat_bas: "09:00",
        saat_bit: "17:00",
    });

    onMount(async () => {
        isCheckingAuth = false;

        if (!$isDoctor) {
            goto("/", { replaceState: true });
            return;
        }

        // Get doctor ID from current user
        let currentUser = null;
        auth.subscribe((state) => {
            currentUser = state.user ?? null;
        })();

        if (!currentUser) {
            toast.error("Kullanıcı bilgisi bulunamadı");
            goto("/", { replaceState: true });
            return;
        }

        if (currentUser.rol === "doktor") {
            if (!currentUser.doktor_id) {
                toast.error("Doktor bilgisi bulunamadı");
                goto("/", { replaceState: true });
                return;
            }
            doktorId = currentUser.doktor_id;
        } else {
            goto("/", { replaceState: true });
            return;
        }
        await loadData(doktorId);
    });

    async function loadData(doktorId: number) {
        isLoading = true;
        try {
            const [hours, slotList] = await Promise.all([
                calismaSaatiApi.getByDoktor(doktorId),
                randevuSlotApi.getByDoktor(doktorId),
            ]);
            workingHours = hours;
            slots = slotList;
        } catch (error) {
            console.error("Failed to load data:", error);
        }
        isLoading = false;
    }

    async function handleGenerateSlots() {
        if (!doktorId) return;

        const today = new Date();
        const nextMonth = new Date(today);
        nextMonth.setDate(today.getDate() + 30);

        isGenerating = true;
        try {
            const result = await randevuSlotApi.generate(
                doktorId,
                today.toISOString().split("T")[0],
                nextMonth.toISOString().split("T")[0],
                30,
            );
            toast.success(result.message);
            await loadData(doktorId);
        } catch (error) {
            console.error("Failed to generate slots:", error);
            toast.error("Randevu slotları oluşturulurken hata oluştu");
        }
        isGenerating = false;
    }

    async function handleDeleteSlot(slotId: number) {
        isDeleting = slotId;
        try {
            await randevuSlotApi.delete(slotId);
            slots = slots.filter((s) => s.id !== slotId);
            toast.success("Slot silindi");
        } catch (error) {
            console.error("Failed to delete slot:", error);
            toast.error("Slot silinirken hata oluştu");
        }
        isDeleting = null;
    }

    async function handleDeleteHour(hourId: number) {
        try {
            await calismaSaatiApi.delete(hourId);
            workingHours = workingHours.filter((h) => h.id !== hourId);
            toast.success("Çalışma saati silindi");
        } catch (error) {
            console.error("Failed to delete hour:", error);
            toast.error("Çalışma saati silinirken hata oluştu");
        }
    }

    async function handleAddHour() {
        if (!doktorId) return;

        if (hourForm.saat_bas >= hourForm.saat_bit) {
            toast.error("Bitiş saati başlangıç saatinden sonra olmalıdır");
            return;
        }

        isAddingHour = true;
        try {
            await calismaSaatiApi.create({
                doktor_id: doktorId,
                gun: hourForm.gun,
                saat_bas: hourForm.saat_bas,
                saat_bit: hourForm.saat_bit,
            });
            await loadData(doktorId);
            hourForm = {
                gun: "Pazartesi",
                saat_bas: "09:00",
                saat_bit: "17:00",
            };
            showAddHourForm = false;
            toast.success("Çalışma saati eklendi");
        } catch (error) {
            console.error("Failed to add hour:", error);
            toast.error("Çalışma saati eklenirken hata oluştu");
        }
        isAddingHour = false;
    }

    const upcomingSlots = $derived(
        slots
            .filter((s) => new Date(s.tarih) >= new Date() && s.dolu === 0)
            .sort(
                (a, b) =>
                    new Date(a.tarih).getTime() - new Date(b.tarih).getTime(),
            )
            .slice(0, 10),
    );
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
            <h1 class="text-3xl font-bold text-slate-900">Doktor Paneli</h1>
            <p class="mt-2 text-slate-600">
                Çalışma saatlerinizi ve randevu slotlarınızı yönetin.
            </p>
        </div>

        {#if isLoading}
            <div class="flex min-h-[400px] items-center justify-center">
                <div class="text-center">
                    <Loader2
                        class="mx-auto h-8 w-8 animate-spin text-blue-600"
                    />
                    <p class="mt-2 text-slate-600">Yükleniyor...</p>
                </div>
            </div>
        {:else}
            <Tabs value={activeTab}>
                <TabsList class="mb-6">
                    <TabsTrigger
                        onclick={() => (activeTab = "slots")}
                        value="slots">Randevu Slotları</TabsTrigger
                    >
                    <TabsTrigger
                        onclick={() => (activeTab = "hours")}
                        value="hours">Çalışma Saatleri</TabsTrigger
                    >
                </TabsList>

                <!-- Slots Tab -->
                <TabsContent value="slots">
                    <div class="grid gap-6 lg:grid-cols-2">
                        <!-- Generate Slots Card -->
                        <Card>
                            <CardHeader>
                                <CardTitle>Randevu Slotları Oluştur</CardTitle>
                                <CardDescription>
                                    Çalışma saatlerinize göre otomatik olarak
                                    randevu slotları oluşturun.
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <Button
                                    onclick={handleGenerateSlots}
                                    disabled={isGenerating}
                                    class="w-full"
                                >
                                    {#if isGenerating}
                                        <Loader2
                                            class="mr-2 h-4 w-4 animate-spin"
                                        />
                                        Oluşturuluyor...
                                    {:else}
                                        <Plus class="mr-2 h-4 w-4" />
                                        30 Günlük Slot Oluştur
                                    {/if}
                                </Button>
                                <p class="mt-4 text-sm text-slate-600">
                                    Toplam {slots.length} randevu slotı mevcut.
                                </p>
                            </CardContent>
                        </Card>

                        <!-- Upcoming Slots Card -->
                        <Card>
                            <CardHeader>
                                <CardTitle>Yaklaşan Randevu Slotları</CardTitle>
                                <CardDescription
                                    >Müsait olan yaklaşan slotlar.</CardDescription
                                >
                            </CardHeader>
                            <CardContent>
                                {#if upcomingSlots.length === 0}
                                    <p
                                        class="py-4 text-center text-sm text-slate-600"
                                    >
                                        Uygun slot bulunamadı.
                                    </p>
                                {:else}
                                    <div class="space-y-2">
                                        {#each upcomingSlots as slot}
                                            <div
                                                class="flex items-center justify-between rounded-md border p-3"
                                            >
                                                <div
                                                    class="flex items-center gap-3"
                                                >
                                                    <Calendar
                                                        class="h-4 w-4 text-slate-500"
                                                    />
                                                    <div>
                                                        <p
                                                            class="text-sm font-medium"
                                                        >
                                                            {new Date(
                                                                slot.tarih,
                                                            ).toLocaleDateString(
                                                                "tr-TR",
                                                                {
                                                                    month: "short",
                                                                    day: "numeric",
                                                                },
                                                            )}
                                                        </p>
                                                        <p
                                                            class="text-xs text-slate-600"
                                                        >
                                                            {slot.saat}
                                                        </p>
                                                    </div>
                                                </div>
                                                <Badge variant="outline"
                                                    >Müsait</Badge
                                                >
                                            </div>
                                        {/each}
                                    </div>
                                {/if}
                            </CardContent>
                        </Card>
                    </div>

                    <!-- All Slots Table -->
                    <Card class="mt-6">
                        <CardHeader>
                            <CardTitle>Tüm Randevu Slotları</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div class="overflow-x-auto">
                                <table class="w-full text-sm">
                                    <thead>
                                        <tr class="border-b">
                                            <th
                                                class="py-3 text-left font-medium"
                                                >Tarih</th
                                            >
                                            <th
                                                class="py-3 text-left font-medium"
                                                >Saat</th
                                            >
                                            <th
                                                class="py-3 text-left font-medium"
                                                >Durum</th
                                            >
                                            <th
                                                class="py-3 text-right font-medium"
                                                >İşlem</th
                                            >
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {#each slots.slice(0, 20) as slot}
                                            <tr class="border-b">
                                                <td class="py-3">
                                                    {new Date(
                                                        slot.tarih,
                                                    ).toLocaleDateString(
                                                        "tr-TR",
                                                        {
                                                            month: "long",
                                                            day: "numeric",
                                                            year: "numeric",
                                                        },
                                                    )}
                                                </td>
                                                <td class="py-3">{slot.saat}</td
                                                >
                                                <td class="py-3">
                                                    <Badge
                                                        variant={slot.dolu > 0
                                                            ? "destructive"
                                                            : "default"}
                                                    >
                                                        {slot.dolu > 0
                                                            ? "Dolu"
                                                            : "Müsait"}
                                                    </Badge>
                                                </td>
                                                <td class="py-3 text-right">
                                                    {#if isDeleting === slot.id}
                                                        <Loader2
                                                            class="mx-auto h-4 w-4 animate-spin"
                                                        />
                                                    {:else}
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onclick={() =>
                                                                handleDeleteSlot(
                                                                    slot.id,
                                                                )}
                                                        >
                                                            <Trash2
                                                                class="h-4 w-4"
                                                            />
                                                        </Button>
                                                    {/if}
                                                </td>
                                            </tr>
                                        {/each}
                                    </tbody>
                                </table>
                            </div>
                        </CardContent>
                    </Card>
                </TabsContent>

                <!-- Working Hours Tab -->
                <TabsContent value="hours">
                    <div class="grid gap-6 lg:grid-cols-3">
                        <!-- Working Hours List -->
                        <div class="lg:col-span-2">
                            <Card>
                                <CardHeader
                                    class="flex flex-row items-center justify-between"
                                >
                                    <div>
                                        <CardTitle>Çalışma Saatleri</CardTitle>
                                        <CardDescription
                                            >Haftalık çalışma saatlerinizi
                                            buradan yönetin.</CardDescription
                                        >
                                    </div>
                                    <Button
                                        onclick={() =>
                                            (showAddHourForm =
                                                !showAddHourForm)}
                                        size="sm"
                                    >
                                        <Plus class="mr-2 h-4 w-4" />
                                        Saat Ekle
                                    </Button>
                                </CardHeader>
                                <CardContent>
                                    {#if workingHours.length === 0}
                                        <div class="py-8 text-center">
                                            <Clock
                                                class="mx-auto h-12 w-12 text-slate-300"
                                            />
                                            <p class="mt-4 text-slate-600">
                                                Henüz çalışma saati
                                                tanımlanmamış.
                                            </p>
                                        </div>
                                    {:else}
                                        <div class="space-y-4">
                                            {#each DAYS as day}
                                                {#if workingHours.filter((h) => h.gun === day).length > 0}
                                                    <div>
                                                        <h4
                                                            class="mb-2 text-sm font-medium text-slate-700"
                                                        >
                                                            {day}
                                                        </h4>
                                                        <div class="space-y-2">
                                                            {#each workingHours.filter((h) => h.gun === day) as hour}
                                                                <div
                                                                    class="flex items-center justify-between rounded-md border p-3"
                                                                >
                                                                    <div
                                                                        class="flex items-center gap-3"
                                                                    >
                                                                        <Clock
                                                                            class="h-4 w-4 text-slate-500"
                                                                        />
                                                                        <span
                                                                            class="text-sm"
                                                                        >
                                                                            <span
                                                                                class="font-medium"
                                                                                >{hour.saat_bas}</span
                                                                            >
                                                                            <span
                                                                                class="text-slate-500 mx-1"
                                                                                >-</span
                                                                            >
                                                                            <span
                                                                                class="font-medium"
                                                                                >{hour.saat_bit}</span
                                                                            >
                                                                        </span>
                                                                    </div>
                                                                    <Button
                                                                        variant="ghost"
                                                                        size="sm"
                                                                        onclick={() =>
                                                                            handleDeleteHour(
                                                                                hour.id,
                                                                            )}
                                                                    >
                                                                        <Trash2
                                                                            class="h-4 w-4"
                                                                        />
                                                                    </Button>
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

                        <!-- Add Hour Form -->
                        {#if showAddHourForm}
                            <Card>
                                <CardHeader>
                                    <CardTitle>Yeni Çalışma Saati</CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <form
                                        onsubmit={(e) => {
                                            e.preventDefault();
                                            handleAddHour();
                                        }}
                                        class="space-y-4"
                                    >
                                        <div class="space-y-2">
                                            <label
                                                for="gun"
                                                class="text-sm font-medium"
                                                >Gün</label
                                            >
                                            <select
                                                id="gun"
                                                bind:value={hourForm.gun}
                                                class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm"
                                            >
                                                {#each DAYS as day}
                                                    <option value={day}
                                                        >{day}</option
                                                    >
                                                {/each}
                                            </select>
                                        </div>

                                        <div class="space-y-2">
                                            <label
                                                for="baslangic"
                                                class="text-sm font-medium"
                                                >Başlangıç</label
                                            >
                                            <select
                                                id="baslangic"
                                                bind:value={hourForm.saat_bas}
                                                class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm"
                                            >
                                                {#each TIME_SLOTS as time}
                                                    <option value={time}
                                                        >{time}</option
                                                    >
                                                {/each}
                                            </select>
                                        </div>

                                        <div class="space-y-2">
                                            <label
                                                for="bitis"
                                                class="text-sm font-medium"
                                                >Bitiş</label
                                            >
                                            <select
                                                id="bitis"
                                                bind:value={hourForm.saat_bit}
                                                class="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm"
                                            >
                                                {#each TIME_SLOTS as time}
                                                    <option value={time}
                                                        >{time}</option
                                                    >
                                                {/each}
                                            </select>
                                        </div>

                                        <div class="flex gap-2">
                                            <Button
                                                type="button"
                                                variant="outline"
                                                onclick={() =>
                                                    (showAddHourForm = false)}
                                                class="flex-1"
                                            >
                                                İptal
                                            </Button>
                                            <Button
                                                type="submit"
                                                disabled={isAddingHour}
                                                class="flex-1"
                                            >
                                                {#if isAddingHour}
                                                    <Loader2
                                                        class="mr-2 h-4 w-4 animate-spin"
                                                    />
                                                {/if}
                                                Ekle
                                            </Button>
                                        </div>
                                    </form>
                                </CardContent>
                            </Card>
                        {/if}
                    </div>
                </TabsContent>
            </Tabs>
        {/if}
    </div>
{/if}
