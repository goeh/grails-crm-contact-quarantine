/*
 * Copyright (c) 2016 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.contact

import grails.events.Listener
import grails.plugins.crm.core.TenantUtils
import org.grails.databinding.SimpleMapDataBindingSource

/**
 * Created by goran on 2016-06-21.
 */
class CrmContactQuarantineService {

    private static final String USER_AGENT = 'GR8CRM'

    def grailsApplication
    def grailsWebDataBinder
    def crmSecurityService
    def crmTagService

    @Listener(namespace = "crmContact", topic = "enableFeature")
    def enableFeature(event) {
        TenantUtils.withTenant(event.tenant) {
            crmTagService.createTag(name: CrmContactQuarantine.name, multiple: true)
        }
    }

    @Listener(namespace = "crmContact", topic = "quarantine")
    def quarantineListener(Map data) {
        quarantine(data)
    }

    def quarantine(Map data) {
        def tenant = data.tenant ?: TenantUtils.tenant
        def contact = new CrmContactQuarantine(tenantId: tenant)
        grailsWebDataBinder.bind(contact, data as SimpleMapDataBindingSource, null, CrmContactQuarantine.BIND_WHITELIST_QUARANTINE, null, null)
        if (!contact.save()) {
            log.error("Cannot quarantine contact information $data ${contact.errors.allErrors}")
        }
        contact
    }

    Collection list() {
        def tenant = TenantUtils.tenant
        CrmContactQuarantine.createCriteria().list() {
            eq('tenantId', tenant)
        }
    }

    def get(String id) {
        CrmContactQuarantine.get(id)
    }

    def update(Map params) {
        def id = params.id
        def app = grailsApplication.metadata['app.name']
        def version = grailsApplication.metadata['app.version']
        def tenant = TenantUtils.tenant
        def currentUser = crmSecurityService.currentUser
        def rval

        params.tenant = tenant
        params.application = app
        params.username = currentUser?.username

        def contact = id ? CrmContactQuarantine.get(id) : new CrmContactQuarantine(tenantId: tenant)
        if (contact) {
            grailsWebDataBinder.bind(contact, params as SimpleMapDataBindingSource, null, CrmContactQuarantine.BIND_WHITELIST_QUARANTINE, null, null)
            rval = contact.save()
            if (!rval) {
                log.error("Cannot quarantine contact information $params ${contact.errors.allErrors}")
            }
        }
        rval
    }

    def delete(String id) {
        def rval
        def contact = CrmContactQuarantine.get(id)
        if (contact) {
            rval = contact.dao
            contact.delete()
        }
        rval
    }
}
