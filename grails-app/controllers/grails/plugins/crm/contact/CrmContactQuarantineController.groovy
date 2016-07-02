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
import grails.plugins.crm.task.CrmTaskAttender
import grails.plugins.crm.task.CrmTaskAttenderStatus

/**
 * Created by goran on 2016-06-23.
 */
class CrmContactQuarantineController {

    def crmContactQuarantineService
    def crmSecurityService
    def crmContactService
    def crmBookingService
    def crmTaskService
    def crmTrainingService
    def selectionService

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

    def contacts(String id, String q) {
        def contact = crmContactQuarantineService.get(id)
        def timeout = (grailsApplication.config.crm.quarantine.timeout ?: 60) * 1000
        def result
        if (q) {
            result = crmContactService.list([name: q], [max: 100])*.dao
        } else {
            result = event(for: 'quarantine', topic: 'duplicates',
                    data: [tenant: TenantUtils.tenant, person: contact]).waitFor(timeout)?.value
        }
        render template: 'contacts', model: [list: result]
    }

    def companies(String id, String q) {
        def contact = crmContactQuarantineService.get(id)
        def timeout = (grailsApplication.config.crm.quarantine.timeout ?: 60) * 1000
        def result
        if (q) {
            result = crmContactService.list([name: q], [max: 100])*.dao
        } else {
            result = event(for: 'quarantine', topic: 'duplicates',
                    data: [tenant: TenantUtils.tenant, company: contact]).waitFor(timeout)?.value
        }
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
            def crmTaskAttender = new CrmTaskAttender()
            def statusList = crmTaskService.listAttenderStatus()
            def events = crmTrainingService.listTrainingEvents([fromDate: new Date() - 7], [max: 10, sort: 'startTime', order: 'asc'])
            def bookingList = events.bookings.flatten()
            render template: 'createBooking',
                    model: [bean        : contact, crmTaskAttender: crmTaskAttender,
                            statusList  : statusList, bookingList: bookingList,
                            trainingList: events]
        }
    }

    def createTask(String id) {
        if (request.post) {
            def m = crmTaskService.createTask(params, true)
            render m as JSON
        } else {
            if(! params.name) {
                params.name = 'Ny aktivitet'
            }
            def contact = crmContactQuarantineService.get(id)
            def crmTask = crmTaskService.createTask(params, false)
            def typeList = crmTaskService.listTaskTypes()
            def timeList = (7..19).inject([]) { list, h ->
                2.times {
                    list << String.format("%02d:%02d", h, it * 30)
                }; list
            }
            timeList = [''] + timeList.sort()
            render template: 'createTask', model: [bean    : contact, crmTask: crmTask,
                                                   typeList: typeList, timeList: timeList]
        }
    }
}
