# Draft Release Notes — v1.7.0 (Android) — DO NOT PUBLISH YET

## New: Auto-Connect & Recovery (Settings → Auto-Connect & Recovery)
Five independent, opt-in (default OFF) toggles, persisted in app settings:

- **Auto-connect on app start** (Android + Desktop) — restores connection ~1.5–2s after launch.
- **Auto-connect on device restart** (Android only) — `BootReceiver` (`BOOT_COMPLETED`, `QUICKBOOT_POWERON`); needs prior VPN consent + ideally battery-optimization exemption.
- **Auto-connect on network change** (Android only) — `ConnectivityManager.NetworkCallback` with 3s debounce; re-checks status and settings after debounce; never fires while connected/connecting.
- **Auto restart after crash** (Android only) — crash handler records crash with 3-crashes-per-60s loop guard; auto-restart disabled on loop detection.
- **Auto connect after crash** (Android only) — reconnects on the post-crash launch.

Safety properties:
- Manual Disconnect always wins: any explicit stop sets a flag that suppresses all auto-connects until the user connects again.
- Single-flight: all triggers skip when status is RUNNING/TUN_ACTIVE/STARTING/VALIDATING/DATAPLANE_VALIDATED/SOCKS_READY/RECONNECTING.
- No secrets in logs or crash files (thread/class/message only); crash log capped at 32KB.
- Desktop: boot/network/crash toggles disabled with "Only available on Android". Desktop app-start works; TUNNEL mode auto-connect is skipped without admin rights (logged).

## Fixes in this change
- `Settings.getLong/putLong` added (interface + Android + Desktop) — `AutoConnectManager` did not compile without them.
- `ConnectionStatus.CONNECTING` (nonexistent) replaced with STARTING/VALIDATING checks.
- Crash pending flags: previously written by raw XML string-edit and never consumed — now committed synchronously and consumed once on next start.
- Persian strings: removed stray Cyrillic `при` from 3 labels.

## Verification status
- [ ] `./gradlew :app:testDebugUnitTest` (CrashRecoveryPolicyTest, 7 tests) — NOT RUN HERE (no JDK/Android SDK in this environment); must pass in CI/local before release
- [ ] `./gradlew assembleRelease` — NOT RUN HERE; release APKs must come from the `release.yml` workflow (`v*-android` tag) after review
- [ ] Manual checklist: `docs/AUTO_CONNECT_TEST_CHECKLIST.md` — to be executed on device

## Risks / limitations
- Boot auto-connect on some OEM ROMs (Xiaomi/Huawei/Samsung) may be blocked by aggressive background restrictions — document "battery unrestricted" requirement.
- Android 12+ boot: VPN start before user unlocks (Direct Boot) is not supported — by design, connects after unlock.
- Desktop has no boot/network/crash support in this change.
- iOS: N/A (no iOS target).
