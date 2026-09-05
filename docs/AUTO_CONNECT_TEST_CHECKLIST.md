# Auto-Connect & Crash Recovery — Manual Test Checklist

Feature branch work on `Omarchy71/Aether-fork`. All 5 toggles live in
Settings → **Auto-Connect & Recovery**. Defaults are OFF.

## Setup
- [ ] Fresh install (or clear app data) → open Settings → Auto-Connect & Recovery
- [ ] All 5 toggles visible, all OFF by default
- [ ] Toggle each ON, kill app from recents, reopen → all 5 persist ON
- [ ] Toggle each OFF again → persist OFF after restart

## 1. Auto-connect on app start
- [ ] Connect VPN manually → disconnect via button → enable toggle → force-close app → reopen → connects automatically (~1.5s delay)
- [ ] With toggle ON: tap Disconnect → force-close → reopen → does NOT connect (manual-disconnect respected)
- [ ] With toggle OFF: connected? close → reopen → does NOT connect

## 2. Auto-connect on device restart
- [ ] Enable toggle, connect, reboot device → app connects after boot (requires VPN permission previously granted; battery-optimization exempt recommended)
- [ ] Disable toggle → reboot → no connect
- [ ] Manual disconnect before reboot → reboot → no connect

## 3. Auto-connect on network change
- [ ] Disconnect VPN, enable toggle → switch Wi-Fi ↔ mobile data → reconnects once (not repeatedly)
- [ ] Toggle Wi-Fi off/on rapidly 3× → single reconnect, no loop (3s debounce)
- [ ] Airplane mode on (VPN disconnected) → airplane off → reconnects when network validates
- [ ] Toggle OFF → network switch → no reconnect

## 4. Crash recovery
- [ ] Enable both crash toggles → force a crash (e.g. dev menu / bad config) → relaunch → app restarts and reconnects
- [ ] Enable restart but NOT auto-connect → relaunch after crash → app opens, stays disconnected
- [ ] Both OFF → crash → relaunch → stays disconnected
- [ ] Crash 4× within 60s → 4th relaunch does NOT auto-restart; toggle auto-disabled; log shows "Crash loop detected"

## 5. No-interference checks (all toggles OFF)
- [ ] Normal connect/disconnect works as before
- [ ] Tile, widget, Quick-Settings actions unaffected
- [ ] No unexpected log spam (`[AutoConnect]` lines only when enabled)

## 6. Desktop (Windows/Linux)
- [ ] Only "Auto connect on app start" toggle enabled; boot/network/crash show "Only available on Android" and are disabled
- [ ] Enable app-start toggle → restart app → connects (PROXY modes always; TUNNEL mode only when run as admin, otherwise skipped with log)
- [ ] No crash on startup when toggle enabled

## 7. Language
- [ ] Switch app language EN → FA → settings labels render correctly (no `при` artifacts)
