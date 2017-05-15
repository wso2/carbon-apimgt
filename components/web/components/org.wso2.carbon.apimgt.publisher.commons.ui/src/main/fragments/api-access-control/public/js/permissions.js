'use strict'

$(
    function () {
        var roleIndex = 0;
//        $("[id=remove-2]").on('click',function(e){
//            e.preventDefault();
//            var par = $("[id=role-2]");
//            par.remove();
//        });
        $('.add-role').on('click', function() {
            var role = $('#role-name').val().trim();
            if(role && role != ""){
                roleIndex++;
                var $permissionsTemplate = $('#permission-div'),
                $clone = $permissionsTemplate.clone().removeClass('hide').attr('id','role-' + roleIndex).insertBefore($permissionsTemplate);
                $clone
                    .find('[id="remove"]').attr('id', 'remove-' + roleIndex ).end();
                $clone.find('label[for="permission-options"]').html(role + '&nbsp; : &nbsp; &nbsp;');
                $('#no-roles-msg').hide();
                $('#role-name').val(null);
            } else {
                var message = "Please specify a role";
                noty({
                    text: message,
                    type: 'warning',
                    dismissQueue: true,
                    progressBar: true,
                    timeout: 5000,
                    layout: 'topCenter',
                    theme: 'relax',
                    maxVisible: 10
                });
                return;
            }
        });
    }
);