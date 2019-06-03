$("#btnver").click(function () {
    var tablaresumen;
    try {
        $('#tablaresumen').DataTable().destroy();
    } catch (e) {
        console.log(e);
    }

    $.ajax({
        type: "POST",
        dataType: 'json',
        contentType: "application/json",
        url: "apis/resumen",
        data: JSON.stringify({}),
        success: function (respuesta) {
            tablaresumen = $('#tablaresumen').DataTable({
                data: respuesta,
                lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "Todos"]],
                columns: [
                    {data: "campana"},
                    {data: "agente"},
                    {data: "nombre"},
                    {data: "pendiente"},
                    {data: "listacolas"},
                    {data: "estadoagente"},
                    {data: "pedido_pausa"}
                ],
                dom: 'lrtip' // esto sirve para poder controlar lo que se ve https://datatables.net/reference/option/dom
            });
        },
        error: function (response) {
            $("#divasistencia").prepend('<div  id="mensajeeror" class="col-sm-12 animated fadeIn">' +
                    '<div  class="alert  alert-danger alert-dismissible fade show" role="alert">' +
                    '<span class="badge badge-pill badge-danger">Error</span> Error' +
                    '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
                    '<span aria-hidden="true">&times;</span></button></div>');
        }
    });
});


         