<table class="table">
    <thead>
    <tr>
        <th></th>
        <th>Företagsnamn</th>
        <th>E-post</th>
        <th>Telefon</th>
        <th>Adress</th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${list}" var="c">
            <tr>
                <td><input type="radio" name="company" value="${c.id}"/></td>
                <td>${c.name}</td>
                <td>${c.email}</td>
                <td>${c.telephone}</td>
                <td>${c.fullAddress}</td>
            </tr>
        </g:each>
    </tbody>
</table>