package com.deepseek.apiplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth")
public class OAuthConfig {
    private GiteeConfig gitee;
    private GithubConfig github;

    public GiteeConfig getGitee() { return gitee; }
    public void setGitee(GiteeConfig gitee) { this.gitee = gitee; }

    public GithubConfig getGithub() { return github; }
    public void setGithub(GithubConfig github) { this.github = github; }

    public static class GiteeConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

        public String getRedirectUri() { return redirectUri; }
        public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    }

    public static class GithubConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

        public String getRedirectUri() { return redirectUri; }
        public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    }
}
