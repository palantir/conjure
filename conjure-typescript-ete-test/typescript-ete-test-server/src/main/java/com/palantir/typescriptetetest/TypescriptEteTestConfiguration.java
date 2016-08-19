/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.typescriptetetest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.palantir.indexpage.IndexPageConfigurable;
import com.palantir.multipass.dropwizard.AuthConfiguration;
import com.palantir.multipass.dropwizard.AuthConfigurationProvider;
import com.palantir.multipass.dropwizard.ProtectionType;
import com.palantir.multipass.dropwizard.ProtectionTypeEntry;
import com.palantir.websecurity.WebSecurityConfigurable;
import com.palantir.websecurity.WebSecurityConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.bundles.assets.AssetsBundleConfiguration;
import io.dropwizard.bundles.assets.AssetsConfiguration;
import java.util.List;
import javax.validation.Valid;
import org.hibernate.validator.constraints.NotBlank;

public final class TypescriptEteTestConfiguration extends Configuration
        implements AssetsBundleConfiguration, AuthConfigurationProvider, IndexPageConfigurable,
        WebSecurityConfigurable {

    @Valid
    @JsonProperty("auth")
    private Optional<AuthConfiguration> auth = Optional.absent();

    @Valid
    @JsonProperty("webSecurity")
    private WebSecurityConfiguration webSecurityConfiguration = WebSecurityConfiguration.DEFAULT;

    @Valid
    @JsonProperty("assets")
    private AssetsConfiguration assets = AssetsConfiguration.builder()
            .mimeTypes(ImmutableMap.of(
                    "eot", "application/vnd.ms-fontobject",
                    "ttf", "application/font-sfnt",
                    "woff", "application/font-woff"))
            .build();

    @NotBlank
    @JsonProperty("indexPagePath")
    private String indexPagePath = "assets/index.html";

    // for more details: https://rtfm.yojoe.local/docs/multipass/en/latest/development/dropwizard-multipass.html
    @Override
    public List<ProtectionTypeEntry> getProtectionTypes(String apiBase) {
        return ImmutableList.of(
                ProtectionTypeEntry.of(apiBase + "/**", ProtectionType.API),
                ProtectionTypeEntry.of("/**", ProtectionType.APP));
    }

    @Override
    public AssetsConfiguration getAssetsConfiguration() {
        return this.assets;
    }

    @Override
    public Optional<AuthConfiguration> getAuth() {
        return this.auth;
    }

    @Override
    public String getIndexPagePath() {
        return indexPagePath;
    }

    @Override
    public boolean getSessionsEnabled() {
        return true;
    }

    @Override
    public WebSecurityConfiguration getWebSecurityConfiguration() {
        return this.webSecurityConfiguration;
    }
}
