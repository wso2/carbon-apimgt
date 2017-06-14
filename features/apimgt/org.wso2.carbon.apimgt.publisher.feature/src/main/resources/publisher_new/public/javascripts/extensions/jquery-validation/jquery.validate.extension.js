$.validator.setDefaults({
    highlight: function(element, errorClass, validClass) {
        if ((element.type === "radio") || (element.type === "checkbox")) {
            this.findByName(element.name).addClass(errorClass).removeClass(validClass);
        } else {
            $(element).parent().removeClass('has-success has-feedback').addClass('has-error has-feedback');
            $(element).parent().find('i.fw').remove();
            $(element).parent().append('<i class="fw fw-error fa-lg form-control-feedback"></i>');
        }
    },
    unhighlight: function(element, errorClass, validClass) {
        if(element.value !== ''){
            if ((element.type === "radio") || (element.type === "checkbox")) {
                this.findByName(element.name).removeClass(errorClass).addClass(validClass);
            } else {
                $(element).parent().removeClass('has-error has-feedback').addClass('has-success has-feedback');
                $(element).parent().find('i.fw').remove();
                $(element).parent().append('<i class="fw fw-success fa-lg form-control-feedback"></i>');
            }
        }
        else {
            $(element).parent().removeClass('has-error has-success has-feedback');
            $(element).parent().find('i.fw').remove();
        }
    },
    errorElement: 'span',
    errorClass: 'help-block',
    errorPlacement: function(error, element) {
        if(element.parent('.input-group').length) {
            error.insertAfter(element.parent());
        } else {
            error.insertAfter(element);
        }
    }
});