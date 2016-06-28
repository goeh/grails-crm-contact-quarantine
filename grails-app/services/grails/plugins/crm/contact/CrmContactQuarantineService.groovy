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
import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.entity.ContentType
import org.grails.databinding.SimpleMapDataBindingSource

/**
 * Created by goran on 2016-06-21.
 */
class CrmContactQuarantineService {

    private static final String USER_AGENT = 'GR8CRM'
    private static final String SERVICE_URL = 'http://localhost:30316/quarantine'

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

    CrmContactQuarantine quarantine(Map data) {
        def tenant = data.tenant ?: TenantUtils.tenant
        def contact = new CrmContactQuarantine(tenantId: tenant)
        grailsWebDataBinder.bind(contact, data as SimpleMapDataBindingSource, null, CrmContactQuarantine.BIND_WHITELIST_QUARANTINE, null, null)
        if (!contact.save()) {
            log.error("Cannot quarantine contact information $data ${contact.errors.allErrors}")
        }
        contact
    }

    private Object fetch(String url) {
        String data = new URL(url)
                .getText(connectTimeout: 5000,
                readTimeout: 10000,
                useCaches: true,
                allowUserInteraction: false,
                requestProperties: ['Connection': 'close', 'User-Agent': USER_AGENT])
        return data ? new JsonSlurper().parseText(data) : null
    }

    Collection list() {
        def app = grailsApplication.metadata['app.name']
        def tenant = TenantUtils.tenant
        def currentUser = crmSecurityService.currentUser
        fetch("${SERVICE_URL}?application=${app}&username=${currentUser.username}&tenant=${tenant}")
    }

    def get(String id) {
        def app = grailsApplication.metadata['app.name']
        def tenant = TenantUtils.tenant
        def currentUser = crmSecurityService.currentUser
        fetch("${SERVICE_URL}/${id}?application=${app}&username=${currentUser.username}&tenant=${tenant}")
    }

    def update(Map params) {
        def app = grailsApplication.metadata['app.name']
        def version = grailsApplication.metadata['app.version']
        def tenant = TenantUtils.tenant
        def currentUser = crmSecurityService.currentUser
        def id = params.id

        params.tenant = tenant
        params.application = app
        params.username = currentUser?.username

        def http = new HTTPBuilder("${SERVICE_URL}/$id?application=${app}&username=${currentUser.username}&tenant=${tenant}")
        def rval
        try {
            def httpParams = http.getClient().getParams()
            httpParams.setParameter("http.connection.timeout", 5000)
            httpParams.setParameter("http.socket.timeout", 20000)
            http.request(Method.POST, ContentType.APPLICATION_JSON) {
                headers.'User-Agent' = "$app/$version"
                headers.Accept = ContentType.APPLICATION_JSON.toString()
                body = params
                response.success = { resp, data ->
                    rval = data
                }
            }
        } finally {
            http.shutdown()
        }
        rval
    }

    def delete(String id) {
        def app = grailsApplication.metadata['app.name']
        def version = grailsApplication.metadata['app.version']
        def tenant = TenantUtils.tenant
        def currentUser = crmSecurityService.currentUser

        def http = new HTTPBuilder("${SERVICE_URL}/$id?application=${app}&username=${currentUser.username}&tenant=${tenant}")
        def rval
        try {
            def httpParams = http.getClient().getParams()
            httpParams.setParameter("http.connection.timeout", 5000)
            httpParams.setParameter("http.socket.timeout", 20000)
            http.request(Method.DELETE) {
                headers.'User-Agent' = "$app/$version"
                headers.Accept = ContentType.APPLICATION_JSON.toString()
                response.success = { resp, data ->
                    rval = data
                }
            }
        } finally {
            http.shutdown()
        }
        rval
    }
}
