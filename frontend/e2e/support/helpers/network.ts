import type { Page, Route } from '@playwright/test';

interface InterceptOptions {
  page: Page;
  url: string | RegExp | ((url: URL) => boolean);
  method?: string;
  fulfill?: {
    status?: number;
    contentType?: string;
    body?: string | Buffer;
    json?: unknown;
    headers?: Record<string, string>;
  };
}

/**
 * Utility to intercept and optionally fulfill network calls.
 * Follows the pattern recommended by the TEA framework.
 */
export async function interceptNetworkCall(options: InterceptOptions) {
  const { page, url, method, fulfill } = options;

  let requestBody: unknown = null;
  let responseJson: unknown = null;
  let status: number = 0;

  // Setup route BEFORE waiting for response
  await page.route(url, async (route: Route) => {
    const request = route.request();
    if (method && request.method() !== method) {
      return route.continue();
    }

    // Capture request body
    const postData = request.postData();
    if (postData) {
      try {
        requestBody = JSON.parse(postData);
      } catch {
        requestBody = postData;
      }
    }

    if (fulfill) {
      await route.fulfill(fulfill);
    } else {
      await route.continue();
    }
  });

  // Return a helper to wait for the next matching response
  return {
    waitForCall: async () => {
      const response = await page.waitForResponse(url);
      status = response.status();
      try {
        responseJson = await response.json();
      } catch {
        responseJson = await response.text();
      }
      return { requestBody, responseJson, status };
    },
  };
}
