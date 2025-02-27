package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final ComLibraryAccessors laccForComLibraryAccessors = new ComLibraryAccessors(owner);
    private final JakartaLibraryAccessors laccForJakartaLibraryAccessors = new JakartaLibraryAccessors(owner);
    private final OrgLibraryAccessors laccForOrgLibraryAccessors = new OrgLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

    /**
     * Returns the group of libraries at com
     */
    public ComLibraryAccessors getCom() {
        return laccForComLibraryAccessors;
    }

    /**
     * Returns the group of libraries at jakarta
     */
    public JakartaLibraryAccessors getJakarta() {
        return laccForJakartaLibraryAccessors;
    }

    /**
     * Returns the group of libraries at org
     */
    public OrgLibraryAccessors getOrg() {
        return laccForOrgLibraryAccessors;
    }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class ComLibraryAccessors extends SubDependencyFactory {
        private final ComFasterxmlLibraryAccessors laccForComFasterxmlLibraryAccessors = new ComFasterxmlLibraryAccessors(owner);

        public ComLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.fasterxml
         */
        public ComFasterxmlLibraryAccessors getFasterxml() {
            return laccForComFasterxmlLibraryAccessors;
        }

    }

    public static class ComFasterxmlLibraryAccessors extends SubDependencyFactory {
        private final ComFasterxmlJacksonLibraryAccessors laccForComFasterxmlJacksonLibraryAccessors = new ComFasterxmlJacksonLibraryAccessors(owner);

        public ComFasterxmlLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.fasterxml.jackson
         */
        public ComFasterxmlJacksonLibraryAccessors getJackson() {
            return laccForComFasterxmlJacksonLibraryAccessors;
        }

    }

    public static class ComFasterxmlJacksonLibraryAccessors extends SubDependencyFactory {
        private final ComFasterxmlJacksonCoreLibraryAccessors laccForComFasterxmlJacksonCoreLibraryAccessors = new ComFasterxmlJacksonCoreLibraryAccessors(owner);

        public ComFasterxmlJacksonLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.fasterxml.jackson.core
         */
        public ComFasterxmlJacksonCoreLibraryAccessors getCore() {
            return laccForComFasterxmlJacksonCoreLibraryAccessors;
        }

    }

    public static class ComFasterxmlJacksonCoreLibraryAccessors extends SubDependencyFactory {
        private final ComFasterxmlJacksonCoreJacksonLibraryAccessors laccForComFasterxmlJacksonCoreJacksonLibraryAccessors = new ComFasterxmlJacksonCoreJacksonLibraryAccessors(owner);

        public ComFasterxmlJacksonCoreLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.fasterxml.jackson.core.jackson
         */
        public ComFasterxmlJacksonCoreJacksonLibraryAccessors getJackson() {
            return laccForComFasterxmlJacksonCoreJacksonLibraryAccessors;
        }

    }

    public static class ComFasterxmlJacksonCoreJacksonLibraryAccessors extends SubDependencyFactory {

        public ComFasterxmlJacksonCoreJacksonLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for annotations (com.fasterxml.jackson.core:jackson-annotations)
         * with versionRef 'com.fasterxml.jackson.core.jackson.annotations'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getAnnotations() {
                return create("com.fasterxml.jackson.core.jackson.annotations");
        }

            /**
             * Creates a dependency provider for core (com.fasterxml.jackson.core:jackson-core)
         * with versionRef 'com.fasterxml.jackson.core.jackson.core'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCore() {
                return create("com.fasterxml.jackson.core.jackson.core");
        }

            /**
             * Creates a dependency provider for databind (com.fasterxml.jackson.core:jackson-databind)
         * with versionRef 'com.fasterxml.jackson.core.jackson.databind'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getDatabind() {
                return create("com.fasterxml.jackson.core.jackson.databind");
        }

    }

    public static class JakartaLibraryAccessors extends SubDependencyFactory {
        private final JakartaEnterpriseLibraryAccessors laccForJakartaEnterpriseLibraryAccessors = new JakartaEnterpriseLibraryAccessors(owner);
        private final JakartaServletLibraryAccessors laccForJakartaServletLibraryAccessors = new JakartaServletLibraryAccessors(owner);
        private final JakartaValidationLibraryAccessors laccForJakartaValidationLibraryAccessors = new JakartaValidationLibraryAccessors(owner);
        private final JakartaWebsocketLibraryAccessors laccForJakartaWebsocketLibraryAccessors = new JakartaWebsocketLibraryAccessors(owner);

        public JakartaLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.enterprise
         */
        public JakartaEnterpriseLibraryAccessors getEnterprise() {
            return laccForJakartaEnterpriseLibraryAccessors;
        }

        /**
         * Returns the group of libraries at jakarta.servlet
         */
        public JakartaServletLibraryAccessors getServlet() {
            return laccForJakartaServletLibraryAccessors;
        }

        /**
         * Returns the group of libraries at jakarta.validation
         */
        public JakartaValidationLibraryAccessors getValidation() {
            return laccForJakartaValidationLibraryAccessors;
        }

        /**
         * Returns the group of libraries at jakarta.websocket
         */
        public JakartaWebsocketLibraryAccessors getWebsocket() {
            return laccForJakartaWebsocketLibraryAccessors;
        }

    }

    public static class JakartaEnterpriseLibraryAccessors extends SubDependencyFactory {
        private final JakartaEnterpriseJakartaLibraryAccessors laccForJakartaEnterpriseJakartaLibraryAccessors = new JakartaEnterpriseJakartaLibraryAccessors(owner);

        public JakartaEnterpriseLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.enterprise.jakarta
         */
        public JakartaEnterpriseJakartaLibraryAccessors getJakarta() {
            return laccForJakartaEnterpriseJakartaLibraryAccessors;
        }

    }

    public static class JakartaEnterpriseJakartaLibraryAccessors extends SubDependencyFactory {
        private final JakartaEnterpriseJakartaEnterpriseLibraryAccessors laccForJakartaEnterpriseJakartaEnterpriseLibraryAccessors = new JakartaEnterpriseJakartaEnterpriseLibraryAccessors(owner);

        public JakartaEnterpriseJakartaLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.enterprise.jakarta.enterprise
         */
        public JakartaEnterpriseJakartaEnterpriseLibraryAccessors getEnterprise() {
            return laccForJakartaEnterpriseJakartaEnterpriseLibraryAccessors;
        }

    }

    public static class JakartaEnterpriseJakartaEnterpriseLibraryAccessors extends SubDependencyFactory {
        private final JakartaEnterpriseJakartaEnterpriseCdiLibraryAccessors laccForJakartaEnterpriseJakartaEnterpriseCdiLibraryAccessors = new JakartaEnterpriseJakartaEnterpriseCdiLibraryAccessors(owner);

        public JakartaEnterpriseJakartaEnterpriseLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.enterprise.jakarta.enterprise.cdi
         */
        public JakartaEnterpriseJakartaEnterpriseCdiLibraryAccessors getCdi() {
            return laccForJakartaEnterpriseJakartaEnterpriseCdiLibraryAccessors;
        }

    }

    public static class JakartaEnterpriseJakartaEnterpriseCdiLibraryAccessors extends SubDependencyFactory {

        public JakartaEnterpriseJakartaEnterpriseCdiLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (jakarta.enterprise:jakarta.enterprise.cdi-api)
         * with versionRef 'jakarta.enterprise.jakarta.enterprise.cdi.api'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("jakarta.enterprise.jakarta.enterprise.cdi.api");
        }

    }

    public static class JakartaServletLibraryAccessors extends SubDependencyFactory {
        private final JakartaServletJakartaLibraryAccessors laccForJakartaServletJakartaLibraryAccessors = new JakartaServletJakartaLibraryAccessors(owner);

        public JakartaServletLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.servlet.jakarta
         */
        public JakartaServletJakartaLibraryAccessors getJakarta() {
            return laccForJakartaServletJakartaLibraryAccessors;
        }

    }

    public static class JakartaServletJakartaLibraryAccessors extends SubDependencyFactory {
        private final JakartaServletJakartaServletLibraryAccessors laccForJakartaServletJakartaServletLibraryAccessors = new JakartaServletJakartaServletLibraryAccessors(owner);

        public JakartaServletJakartaLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.servlet.jakarta.servlet
         */
        public JakartaServletJakartaServletLibraryAccessors getServlet() {
            return laccForJakartaServletJakartaServletLibraryAccessors;
        }

    }

    public static class JakartaServletJakartaServletLibraryAccessors extends SubDependencyFactory {

        public JakartaServletJakartaServletLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (jakarta.servlet:jakarta.servlet-api)
         * with versionRef 'jakarta.servlet.jakarta.servlet.api'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("jakarta.servlet.jakarta.servlet.api");
        }

    }

    public static class JakartaValidationLibraryAccessors extends SubDependencyFactory {
        private final JakartaValidationJakartaLibraryAccessors laccForJakartaValidationJakartaLibraryAccessors = new JakartaValidationJakartaLibraryAccessors(owner);

        public JakartaValidationLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.validation.jakarta
         */
        public JakartaValidationJakartaLibraryAccessors getJakarta() {
            return laccForJakartaValidationJakartaLibraryAccessors;
        }

    }

    public static class JakartaValidationJakartaLibraryAccessors extends SubDependencyFactory {
        private final JakartaValidationJakartaValidationLibraryAccessors laccForJakartaValidationJakartaValidationLibraryAccessors = new JakartaValidationJakartaValidationLibraryAccessors(owner);

        public JakartaValidationJakartaLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.validation.jakarta.validation
         */
        public JakartaValidationJakartaValidationLibraryAccessors getValidation() {
            return laccForJakartaValidationJakartaValidationLibraryAccessors;
        }

    }

    public static class JakartaValidationJakartaValidationLibraryAccessors extends SubDependencyFactory {

        public JakartaValidationJakartaValidationLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (jakarta.validation:jakarta.validation-api)
         * with versionRef 'jakarta.validation.jakarta.validation.api'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("jakarta.validation.jakarta.validation.api");
        }

    }

    public static class JakartaWebsocketLibraryAccessors extends SubDependencyFactory {
        private final JakartaWebsocketJakartaLibraryAccessors laccForJakartaWebsocketJakartaLibraryAccessors = new JakartaWebsocketJakartaLibraryAccessors(owner);

        public JakartaWebsocketLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.websocket.jakarta
         */
        public JakartaWebsocketJakartaLibraryAccessors getJakarta() {
            return laccForJakartaWebsocketJakartaLibraryAccessors;
        }

    }

    public static class JakartaWebsocketJakartaLibraryAccessors extends SubDependencyFactory {
        private final JakartaWebsocketJakartaWebsocketLibraryAccessors laccForJakartaWebsocketJakartaWebsocketLibraryAccessors = new JakartaWebsocketJakartaWebsocketLibraryAccessors(owner);

        public JakartaWebsocketJakartaLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at jakarta.websocket.jakarta.websocket
         */
        public JakartaWebsocketJakartaWebsocketLibraryAccessors getWebsocket() {
            return laccForJakartaWebsocketJakartaWebsocketLibraryAccessors;
        }

    }

    public static class JakartaWebsocketJakartaWebsocketLibraryAccessors extends SubDependencyFactory {
        private final JakartaWebsocketJakartaWebsocketClientLibraryAccessors laccForJakartaWebsocketJakartaWebsocketClientLibraryAccessors = new JakartaWebsocketJakartaWebsocketClientLibraryAccessors(owner);

        public JakartaWebsocketJakartaWebsocketLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (jakarta.websocket:jakarta.websocket-api)
         * with versionRef 'jakarta.websocket.jakarta.websocket.api'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("jakarta.websocket.jakarta.websocket.api");
        }

        /**
         * Returns the group of libraries at jakarta.websocket.jakarta.websocket.client
         */
        public JakartaWebsocketJakartaWebsocketClientLibraryAccessors getClient() {
            return laccForJakartaWebsocketJakartaWebsocketClientLibraryAccessors;
        }

    }

    public static class JakartaWebsocketJakartaWebsocketClientLibraryAccessors extends SubDependencyFactory {

        public JakartaWebsocketJakartaWebsocketClientLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (jakarta.websocket:jakarta.websocket-client-api)
         * with versionRef 'jakarta.websocket.jakarta.websocket.client.api'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("jakarta.websocket.jakarta.websocket.client.api");
        }

    }

    public static class OrgLibraryAccessors extends SubDependencyFactory {
        private final OrgBouncycastleLibraryAccessors laccForOrgBouncycastleLibraryAccessors = new OrgBouncycastleLibraryAccessors(owner);
        private final OrgSlf4jLibraryAccessors laccForOrgSlf4jLibraryAccessors = new OrgSlf4jLibraryAccessors(owner);

        public OrgLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.bouncycastle
         */
        public OrgBouncycastleLibraryAccessors getBouncycastle() {
            return laccForOrgBouncycastleLibraryAccessors;
        }

        /**
         * Returns the group of libraries at org.slf4j
         */
        public OrgSlf4jLibraryAccessors getSlf4j() {
            return laccForOrgSlf4jLibraryAccessors;
        }

    }

    public static class OrgBouncycastleLibraryAccessors extends SubDependencyFactory {
        private final OrgBouncycastleBcpkixLibraryAccessors laccForOrgBouncycastleBcpkixLibraryAccessors = new OrgBouncycastleBcpkixLibraryAccessors(owner);
        private final OrgBouncycastleBcprovLibraryAccessors laccForOrgBouncycastleBcprovLibraryAccessors = new OrgBouncycastleBcprovLibraryAccessors(owner);

        public OrgBouncycastleLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.bouncycastle.bcpkix
         */
        public OrgBouncycastleBcpkixLibraryAccessors getBcpkix() {
            return laccForOrgBouncycastleBcpkixLibraryAccessors;
        }

        /**
         * Returns the group of libraries at org.bouncycastle.bcprov
         */
        public OrgBouncycastleBcprovLibraryAccessors getBcprov() {
            return laccForOrgBouncycastleBcprovLibraryAccessors;
        }

    }

    public static class OrgBouncycastleBcpkixLibraryAccessors extends SubDependencyFactory {

        public OrgBouncycastleBcpkixLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for jdk18on (org.bouncycastle:bcpkix-jdk18on)
         * with versionRef 'org.bouncycastle.bcpkix.jdk18on'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJdk18on() {
                return create("org.bouncycastle.bcpkix.jdk18on");
        }

    }

    public static class OrgBouncycastleBcprovLibraryAccessors extends SubDependencyFactory {

        public OrgBouncycastleBcprovLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for jdk18on (org.bouncycastle:bcprov-jdk18on)
         * with versionRef 'org.bouncycastle.bcprov.jdk18on'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJdk18on() {
                return create("org.bouncycastle.bcprov.jdk18on");
        }

    }

    public static class OrgSlf4jLibraryAccessors extends SubDependencyFactory {
        private final OrgSlf4jSlf4jLibraryAccessors laccForOrgSlf4jSlf4jLibraryAccessors = new OrgSlf4jSlf4jLibraryAccessors(owner);

        public OrgSlf4jLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.slf4j.slf4j
         */
        public OrgSlf4jSlf4jLibraryAccessors getSlf4j() {
            return laccForOrgSlf4jSlf4jLibraryAccessors;
        }

    }

    public static class OrgSlf4jSlf4jLibraryAccessors extends SubDependencyFactory {

        public OrgSlf4jSlf4jLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (org.slf4j:slf4j-api)
         * with versionRef 'org.slf4j.slf4j.api'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("org.slf4j.slf4j.api");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        private final ComVersionAccessors vaccForComVersionAccessors = new ComVersionAccessors(providers, config);
        private final JakartaVersionAccessors vaccForJakartaVersionAccessors = new JakartaVersionAccessors(providers, config);
        private final OrgVersionAccessors vaccForOrgVersionAccessors = new OrgVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com
         */
        public ComVersionAccessors getCom() {
            return vaccForComVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.jakarta
         */
        public JakartaVersionAccessors getJakarta() {
            return vaccForJakartaVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.org
         */
        public OrgVersionAccessors getOrg() {
            return vaccForOrgVersionAccessors;
        }

    }

    public static class ComVersionAccessors extends VersionFactory  {

        private final ComFasterxmlVersionAccessors vaccForComFasterxmlVersionAccessors = new ComFasterxmlVersionAccessors(providers, config);
        public ComVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.fasterxml
         */
        public ComFasterxmlVersionAccessors getFasterxml() {
            return vaccForComFasterxmlVersionAccessors;
        }

    }

    public static class ComFasterxmlVersionAccessors extends VersionFactory  {

        private final ComFasterxmlJacksonVersionAccessors vaccForComFasterxmlJacksonVersionAccessors = new ComFasterxmlJacksonVersionAccessors(providers, config);
        public ComFasterxmlVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.fasterxml.jackson
         */
        public ComFasterxmlJacksonVersionAccessors getJackson() {
            return vaccForComFasterxmlJacksonVersionAccessors;
        }

    }

    public static class ComFasterxmlJacksonVersionAccessors extends VersionFactory  {

        private final ComFasterxmlJacksonCoreVersionAccessors vaccForComFasterxmlJacksonCoreVersionAccessors = new ComFasterxmlJacksonCoreVersionAccessors(providers, config);
        public ComFasterxmlJacksonVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.fasterxml.jackson.core
         */
        public ComFasterxmlJacksonCoreVersionAccessors getCore() {
            return vaccForComFasterxmlJacksonCoreVersionAccessors;
        }

    }

    public static class ComFasterxmlJacksonCoreVersionAccessors extends VersionFactory  {

        private final ComFasterxmlJacksonCoreJacksonVersionAccessors vaccForComFasterxmlJacksonCoreJacksonVersionAccessors = new ComFasterxmlJacksonCoreJacksonVersionAccessors(providers, config);
        public ComFasterxmlJacksonCoreVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.fasterxml.jackson.core.jackson
         */
        public ComFasterxmlJacksonCoreJacksonVersionAccessors getJackson() {
            return vaccForComFasterxmlJacksonCoreJacksonVersionAccessors;
        }

    }

    public static class ComFasterxmlJacksonCoreJacksonVersionAccessors extends VersionFactory  {

        public ComFasterxmlJacksonCoreJacksonVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.fasterxml.jackson.core.jackson.annotations (2.14.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAnnotations() { return getVersion("com.fasterxml.jackson.core.jackson.annotations"); }

            /**
             * Returns the version associated to this alias: com.fasterxml.jackson.core.jackson.core (2.14.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getCore() { return getVersion("com.fasterxml.jackson.core.jackson.core"); }

            /**
             * Returns the version associated to this alias: com.fasterxml.jackson.core.jackson.databind (2.14.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getDatabind() { return getVersion("com.fasterxml.jackson.core.jackson.databind"); }

    }

    public static class JakartaVersionAccessors extends VersionFactory  {

        private final JakartaEnterpriseVersionAccessors vaccForJakartaEnterpriseVersionAccessors = new JakartaEnterpriseVersionAccessors(providers, config);
        private final JakartaServletVersionAccessors vaccForJakartaServletVersionAccessors = new JakartaServletVersionAccessors(providers, config);
        private final JakartaValidationVersionAccessors vaccForJakartaValidationVersionAccessors = new JakartaValidationVersionAccessors(providers, config);
        private final JakartaWebsocketVersionAccessors vaccForJakartaWebsocketVersionAccessors = new JakartaWebsocketVersionAccessors(providers, config);
        public JakartaVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.enterprise
         */
        public JakartaEnterpriseVersionAccessors getEnterprise() {
            return vaccForJakartaEnterpriseVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.jakarta.servlet
         */
        public JakartaServletVersionAccessors getServlet() {
            return vaccForJakartaServletVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.jakarta.validation
         */
        public JakartaValidationVersionAccessors getValidation() {
            return vaccForJakartaValidationVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.jakarta.websocket
         */
        public JakartaWebsocketVersionAccessors getWebsocket() {
            return vaccForJakartaWebsocketVersionAccessors;
        }

    }

    public static class JakartaEnterpriseVersionAccessors extends VersionFactory  {

        private final JakartaEnterpriseJakartaVersionAccessors vaccForJakartaEnterpriseJakartaVersionAccessors = new JakartaEnterpriseJakartaVersionAccessors(providers, config);
        public JakartaEnterpriseVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.enterprise.jakarta
         */
        public JakartaEnterpriseJakartaVersionAccessors getJakarta() {
            return vaccForJakartaEnterpriseJakartaVersionAccessors;
        }

    }

    public static class JakartaEnterpriseJakartaVersionAccessors extends VersionFactory  {

        private final JakartaEnterpriseJakartaEnterpriseVersionAccessors vaccForJakartaEnterpriseJakartaEnterpriseVersionAccessors = new JakartaEnterpriseJakartaEnterpriseVersionAccessors(providers, config);
        public JakartaEnterpriseJakartaVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.enterprise.jakarta.enterprise
         */
        public JakartaEnterpriseJakartaEnterpriseVersionAccessors getEnterprise() {
            return vaccForJakartaEnterpriseJakartaEnterpriseVersionAccessors;
        }

    }

    public static class JakartaEnterpriseJakartaEnterpriseVersionAccessors extends VersionFactory  {

        private final JakartaEnterpriseJakartaEnterpriseCdiVersionAccessors vaccForJakartaEnterpriseJakartaEnterpriseCdiVersionAccessors = new JakartaEnterpriseJakartaEnterpriseCdiVersionAccessors(providers, config);
        public JakartaEnterpriseJakartaEnterpriseVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.enterprise.jakarta.enterprise.cdi
         */
        public JakartaEnterpriseJakartaEnterpriseCdiVersionAccessors getCdi() {
            return vaccForJakartaEnterpriseJakartaEnterpriseCdiVersionAccessors;
        }

    }

    public static class JakartaEnterpriseJakartaEnterpriseCdiVersionAccessors extends VersionFactory  {

        public JakartaEnterpriseJakartaEnterpriseCdiVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: jakarta.enterprise.jakarta.enterprise.cdi.api (4.0.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getApi() { return getVersion("jakarta.enterprise.jakarta.enterprise.cdi.api"); }

    }

    public static class JakartaServletVersionAccessors extends VersionFactory  {

        private final JakartaServletJakartaVersionAccessors vaccForJakartaServletJakartaVersionAccessors = new JakartaServletJakartaVersionAccessors(providers, config);
        public JakartaServletVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.servlet.jakarta
         */
        public JakartaServletJakartaVersionAccessors getJakarta() {
            return vaccForJakartaServletJakartaVersionAccessors;
        }

    }

    public static class JakartaServletJakartaVersionAccessors extends VersionFactory  {

        private final JakartaServletJakartaServletVersionAccessors vaccForJakartaServletJakartaServletVersionAccessors = new JakartaServletJakartaServletVersionAccessors(providers, config);
        public JakartaServletJakartaVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.servlet.jakarta.servlet
         */
        public JakartaServletJakartaServletVersionAccessors getServlet() {
            return vaccForJakartaServletJakartaServletVersionAccessors;
        }

    }

    public static class JakartaServletJakartaServletVersionAccessors extends VersionFactory  {

        public JakartaServletJakartaServletVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: jakarta.servlet.jakarta.servlet.api (6.0.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getApi() { return getVersion("jakarta.servlet.jakarta.servlet.api"); }

    }

    public static class JakartaValidationVersionAccessors extends VersionFactory  {

        private final JakartaValidationJakartaVersionAccessors vaccForJakartaValidationJakartaVersionAccessors = new JakartaValidationJakartaVersionAccessors(providers, config);
        public JakartaValidationVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.validation.jakarta
         */
        public JakartaValidationJakartaVersionAccessors getJakarta() {
            return vaccForJakartaValidationJakartaVersionAccessors;
        }

    }

    public static class JakartaValidationJakartaVersionAccessors extends VersionFactory  {

        private final JakartaValidationJakartaValidationVersionAccessors vaccForJakartaValidationJakartaValidationVersionAccessors = new JakartaValidationJakartaValidationVersionAccessors(providers, config);
        public JakartaValidationJakartaVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.validation.jakarta.validation
         */
        public JakartaValidationJakartaValidationVersionAccessors getValidation() {
            return vaccForJakartaValidationJakartaValidationVersionAccessors;
        }

    }

    public static class JakartaValidationJakartaValidationVersionAccessors extends VersionFactory  {

        public JakartaValidationJakartaValidationVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: jakarta.validation.jakarta.validation.api (3.0.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getApi() { return getVersion("jakarta.validation.jakarta.validation.api"); }

    }

    public static class JakartaWebsocketVersionAccessors extends VersionFactory  {

        private final JakartaWebsocketJakartaVersionAccessors vaccForJakartaWebsocketJakartaVersionAccessors = new JakartaWebsocketJakartaVersionAccessors(providers, config);
        public JakartaWebsocketVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.websocket.jakarta
         */
        public JakartaWebsocketJakartaVersionAccessors getJakarta() {
            return vaccForJakartaWebsocketJakartaVersionAccessors;
        }

    }

    public static class JakartaWebsocketJakartaVersionAccessors extends VersionFactory  {

        private final JakartaWebsocketJakartaWebsocketVersionAccessors vaccForJakartaWebsocketJakartaWebsocketVersionAccessors = new JakartaWebsocketJakartaWebsocketVersionAccessors(providers, config);
        public JakartaWebsocketJakartaVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.jakarta.websocket.jakarta.websocket
         */
        public JakartaWebsocketJakartaWebsocketVersionAccessors getWebsocket() {
            return vaccForJakartaWebsocketJakartaWebsocketVersionAccessors;
        }

    }

    public static class JakartaWebsocketJakartaWebsocketVersionAccessors extends VersionFactory  {

        private final JakartaWebsocketJakartaWebsocketClientVersionAccessors vaccForJakartaWebsocketJakartaWebsocketClientVersionAccessors = new JakartaWebsocketJakartaWebsocketClientVersionAccessors(providers, config);
        public JakartaWebsocketJakartaWebsocketVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: jakarta.websocket.jakarta.websocket.api (2.1.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getApi() { return getVersion("jakarta.websocket.jakarta.websocket.api"); }

        /**
         * Returns the group of versions at versions.jakarta.websocket.jakarta.websocket.client
         */
        public JakartaWebsocketJakartaWebsocketClientVersionAccessors getClient() {
            return vaccForJakartaWebsocketJakartaWebsocketClientVersionAccessors;
        }

    }

    public static class JakartaWebsocketJakartaWebsocketClientVersionAccessors extends VersionFactory  {

        public JakartaWebsocketJakartaWebsocketClientVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: jakarta.websocket.jakarta.websocket.client.api (2.1.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getApi() { return getVersion("jakarta.websocket.jakarta.websocket.client.api"); }

    }

    public static class OrgVersionAccessors extends VersionFactory  {

        private final OrgBouncycastleVersionAccessors vaccForOrgBouncycastleVersionAccessors = new OrgBouncycastleVersionAccessors(providers, config);
        private final OrgSlf4jVersionAccessors vaccForOrgSlf4jVersionAccessors = new OrgSlf4jVersionAccessors(providers, config);
        public OrgVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.bouncycastle
         */
        public OrgBouncycastleVersionAccessors getBouncycastle() {
            return vaccForOrgBouncycastleVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.org.slf4j
         */
        public OrgSlf4jVersionAccessors getSlf4j() {
            return vaccForOrgSlf4jVersionAccessors;
        }

    }

    public static class OrgBouncycastleVersionAccessors extends VersionFactory  {

        private final OrgBouncycastleBcpkixVersionAccessors vaccForOrgBouncycastleBcpkixVersionAccessors = new OrgBouncycastleBcpkixVersionAccessors(providers, config);
        private final OrgBouncycastleBcprovVersionAccessors vaccForOrgBouncycastleBcprovVersionAccessors = new OrgBouncycastleBcprovVersionAccessors(providers, config);
        public OrgBouncycastleVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.bouncycastle.bcpkix
         */
        public OrgBouncycastleBcpkixVersionAccessors getBcpkix() {
            return vaccForOrgBouncycastleBcpkixVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.org.bouncycastle.bcprov
         */
        public OrgBouncycastleBcprovVersionAccessors getBcprov() {
            return vaccForOrgBouncycastleBcprovVersionAccessors;
        }

    }

    public static class OrgBouncycastleBcpkixVersionAccessors extends VersionFactory  {

        public OrgBouncycastleBcpkixVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: org.bouncycastle.bcpkix.jdk18on (1.76)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJdk18on() { return getVersion("org.bouncycastle.bcpkix.jdk18on"); }

    }

    public static class OrgBouncycastleBcprovVersionAccessors extends VersionFactory  {

        public OrgBouncycastleBcprovVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: org.bouncycastle.bcprov.jdk18on (1.76)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJdk18on() { return getVersion("org.bouncycastle.bcprov.jdk18on"); }

    }

    public static class OrgSlf4jVersionAccessors extends VersionFactory  {

        private final OrgSlf4jSlf4jVersionAccessors vaccForOrgSlf4jSlf4jVersionAccessors = new OrgSlf4jSlf4jVersionAccessors(providers, config);
        public OrgSlf4jVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.slf4j.slf4j
         */
        public OrgSlf4jSlf4jVersionAccessors getSlf4j() {
            return vaccForOrgSlf4jSlf4jVersionAccessors;
        }

    }

    public static class OrgSlf4jSlf4jVersionAccessors extends VersionFactory  {

        public OrgSlf4jSlf4jVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: org.slf4j.slf4j.api (1.7.30)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getApi() { return getVersion("org.slf4j.slf4j.api"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

    }

}
