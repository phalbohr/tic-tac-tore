🎯 What
Replaces `JSON.parse(JSON.stringify(profile.value))` with `{ ...profile.value }` in `frontend/src/stores/auth.ts`.
Adds a new unit test in `frontend/src/stores/__tests__/auth.spec.ts` to assert fallback properties including undefined values are successfully rolled back.

💡 Why
Using `JSON.stringify` drops fields that have `undefined` values (e.g., `tutorialCompleted`). This breaks the UI optimistic updates if a patch request fails, as the fallback state drops properties it was intended to preserve. A shallow copy correctly retains `undefined` values.

✅ Verification
- Unit test added verifying the mocked rejected patch response explicitly asserts that the original `undefined` fields are present in the resulting store profile.
- All 76 frontend unit tests execute successfully.
- Local E2E tests verified per CI failure instructions for webServer startups.

✨ Result
The `auth.ts` optimistic rollback acts safely and resiliently during failure scenarios without corrupting the store state for `undefined` properties.
