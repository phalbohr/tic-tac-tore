import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useMatchDraftStore } from '@/features/match/stores/matchDraftStore';
import { useAuthStore } from '@/stores/auth';

describe.skip('useMatchDraftStore — Defaults Integration (ATDD RED PHASE — Story 6.2)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('[P0] should initialize draft with authStore.profile defaultRuleConfigurationId and defaultGroupId (AC 3)', async () => {
    const authStore = useAuthStore();
    authStore.profile = {
      id: 'user-1',
      nickname: 'TestUser',
      defaultGroupId: 'group-uuid-1',
      defaultRuleConfigurationId: 'rule-uuid-custom',
    } as any;

    const draftStore = useMatchDraftStore();
    draftStore.initDraft();

    expect(draftStore.ruleConfigurationId).toBe('rule-uuid-custom');
    expect(draftStore.selectedGroupId).toBe('group-uuid-1');
  });

  it('[P0] should fall back to standard preset when profile defaultRuleConfigurationId is not set', async () => {
    const authStore = useAuthStore();
    authStore.profile = {
      id: 'user-1',
      nickname: 'TestUser',
      defaultGroupId: null,
      defaultRuleConfigurationId: null,
    } as any;

    const draftStore = useMatchDraftStore();
    draftStore.initDraft();

    expect(draftStore.ruleConfigurationId).toBeNull();
    expect(draftStore.selectedGroupId).toBeNull();
  });

  it('[P0] should allow setting inline default rule template calling authStore.updateProfile (AC 4)', async () => {
    const authStore = useAuthStore();
    authStore.updateProfile = vi.fn().mockResolvedValue({});

    const draftStore = useMatchDraftStore();
    await draftStore.setDefaultRule('rule-uuid-custom');

    expect(authStore.updateProfile).toHaveBeenCalledWith({
      defaultRuleConfigurationId: 'rule-uuid-custom',
    });
  });

  it('[P0] should allow setting inline default player group calling authStore.updateProfile (AC 4)', async () => {
    const authStore = useAuthStore();
    authStore.updateProfile = vi.fn().mockResolvedValue({});

    const draftStore = useMatchDraftStore();
    await draftStore.setDefaultGroup('group-uuid-1');

    expect(authStore.updateProfile).toHaveBeenCalledWith({
      defaultGroupId: 'group-uuid-1',
    });
  });
});
