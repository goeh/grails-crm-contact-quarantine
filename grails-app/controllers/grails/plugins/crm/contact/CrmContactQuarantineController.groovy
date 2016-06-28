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

import grails.converters.JSON
import grails.plugins.crm.core.TenantUtils

/**
 * Created by goran on 2016-06-23.
 */
class CrmContactQuarantineController {

    def crmContactQuarantineService
    def crmSecurityService
    def crmContactService
    def crmBookingService
    def crmTaskService

    def index() {
        def result = crmContactQuarantineService.list()
        def currentUser = crmSecurityService.currentUser
        [user: currentUser, result: result]
    }

    def show(String id) {
        def currentUser = crmSecurityService.currentUser
        def contact = crmContactQuarantineService.get(id)
        [user: currentUser, contact: contact]
    }

    def update() {
        def contact = crmContactQuarantineService.update(params)
        flash.success = "${contact.name} uppdaterad"
        redirect action: 'show', id: params.id
    }

    def delete() {
        def selected = params.list('id')
        for (id in selected) {
            crmContactQuarantineService.delete(id)
        }
        flash.warning = "${selected.size()} st poster raderade"
        redirect action: 'index'
    }

    def contacts(String id) {
        def contact = crmContactQuarantineService.get(id)
        def timeout = (grailsApplication.config.crm.quarantine.timeout ?: 60) * 1000
        def result = event(for: 'quarantine', topic: 'duplicates',
                data: [tenant: TenantUtils.tenant, person: contact]).waitFor(timeout)?.value
        render template: 'contacts', model: [list: result]
    }

    def companies(String id) {
        def contact = crmContactQuarantineService.get(id)
        def timeout = (grailsApplication.config.crm.quarantine.timeout ?: 60) * 1000
        def result = event(for: 'quarantine', topic: 'duplicates',
                data: [tenant: TenantUtils.tenant, company: contact]).waitFor(timeout)?.value
        render template: 'companies', model: [list: result]
    }

    def createCompany(String id) {
        if (request.post) {
            def m = crmContactService.createCompany(params, true)
            render m as JSON
        } else {
            def contact = crmContactQuarantineService.get(id)
            render template: 'createCompany', model: [bean: contact]
        }
    }

    def createPerson(String id) {
        if (request.post) {
            def m = crmContactService.createPerson(params, true)
            render m as JSON
        } else {
            def contact = crmContactQuarantineService.get(id)
            render template: 'createPerson', model: [bean: contact]
        }
    }

    def createBooking(String id) {
        if (request.post) {
            def m = crmBookingService.createBooking(params, true)
            render m as JSON
        } else {
            def contact = crmContactQuarantineService.get(id)
            render template: 'createBooking', model: [bean: contact]
        }
    }

    def createTask(String id) {
        if (request.post) {
            def m = crmTaskService.createTask(params, true)
            render m as JSON
        } else {
            def contact = crmContactQuarantineService.get(id)
            render template: 'createTask', model: [bean: contact]
        }
    }
}
