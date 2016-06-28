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

import grails.plugins.crm.core.*
import groovy.transform.CompileStatic

/**
 * Created by goran on 2016-06-21.
 */
@TenantEntity
@AuditEntity
class CrmContactQuarantine extends CrmAddress implements CrmMutableContactInformation, Serializable {

    String firstName
    String lastName
    String companyName
    Long companyId
    String title
    String telephone
    String email
    String number
    String comments

    static constraints = {
        firstName(maxSize: 40, nullable: true)
        lastName(maxSize: 40, nullable: true)
        companyName(maxSize: 80, nullable: true)
        companyId(nullable: true)
        title(maxSize: 80, nullable: true)
        telephone(maxSize: 20, nullable: true)
        email(maxSize: 80, nullable: true, email: true)
        number(maxSize: 40, nullable: true)
        comments(maxSize: 2000, nullable: true)
    }

    static transients = ['name', 'fullName', 'fullAddress', 'addressInformation', 'dao'] + CrmAddress.transients

    static taggable = true
    static attachmentable = true
    static dynamicProperties = true

    static final List BIND_WHITELIST_QUARANTINE = (['firstName', 'lastName', 'companyName', 'companyId',
                                                       'title', 'telephone', 'email', 'number', 'comments'] + CrmAddress.BIND_WHITELIST).asImmutable()

    @Override
    @CompileStatic
    transient String getName() {
        def s = new StringBuilder()
        if (firstName) {
            s << firstName
        }
        if (lastName) {
            if (s.length()) {
                s << ' '
            }
            s << lastName
        }
        if (s.length() == 0 && companyName != null) {
            s << companyName
        }
        s.toString()
    }

    @Override
    @CompileStatic
    transient String getFullName() {
        def s = new StringBuilder()
        if (firstName || lastName) {
            s << getName()
        }
        if (companyName) {
            if (s.length()) {
                s << ', '
            }
            s << companyName
        }
        s.toString()
    }

    @Override
    @CompileStatic
    transient String getFullAddress() {
        getAddress(true)
    }

    @Override
    @CompileStatic
    transient CrmAddressInformation getAddressInformation() {
        this
    }

    @Override
    @CompileStatic
    void setAddressInformation(CrmAddressInformation arg) {
        if (arg == null) {
            this.address1 = null
            this.address2 = null
            this.address3 = null
            this.postalCode = null
            this.city = null
            this.country = null
        } else {
            this.address1 = arg.address1
            this.address2 = arg.address2
            this.address3 = arg.address3
            this.postalCode = arg.postalCode
            this.city = arg.city
            this.country = arg.country
        }
    }

    transient Map<String, Object> getDao() {
        def result = ['id', 'firstName', 'lastName', 'companyName', 'companyId', 'title', 'telephone', 'email', 'number', 'comments'].inject(super.getDao()) { map, prop ->
            def v = this[prop]
            if (v != null) {
                map[prop] = v
            }
            map
        }
        result.tenant = this.tenantId
        result.name = getName()
        result.fullName = getFullName()
        result.fullAddress = getFullAddress()

        result
    }


    @Override
    String toString() {
        getName()
    }
}
