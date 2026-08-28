import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { CreatePoolPayload, PoolResponse } from '../types/pool';
import * as poolService from '../services/poolService';

export const usePoolStore = defineStore('pool', () => {
  const activePools = ref<PoolResponse[]>([]);
  const currentPool = ref<PoolResponse | null>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  async function createPool(payload: CreatePoolPayload): Promise<PoolResponse> {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await poolService.createPool(payload);
      currentPool.value = response;
      activePools.value.push(response);
      return response;
    } catch (err: any) {
      error.value = err.message || 'Failed to create matchmaking pool';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function fetchPool(id: string): Promise<PoolResponse> {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await poolService.fetchPoolById(id);
      currentPool.value = response;
      return response;
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch matchmaking pool';
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  return {
    activePools,
    currentPool,
    isLoading,
    error,
    createPool,
    fetchPool,
  };
});
