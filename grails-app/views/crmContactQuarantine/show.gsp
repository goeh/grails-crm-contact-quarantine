<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Contact Quarantine</title>
    <r:script>
        var crmContactQuarantine = {
            loadCompanies: function() {
                $('#company-container').load("${createLink(action: 'companies', id: contact.id)}");
            },
            loadContacts: function() {
                $('#person-container').load("${createLink(action: 'contacts', id: contact.id)}");
            }
        };
        $(document).ready(function () {
            $('button[name="_action_company"]').click(function (ev) {
                ev.preventDefault();
                var $modal = $('#modal-container');
                $modal.load("${createLink(action: 'createCompany', id: contact.id)}", function() {
                    $modal.modal('show');
                    $('form', $modal).submit(function(ev) {
                        ev.preventDefault();
                        var $form = $(this);
                        $.ajax({
                            type: $form.attr('method'),
                            url: $form.attr('action'),
                            data: $form.serialize(),
                            success: function (data) {
                                $modal.modal('hide');
                                crmContactQuarantine.loadCompanies();
                            },
                            error: function(data) {
                                alert("Error " + data);
                            }
                        });
                    });
                });
            });
            $('button[name="_action_person"]').click(function (ev) {
                ev.preventDefault();
                var $modal = $('#modal-container');
                $modal.load("${createLink(action: 'createPerson', id: contact.id)}", function() {
                    $modal.modal('show');
                    $('form', $modal).submit(function(ev) {
                        ev.preventDefault();
                        var $form = $(this);
                        $.ajax({
                            type: $form.attr('method'),
                            url: $form.attr('action'),
                            data: $form.serialize(),
                            success: function (data) {
                                $modal.modal('hide');
                                crmContactQuarantine.loadContacts();
                            },
                            error: function(data) {
                                alert("Error " + data);
                            }
                        });
                    });
                });
            });
            $('button[name="_action_booking"]').click(function (ev) {
                ev.preventDefault();
                var $modal = $('#modal-container');
                $modal.load("${createLink(action: 'createBooking', id: contact.id)}", function() {
                    $modal.modal('show');
                });
            });
            $('button[name="_action_task"]').click(function (ev) {
                ev.preventDefault();
                var $modal = $('#modal-container');
                $modal.load("${createLink(action: 'createTask', id: contact.id)}", function() {
                    $modal.modal('show');
                });
            });

            crmContactQuarantine.loadContacts();
            crmContactQuarantine.loadCompanies();
        });
    </r:script>
    <style type="text/css">
    </style>
</head>

<body>

<g:form>
    <div class="row-fluid">
        <div class="span5">
            <div class="row-fluid">
                <div class="control-group">
                    <label class="control-label">Namn</label>

                    <div class="controls">
                        <g:textField name="firstName" value="${contact.firstName}" class="span5"/>
                        <g:textField name="lastName" value="${contact.lastName}" class="span6"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">Titel</label>

                    <div class="controls">
                        <g:textField name="title" value="${contact.title}" class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">Företag</label>

                    <div class="controls">
                        <g:textField name="companyName" value="${contact.companyName}" class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">Adress</label>

                    <div class="controls">
                        <g:textField name="address1" value="${contact.address1}" class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">Postnr/ort</label>

                    <div class="controls">
                        <g:textField name="postalCode" value="${contact.postalCode}" class="span3"/>
                        <g:textField name="city" value="${contact.city}" class="span8"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">Telefon</label>

                    <div class="controls">
                        <g:textField name="telephone" value="${contact.telephone}" class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">E-post</label>

                    <div class="controls">
                        <g:textField name="email" value="${contact.email}" class="span11"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">Meddelande</label>

                    <div class="controls">
                        <g:textArea name="comments" value="${contact.comments}" rows="3" class="span11"/>
                    </div>
                </div>

            </div>
        </div>

        <div class="span7">
            <div class="row-fluid">

                <h4>Matchande personer</h4>

                <div id="person-container"><g:img dir="images" file="spinner.gif" alt="Loading..."/></div>

                <h4>Matchande företag</h4>

                <div id="company-container"><g:img dir="images" file="spinner.gif" alt="Loading..."/></div>

            </div>
        </div>
    </div>

    <div class="form-actions">
        <input type="hidden" name="id" value="${contact.id}"/>
        <crm:button action="update" label="Uppdatera" icon="icon-pencil icon-white" visual="warning"/>
        <crm:button action="company" label="Nytt företag" icon="icon-home icon-white" visual="success"/>
        <crm:button action="person" label="Ny person" icon="icon-user icon-white" visual="success"/>
        <crm:button action="booking" label="Ny bokning" icon="icon-glass icon-white" visual="info"/>
        <crm:button action="task" label="Ny aktivitet" icon="icon-time icon-white" visual="info"/>
        <crm:button action="delete" label="Ta bort" icon="icon-trash icon-white" visual="danger"
                    confirm="Bekräfta borttag"/>
        <crm:button type="link" action="index" label="Tillbaka" icon="icon-remove"/>
    </div>

</g:form>

<div class="modal hide fade" id="modal-container"></div>

</body>
</html>