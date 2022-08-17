/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;
import com.sun.enterprise.deployment.util.ComponentVisitor;
import com.sun.enterprise.deployment.web.AppListenerDescriptor;
import com.sun.enterprise.deployment.web.ContextParameter;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.web.MimeMapping;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.SecurityRole;
import com.sun.enterprise.deployment.web.SecurityRoleReference;
import com.sun.enterprise.deployment.web.ServletFilter;
import com.sun.enterprise.deployment.web.ServletFilterMapping;
import com.sun.enterprise.deployment.web.SessionConfig;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.event.EventTypes;
import org.glassfish.deployment.common.DescriptorVisitor;
import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * I am an object that represents all the deployment information about
 * a web app [{0}]lication.
 *
 * @author Danny Coward
 */
public abstract class WebBundleDescriptor extends CommonResourceBundleDescriptor
    implements WritableJndiNameEnvironment, ResourceReferenceContainer, ResourceEnvReferenceContainer,
    EjbReferenceContainer, MessageDestinationReferenceContainer, ServiceReferenceContainer {

    public static final EventTypes<WebBundleDescriptor> AFTER_SERVLET_CONTEXT_INITIALIZED_EVENT = EventTypes
        .create("After_Servlet_Context_Initialized", WebBundleDescriptor.class);

    protected boolean conflictLoginConfig;
    protected boolean conflictDataSourceDefinition;
    protected boolean conflictMailSessionDefinition;
    protected boolean conflictConnectionFactoryDefinition;
    protected boolean conflictAdminObjectDefinition;
    protected boolean conflictJMSConnectionFactoryDefinition;
    protected boolean conflictJMSDestinationDefinition;
    protected boolean conflictEnvironmentEntry;
    protected boolean conflictEjbReference;
    protected boolean conflictServiceReference;
    protected boolean conflictResourceReference;
    protected boolean conflictResourceEnvReference;
    protected boolean conflictMessageDestinationReference;
    protected boolean conflictEntityManagerReference;
    protected boolean conflictEntityManagerFactoryReference;

    public abstract void addWebBundleDescriptor(WebBundleDescriptor webBundleDescriptor);

    public abstract void addDefaultWebBundleDescriptor(WebBundleDescriptor webBundleDescriptor);

    public abstract void addJndiNameEnvironment(JndiNameEnvironment env);

    @Override
    public abstract boolean isEmpty();

    @Override
    public abstract String getDefaultSpecVersion();

    public abstract Collection getNamedDescriptors();

    public abstract Vector<NamedReferencePair> getNamedReferencePairs();

    public abstract String getContextRoot();

    public abstract void setContextRoot(String contextRoot);

    public abstract String getRequestCharacterEncoding();

    public abstract void setRequestCharacterEncoding(String requestCharacterEcoding);

    public abstract String getResponseCharacterEncoding();

    public abstract void setResponseCharacterEncoding(String responseCharacterEncoding);

    public abstract Set<WebComponentDescriptor> getWebComponentDescriptors();

    public abstract void addWebComponentDescriptor(WebComponentDescriptor webComponentDescriptor);

    protected abstract WebComponentDescriptor combineWebComponentDescriptor(
        WebComponentDescriptor webComponentDescriptor);

    public abstract void removeWebComponentDescriptor(WebComponentDescriptor webComponentDescriptor);

    public abstract SessionConfig getSessionConfig();

    public abstract void setSessionConfig(SessionConfig sessionConfig);

    public abstract boolean hasServiceReferenceDescriptors();

    @Override
    public abstract Set<ServiceReferenceDescriptor> getServiceReferenceDescriptors();

    @Override
    public abstract void addServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef);

    @Override
    public abstract void removeServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef);

    @Override
    public abstract ServiceReferenceDescriptor getServiceReferenceByName(String name);

    protected abstract ServiceReferenceDescriptor _getServiceReferenceByName(String name);

    protected abstract void combineServiceReferenceDescriptors(JndiNameEnvironment env);

    @Override
    public abstract Set<ResourceEnvReferenceDescriptor> getResourceEnvReferenceDescriptors();

    @Override
    public abstract void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvRefReference);

    @Override
    public abstract void removeResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvRefReference);

    @Override
    public abstract ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name);

    protected abstract ResourceEnvReferenceDescriptor _getResourceEnvReferenceByName(String name);

    protected abstract void combineResourceEnvReferenceDescriptors(JndiNameEnvironment env);

    protected abstract void combineResourceDescriptors(JndiNameEnvironment env, JavaEEResourceType javaEEResourceType);

    public abstract Set<MimeMapping> getMimeMappingsSet();

    public abstract void setMimeMappings(Set<MimeMapping> mimeMappings);

    public abstract Enumeration<MimeMapping> getMimeMappings();

    public abstract String addMimeMapping(MimeMapping mimeMapping);

    public abstract void removeMimeMapping(MimeMapping mimeMapping);

    public abstract LocaleEncodingMappingListDescriptor getLocaleEncodingMappingListDescriptor();

    public abstract void setLocaleEncodingMappingListDescriptor(LocaleEncodingMappingListDescriptor lemDesc);

    public abstract Enumeration<String> getWelcomeFiles();

    public abstract Set<String> getWelcomeFilesSet();

    public abstract void addWelcomeFile(String fileUri);

    public abstract void removeWelcomeFile(String fileUri);

    public abstract void setWelcomeFiles(Set<String> welcomeFiles);

    public abstract Set<ContextParameter> getContextParametersSet();

    public abstract Enumeration<ContextParameter> getContextParameters();

    public abstract void addContextParameter(ContextParameter contextParameter);

    public abstract void addContextParameter(EnvironmentProperty contextParameter);

    public abstract void removeContextParameter(ContextParameter contextParameter);

    public abstract boolean isDistributable();

    public abstract void setDistributable(boolean isDistributable);

    public abstract Enumeration<EjbReferenceDescriptor> getEjbReferences();

    @Override
    public abstract Set<EjbReferenceDescriptor> getEjbReferenceDescriptors();

    public abstract EjbReferenceDescriptor getEjbReferenceByName(String name);

    @Override
    public abstract EjbReferenceDescriptor getEjbReference(String name);

    protected abstract EjbReference _getEjbReference(String name);

    @Override
    public abstract ResourceReferenceDescriptor getResourceReferenceByName(String name);

    protected abstract ResourceReferenceDescriptor _getResourceReferenceByName(String name);

    @Override
    public abstract Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors();

    @Override
    public abstract Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors();

    @Override
    public abstract EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name);

    protected abstract EntityManagerFactoryReferenceDescriptor _getEntityManagerFactoryReferenceByName(String name);

    @Override
    public abstract void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor reference);

    protected abstract void combineEntityManagerFactoryReferenceDescriptors(JndiNameEnvironment env);

    @Override
    public abstract Set<EntityManagerReferenceDescriptor> getEntityManagerReferenceDescriptors();

    @Override
    public abstract EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name);

    protected abstract EntityManagerReferenceDescriptor _getEntityManagerReferenceByName(String name);

    @Override
    public abstract void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor reference) ;

    protected abstract void combineEntityManagerReferenceDescriptors(JndiNameEnvironment env);

    @Override
    public abstract Collection<? extends PersistenceUnitDescriptor> findReferencedPUs();

    @Override
    public abstract Set<EnvironmentProperty> getEnvironmentProperties();

    @Override
    public abstract void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReferenceDescriptor);

    @Override
    public abstract void removeEjbReferenceDescriptor(EjbReferenceDescriptor ejbReferenceDescriptor);

    protected abstract void combineEjbReferenceDescriptors(JndiNameEnvironment env);

    public abstract Enumeration<ResourceReferenceDescriptor> getResourceReferences();

    @Override
    public abstract void addResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference);

    @Override
    public abstract void removeResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference);

    protected abstract void combineResourceReferenceDescriptors(JndiNameEnvironment env);

    @Override
    public abstract Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors();

    @Override
    public abstract void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor messageDestRef) ;

    @Override
    public abstract void removeMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef);

    @Override
    public abstract MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name);

    protected abstract MessageDestinationReferenceDescriptor _getMessageDestinationReferenceByName(String name);

    protected abstract void combineMessageDestinationReferenceDescriptors(JndiNameEnvironment env);

    @Override
    public abstract Set<LifecycleCallbackDescriptor> getPostConstructDescriptors();

    @Override
    public abstract void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc);

    @Override
    public abstract LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className);

    protected abstract void combinePostConstructDescriptors(WebBundleDescriptor webBundleDescriptor);

    @Override
    public abstract Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors();

    @Override
    public abstract void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc);

    @Override
    public abstract LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className);

    protected abstract void combinePreDestroyDescriptors(WebBundleDescriptor webBundleDescriptor);

    @Override
    protected abstract List<InjectionCapable> getInjectableResourcesByClass(String className, JndiNameEnvironment jndiNameEnv);

    @Override
    public abstract List<InjectionCapable> getInjectableResourcesByClass(String className);

    @Override
    public abstract InjectionInfo getInjectionInfoByClass(Class clazz);

    public abstract Enumeration<SecurityRoleDescriptor> getSecurityRoles();

    public abstract void addSecurityRole(SecurityRole securityRole);

    public abstract void addSecurityRole(SecurityRoleDescriptor securityRole);

    public abstract SecurityRoleReference getSecurityRoleReferenceByName(String compName, String roleName);

    public abstract boolean isDenyUncoveredHttpMethods();

    protected abstract void combineSecurityConstraints(Set<SecurityConstraint> firstScSet, Set<SecurityConstraint> secondScSet);

    public abstract Set<SecurityConstraint> getSecurityConstraintsSet();

    public abstract Enumeration<SecurityConstraint> getSecurityConstraints();

    public abstract Collection<SecurityConstraint> getSecurityConstraintsForUrlPattern(String urlPattern);

    public abstract void addSecurityConstraint(SecurityConstraint securityConstraint);

    public abstract void removeSecurityConstraint(SecurityConstraint securityConstraint);

    public abstract Set<WebComponentDescriptor> getServletDescriptors();

    public abstract Set<WebComponentDescriptor> getJspDescriptors();

    public abstract Set<EnvironmentProperty> getEnvironmentEntrySet();

    public abstract Enumeration<EnvironmentProperty> getEnvironmentEntries();

    public abstract void addEnvironmentEntry(EnvironmentProperty environmentEntry);

    protected abstract EnvironmentProperty _getEnvironmentPropertyByName(String name);

    @Override
    public abstract EnvironmentProperty getEnvironmentPropertyByName(String name);

    @Override
    public abstract void removeEnvironmentProperty(EnvironmentProperty environmentProperty);

    @Override
    public abstract void addEnvironmentProperty(EnvironmentProperty environmentProperty);

    public abstract void removeEnvironmentEntry(EnvironmentProperty environmentEntry);

    protected abstract void combineEnvironmentEntries(JndiNameEnvironment env);

    public abstract LoginConfiguration getLoginConfiguration();

    public abstract void setLoginConfiguration(LoginConfiguration loginConfiguration);

    protected abstract void combineLoginConfiguration(WebBundleDescriptor webBundleDescriptor);

    public abstract WebComponentDescriptor getWebComponentByName(String name);

    public abstract WebComponentDescriptor getWebComponentByCanonicalName(String name);

    public abstract WebComponentDescriptor[] getWebComponentByImplName(String name);

    public abstract Vector<ServletFilter> getServletFilters();

    public abstract Vector<ServletFilter> getServletFilterDescriptors();

    public abstract void addServletFilter(ServletFilter ref);

    public abstract void removeServletFilter(ServletFilter ref);

    protected abstract void combineServletFilters(WebBundleDescriptor webBundleDescriptor);

    public abstract Vector<ServletFilterMapping> getServletFilterMappings();

    public abstract Vector<ServletFilterMapping> getServletFilterMappingDescriptors();

    public abstract void addServletFilterMapping(ServletFilterMapping ref);

    public abstract void removeServletFilterMapping(ServletFilterMapping ref);

    public abstract void moveServletFilterMapping(ServletFilterMapping ref, int relPos);

    protected abstract void combineServletFilterMappings(WebBundleDescriptor webBundleDescriptor);

    public abstract Vector<AppListenerDescriptor> getAppListeners();

    public abstract Vector<AppListenerDescriptor> getAppListenerDescriptors();

    public abstract void setAppListeners(Collection<? extends AppListenerDescriptor> c);

    public abstract void addAppListenerDescriptor(AppListenerDescriptor ref);

    public abstract void addAppListenerDescriptorToFirst(AppListenerDescriptor ref);

    public abstract void removeAppListenerDescriptor(AppListenerDescriptor ref);

    public abstract void moveAppListenerDescriptor(AppListenerDescriptor ref, int relPos);

    public abstract boolean isShowArchivedRealPathEnabled();

    public abstract void setShowArchivedRealPathEnabled(boolean enabled);

    public abstract int getServletReloadCheckSecs();

    public abstract void setServletReloadCheckSecs(int secs);

    @Override
    public abstract boolean hasWebServiceClients();

    protected abstract boolean removeVectorItem(Vector<? extends Object> list, Object ref);

    protected abstract void moveVectorItem(Vector list, Object ref, int rpos);

    public abstract void putJarNameWebFragmentNamePair(String jarName, String webFragName);

    public abstract Map<String, String> getJarNameToWebFragmentNameMap();

    public abstract Map<String, String> getUrlPatternToServletNameMap();

    public abstract void resetUrlPatternToServletNameMap();

    public abstract List<String> getOrderedLibs();

    public abstract void addOrderedLib(String libName);

    protected abstract void combineInjectionTargets(EnvironmentProperty env1, EnvironmentProperty env2);

    public abstract void printCommon(StringBuffer toStringBuffer);

    @Override
    public abstract ArchiveType getModuleType();

    @Override
    public abstract ComponentVisitor getBundleVisitor();

    @Override
    public abstract DescriptorVisitor getTracerVisitor();

    @Override
    public abstract String getDeploymentDescriptorDir() ;

    public abstract SunWebApp getSunDescriptor();

    public abstract void setSunDescriptor(SunWebApp webApp);

    public abstract void setExtensionProperty(String key, String value);

    public abstract boolean hasExtensionProperty(String key);

    public boolean isConflictLoginConfig() {
        return conflictLoginConfig;
    }

    public boolean isConflictDataSourceDefinition() {
        return conflictDataSourceDefinition;
    }

    public boolean isConflictMailSessionDefinition() {
        return conflictMailSessionDefinition;
    }

    public boolean isConflictConnectionFactoryDefinition() {
        return conflictConnectionFactoryDefinition;
    }

    public boolean isConflictAdminObjectDefinition() {
        return conflictAdminObjectDefinition;
    }

    public boolean isConflictJMSConnectionFactoryDefinition() {
        return conflictJMSConnectionFactoryDefinition;
    }

    public boolean isConflictJMSDestinationDefinition() {
        return conflictJMSDestinationDefinition;
    }

    public boolean isConflictEnvironmentEntry() {
        return conflictEnvironmentEntry;
    }

    public boolean isConflictEjbReference() {
        return conflictEjbReference;
    }

    public boolean isConflictServiceReference() {
        return conflictServiceReference;
    }

    public boolean isConflictResourceReference() {
        return conflictResourceReference;
    }

    public boolean isConflictResourceEnvReference() {
        return conflictResourceEnvReference;
    }

    public boolean isConflictMessageDestinationReference() {
        return conflictMessageDestinationReference;
    }

    public boolean isConflictEntityManagerReference() {
        return conflictEntityManagerReference;
    }

    public boolean isConflictEntityManagerFactoryReference() {
        return conflictEntityManagerFactoryReference;
    }

    public abstract Set<String> getConflictedMimeMappingExtensions();
}

