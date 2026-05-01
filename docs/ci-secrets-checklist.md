# CI Secrets Checklist

The following secrets should be configured in **GitHub Repository Settings > Secrets and variables > Actions**:

| Secret Name | Purpose | Required |
|-------------|---------|----------|
| `DOCKER_USERNAME` | (Optional) For pushing images | No |
| `DOCKER_PASSWORD` | (Optional) For pushing images | No |
| `SONAR_TOKEN` | (Optional) For SonarCloud analysis | No |

*Note: For the current Test Pipeline, no external secrets are strictly required as it uses built-in GitHub Action tokens for artifact uploads.*
